# Zingg Entity Resolution and MDM

Zingg is an entity resolution engine that identifies and links duplicate or related records, such as customers, suppliers, or products, using machine learning. It resolves scattered records into one accurate view, running natively on your own data, with no data leaving your environment.

This app gets Zingg ready to run in your Databricks workspace. It sets up your cluster and deploys the Zingg notebooks you'll use to label data, train a model, and run matching.

## What this app does

1. Lets you pick an existing Databricks cluster to run Zingg on.
2. Installs the Zingg JAR on that cluster.
3. Deploys four Zingg notebooks into a Workspace folder of your choice:
   - **1. Setup**: configure your tables, fields, and match settings
   - **2. Label Training Data**: label candidate record pairs as matches or non-matches
   - **3. Generate Document**: produces a readable summary of your labelled data
   - **4. Train and Match**: trains the model and runs matching

Once the notebooks are deployed, you run them yourself, in order, directly in Databricks. This app does not run Zingg for you and does not read, store, or process your data or match results.

## Before you start

- A Databricks workspace with Unity Catalog enabled.
- A single-user cluster running Databricks Runtime 16.4 LTS. You can use an existing one or create a new one.
- A Unity Catalog Volume where the app can store the Zingg JAR. The app does not create one.

## Permissions required

This app acts using its own Databricks service principal. Before using it, set up the following:

- Add your Volume to the app as a **Unity Catalog volume** resource with **Can read and write**, so the app can store the JAR there.
- Give the app's service principal access to manage the cluster you plan to use.
- Give the app's service principal **Can Edit** permission on the Workspace folder where you want the notebooks saved.
- Make sure the user the cluster is assigned to has **READ VOLUME** on that Volume, so the cluster can install the JAR.

You can find the app's service principal name under the app's **Authorization** tab in Databricks.

## Steps to use this app

1. **Select your cluster.** Open the app and choose the cluster you want Zingg to run on from the dropdown.
2. **Install the Zingg JAR.** The app stores the JAR in the Volume attached to it. Click **Check if JAR is installed**. If it isn't, click **Install bundled JAR**. If prompted, restart the cluster so the JAR loads.
3. **Get the notebooks.** Enter a Workspace folder path (must start with `/Workspace/`) and click **Get notebooks**. All four notebooks are deployed together, since they depend on each other.
4. **Open and run the notebooks.** Click **Open** next to each notebook to get a direct link. Attach each one to your cluster. Start with **1. Setup**, enter your own table names, fields, and match settings, then run the notebooks in order: Setup, Label Training Data, Generate Document, Train and Match.

## Support

- Documentation: [docs.zingg.ai](https://docs.zingg.ai)
- Community: [Zingg Slack](https://join.slack.com/t/zinggai/shared_invite/zt-w7zlcnol-vEuqU9m~Q56kLLUVxRgpOA)
- Zingg Enterprise (persistent Zingg IDs, incremental matching): [sonal@zingg.ai](mailto:sonal@zingg.ai)