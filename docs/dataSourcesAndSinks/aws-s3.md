---
title: AWS S3
parent: Data Sources and Sinks
---


1. Set a bucket e.g. zingg28032023 and a folder inside it e.g. zingg

2. Create aws access key and export via env vars (ensure that the user with below keys has read/write access to above):

export AWS_ACCESS_KEY_ID=<access key id>
export AWS_SECRET_ACCESS_KEY=<access key>

(if mfa is enabled AWS_SESSION_TOKEN env var would also be needed )

3. Download hadoop-aws-3.1.0.jar and aws-java-sdk-bundle-1.11.271.jar via maven

4. Set above in zingg.conf :
spark.jars=/<location>/hadoop-aws-3.1.0.jar,/<location>/aws-java-sdk-bundle-1.11.271.jar

5. Run using:

 ./scripts/zingg.sh --phase findTrainingData --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase label --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase train --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase match --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg

6. Models etc. would get saved in 
Amazon S3 > Buckets > zingg28032023 >zingg > 100
