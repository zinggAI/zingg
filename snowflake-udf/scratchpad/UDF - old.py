from snowflake.snowpark import Session
from snowflake.snowpark.functions import udf
from sklearn.linear_model import LogisticRegression
import pandas as pd 
import snowflake.connector

connection_parameters = {
    "account": "jqrfjub-ynb52084",
    "user": "vikasgupta78",
    "password": "Z!ngg12345"
  }  
  
session = Session.builder.configs(connection_parameters).create() 
session.sql('use database TEST_DB').collect()
session.sql('use warehouse compute_wh').collect()

session.add_packages("pandas", "scikit-learn")
session.add_import("/home/ec2-user/.local/lib/python3.8/site-packages/snowflake/connector/__init__.py")

@udf(name="predict_udf", is_permanent=True, stage_location="@my_stage", replace=True)
def predict_udf(x_dummy: int) -> int:

	conn = snowflake.connector.connect(user="vikasgupta78",password="Z!ngg12345",account="jqrfjub-ynb52084",warehouse="compute_wh",database="TEST_DB",schema="PUBLIC")

	cur = conn.cursor()

	sql = "select * from train_data"
	cur.execute(sql)
	# get the data as pandas DF
	df_train_pd = cur.fetch_pandas_all()

	Y_train = df_train_pd['Y']
	X_train = df_train_pd.drop(['Y'], axis=1)

	# instantiate the model
	logreg = LogisticRegression(solver='liblinear', random_state=0)

	# fit the model
	logreg.fit(X_train, Y_train)

	sql_test =  "select * from test_data"
	cur_test = conn.cursor()
	cur_test.execute(sql_test)
	df_test_pd = cur_test.fetch_pandas_all()

	X_test = df_test_pd.drop(['Y'], axis=1)
	Y_pred_test = logreg.predict(X_test)
	Y_pred_test
	
	conn.close()

session.close()