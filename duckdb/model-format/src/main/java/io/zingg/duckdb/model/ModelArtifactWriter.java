package io.zingg.duckdb.model;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Publishes complete model artifacts using atomic directory moves only. */
public final class ModelArtifactWriter {
  private static final System.Logger LOGGER = System.getLogger(ModelArtifactWriter.class.getName());
  private static final Set<String> MANIFEST_FIELDS = Set.of(
      "profile", "zinggVersion", "formatVersion", "modelType", "features", "sha256");
  private static final Set<String> PROVENANCE_FIELDS = Set.of(
      "sourcePath", "sourceVersion", "importerVersion", "importedAt", "sha256");

  @FunctionalInterface
  interface AtomicMover {
    void move(Path source, Path target) throws IOException;
  }

  @FunctionalInterface
  interface TreeDeleter {
    void delete(Path path) throws IOException;
  }

  private final Path root;
  private final AtomicMover mover;
  private final TreeDeleter deleter;
  private final BooleanSupplier cancellationRequested;

  public ModelArtifactWriter(Path root) {
    this(root, () -> false);
  }

  /** Creates an atomic model publisher with a cancellation check at publication boundaries. */
  public ModelArtifactWriter(Path root, BooleanSupplier cancellationRequested) {
    this(root,
        (source, target) -> Files.move(source, target, StandardCopyOption.ATOMIC_MOVE),
        ModelArtifactWriter::deleteTree, cancellationRequested);
  }

  ModelArtifactWriter(Path root, AtomicMover mover, TreeDeleter deleter) {
    this(root, mover, deleter, () -> false);
  }

  ModelArtifactWriter(Path root, AtomicMover mover, TreeDeleter deleter,
      BooleanSupplier cancellationRequested) {
    this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
    this.mover = Objects.requireNonNull(mover, "mover");
    this.deleter = Objects.requireNonNull(deleter, "deleter");
    this.cancellationRequested = Objects.requireNonNull(cancellationRequested,
        "cancellationRequested");
  }

  public Path write(ModelManifest manifest, byte[] payload, ImportProvenance provenance)
      throws IOException {
    return write(manifest, payload, provenance, 0);
  }

  /** Writes only if the complete staged artifact fits the optional byte limit. */
  public Path write(ModelManifest manifest, byte[] payload, ImportProvenance provenance,
      long maxArtifactBytes) throws IOException {
    Objects.requireNonNull(manifest, "manifest");
    Objects.requireNonNull(payload, "payload");
    Objects.requireNonNull(provenance, "provenance");
    if (maxArtifactBytes < 0) throw new IllegalArgumentException("artifact byte limit cannot be negative");
    ModelFormat.requireSupported(manifest.formatVersion());
    Path parent = root.getParent();
    if (parent == null || root.getFileName() == null)
      throw new IOException("model artifact path must have a parent and file name");
    Files.createDirectories(parent);
    if (Files.exists(root, LinkOption.NOFOLLOW_LINKS)
        && (Files.isSymbolicLink(root) || !Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)))
      throw new IOException("model artifact root must be a real directory");

