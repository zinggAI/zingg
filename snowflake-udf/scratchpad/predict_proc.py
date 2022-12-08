CREATE OR REPLACE PROCEDURE predict_proc(train_table STRING, test_table STRING)
  RETURNS STRING
  LANGUAGE PYTHON
  RUNTIME_VERSION = '3.8'
  PACKAGES = ('snowflake-snowpark-python','pandas', 'scikit-learn')
  HANDLER = 'run'
AS
$$
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import PolynomialFeatures
import pandas as pd 
def run(session, train_table, test_table):
	df_train_pd = session.table(train_table).to_pandas()
	poly = PolynomialFeatures(3)
	Y_train = df_train_pd['Y']
	X_train = df_train_pd.drop(['Y'], axis=1)
	X_train_poly = poly.fit_transform(X_train)
	logreg = LogisticRegression(solver='liblinear', random_state=0)
	logreg.fit(X_train_poly, Y_train)
	
	df_test_pd = session.table(test_table).to_pandas()
	X_test = df_test_pd.drop(['Y'], axis=1)
	X_test_poly = poly.fit_transform(X_test)
	Y_pred_test = logreg.predict(X_test_poly)    
	return Y_pred_test
$$;
