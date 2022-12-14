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
                        "whl":"dbfs:/FileStore/py/zingg-0.3.4-py2.py3-none-any.whl"
                    },
                    {
                        "pypi": {
                            "package": "databricks-cli"
                        }
                    },
                    {
                      "jar": "dbfs:/FileStore/jars/zingg_0_3_4_SNAPSHOT.jar"
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
                    
                    "spark_env_vars": {
                        "PYSPARK_PYTHON": "/databricks/python3/bin/python3"
                    },
                    "enable_elastic_disk": 'true'
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
    
    def __init__(self, args, options, nodeType, numWorkers, cliArgs):
        self.args = args
        self.cliArgs = cliArgs
        self.phase = options.getClientOptions().getOptionValue(ClientOptions.PHASE)
        self.localNotebookLocation = cliArgs[0]
        self.options = options
        self.nodeType = nodeType
        self.numWorkers = numWorkers
        
        print('phase ' + self.phase)
        print('cliArgs are ' + '||'.join(cliArgs[1:]))
        #remote is an option we set and send to job on databricks so that it can work as normal job there
        if (self.phase == 'label'):
           self.client = jvm.zingg.client.Client(args.getArgs(), options.getClientOptions())
        else:
            self.client = jvm.zingg.client.Client(args.getArgs(), options.getClientOptions(), spark._jsparkSession)
        try:
            self.isRemote = options.getClientOptions().getOptionValue(ClientOptions.REMOTE)
        except:
            self.isRemote = False
            self.dbfsHelper = DbfsHelper()
            self.jobsHelper = JobsHelper()
        
    

    def init(self):
        ## if label, call dbfs service, copy model
        ## else cp over the notebook and execute that with param remote
        print('phase ' + self.phase)
        if (self.isRemote):
            self.client.init()
        else:
            if (self.phase != 'label'):
                self.dbfsHelper.copyNotebookToDBFS(self.localNotebookLocation)
        

    def execute(self):
        """ Method to execute this class object """
        #self.client.execute()
        ## if label, call dbfs cp and send model back
        if (self.isRemote):
            self.client.execute()
        else:
            if (self.phase == 'label'):
                self.dbfsHelper.copyModelFromDBFS(self.args)
                #massage args
                localArgs = self.args.copyArgs(self.phase)
                localArgs.setZinggDir(".")
                zinggWithSpark = ZinggWithSpark(localArgs, self.options)
                zinggWithSpark.initAndExecute()
                self.dbfsHelper.copyModelToDBFS(self.args)
            else:
                job_spec = deepcopy(job_spec_template)
                currTimeString = getCurrentTime()
                job_spec['name'] = self.phase + currTimeString
                job_spec['tasks'][0]['task_key'] = self.phase+currTimeString
                job_spec['tasks'][0]['spark_python_task']['python_file'] ='dbfs:/Filestore/' + self.localNotebookLocation
                paramsCopy = self.cliArgs[1:].copy()
                paramsCopy.append(ClientOptions.REMOTE)
                paramsCopy.append("True")
                job_spec['tasks'][0]['spark_python_task']['parameters'] = paramsCopy
                job_spec['job_clusters'][0]['new_cluster']['node_type_id'] = self.nodeType
                job_spec['job_clusters'][0]['new_cluster']['num_workers'] = int(self.numWorkers)
                print(job_spec)
                
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
        self.api_client = ApiClient(
            host  = os.getenv('DATABRICKS_HOST'),
            token = os.getenv('DATABRICKS_TOKEN')
        )
        self.dbfs_api=DbfsApi(self.api_client)

    def copyNotebookToDBFS(self, localLocation):
        print('copying over file to dbfs')
        self.dbfs_api.cp(True, True, localLocation, 'dbfs:/Filestore/' + localLocation)
        

    def copyModelFromDBFS(self, args):
        print ("copy model from dbfs")
        print("dbfs location is " + "dbfs:" + args.getZinggBaseModelDir())
        self.dbfs_api.cp(True, True, 'dbfs:' + args.getZinggBaseModelDir(), './' + args.getModelId())
    
    def copyModelToDBFS(self, args):
        print ("copy model from dbfs")
        ##backup in dbfs
        self.dbfs_api.cp(True, True, 'dbfs:' + args.getZinggBaseModelDir(), 'dbfs:' + args.getZinggBaseModelDir() + "/backup/" + getCurrentTime())
        self.dbfs_api.cp(True, True, './' + args.getModelId(), 'dbfs:' + args.getZinggBaseModelDir())


class JobsHelper:
    
    def __init__(self):
        self.api_client = ApiClient(
            host  = os.getenv('DATABRICKS_HOST'),
            token = os.getenv('DATABRICKS_TOKEN')
        )
        self.jobs_api=JobsApi(self.api_client)
        self.runs_api=RunsApi(self.api_client)

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