    checkCancelled();
    String checksum = NativeModelStore.sha256(payload);
    ModelManifest stamped = new ModelManifest(manifest.profile(), manifest.zinggVersion(),
        manifest.formatVersion(), manifest.modelType(), manifest.features(), checksum);
    long stagedBytes;
    try {
      stagedBytes = Math.addExact(payload.length,
          Math.addExact(NativeModelStore.toJson(stamped).getBytes(java.nio.charset.StandardCharsets.UTF_8).length,
              provenanceJson(provenance, checksum).getBytes(java.nio.charset.StandardCharsets.UTF_8).length));
    } catch (ArithmeticException overflow) {
      throw new IOException("model artifact byte size overflow", overflow);
    }
    if (maxArtifactBytes > 0 && stagedBytes > maxArtifactBytes)
      throw new IOException("model artifact byte budget exceeded: " + stagedBytes + " > "
          + maxArtifactBytes + " bytes");
    Path stage = Files.createTempDirectory(parent, ".model-artifact-");
    Throwable primaryFailure = null;
    try {
      NativeModelStore.write(stage, stamped, payload);
      Files.writeString(stage.resolve("provenance.json"), provenanceJson(provenance, checksum));
      checkCancelled();
      publish(stage, stamped, provenance, payload);
      return root;
    } catch (IOException | RuntimeException | Error failure) {
      primaryFailure = failure;
      throw failure;
    } finally {
      try {
        deleter.delete(stage);
      } catch (IOException cleanupFailure) {
        if (primaryFailure != null) primaryFailure.addSuppressed(cleanupFailure);
        else LOGGER.log(System.Logger.Level.WARNING,
            "Published model artifact {0}, but staging cleanup failed: {1}",
            root.getFileName(), cleanupFailure.toString());
      }
    }
  }

  private void publish(Path stage, ModelManifest manifest, ImportProvenance provenance,
      byte[] payload) throws IOException {
    // Readers take the matching shared lock for the entire load. This keeps
    // the existing directory layout and makes the two replacement renames one
    // visible publication transaction to cooperating readers in other JVMs.
    try (var publication = ModelPublicationLock.write(root, cancellationRequested)) {
      Path backup = root.resolveSibling("." + root.getFileName() + ".previous-" + UUID.randomUUID());
      boolean hadPrevious = Files.exists(root, LinkOption.NOFOLLOW_LINKS);
      boolean movedOld = false;
      boolean publishedNew = false;
      try {
        checkCancelled();
        if (Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
          if (Files.isSymbolicLink(root) || !Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS))
            throw new IOException("model artifact root must be a real directory");
          mover.move(root, backup);
          movedOld = true;
          checkCancelled();
        }
        mover.move(stage, root);
        publishedNew = true;
        checkCancelled();
        verifyPublished(manifest, provenance, payload);
        checkCancelled();
      } catch (IOException | RuntimeException failure) {
        // As with the staged-directory move below, a filesystem provider can
        // complete the old-directory rename before surfacing an error. If the
        // unique backup now exists and the destination disappeared, restoration
        // is required even though the mover call did not return normally.
        if (!movedOld && hadPrevious && !Files.exists(root, LinkOption.NOFOLLOW_LINKS)
            && Files.exists(backup, LinkOption.NOFOLLOW_LINKS)) movedOld = true;
        // A provider may complete a move before reporting an error. Detect the
        // staged-directory rename by the disappearance of the private stage path.
        if (!publishedNew && !Files.exists(stage, LinkOption.NOFOLLOW_LINKS)
            && Files.exists(root, LinkOption.NOFOLLOW_LINKS)) publishedNew = true;
        if (publishedNew) {
          try {
            deleter.delete(root);
          } catch (IOException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
          }
        }
        if (movedOld) {
          try {
            mover.move(backup, root);
          } catch (IOException rollbackFailure) {
            failure.addSuppressed(new IOException(
                "previous model artifact could not be restored; recovery copy remains as "
                    + backup.getFileName(),
                rollbackFailure));
          }
        }
        throw failure;
      }

      // A backup-cleanup error must never undo a verified publication. Leave the
      // old directory recoverable and report the cleanup problem instead.
      if (movedOld) {
        try {
          deleter.delete(backup);
        } catch (IOException cleanupFailure) {
          LOGGER.log(System.Logger.Level.WARNING,
              "Published model artifact {0}, but previous artifact backup {1} remains after cleanup failure: {2}",
              root.getFileName(), backup.getFileName(), cleanupFailure.toString());
        }
      }
    } catch (ModelPublicationLock.ModelPublicationCancelledException cancelled) {
      throw new IOException(cancelled.getMessage(), cancelled);
    }
  }

  private void checkCancelled() throws IOException {
    if (cancellationRequested.getAsBoolean())
      throw new IOException("model artifact publication was cancelled");
  }

  private void verifyPublished(ModelManifest expected, ImportProvenance provenance,
      byte[] expectedPayload) throws IOException {
    if (Files.isSymbolicLink(root) || !Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS))
      throw new IOException("published model artifact is not a real directory");
    Path manifestFile = root.resolve("manifest.json");
    Path payloadFile = root.resolve("model.bin");
    Path provenanceFile = root.resolve("provenance.json");
    requireRegularFile(manifestFile);
    requireRegularFile(payloadFile);
    requireRegularFile(provenanceFile);

    byte[] publishedPayload = Files.readAllBytes(payloadFile);
    String actualChecksum = NativeModelStore.sha256(publishedPayload);
    if (!actualChecksum.equals(expected.sha256())
        || !NativeModelStore.sha256(expectedPayload).equals(actualChecksum))
      throw new IOException("published model payload checksum verification failed");

    Map<String, Object> fields = ModelArtifactJson.parseObject(Files.readString(manifestFile));
    if (!fields.keySet().equals(MANIFEST_FIELDS)
        || !Objects.equals(fields.get("profile"), expected.profile())
        || !Objects.equals(fields.get("zinggVersion"), expected.zinggVersion())
        || !Objects.equals(fields.get("formatVersion"), expected.formatVersion())
        || !Objects.equals(fields.get("modelType"), expected.modelType())
        || !Objects.equals(fields.get("features"), expected.features())
        || !Objects.equals(fields.get("sha256"), actualChecksum))
      throw new IOException("published model manifest verification failed");

    Map<String, Object> rawProvenance = ModelArtifactJson.parseObject(Files.readString(provenanceFile));
    if (!rawProvenance.keySet().equals(PROVENANCE_FIELDS))
      throw new IOException("published model provenance verification failed");
    Map<String, String> actualProvenance = new HashMap<>();
    rawProvenance.forEach((key, value) -> {
      if (value instanceof String string) actualProvenance.put(key, string);
    });
    if (actualProvenance.size() != PROVENANCE_FIELDS.size()
        || !Objects.equals(actualProvenance.get("sourcePath"), provenance.sourcePath())
        || !Objects.equals(actualProvenance.get("sourceVersion"), provenance.sourceVersion())
        || !Objects.equals(actualProvenance.get("importerVersion"), provenance.importerVersion())
        || !Objects.equals(actualProvenance.get("importedAt"), provenance.importedAt().toString())
        || !Objects.equals(actualProvenance.get("sha256"), actualChecksum))
      throw new IOException("published model provenance verification failed");
  }

  private static void requireRegularFile(Path path) throws IOException {
    if (Files.isSymbolicLink(path) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
      throw new IOException("published model artifact contains a missing or linked file: " + path.getFileName());
  }

  private static String provenanceJson(ImportProvenance provenance, String checksum) {
    return "{\"sourcePath\":" + ModelArtifactJson.quote(provenance.sourcePath())
        + ",\"sourceVersion\":" + ModelArtifactJson.quote(provenance.sourceVersion())
        + ",\"importerVersion\":" + ModelArtifactJson.quote(provenance.importerVersion())
        + ",\"importedAt\":" + ModelArtifactJson.quote(provenance.importedAt().toString())
        + ",\"sha256\":" + ModelArtifactJson.quote(checksum) + "}";
  }

  private static void deleteTree(Path path) throws IOException {
    if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return;
    try (var paths = Files.walk(path)) {
      for (Path item : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(item);
    }
  }
}
