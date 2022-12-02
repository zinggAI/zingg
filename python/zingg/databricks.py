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

api_client = ApiClient(
            host  = os.getenv('DATABRICKS_HOST'),
            token = os.getenv('DATABRICKS_TOKEN')
        )

job_spec = {
     "email_notifications": {
            "no_alert_for_skipped_runs": 'false'
        },
        "timeout_seconds": 0,
        "max_concurrent_runs": 1,
        "tasks": [
            {
                
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
        
    def initAndExecute(self):
        """ Method to run both init and execute methods consecutively """
        print('phase ' + self.phase)
        self.init()
        #self.execute()



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

    def runJob():
        job = jobs_api.create_job(job_spec)


    def pollJobStatus():
         print ("poll job status")





