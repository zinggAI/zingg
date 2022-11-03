from zingg.client import *
from zingg.pipes import *
import os 
from databricks_cli.sdk.api_client import ApiClient
from databricks_cli.jobs.api import JobsApi
from databricks_cli.runs.api import RunsApi 
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
print(job)

job['notebook_params'] = {}
runs_api = RunsApi(api_client) 
jobRun = jobs_api.run_now(job['job_id'], None, None, None, None)
print(jobRun)




# seconds to sleep between checks
sleep_seconds = 30
start_time = time.time()

# loop indefinitely
while True:
    
    # retrieve job info
    resp = runs_api.get_run(jobRun['run_id'])
    
    #calculate elapsed seconds
    elapsed_seconds = int(time.time()-start_time)
    
    # get job lfe cycle state
    life_cycle_state = resp['state']['life_cycle_state']
    
    # if terminated, then get result state & break loop
    if life_cycle_state == 'TERMINATED':
        result_state = resp['state']['result_state']
        break
        
    # else, report to user and sleep
    else:
        if elapsed_seconds > 0:
            print(f'Job in {life_cycle_state} state at { elapsed_seconds } seconds since launch.  Waiting {sleep_seconds} seconds before checking again.', end='\r')
            time.sleep(sleep_seconds)

# return results
print(f'Job completed in {result_state} state after { elapsed_seconds } seconds.  Please proceed with next steps to process the records identified by the job.')
print('\n')         

