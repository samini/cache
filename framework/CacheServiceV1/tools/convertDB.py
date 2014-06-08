""""
   Copyright 2010 Shahriyar Amini

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
"""

import sqlite3
import commands
import sys
from time import strftime

# accepts an opened sqlite connection cursor
def getTableNames(c):
	tableNames = []
	statement = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
        c.execute(statement)
	for row in c:
		tableNames.append(row[0])
	return tableNames

def getColumns(c, table):
        columns = []
	statement = "PRAGMA table_info(%s);" %table 
	c.execute(statement)
	for row in c:
		columns.append(row[1])
        return columns

inputDB = sys.argv[1]

conn = sqlite3.connect(inputDB)
c = conn.cursor()

dbName = inputDB.rstrip('.db')
ts = strftime("%m%d%Y_%H%M")

# get all the tables
tables = getTableNames(c)

for table in tables:
	outputFileName = "%s_%s_%s.csv" %(dbName, table, ts)
	f = open(outputFileName,'w')

	# write the column header
	columns = getColumns(c, table)
	writeString = ''
	for column in columns:
		writeString = writeString + str(column) + ','
	writeString = writeString.rstrip(',')
	writeString = writeString + '\n'
	f.write(writeString)

	# get the tables entries
	c.execute('select * from %s;' %table)

	entryCount = 0

	for row in c:
		entryCount = entryCount + 1
		writeString = ''
		for i in range(0,len(row)):
			writeString = writeString + str(row[i]) + ','
		writeString = writeString.rstrip(',')
		f.write(writeString + '\n')
		print writeString

	print 'Number of entries: ' +  str(entryCount)

	f.close()

c.close()
conn.close()
