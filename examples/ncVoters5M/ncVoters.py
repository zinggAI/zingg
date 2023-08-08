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

#build the arguments for zingg
args = Arguments()
#set field definitions
recid = FieldDefinition("recid", "string", MatchType.DONT_USE)
givenname = FieldDefinition("givenname", "string", MatchType.FUZZY)
surname = FieldDefinition("surname", "string", MatchType.EXACT)
suburb = FieldDefinition("suburb","string", MatchType.FUZZY)
postcode = FieldDefinition("postcode", "string", MatchType.EXACT)

fieldDefs = [recid, givenname, surname, suburb, postcode]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("104")
args.setZinggDir("models")
args.setNumPartitions(4000)
args.setLabelDataSampleSize(0.1)

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
schema = "recid string, givenname string, surname string, suburb string, postcode string "
inputPipe = CsvPipe("ncVotersTest", "examples/ncVoters5M/5Party-ocp20/", schema)
args.setData(inputPipe)

#setting outputpipe in 'args'
outputPipe = CsvPipe("ncVotersResult", "/tmp/ncVotersOutput")

args.setOutput(outputPipe)

options = ClientOptions([ClientOptions.PHASE,"match"])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()
