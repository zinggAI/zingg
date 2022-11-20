from zingg.client import *
from zingg.pipes import *
import sys

#build the arguments for zingg
args = Arguments()

#phase name to be passed as a command line argument
phase_name = sys.argv[1]

#set field definitions
fname = FieldDefinition("fname", "string", MatchType.FUZZY)
lname = FieldDefinition("lname", "string", MatchType.FUZZY)
streetnumber = FieldDefinition("streetnumber", "string", MatchType.FUZZY)
street = FieldDefinition("street","string", MatchType.FUZZY)
address = FieldDefinition("address", "string", MatchType.FUZZY)
locality = FieldDefinition("locality", "string", MatchType.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchType.FUZZY)
state = FieldDefinition("state", "string", MatchType.FUZZY)
dateofbirth = FieldDefinition("dateofbirth", "string", MatchType.FUZZY)
ssn = FieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [fname, lname, streetnumber, street, address, locality, areacode, state, dateofbirth, ssn]

args.setFieldDefinition(fieldDefs)

#defining input pipe
customerDataStaging = Pipe("customerDataStaging", "jdbc")
customerDataStaging.addProperty("url","jdbc:postgresql://localhost:5432/postgres")
customerDataStaging.addProperty("dbtable", "customers")
customerDataStaging.addProperty("driver", "org.postgresql.Driver")
customerDataStaging.addProperty("user","suchandra")
customerDataStaging.addProperty("password","1234")

#add input pipe to arguments for Zingg client
args.setData(customerDataStaging)

#defining output pipe
customerIdentitiesResolved = Pipe("customerIdentitiesResolved", "jdbc")
customerIdentitiesResolved.addProperty("url","jdbc:postgresql://localhost:5432/postgres")
customerIdentitiesResolved.addProperty("dbtable", "customers_unified")
customerIdentitiesResolved.addProperty("driver", "org.postgresql.Driver")
customerIdentitiesResolved.addProperty("user","suchandra")
customerIdentitiesResolved.addProperty("password","1234")

#add output pipe to arguments for Zingg client
args.setOutput(customerIdentitiesResolved)

#save latest model in directory models/customer360
args.setModelId("customer360")
#store all models in directory models/
args.setZinggDir("models")
#sample size for selecting data for labelling
args.setNumPartitions(4)
#fraction of total dataset to select data for labelling
args.setLabelDataSampleSize(0.5)


options = ClientOptions([ClientOptions.PHASE,phase_name])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()