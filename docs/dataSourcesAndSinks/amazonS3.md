# S3

Zingg can use AWS S3 as a source and sink

## Steps to run zingg on S3

* Set a bucket e.g. zingg28032023 and a folder inside it e.g. zingg

* Create aws access key and export via env vars (ensure that the user with below keys has read/write access to above)
	export AWS_ACCESS_KEY_ID=<access key id>
	export AWS_SECRET_ACCESS_KEY=<access key>
	(if mfa is enabled AWS_SESSION_TOKEN env var would also be needed )

* Download hadoop-aws-3.1.0.jar and aws-java-sdk-bundle-1.11.271.jar via maven

* Set above in zingg.conf
	spark.jars=/<location>/hadoop-aws-3.1.0.jar,/<location>/aws-java-sdk-bundle-1.11.271.jar

* Run using below commands

```bash
 ./scripts/zingg.sh --phase findTrainingData --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase label --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase train --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase match --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ```

 ## Model location
	Models etc. would get saved in 
	*Amazon S3 > Buckets > zingg28032023 >zingg > 100*
