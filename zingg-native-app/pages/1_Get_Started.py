import os
import streamlit as st
from utils.db_client import (
    get_workspace_client, list_clusters, check_jar_installed,
    install_jar, restart_cluster, restart_running_cluster, deploy_notebook_set,
)

st.set_page_config(page_title="Get Started", layout="wide")
st.title("Get Started")

APP_NAME = "zingg"

JAR_RELATIVE_PATH = "artifacts/zingg-0.7.0.jar"
JAR_VERSION = "0.7.0"

# 02/03/04 each start with "%run ./01-SettingUp-Zingg", so all four must be
# deployed together into the same folder, with these exact filenames.
BUNDLED_NOTEBOOKS = [
    {"relative_path": "templates/01-SettingUp-Zingg.ipynb", "filename": "01-SettingUp-Zingg",
     "display_name": "1. Setup"},
    {"relative_path": "templates/02-LabelTrainingData.ipynb", "filename": "02-LabelTrainingData",
     "display_name": "2. Label Training Data"},
    {"relative_path": "templates/03-GenerateDocument.ipynb", "filename": "03-GenerateDocument",
     "display_name": "3. Generate Document"},
    {"relative_path": "templates/04-TrainAndMatch.ipynb", "filename": "04-TrainAndMatch",
     "display_name": "4. Train and Match"},
]

st.markdown("""
Set up a cluster and notebook, then run Zingg the way you always have.
""")

w = get_workspace_client()

with st.expander("Before you start"):
    st.markdown("""
1. Create or choose a **single-user cluster** running **Databricks Runtime 16.4 LTS**.
2. Give this app permission to save the notebook to your Workspace folder: open the folder, go to **Permissions**, add this app (see the **Authorization** tab for its name), and set access to **Can Edit**.
3. Attach a **Unity Catalog volume** to this app as a resource with **Can read and write**. The app stores the Zingg JAR there, and the cluster's user needs **READ VOLUME** on it.
""")

st.divider()
st.subheader("1. Select your cluster")

clusters = list_clusters(w)
if not clusters:
    st.error("No clusters visible to this app / user.")
    st.stop()

cluster_labels = [f"{c['name']} ({c['cluster_id']}), {c['state']}" for c in clusters]
selected_idx = st.selectbox("Cluster", range(len(clusters)), format_func=lambda i: cluster_labels[i])
selected_cluster = clusters[selected_idx]
cluster_id = selected_cluster["cluster_id"]

st.caption(f"Spark version: {selected_cluster['spark_version']} | Access mode: {selected_cluster['access_mode']}")

st.divider()
st.subheader("2. Install Zingg JAR")

# The Volume that holds the JAR is a declared app resource (key "volume" in
# app.yaml), bound by the workspace admin when the app is installed. When it is
# bound, its path arrives in ZINGG_JAR_VOLUME. If it is not bound (for example
# a local run), fall back to asking for a folder.
declared_volume = os.getenv("ZINGG_JAR_VOLUME", "").strip()

if declared_volume:
    st.caption("This app installs the Zingg JAR on your cluster, "
               "using the Unity Catalog Volume attached to this app.")
    jar_folder = declared_volume
    st.markdown(f"JAR Volume: `{jar_folder}`")
else:
    st.caption("This app installs the Zingg JAR on your cluster. "
               "Enter the folder where it should go.")
    jar_folder = st.text_input(
        "Destination folder for the JAR",
        value=st.session_state.get("jar_folder", "/Volumes/zingg_catalog/zingg/artifacts"),
    )
st.session_state["jar_folder"] = jar_folder

jar_filename = f"zingg-{JAR_VERSION}.jar"
jar_path = f"{jar_folder.rstrip('/')}/{jar_filename}"
st.caption(f"Will be installed as: `{jar_path}`")

col1, col2 = st.columns(2)
with col1:
    if st.button("Check if JAR is installed"):
        try:
            installed = check_jar_installed(w, cluster_id, jar_path)
            st.session_state["jar_installed"] = installed
            if installed:
                st.success("JAR is installed on this cluster.")
            else:
                st.warning("JAR is not installed on this cluster.")
        except Exception as e:
            st.error(f"Could not check library status: {e}")

