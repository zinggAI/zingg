CREATE OR REPLACE PROCEDURE persist_model_proc(train_table STRING, persist_model_tbl STRING)
  RETURNS STRING
  LANGUAGE PYTHON
  RUNTIME_VERSION = '3.8'
  PACKAGES = ('snowflake-snowpark-python','pandas', 'scikit-learn','cloudpickle')
  HANDLER = 'run'
AS
$$
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import PolynomialFeatures
import pandas as pd 
import pickle
def run(session, train_table, persist_model_tbl):
    df_train_pd = session.table(train_table).to_pandas()
    poly = PolynomialFeatures(3)
    Y_train = df_train_pd['Y']
    X_train = df_train_pd.drop(['Y'], axis=1)
    X_train_poly = poly.fit_transform(X_train)
    logreg = LogisticRegression(solver='liblinear', random_state=0)
    logreg.fit(X_train_poly, Y_train)

    #persist logreg into the table persist_model_table
    logreg_hex = pickle.dumps(logreg).hex()

    sql_insert = f"INSERT INTO {persist_model_tbl} VALUES(to_binary('{logreg_hex}','HEX'))"

    #TODO do by bind variable    
    session.sql(sql_insert).collect()

    return "PERSISTED"
$$;

CREATE OR REPLACE PROCEDURE predict_proc(persist_model_table STRING, test_table STRING)
  RETURNS STRING
  LANGUAGE PYTHON
  RUNTIME_VERSION = '3.8'
  PACKAGES = ('snowflake-snowpark-python','pandas', 'scikit-learn','cloudpickle')
  HANDLER = 'run'
AS
$$
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import PolynomialFeatures
import pandas as pd 
import pickle
def run(session, persist_model_table, test_table):

    poly = PolynomialFeatures(3)

    results = session.sql('SELECT MODEL_OBJ FROM '+persist_model_table+'  limit 1').collect()

    logreg_hex = results[0]['MODEL_OBJ']

    logreg = pickle.loads(logreg_hex, encoding='HEX')

    df_test_pd = session.table(test_table).to_pandas()
    X_test = df_test_pd.drop(['Y'], axis=1)
    X_test_poly = poly.fit_transform(X_test)
    Y_pred_test = logreg.predict(X_test_poly) 

    return Y_pred_test
$$;
