# AWS S3

Zingg can use AWS S3 as a source and sink

## Steps to run zingg on S3

* Set a bucket, for example - _zingg28032023_ and a folder inside it,for example - _zingg_
* Create aws access key and export via env vars (ensure that the user with below keys has read/write access to above)\
  `export AWS_ACCESS_KEY_ID=<access key id>`\
  `export AWS_SECRET_ACCESS_KEY=<access key>`\
  (if mfa is enabled AWS\_SESSION\_TOKEN env var would also be needed )
* Download _hadoop-aws-3.1.0.jar_ and _aws-java-sdk-bundle-1.11.271.jar_ via maven
* Set above in zingg.conf\
  spark.jars=//hadoop-aws-3.1.0.jar,//aws-java-sdk-bundle-1.11.271.jar
* Run using below commands

```bash
 ./scripts/zingg.sh --phase findTrainingData --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase label --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase train --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
 ./scripts/zingg.sh --phase match --properties-file config/zingg.conf  --conf examples/febrl/config.json --zinggDir  s3a://zingg28032023/zingg
```

## Model location

```
Models etc. would get saved in 
Amazon S3 > Buckets > zingg28032023 > zingg > 100
```