with col2:
    if st.button("Install bundled JAR", type="primary"):
        try:
            bundled_jar_local_path = os.path.join(
                os.path.dirname(os.path.dirname(os.path.abspath(__file__))), JAR_RELATIVE_PATH
            )
            install_jar(w, cluster_id, jar_path, bundled_jar_local_path=bundled_jar_local_path)
            st.success(f"Uploaded app's bundled JAR (v{JAR_VERSION}) to {jar_path} and requested install.")
            st.info("A cluster restart may be required for the JAR to load. Use the restart button below if needed.")
        except Exception as e:
            st.error(f"Could not install JAR: {e}")

col_a, col_b = st.columns(2)
with col_a:
    if st.button("Start cluster (if not already running)"):
        try:
            action = restart_cluster(w, cluster_id)
            st.success(f"Cluster {action}. It may take a few minutes to become available.")
        except Exception as e:
            st.error(f"Could not start cluster: {e}")

with col_b:
    st.caption("Restarting stops any other jobs/notebooks currently running on this cluster.")
    confirm_restart = st.checkbox("I understand, restart anyway", key="confirm_restart_running")
    if st.button("Restart running cluster (e.g. to load a newly installed JAR)"):
        if not confirm_restart:
            st.error("Check the box above to confirm before restarting.")
        else:
            try:
                action = restart_running_cluster(w, cluster_id)
                st.success(f"Cluster {action}.")
            except Exception as e:
                st.error(f"Could not restart cluster: {e}")

st.divider()
st.subheader("3. Working with notebooks")

st.caption("This saves all four Zingg notebooks together in one folder, so they can "
           "find each other. Use a new folder each time you want a fresh copy. "
           "An existing notebook at the same path is never overwritten.")

deploy_folder = st.text_input(
    "Workspace folder to save notebooks in (must start with /Workspace/)",
    value=st.session_state.get("deploy_folder", "/Workspace/Users/you@example.com/zingg"),
)
st.session_state["deploy_folder"] = deploy_folder

if not deploy_folder.startswith("/Workspace/"):
    st.warning(
        "This path doesn't start with /Workspace/. Notebooks are stored in the "
        "Workspace object tree, not in Unity Catalog Volumes (/Volumes/...)."
    )

if "notebook_paths" not in st.session_state:
    st.session_state["notebook_paths"] = {}

if st.button("Get notebooks", type="primary"):
    try:
        app_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        results = deploy_notebook_set(w, app_dir, BUNDLED_NOTEBOOKS, deploy_folder)

        for r in results:
            if r["error"]:
                st.error(f"{r['display_name']}: could not get notebook. {r['error']}")
            elif r["was_new"]:
                st.success(f"{r['display_name']}: saved to {r['path']}")
            else:
                st.info(f"{r['display_name']}: already exists at {r['path']}.")
            if r["path"]:
                st.session_state["notebook_paths"][r["display_name"]] = r["path"]
    except Exception as e:
        st.error(f"Could not get notebooks: {e}")

st.markdown("**Open a notebook:**")
for nb in BUNDLED_NOTEBOOKS:
    default_path = f"{deploy_folder.rstrip('/')}/{nb['filename']}"
    path = st.session_state["notebook_paths"].get(nb["display_name"], default_path)

    # A keyed text box keeps its first value and ignores a changed default, so
    # push the computed path into the box whenever that path changes (new folder
    # typed, or notebooks just saved). A manual edit is kept until then.
    widget_key = f"path_{nb['display_name']}"
    if st.session_state.get(f"{widget_key}_source") != path:
        st.session_state[widget_key] = path
        st.session_state[f"{widget_key}_source"] = path

    col1, col2 = st.columns([3, 1])
    with col1:
        notebook_path = st.text_input(nb["display_name"], key=widget_key)
    with col2:
        st.write("")
        if st.button("Open", key=f"open_{nb['display_name']}"):
            try:
                workspace_id = w.get_workspace_id()
                host = w.config.host.rstrip("/")
                notebook_url = f"{host}/?o={workspace_id}#workspace{notebook_path}"
                st.markdown(f"[Click here to open {nb['display_name']}]({notebook_url})")
            except Exception as e:
                st.error(f"Could not build notebook link: {e}")

st.caption("Opens in a new tab. Attach it to the cluster you selected above, update the setup "
           "notebook for your data, then run each notebook in order.")