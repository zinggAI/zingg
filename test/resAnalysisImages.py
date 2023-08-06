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
cluster_df = cluster_df.select('z_cluster','item_id','brand','item_name','path')
cluster_df.write.csv('/tmp/res_image_cluster')

#write relavant cols to csv for analysis
res_partial = res_df.select('z_cluster','item_id','brand','item_name','path').sort(fn.desc('item_name'))
res_partial.show()
res_partial.write.csv('/tmp/res_analysis_partial')
#analyse above DF one by one for data