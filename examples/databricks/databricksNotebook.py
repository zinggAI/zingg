#  Zingg
#  Copyright (C) 2021-Present  Zingg Labs,inc
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Affero General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

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