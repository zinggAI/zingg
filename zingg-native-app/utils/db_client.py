"""
utils/db_client.py

Shared Databricks SDK client and config helpers for all app pages.
"""

import io
import os
import streamlit as st
from databricks.sdk import WorkspaceClient
from databricks.sdk.core import Config


def get_workspace_client() -> WorkspaceClient:
    """Returns a WorkspaceClient authenticated as the APP'S SERVICE PRINCIPAL
    (app authorization) -- required for cluster management, library
    installation, and writing notebooks into the Workspace"""
    return WorkspaceClient()


def get_current_user_email() -> str:
    """Returns the current user's email from the forwarded header, for
    display purposes."""
    return st.context.headers.get("X-Forwarded-Email", "")


def list_clusters(w: WorkspaceClient):
    """Returns a list of (cluster_id, cluster_name, state) for clusters the
    current user can see, for the cluster-picker dropdown on the Get Started page."""
    clusters = []
    for c in w.clusters.list():
        clusters.append({
            "cluster_id": c.cluster_id,
            "name": c.cluster_name,
            "state": str(c.state) if c.state else "UNKNOWN",
            "spark_version": c.spark_version,
            "access_mode": str(getattr(c, "data_security_mode", "UNKNOWN")),
        })
    return clusters


def check_jar_installed(w: WorkspaceClient, cluster_id: str, jar_volume_path: str) -> bool:
    """Checks whether the Zingg OSS JAR is already installed on the given
    cluster. Returns True/False."""
    for lib_status in w.libraries.cluster_status(cluster_id):
        lib = lib_status.library
        if lib and lib.jar == jar_volume_path:
            return True
    return False


def install_jar(w: WorkspaceClient, cluster_id: str, jar_volume_path: str,
                bundled_jar_local_path: str = None) -> None:
    """Installs the Zingg OSS JAR on the given cluster via the Libraries API.
    """
    from databricks.sdk.service.compute import Library

    if bundled_jar_local_path:
        with open(bundled_jar_local_path, "rb") as f:
            content = f.read()
        w.files.upload(jar_volume_path, io.BytesIO(content), overwrite=True)

    w.libraries.install(cluster_id=cluster_id, libraries=[Library(jar=jar_volume_path)])


def restart_cluster(w: WorkspaceClient, cluster_id: str) -> str:
    """Starts or restarts the given cluster, whichever is correct for its
    current state. Uses the SDK's own ensure_cluster_is_running() helper,
    which handles the PENDING/RUNNING/TERMINATED state distinction internally
    """
    w.clusters.ensure_cluster_is_running(cluster_id)
    return "started (or confirmed already running)"


def restart_running_cluster(w: WorkspaceClient, cluster_id: str) -> str:
    """Explicitly restarts a cluster that is currently running (e.g. to pick
    up a newly installed library). Only call this when the cluster is known
    to be running -- restart() is invalid on a terminated cluster."""
    w.clusters.restart(cluster_id)
    return "restarted"


def deploy_notebook_template(w: WorkspaceClient, template_local_path: str,
                             target_folder: str, target_filename: str) -> tuple[str, bool]:
    """Deploys one bundled template notebook (a plain .ipynb file, tracked
    in this app's own Git repo alongside app.yaml, the JAR, etc.) to
    target_folder/target_filename in the Workspace.

    Never overwrites an existing file. If that exact path already exists,
    does nothing and returns (path, False). Returns (path, True) if newly
    written.

    template_local_path: a LOCAL filesystem path (the running app's own
    checked-out files, same as how install_jar reads the bundled JAR).
    """
    from databricks.sdk.service.workspace import ImportFormat

    if not target_folder.startswith("/Workspace/"):
        raise ValueError(
            f"target_folder must be a Workspace path starting with /Workspace/ "
            f"(got: {target_folder}). Notebooks cannot be stored under /Volumes/."
        )

    target_path = f"{target_folder.rstrip('/')}/{target_filename}"

    try:
        w.workspace.get_status(target_path)
        return target_path, False  # already exists, untouched
    except Exception:
        pass  # doesn't exist yet -- safe to write

    w.workspace.mkdirs(target_folder)

    with open(template_local_path, "rb") as f:
        content = f.read()

    try:
        w.workspace.upload(target_path, content, format=ImportFormat.JUPYTER, overwrite=False)
    except Exception as e:
        if "does not exist" in str(e):
            raise RuntimeError(
                f"Could not write to {target_folder}. This folder exists, "
                f"but the app's service principal likely lacks write "
                f"permission on it. Grant the app's service principal "
                f"'Can Manage' or 'Can Edit' permission on this Workspace "
                f"folder, then try again. Original error: {e}"
            ) from e
        raise

    return target_path, True


def deploy_notebook_set(w: WorkspaceClient, app_local_dir: str,
                        notebooks: list, target_folder: str) -> list:
    """Deploys a linked SET of modular notebooks into the SAME target_folder,
    preserving each one's original filename exactly.

    app_local_dir: the app's own local root directory (e.g.
    os.path.dirname(os.path.dirname(__file__)) from the calling page).
    """
    results = []
    for nb in notebooks:
        local_path = os.path.join(app_local_dir, nb["relative_path"])
        try:
            path, was_new = deploy_notebook_template(w, local_path, target_folder, nb["filename"])
            results.append({"display_name": nb["display_name"], "path": path, "was_new": was_new, "error": None})
        except Exception as e:
            results.append({"display_name": nb["display_name"], "path": None, "was_new": False, "error": str(e)})
    return results