## Hardware Sizing 
Zingg has been built to scale. Performance is dependent on 
- The number of records to be matched.
- The number of fields to be compared against each other. 
- The actual number of duplicates. 

Here are some performance numbers you can use to determine the appropriate hardware for your data.
- 120k records of examples/febrl120k/test.csv take 5 minutes to run on a 4 core, 10 GB RAM local Spark cluster.
- TODO: Add 0.5 and 1 m numbers here
- 9m records with 3 fields - first name, last name, email take 45 minutes to run on AWS m5.24xlarge instance with 96 cores, 384 gb RAM 
