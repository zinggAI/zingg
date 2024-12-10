---
description: >-
  ML based entity resolution to identify duplicate customers in dimension
  tables.
---

# Using Config JSON

Warehouses, being the destination of source data from multiple enterprise systems and applications, often become messy with duplicate information. A common problem is multiple records belonging to one single customer. As data in the warehouse comes from different sources like offline stores, web properties, mobile applications, guest checkouts, CRM systems like Salesforce and customer service, the identity of the customer itself becomes questionable...The source systems do not share common identifiers for the customers and hence it is extremely difficult to spot the duplicates.

In fact, this problem is manifests itself not just to customer tables. The same issue is also visible in Supplier tables, locations, and other non-transactional data - all the dimensions in traditional warehouse language.

This becomes detrimental for our business decisions because without clean definition of entities, our metrics become unreliable and inaccurate. Imagine even simple analytics like customer lifetime value. If one customer is actually represented by 3 or 5 records, can we really trust the metric? Without a trustworthy customer identifier, it is impossible to properly segment, analyze and report from the warehouse. with the rising operationalization of data in the warehouse and the growth of reverse ETL, this unreliable dimensional data will get fed into our operational systems and affect our day to day work.

**If we can not even identify our customers accurately after all the investment in the warehouse, is it really worth it?**

