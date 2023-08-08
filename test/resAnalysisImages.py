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

from zingg.client import *
from zingg.pipes import *
from pyspark.sql.types import *
import pandas

import pyspark.sql.functions as fn
from sentence_transformers import SentenceTransformer, util
import torch
import pickle

from PIL import Image


#read data from parquet
res_df = getSparkSession().read.parquet('/tmp/resultSmallImages')
res_df.count()
res_df.show()

#get another df having z_cluster count more than 1
cluster_df = res_df.groupBy('z_cluster').count()
cluster_df = cluster_df.filter("count > 1")
cluster_df = cluster_df.sort(fn.desc('count'))
cluster_df.write.csv('/tmp/res_image_cluster')

#write relavant cols to csv for analysis
res_partial = res_df.select('z_cluster','item_id','brand','item_name','path').sort(fn.desc('item_name'))
res_partial.show()
res_partial.write.csv('/tmp/res_analysis_partial')
#analyse above DF one by one for data