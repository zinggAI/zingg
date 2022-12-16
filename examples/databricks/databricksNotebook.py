#from zingg.client import *
#from zingg.pipes import *
from zingg.databricks import *
import sys

#wkspaceUrl = spark.sparkContext.getConf().get('spark.databricks.workspaceUrl')
#token = dbutils.notebook.entry_point.getDbutils().notebook().getContext().apiToken().getOrElse(None)

#print(wkspaceUrl)
#print(token)
jobsHelper = DatabricksJobsHelper()

job = jobsHelper.createJob("test1234", "dbfs:/FileStore/py/febrlDb.py", [ClientOptions.PHASE, "findTrainingData"], 'm5.xlarge', 1)
jobRun = jobsHelper.runJob(job)
jobsHelper.pollJobStatus(jobRun)