When we start looking at this problem it feels like an easy thing to solve. It is hard to believe that we do not have phone numbers or email IDs to leverage consistently across the entire dataset. Analyzing our data reveals that in many cases, people use work, personal, school and other email IDs. So even though we can reconcile some records, our problem is far bigger. There are [different ways in which we enter names, addresses, and other details on web and print forms and hence it is always challenging to identify these duplicates.](https://medium.com/@sonalgoyal/deduplicating-records-is-machine-learning-the-answer-e9579cfda935)

Let us take a look at our customer table in Snowflake. The data in the table is loaded from [this CSV](https://github.com/zinggAI/zingg/blob/main/examples/febrl/test.csv).

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170b902a2245521be4c9_1*IJjjNWx9M5c_Sy6TDQoKWA.png" alt=""><figcaption></figcaption></figure>

It seems like with SSN column in the customer table, we may be able to identify duplicates but unfortunately it is not consistent and so we can not rely on it. The warehouse table does have identifier column, but that too has multiple records belonging to the same customer with different IDs.

For example, the following two records belong to customer Thomas George and have multiple variations across all attributes.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170bc9804b2468382d13_1*q9sXA8soGVbAjD3dsVKAhg.png" alt=""><figcaption></figcaption></figure>

Customer Jackson Eglinton seems to have even greater number of records.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170b12765114002c2059_1*LO0ns0K3-05zjVfSZLvP-g.png" alt=""><figcaption></figcaption></figure>

There are some approaches worth thinking about to solve this. How about building some similarity rules and use SQL or programming to build our identifiers and match these records? A closer look reveals that this will soon get complex catering to the variations above. It is tempting to think about using [Snowflake’s edit distance](https://docs.snowflake.com/en/sql-reference/functions/editdistance.html) function or fuzzywuzzy or some such library. Unfortunately, this problem is far more complex as we need to know which pairs to compare or find edit distance for otherwise we will end up with a cartesian join on multiple attributes(!!).

**Figuring out what to match and how to match are both unique challenges to deduplication.**

To understand this better, let us take a look at the number of comparisons we have to make as the size of our customer table increases 10 fold or 100 fold. The following table assumes that we are ONLY comparing on a single attribute. Yet, the number of comparisons explodes. Thus, we have to carefully plan the data matching while doing deduplication even with small datasets.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170bec616d851f7b8806_1*svNQBN2gsOmnsoLADtx0kA.jpeg" alt=""><figcaption></figcaption></figure>

Zingg, as a framework, solves the above challenges through machine learning. Let us see how we to use Zingg to resolve our customers and identify duplicates.

We can use the [docker image provided or install ](https://docs.zingg.ai/zingg/stepbystep/installation)binaries of Java, Apache Spark and Zingg. Do NOT be intimidated if you are not a Java programmer or a distributed programming geek writing Spark programs on petabyte size clusters. Zingg uses these technologies under the hood so for most [practical purposes](https://docs.zingg.ai/zingg/stepbystep/hardwaresizing), we can work off a single laptop or machine. Since Zingg trains on our data and does not transmit anything to an external party, we do not need to worry at all about security and privacy while running Zingg within our environment.

Let us configure Zingg to read and write from Snowflake. For this, the [Zingg config](https://github.com/zinggAI/zingg/blob/main/docs/configuration.md) is set with our Snowflake instance and table details. Here is the excerpt of the configuration for our input CUSTOMERS table from Snowflake.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170bcf512bfe86a0c27c_1*xhA58axrU3sgdV7M_MOZNg.png" alt=""><figcaption></figcaption></figure>

We want to have the proper dimensions in the UNIFIED\_CUSTOMERS table. Thus, we set that as the output for Zingg. This table does not exist in Snowflake yet, but Zingg will create it while writing its output, so we do not need to build it.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170c15bc731d9368972c_1*1Gj8iyyI9Bl-XsxDfe_mYw.png" alt=""><figcaption><p>Image by Author</p></figcaption></figure>

Let us now specify which attributes to use for matching, and [what kind](https://docs.zingg.ai/zingg/stepbystep/configuration#fielddefinition) of matching we want. As an example, the first name attribute is set for FUZZY match type.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170cec616d48257b8807_1*HpZ722XLMEvKxJyQMHNEdg.png" alt=""><figcaption></figcaption></figure>

We do not wish to use the SSN for our matching, so that we can see how well the matching performs, so we mark that field as DO\_NOT\_USE. The other parts of the configuration are fairly boilerplate; you can check the entire configuration [here](https://github.com/zinggAI/zingg/blob/main/examples/febrl/configSnow.json).

As an ML based system, Zingg learns what to match (scale) and how to match (similarity) based on training samples. Zingg's interactive learner picks out representative sample pairs which the user can mark as acceptable matches or non-matches. Let us now build the training samples from which Zingg will learn. We pass the configuration to Zingg and run it in the [**findTrainingData**](https://github.com/zinggAI/zingg/blob/main/docs/running.md#findtrainingdata---finding-pairs-of-records-which-could-be-similar-to-train-zingg) phase. This is a simple command line execution.

```json
zingg.sh --phase findTrainingData --conf examples/febrl/configSnow.json
```

Under the hood, Zingg runs over our data during **findTrainingData** to spot the right representative pairs to build the training data for deduplication. Zingg minimizes user effort, so it is very frugal about the records it shows for labeling. Once the job is finished, we will go to the next phase and [mark or label the pairs](https://docs.zingg.ai/zingg/stepbystep/createtrainingdata/label).

```json
zingg.sh --phase label --conf examples/febrl/configSnow.json
```

The above command invokes the interactive learner. The learner reads the work done by the **findTrainingData** phase and shows us record pairs to mark as matches or non matches. This will build the training data for Zingg ML models tailored to our data. This is how it looks like

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170c23ecc8af910fa58e_1*ruA37saR-MeWUGskEyenRg.png" alt=""><figcaption></figcaption></figure>

To build the trainign data, Zingg selects different kinds of pairs — absolute non-matches, sure matches as well as doubtful cases so that a robust training set can be built. These records are selected after a very rigorous scan of the input so that proper generalization can be made and every single variation across attributes **does not** have to be hand labelled by the user. The following is an example of Zingg output for our data.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170ca04f32f030d9ddef_1*p6MEe8fLMEIE-wJ6A3uSeQ.png" alt=""><figcaption><p>Image by Author</p></figcaption></figure>

The above cycle of running **findTrainingData** and **label** is repeated a few times till we have marked 30–50 matching pairs. This should be good enough to train Zingg to run on millions of records with reasonable accuracy. As the Zingg learner automatically selects the representatives and generalizes through that, we do not have to worry about each and every kind of variation in the system.

If you are unsure when to stop, you can always halt the learner, run Zingg and check its output. If not satisfied, come back and train a bit more!

In our simplistic case of only 65 examples, one round of **findTrainingData** and **label** is enough and so we can stop here. With the training data in place, we will now build the Zingg machine learning models by invoking its **train** phase. Internally, Zingg does hyperparameter search, feature weighing, threshold selection and other work to build a balanced model — one that does not leave out matches (recall) AND one that does not predict wrong matches (precision).

```json
zingg.sh --phase train --conf examples/febrl/configSnow.json
```

The above will build and save the models which are applied to this and any other new data to predict the matching duplicates. We do not need to retrain unless there is a change to the attributes to be matched.

Let us now run the models on our data to predict which records are indeed matches — or duplicates to each other.

```json
zingg.sh --phase match --conf examples/febrl/configSnow.json
```

When the Zingg match phase has run, we check our Snowflake instance and see that a new table with the following columns has been created.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170cea0315c076630c1a_1*ZyCOD7NrgwEA_5TR4O8HOw.png" alt=""><figcaption><p>Image by Author</p></figcaption></figure>

Along with the columns from the source table, Zingg adds 3 columns to each row of the output.

* The Z\_CLUSTER column is the customer ID Zingg gives — matching or duplicate records get the same cluster identifier. This helps to group the matching records together.
* The Z\_MINSCORE column is an indicator for the least that record matched to any other record in the cluster
* The Z\_MAXSCORE is an indicator for the most that record matched to another record in the cluster.

To understand how well this works, let us look at the records for customer Thomas George in the output. Both the records get the same z\_cluster. No other record gets the same ID. The scores are pretty good too, which means we can be confident of this match.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/6232170cdea6de41d3ecb468_1*XFOl9c3nCtHfD6nFWiAeAg.png" alt=""><figcaption></figcaption></figure>

What happened to customer Jackson Eglinton? Here is what the output looks like for this customer.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/623215a18e644774bb808e43_0*cBrSvKDAqbOdyOup.png" alt=""><figcaption></figcaption></figure>

As we can see above, the 5 records get an identifier distinct from the other records in the table. A closer look at the scores shows that the minimum score of two records is close to 0.69, which means that the confidence of these record belonging to the cluster is low. This is appropriate, as in one case, the street and address attributes are swapped. In the other, the last name is different from other records in the cluster.

We can decide how to use the scores provided by Zingg as per our business requirements. We could choose a cutoff on z\_minScore or z\_maxScore or take an average so as to be confident of the matches. We can send the records that fall below our cutoff to another workflow — likely human review.

In the most likely scenario, the output of Zingg is used further along in the data pipeline as the definitive source of entity data. Zingg output either gets picked up for transformations by [dbt](https://www.getdbt.com/) and used thereof, or Zingg output is streamed to the lakehouse and utilized for data science.

We can confidently use the resolved entities downstream as we are sure that we have taken care of the duplicates and our dimensions are reliable!
