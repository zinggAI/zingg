from snowflake.snowpark import Session
from snowflake.snowpark.functions import udf
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import PolynomialFeatures
import pandas as pd 
from sqlalchemy import create_engine

connection_parameters = {"account": "jqrfjub-ynb52084","user": "vikasgupta78","password": "Z!ngg12345"}  

session = Session.builder.configs(connection_parameters).create() 
session.sql('use database TEST_DB').collect()
session.sql('use warehouse compute_wh').collect()

session.add_packages("pandas", "scikit-learn","sqlalchemy")

@udf(name="predict_udf", is_permanent=True, stage_location="@my_stage", replace=True)
def predict_udf() -> int:

	url = 'snowflake://vikasgupta78:Z!ngg12345@jqrfjub-ynb52084/TEST_DB/PUBLIC?role=ACCOUNTADMIN&warehouse=compute_wh'

	engine = create_engine(url)

	connection = engine.connect()

	query_train = "select * from train_data"
	# get the data as pandas DF
	df_train_pd = pd.read_sql(query_train, connection)

	poly = PolynomialFeatures(3)

	Y_train = df_train_pd['y']
	X_train = df_train_pd.drop(['y'], axis=1)
	X_train_poly = poly.fit_transform(X_train)

	# instantiate the model
	logreg = LogisticRegression(solver='liblinear', random_state=0)

	# fit the model
	logreg.fit(X_train_poly, Y_train)
    #TODO save logreg so that can be reused

    #TODO it should be in seperate module as both won't be called together
	#TODO if possible this part needs to be inside a 
    query_test =  "select * from test_data"
    #TODO has to be batched out as can't have everything in memory
    #TODO may be use DataFrame of Snowflake or make this a UDF which can be called per row    
    #TODO Challenge : Number of columns would be dynamic so it can't be given at compile time, it will be known at run time
	df_test_pd = pd.read_sql(query_test, connection)

	X_test = df_test_pd.drop(['y'], axis=1)
	X_test_poly = poly.fit_transform(X_test)
	#TODO persist probability instead of 0 , 1
    Y_pred_test = logreg.predict(X_test_poly)
    
	Y_pred_test
	
	connection.close()

	return 1


session.close()