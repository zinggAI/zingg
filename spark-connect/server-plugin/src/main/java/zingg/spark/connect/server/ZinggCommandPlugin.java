package zingg.spark.connect.server;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.connect.plugin.CommandPlugin;
import org.apache.spark.sql.connect.planner.SparkConnectPlanner;

import scala.Option;
import scala.runtime.BoxedUnit;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.options.ZinggOption;
import zingg.common.client.options.ZinggOptions;
import zingg.spark.client.SparkClient;
import zingg.spark.connect.proto.ZinggCommand;

/**
 * Spark Connect CommandPlugin that dispatches a ZinggCommand (packed as the
 * Any extension on spark.connect.Command) into the existing, unmodified
 * IZingg/ZinggOptions/SparkZFactory execution path -- this class adds no new
 * matching/training logic of its own, it only bridges the Connect wire
 * protocol to zingg.spark.client.SparkClient exactly as SparkClient.main /
 * the py4j Zingg python wrapper already do.
 *
 * Registration (server side, not wired into any build yet):
 *   --conf spark.connect.extensions.command.classes=zingg.spark.connect.server.ZinggCommandPlugin
 *
 * Limitation: CommandPlugin#process only signals handled/not-handled -- there
 * is no channel back to the client for a data payload. A thrown exception
 * propagates to the client as a gRPC error; a normal return just completes
 * the RPC with no result rows. That is sufficient for fire-and-execute phases
 * (train, match, trainMatch, link, findTrainingData, generateDocs, recommend,
 * updateLabel) but NOT for phases that must return row data to the caller
 * (the interactive label/findAndLabel loop) -- those need a RelationPlugin
 * instead, which is not implemented in this module yet.
 */
public class ZinggCommandPlugin implements CommandPlugin {

	public static final Log LOG = LogFactory.getLog(ZinggCommandPlugin.class);

	/**
	 * CommandPlugin is a Scala trait, not a plain Java interface: the actual
	 * released spark-connect_2.12 jars (verified by decompiling both the
	 * 3.4.0 and 3.5.5 artifacts with javap -- the interface shape differs
	 * from what's currently on the apache/spark master branch source, which
	 * has since moved to a plain Java boolean/byte[] signature) declare
	 *   process(org.sparkproject.connect.protobuf.Any): scala.Option[BoxedUnit]
	 * i.e. None = "not handled", Some(()) = "handled". The Any parameter is
	 * Spark's own shaded protobuf type -- structurally identical to our
	 * unshaded com.google.protobuf.Any (same type_url/value fields), just
	 * relocated to avoid classpath clashes inside Spark's own jar -- so we
	 * round-trip it through bytes into the Any type our generated
	 * ZinggCommand code actually understands.
	 */
	@Override
	public Option<BoxedUnit> process(org.sparkproject.connect.protobuf.Any command, SparkConnectPlanner planner) {
		Any any;
		try {
			any = Any.parseFrom(command.toByteArray());
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException("Malformed Spark Connect command extension", e);
		}
		if (!any.is(ZinggCommand.class)) {
			// Not ours -- let any other registered CommandPlugin take a turn.
			return Option.empty();
		}

		ZinggCommand zinggCommand;
		try {
			zinggCommand = any.unpack(ZinggCommand.class);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException("Failed to unpack ZinggCommand", e);
		}

		try {
			runPhase(zinggCommand, planner.session());
		} catch (ZinggClientException e) {
			// CommandPlugin#process declares no checked exceptions; wrap so
			// the failure still propagates to the Connect client as an error.
			throw new RuntimeException("Zingg phase '" + zinggCommand.getPhase() + "' failed", e);
		}
		return Option.apply(BoxedUnit.UNIT);
	}

	private void runPhase(ZinggCommand zinggCommand, SparkSession session) throws ZinggClientException {
		String phase = zinggCommand.getPhase();
		ZinggOptions.verifyPhase(phase);
		ZinggOption zinggOption = ZinggOptions.getByValue(phase);
		if (zinggOption.equals(ZinggOptions.LABEL) || zinggOption.equals(ZinggOptions.FIND_AND_LABEL)) {
			throw new ZinggClientException(
					"Phase '" + phase + "' returns row data to the caller and is not yet supported over "
							+ "the Spark Connect CommandPlugin path -- it needs a RelationPlugin implementation.");
		}

		IZArgs args = ZinggProtoConverters.toJavaArguments(zinggCommand.getArgs());
		ClientOptions options = ZinggProtoConverters.toJavaClientOptions(phase, zinggCommand.getOptions());

		LOG.info("Running Zingg phase '" + phase + "' via Spark Connect command plugin");
		SparkClient client = new SparkClient(args, options, session);
		client.init();
		client.execute();
		client.stop();
	}
}
