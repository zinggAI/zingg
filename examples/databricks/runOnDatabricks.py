from zingg.client import *
from zingg.pipes import *
import os 
from databricks_cli.sdk.api_client import ApiClient
from databricks_cli.jobs.api import JobsApi
from databricks_cli.runs.api import RunsApi 
from databricks_cli.dbfs.api import DbfsApi
from databricks_cli.dbfs.dbfs_path import DbfsPath, DbfsPathClickType
from copy import deepcopy
import datetime
import time

nowTime = str(time.time_ns())
name = 'findTrainingData' + nowTime
task_key = nowTime
job_spec = {
 
        "name": name,
        "email_notifications": {
            "no_alert_for_skipped_runs": 'false'
        },
        "timeout_seconds": 0,
        "max_concurrent_runs": 1,
        "tasks": [
            {
                "task_key": task_key,
                "spark_python_task": {
                    "python_file": "dbfs:/FileStore/febrlEx.py",
                    "parameters": ["findTrainingData"]
                },
                "job_cluster_key": "_cluster",
                "libraries": [
                    {
                        "pypi": {
                            "package": "zingg"
                        }
                    },
                    {
                      "jar": "dbfs:/FileStore/zingg_0_3_4_SNAPSHOT.jar"
                   }
                   ],
                "timeout_seconds": 0,
                "email_notifications": {}
            }
        ],
        "job_clusters": [
            {
                "job_cluster_key": "_cluster",
                "new_cluster": {
                    "spark_version": "10.4.x-scala2.12",
                    "node_type_id": "m5.large",
                    "spark_env_vars": {
                        "PYSPARK_PYTHON": "/databricks/python3/bin/python3"
                    },
                    "enable_elastic_disk": 'true',
                    "num_workers": 1
            }
            }
        ],
        "format": "MULTI_TASK"
    
}


print ('calling api client')
api_client = ApiClient(
  host  = os.getenv('DATABRICKS_HOST'),
  token = os.getenv('DATABRICKS_TOKEN')
)

jobs_api = JobsApi(api_client)
job = jobs_api.create_job(job_spec)


dbfs_api=DbfsApi(api_client)
#dbfs_api.cp(True, True, 'dbfs:/models/100', '.')
dbfs_api.cp(True, True, '/tmp/dbfs/models/100', 'dbfs:/models/100')

