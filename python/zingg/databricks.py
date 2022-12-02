from zingg.client import *
import os 
from databricks_cli.sdk.api_client import ApiClient
from databricks_cli.jobs.api import JobsApi
from databricks_cli.runs.api import RunsApi 
from databricks_cli.dbfs.api import DbfsApi
from databricks_cli.dbfs.dbfs_path import DbfsPath, DbfsPathClickType
from copy import deepcopy
import datetime
import time
import sys

##handle internal error
##retry
##check header is passed

api_client = ApiClient(
            host  = os.getenv('DATABRICKS_HOST'),
            token = os.getenv('DATABRICKS_TOKEN')
        )

job_spec_template = {
     "email_notifications": {
            "no_alert_for_skipped_runs": 'false'
        },
        "timeout_seconds": 0,
        "max_concurrent_runs": 1,
        "tasks": [
            {
                "spark_python_task": {
                },
                "job_cluster_key": "_cluster",
                "libraries": [
                    {
                         "whl": "https://dbc-2e51e45f-aaeb.cloud.databricks.com/files/jars/a9f7fe7b_887a_4094_9dcc_11ca641cb6a3/zingg-0.3.4-py2.py3-none-any.whl"
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

def getCurrentTime():
    return str(time.time_ns())

class ZinggWithDatabricks(Zingg):

    """ This class is the main point of interface with the Zingg matching product. Construct a client to Zingg using provided arguments and spark master. If running locally, set the master to local.

    :param args: arguments for training and matching
    :type args: Arguments
    :param options: client option for this class object
    :type options: ClientOptions

    """
    
    def __init__(self, args, options, cliArgs):
        self.phase = options.getClientOptions().getOptionValue(ClientOptions.PHASE)
        print('phase ' + self.phase)
        print('cliArgs are ' + '||'.join(cliArgs[1:]))
        if (self.phase == 'label'):
           self.client = jvm.zingg.client.Client(args.getArgs(), options.getClientOptions())
        else:
            self.client = jvm.zingg.client.Client(args.getArgs(), options.getClientOptions(), spark._jsparkSession)
        try:
            self.isRemote = options.getClientOptions().getOptionValue(ClientOptions.REMOTE)
        except:
            self.isRemote = False
        self.localNotebooLocation = cliArgs[0]
        self.dbfsHelper = DbfsHelper()
        self.cliArgs = cliArgs
        self.jobsHelper = JobsHelper()
    

    def init(self):
        ## if label, call dbfs service, copy model
        ## else cp over the notebook and execute that with param remote
        print('phase ' + self.phase)
        if (self.isRemote):
            self.client.init()
        else:
            self.dbfsHelper.copyNotebookToDBFS(self.cliArgs[0])
        

    def execute(self):
        """ Method to execute this class object """
        #self.client.execute()
        ## if label, call dbfs cp and send model back
        if (self.isRemote):
            self.client.execute()
        else:
            job_spec = deepcopy(job_spec_template)
            job_spec['tasks'][0]['task_key'] = self.phase+getCurrentTime()
            job_spec['tasks'][0]['spark_python_task']['python_file'] ='dbfs:/Filestore/' + self.cliArgs[0]
            paramsCopy = self.cliArgs.copy()
            paramsCopy.append(ClientOptions.REMOTE)
            paramsCopy.append("True")
            job_spec['tasks'][0]['spark_python_task']['parameters'] = paramsCopy
            job = self.jobsHelper.createJob(job_spec)
            jobRun = self.jobsHelper.runJob(job)
            self.jobsHelper.pollJobStatus(jobRun)
        
    def initAndExecute(self):
        """ Method to run both init and execute methods consecutively """
        print('phase ' + self.phase)
        self.init()
        self.execute()



class DbfsHelper:
   
    def __init__(self):
        self.dbfs_api=DbfsApi(api_client)

    def copyNotebookToDBFS(self, localLocation):
        print('copying over file to dbfs')
        self.dbfs_api.cp(True, True, localLocation, 'dbfs:/Filestore/' + localLocation)
        

    def copyFromDBFS(self, args):
        print ("copy from dbfs")


class JobsHelper:
    
    def __init__(self):
        self.jobs_api=JobsApi(api_client)
        self.runs_api=RunsApi(api_client)

    def createJob(self, job_spec):
        job = self.jobs_api.create_job(job_spec, {'User-Agent':'zinggai_zingg'})
        return job
    
    def runJob(self, job):
        return self.jobs_api.run_now(job['job_id'], None, None, None, None)


    def pollJobStatus(self, jobRun):
        print ("poll job status")
         # seconds to sleep between checks
        sleep_seconds = 30
        start_time = time.time()

        # loop indefinitely
        while True:
    
            # retrieve job info
            resp = self.runs_api.get_run(jobRun['run_id'])
            
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





