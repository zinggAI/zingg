from zingg.client import *
from zingg.pipes import *
from pyspark.sql.types import *
import pandas

import pyspark.sql.functions as fn 
from sentence_transformers import SentenceTransformer, util
import torch
import pickle
 
from PIL import Image

df = (spark.read.json('/home/ubuntu/image_data/listings/metadata'))

df = (
  df
    .filter("country='US'")
    .select(
      'item_id',
      'brand',
      'bullet_point',
      'domain_name',
      'marketplace',
      'item_keywords',
      'item_name',
      'product_description',
      'main_image_id',
      'other_image_id',
      'node'
    )
  )

image_metadata = (
  spark
    .read
    .csv(
      path='/home/ubuntu/image_data/images/metadata',
      sep=',',
      header=True,
      )   
    )  

@fn.udf(ArrayType(StringType()))
def get_english_values_from_array(array=None):
 
   # prioritized list of english language codes (biased towards us english)
  english = ['en_US','en_CA','en_GB','en_AU','en_IN','en_SG','en_AE']
 
  # initialize search 
  values = []
  if array is None: array=[]
 
  # for each potential english code
  for e in english:
 
    # for each item in array
    for a in array:
      # if we found the english variant we want
      if a['language_tag']==e: 
        # get value and stop
        values += [a['value']] 
 
    # if value has been found, then break
    if len(values) > 0: break
    
  return values

model = SentenceTransformer('clip-ViT-B-32')

@fn.udf(ArrayType(DoubleType()))
#@fn.udf(StringType())
def get_image_embedding(path):
 
  embedding = []
 
  if path is not None:
 
    full_path = '/home/ubuntu/image_data/images/small/' + path
  
    # open image and convert to embedding
    try:
      image = Image.open(full_path).convert('RGB')
      embedding = model.encode(image, batch_size=128, convert_to_tensor=False, show_progress_bar=False)
      embedding = embedding.tolist()
    except:
      print(exception)
    
  # return embedding value
  return embedding
  
items = (
  df
    .alias('a')
    .select(
      'item_id',
      'domain_name',
      'marketplace',
      get_english_values_from_array('brand')[0].alias('brand'),
      get_english_values_from_array('item_name')[0].alias('item_name'),
      get_english_values_from_array('product_description')[0].alias('product_description'),
      get_english_values_from_array('bullet_point').alias('bulletpoint'),
      get_english_values_from_array('item_keywords').alias('item_keywords'),
      fn.split( fn.col('node')[0]['node_name'], '/').alias('hierarchy'),
      'main_image_id'
      )
    .join(
      image_metadata.alias('b').select('image_id','path'),
      on=fn.expr('a.main_image_id=b.image_id'),
      how='left'
      )
    .withColumn('main_image_embedding', get_image_embedding(fn.col('path')))
    .drop('main_image_id','image_id','path','bulletpoint','item_keywords','hierarchy')
  )

#build the arguments for zingg
args = Arguments()
#set field definitions
#TODO MARKING STR ARRAYS AS DONT_USE FOR NOW
item_id = FieldDefinition("item_id", "string", MatchType.DONT_USE)
domain_name = FieldDefinition("domain_name", "string", MatchType.FUZZY)
marketplace = FieldDefinition("marketplace", "string", MatchType.FUZZY)
brand = FieldDefinition("brand","string", MatchType.FUZZY)
item_name = FieldDefinition("item_name", "string", MatchType.FUZZY)
product_description = FieldDefinition("product_description", "string", MatchType.DONT_USE)
#bulletpoint = FieldDefinition("bulletpoint", "ARR_STR_TYPE", MatchType.DONT_USE)
#item_keywords = FieldDefinition("item_keywords", "ARR_STR_TYPE", MatchType.DONT_USE)
#hierarchy = FieldDefinition("hierarchy","ARR_STR_TYPE", MatchType.DONT_USE)
main_image_embedding = FieldDefinition("main_image_embedding", "ARR_DOUBLE_TYPE", MatchType.FUZZY)

#fieldDefs = [item_id, domain_name, marketplace, brand, item_name,product_description, bulletpoint, item_keywords, hierarchy, main_image_embedding]
fieldDefs = [item_id, domain_name, marketplace, brand, item_name,product_description, main_image_embedding]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("9999")
args.setZinggDir("/tmp/modelSmallImages")
args.setNumPartitions(8)
args.setLabelDataSampleSize(0.001)

inputPipeSmallImages=InMemoryPipe("smallImages")
inputPipeSmallImages.setDataset(items)

args.setData(inputPipeSmallImages)

#setting outputpipe in 'args'
outputPipe = CsvPipe("resultSmallImages", "/tmp/resultSmallImages")

args.setOutput(outputPipe)

options = ClientOptions([ClientOptions.PHASE,"findTrainingData"])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()

