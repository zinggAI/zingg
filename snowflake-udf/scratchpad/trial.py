from snowflake.snowpark import Session
from snowflake.snowpark.functions import udf
from sklearn.linear_model import LogisticRegression
import pandas as pd 
from sqlalchemy import create_engine

url = 'snowflake://vikasgupta78:Z!ngg12345@jqrfjub-ynb52084/TEST_DB/PUBLIC?role=ACCOUNTADMIN&warehouse=compute_wh'

engine = create_engine(url)

connection = engine.connect()

query_train = "select * from train_data"
# get the data as pandas DF
df_train_pd = pd.read_sql(query_train, connection)

Y_train = df_train_pd['y']
X_train = df_train_pd.drop(['y'], axis=1)

# instantiate the model
logreg = LogisticRegression(solver='liblinear', random_state=0)

# fit the model
logreg.fit(X_train, Y_train)

query_test =  "select * from test_data"
df_test_pd = pd.read_sql(query_test, connection)

X_test = df_test_pd.drop(['y'], axis=1)
Y_pred_test = logreg.predict(X_test)
Y_pred_test

connection.close()