---
description: >-
  Unifying customer records for customer data platforms, personalization, AML,
  GDPR, analytics, and reporting.
---

# Using External Functions

Enterprises spend massive amounts on building their warehouses, pumping data from different sources to have a clear view of what is happening. However, data-driven insights are only as good as the data itself. To make important business decisions, we need to ensure that our data is clean, consistent, and unified. If the data is riddled with duplicates or not harmonized correctly, the decisions based on them will only be erroneous.

Customer and prospect records in the warehouse come from different devices, offline and online channels. They also come from multiple SAAS and in-house applications like CRMs, ticketing, and procurement. Identifying the unique real-world customer across multiple channels and applications is critical to understand the customer, and delight her with [personalized](https://www.databricks.com/blog/2022/08/04/new-solution-accelerator-customer-entity-resolution.html) messages and offers. Anti-money laundering, CCPA and GDPR can not work without a solid customer identity. Customer identity is also the backbone of AI models on fraud and risk. There is a [growing set of users who are using the warehouse](https://deloitte.wsj.com/articles/bridge-the-customer-data-divide-between-marketing-and-it-01657637033?mod=Deloitte_cmo_wsjsf_h1\&tesla=y\&id=us:2sm:3tw:4dd_dualzone::6dd:20220805181527::7325000954:5\&utm_source=tw\&utm_campaign=dd_dualzone\&utm_content=dd\&utm_medium=social\&linkId=176108611) as the Customer Data Platform, and [identity resolution directly on the warehouse](https://hightouch.com/blog/warehouse-identity-resolution) becomes a building block there.

Let us take a look at this table in Snowflake containing information regarding customer name, address, and other attributes.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff55753b051aa43d8f257_1*5SJWM2K8-NImvus_Oh3sqA.png" alt=""><figcaption><p>Image by Author</p></figcaption></figure>

We can see that the customer **nicole carbone** has multiple records and they have inconsistent values across last name, street, address, and locality attributes. We can try to remove such duplicates using attributes that would be unique to an entity, like for this case SSN number but we cannot be sure they would always be consistent.

What if we want to identify and unify such records, without spending time trying to figure out how attributes need to be compared? As the data has variations, defining match rules gets tricky. Lack of a unique identifier also means [comparing every record with every other record](https://github.com/zinggAI/zingg/#key-zingg-concepts), leading to scalability challenges.

In this article, we will use [Zingg](https://github.com/zinggAI/zingg/), an open-source and scalable ML-based identity resolution tool. We will run Zingg on an AWS EC2 instance, using Snowflake’s external function feature, and resolve customer identities in the table. To build machine learning models for identity resolution, Zingg needs samples of matching and non-matching records.

Zingg has [four main phases](https://docs.zingg.ai/zingg/stepbystep/createtrainingdata) to build and run the ML models for fuzzy matching.

1. **findTrainingData** phase to sample records to show to end user for building training data.
2. An interactive **label** phase where a human annotator marks the pairs pairs from findTrainingData as matches or non matches.
3. The **train** phase where Zingg models utilise the user supplied labels from **label** phase and learns how to identify duplicates.
4. The **match** phase where the models are applied and output with match scores is generated.

Let us now see how we can do identity resolution on Snowflake.

**Step 1: Set up Zingg on AWS EC2 instance**

You can use an existing instance or you can create a new instance for this task. Once the new instance is up and running, we will connect to it using the Visual Studio Code IDE itself, since it facilitates code development on remote machines. The steps to connect VS code to EC2 are as follows

* Install VS Code Remote SSH extension
* Define the config file for Remote SSH extension. Given below is an example file with placeholder values

```json
Host <IP address of the EC2 machine>
HostName <hostname of the EC2 machine>
User <ec2-user>
IdentityFile <path to the private key file which you generated when creating the EC2 instance>
PreferredAuthentications <publickey>
```

* Click on the Open a remote window option in VS Code, select the host which is the EC2 IP you mentioned in the config file and connect.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff55a1508d6a311a083ca_0*0ZJMgHjSgiaob3jt.png" alt=""><figcaption><p>Image By Author</p></figcaption></figure>

The quickest way to run Zingg is to use the Docker image inside this instance.

```docker
docker pull zingg/zingg:0.3.4
docker run -it zingg/zingg:0.3.4 bash
```

To connect Zingg with Snowflake, we need the Snowflake Spark connector and Snowflake JDBC driver as mentioned [her](https://docs.zingg.ai/zingg-0.3.4/connectors/snowflake)e. Follow the [Zingg docs to copy over the jars and set the configuration to add them to the classpath](https://docs.zingg.ai/zingg/connectors/snowflake).

**Step 2: Connect Zingg and Snowflake**

We need to tell Zingg the location of the input records and where to write the matched records. We use the [sample](https://github.com/zinggAI/zingg/blob/main/examples/febrl/configSnow.json) at the Zingg github repository and provide our connection properties to make our config.json.

```json
“data” : [{
  “name”:”identityResolution”,
  “format”:”net.snowflake.spark.snowflake”,
  “props”: {
    “sfUrl”: “uw43628.ap-southeast-1.snowflakecomputing.com”,
    “sfUser”: “troubledWithNonUnifiedData”,
    “sfPassword”:”GIVE YOUR PASSWORD HERE”,
    “sfDatabase”:”nonHarmonizedData”,
    “sfSchema”:”MYSCHEMA”,
    “sfWarehouse”:”COMPUTE_WH”,
    “dbtable”: “disparateCustomerRecords”,
    "application":"zingg_zingg"
  }
}
```

**Step 3: Create an AWS Lambda function to trigger Zingg on EC2**

AWS Lambda is a serverless compute service where we just need to write our code and the entire server side infrastructure is fully managed by AWS. This provides fault tolerance and robustness. The custom code in the Lambda function is executed whenever a request is received for that function via a REST API endpoint. Our Lambda function will receive the Zingg phase name as an input parameter, SSH into the EC2 instance we set up in step 1 and execute that Zingg phase inside the docker container.

AWS expects the following schema for the return value from Lambda functions, an HTTP status code, and data as a nested array. Each row of the data array is a return value, preceded by a row number.

```json
{
“statusCode”: <http_status_code>,
“body”:
{
“data”:[
[ 0, <value> ],
[ 1, <value> ]
]
}
}
```

As we only need to pass the phase name to the function, the input to Lambda from Snowflake will look like this

```json
{
“data”:[
[0, “findTrainingData”]
]
}
```

To parse the above input inside the Lambda function, we use the following python code.

```json
try:
  event_body = event[“body”]
  payload = json.loads(event_body)
  row = payload[“data”]
  #The passed parameters should be Zingg phases or checklog
  row_number = row[0][0]
  phase = row[0][1]
```

We will run [findTrainingData](https://docs.zingg.ai/zingg/stepbystep/createtrainingdata/findtrainingdata), [train](https://docs.zingg.ai/zingg/stepbystep/train) and [match](https://docs.zingg.ai/zingg/stepbystep/train) phases through this procedure. We also add another input option called checklog that will let us see the Zingg logs directly inside Snowflake.

```json
if phase not in [‘findTrainingData’, ‘match’, ‘train’, ‘checklog’]:
  raise ValueError
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(‘ip address of your EC2 instance’, username=’ec2-user’, key_filename=’zinggIdentityResolution.pem’)
```

If the input parameter is a valid phase, the function will SSH into EC2, using the username, IP address of the EC2 instance, and the file path of the private key that was generated when we created the instance.

The following code will execute Zingg phases or read the log file it generates

```json
if phase != ‘checklog’:
#docker exec CONTAINERID bash will create a bash shell inside the #docker container identified with CONTAINERID fab997383957 in EC2  #Zingg instance.
#The container ID is the part between @ #and : , replace #fab997383957 with your current container ID value.
#bash -c will run the command string specified after the -c option #inside the bash terminal.
#the rest is the command and we redirect the output to logfile.txt.
command = “docker exec fab997383957 bash -c ‘zingg.sh — properties-file config/zingg.conf — phase “+ phase + “ — conf examples/febrl/configSnow.json’ > logfile.txt”
stdin, stdout, stderr = ssh.exec_command(command)
json_compatible_string_to_return = json.dumps({“data” : [[ 0, “Started zingg execution for phase = “ + phase]]})
else:
  command = “cat logfile.txt”
  stdin, stdout, stderr = ssh.exec_command(command)
  output = stdout.readlines()
  json_compatible_string_to_return = json.dumps({“data” : [[0, output]]})
```

Let us try and understand the above code. Let us look at the command.

```json
docker exec fab997383957 bash -c ‘zingg.sh — properties-file config/zingg.conf — phase “+ phase + “ — conf examples/febrl/configSnow.json’ > logfile.txt
```

This will

a. Create a bash shell inside the docker container identified with CONTAINERID fab997383957.

b. **bash -c** will run the command string specified after the -c option inside the bash terminal.

c. The rest is the command to run. We redirect the output to logfile.txt.

In EC2, inside the Zingg instance, the container ID is the part between @ and semicolon(:). The following image has the container ID highlighted.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff55957515c32c20e987e_0*TiyGRbv7y5IMXOFN.png" alt=""><figcaption><p>Identity Resolution Docker instance: Image By Author</p></figcaption></figure>

We have 3 Zingg phases to run with Lambda — findTrainingData, train and match. Since the [label](https://docs.zingg.ai/zingg/stepbystep/createtrainingdata/label) phase is interactive it will be run directly on the EC2 instance.

Okay, so now let us put it all together!

* Inside the EC2 instance, let us copy the complete code whose snippets we described above. Let us call this script lambda\_function.py. Please edit the script and specify the placeholder values.

```json
import json
import paramiko
def lambda_handler(event, context):
status_code = 200
try:
  event_body = event[“body”]
  payload = json.loads(event_body)
  row = payload[“data”]
#The passed parameters should be findTrainingData, match or train or checklog
  row_number = row[0][0]
  phase = row[0][1]
  if phase not in [‘findTrainingData’, ‘match’, ‘train’, ‘checklog’]:
    raise ValueError
  ssh = paramiko.SSHClient()
  ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
  ssh.connect(‘<Your ec2 IP>’, username=’ec2-user’, key_filename=’<File path to the private key file generated when you created the ec2 instance>’)
  if phase != ‘checklog’:
    command = “docker exec <your Zingg docker container ID> bash -c ‘zingg.sh — properties-file config/zingg.conf — phase “+ phase + “ — conf examples/febrl/configSnow.json’ > logfile.txt”
    stdin, stdout, stderr = ssh.exec_command(command)
    json_compatible_string_to_return = json.dumps({“data” : [[ 0, “Started zingg execution for phase = “ + phase]]})
  else:
    command = “cat logfile.txt”
    stdin, stdout, stderr = ssh.exec_command(command)
    output = stdout.readlines()
    #error = stdout.readlines()
    json_compatible_string_to_return = json.dumps({“data” : [[0, output]]})
except ValueError:
  status_code = 400
  json_compatible_string_to_return = json.dumps({“data” : [[0, “Invalid parameters passed”]]})
except Exception as e:
  status_code = 400
  json_compatible_string_to_return = json.dumps({“data” : [[0, str(e)]]})
  return {
    ‘statusCode’: status_code,
    ‘body’: json_compatible_string_to_return
  }
```

* We now go to AWS Lambda, select Create Function, give a function name, and select the Python programming language version matching the version on the EC2 instance.
* We select Create a new role with basic Lambda permissions and hit Create Function.
* In EC2, we create a new virtual environment and activate it. Once activated, we install all packages needed by AWS Lambda to run.

```json
python3 –m venv awspackagelayer
```

* Zip the site-packages folder

```json
zip -r deploy.zip /home/ec2-user/awspackagelayer/lib/python3.7/site-packages
```

* Add the script and key file at the root of the zipped folder

```json
zip –g deploy.zip lambda_function.py
zip –g deploy.zip zinggdemonew.pem
```

This is what the final zipped folder directory structure should look like

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff557d369cb44d710b547_0*3vsEKOkBqqPoehkq.png" alt=""><figcaption></figcaption></figure>

* Go to the AWS Lambda function you created, select **Upload From** option and then select **.zip file**

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff557cf1ebc73b680ea14_0*Us48Z7_zQechP-8N.png" alt=""><figcaption></figcaption></figure>

Let’s test the function and see the output. Create a new **Test** event, put the value in **Event JSON** as shown and click on **Test**.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff557be19225f8f7c9bf3_0*kVohwRM5Zv9LwyOG.png" alt=""><figcaption></figcaption></figure>

We get a HTTP 200 status code which means our function has successfully executed . We can see the function output, which says that the Zingg phase findTrainingData has been started.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff558f476c940c57eb00c_0*fjpz6otz7jzL_epz.png" alt=""><figcaption></figcaption></figure>

If we want to check more, we can run tail -f logfile.txt in our EC2 instance and see the processing stages. The output from both AWS Lambda test and log file is shown below.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff55857515c832a0e9878_0*HdBGNkPsQ-Sf49P_.png" alt=""><figcaption><p>Zingg Identity Resolution through VS Code: Image By Author</p></figcaption></figure>

**Step 4 : Connect AWS Lambda with AWS API Gateway**

* Create a new **IAM role** from your AWS account. Entity type should be another AWS account. Specify the **AWS ID** of your account for the field. We will need this when we connect AWS with Snowflake.
* Next, we create an **AWS API gateway**. Select **Gateway Type** as **REST API** with a regional endpoint. Once the API is created, create a resource with a **POST** method. For this method, the **integration type** should be **lambda function. Use Lambda proxy integration** must be selected so that the response data schema is as expected. Mention the Lambda function name we created in step 3, save changes, deploy API by selecting the API name and choose Action, Deploy API and test it. Sample test response input parameter checklog is provided as follows

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff558d0365c4c42b90e1e_0*Kvi4S6ewwPJs0cEh.png" alt=""><figcaption></figcaption></figure>

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff5597bef5765be3bb09e_0*rzSH8pJtpLKMPwE3.png" alt=""><figcaption><p>Image By Author</p></figcaption></figure>

To secure the API gateway, let us add **AWS IAM** authorization to Method Request. We use the following configuration for resource policy and update the placeholders.

```json
{
“Version”: “2012–10–17”,
“Statement”:[
{
“Effect”: “Allow”,
“Principal”:
{
“AWS”: “arn:aws:sts::<YOUR AWS ACCOUNT ID>:assumed-role/<NEW IAM ROLE name>/snowflake”
},
“Action”: “execute-api:Invoke”,
“Resource”: “<method_request_ARN>”
}
]
}
```

**Step 5: Connect Snowflake with AWS**

* In Snowflake, we run use role accountadmin and create the API integration object with the following code

```json
create or replace api integration my_api_integration_01 api_provider = aws_api_gateway api_aws_role_arn = ‘<new_IAM_role_ARN>’ api_allowed_prefixes = (‘https://') enabled = true;
```

We use the ARN of the IAM role from Step 4. **api\_allowed\_prefixes** is set to the resource invocation URL from the AWS API Gateway. The URL can also be found by going to Stages and clicking on stage name. Inside the stage name dropdown options, we see the defined resource. Clicking on the POST method beneath that resource shows the resource invocation URL.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff559d369cb23a210b54c_0*Vq1odmr_qFupiG5B.png" alt=""><figcaption><p>Image By Author</p></figcaption></figure>

* Next, we need to find two values that are required to allow communication between Snowflake and AWS.

```json
describe integration my_api_integration
```

From the output, we note **API\_AWS\_IAM\_USER\_ARN** and **API\_AWS\_EXTERNAL\_ID.**

Let us now edit the Trust Relationship of the IAM role of the API Gateway, place the **API\_AWS\_IAM\_USER\_ARN** as value for the key **Statement.Principal.AWS**

```json
“StringEquals”: { “sts:ExternalId”: API_AWS_EXTERNAL_ID }
```

**Step 6: Create Snowflake external function**

External functions are user-defined functions that run outside Snowflake. The AWS Lambda function we wrote is stored and executed on AWS servers and it is an external function with respect to Snowflake. External functions allow easy access to custom code, REST services involved with data manipulation and API services for machine learning models.

The code for our external function is as follows

```json
create external function my_external_function(param varchar)
returns variant
api_integration = my_api_integration_01
as ‘<resource_invocation_url>’;
```

Run this to grant the proper usage roles

```json
grant usage on function my_external_function(varchar) to accountadmin;
```

* Invoke the external function and get the output as shown below

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff5595b08858127a7d2f3_0*EWpw0_Gysrfn0mH0.png" alt=""><figcaption><p>Finding Sample Pairs to Label To Train Zingg: Image By Author</p></figcaption></figure>

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff55953b051659dd8f268_0*bcSc2qCluZXmD48z.png" alt=""><figcaption><p>Image By Author</p></figcaption></figure>

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff55a838d6627aafd6a3b_0*QSsRwgQ8SMS7CRc9.png" alt=""><figcaption><p>Zingg Identity Resolution Processing: Image By Author</p></figcaption></figure>

We will run findTrainingData as above. Then we will run label directly on the EC2 instance as it needs interaction. The train and match phases are executed through external functions. Once the Zingg phases are done, we will get a new table in Snowflake with the output.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff559f476c950237eb011_0*uYfS71Qrj8j8quOs.png" alt=""><figcaption><p>Customer Identity Resolution Output: Image By Author</p></figcaption></figure>

The output table has three additional columns

_Z\_CLUSTER — unique ID assigned by Zingg, all records in the same cluster are matching or duplicated._

_Z\_MINSCORE — it indicates the least the record matched with any other record in that cluster_

_Z\_MAXSCORE — it indicates the maximum the record matched with any other record in that cluster_

Let’s see these values for “nicole carbone”

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/634ff55bb664f47392dc31c3_0*ntrg7cofLgFI2HmX.png" alt=""><figcaption><p>Customer Identities Resolved: Image By Author</p></figcaption></figure>

All the records with “nicole carbone” are assigned to the same cluster.

Congratulations, identity resolution has been done!

We can now decide a suitable threshold using the scores and use that to automate the post-processing of the identities. Records that cannot be resolved using scores could be passed to a downstream human annotation team for manual review.
