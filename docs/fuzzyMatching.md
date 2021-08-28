

**Introduction**
----------------

Fuzzy Matching is a technique to identify data records in a single data source or across multiple data sources that refer to the same real-world entity and to link the records together. In fuzzy matching, the strings that are nearly identical, but maybe not exactly the same, are matched without having a unique identifier.

Real-world data is far from perfect. Organizations often struggle with a plethora of customer data entered multiple times in unique sources by various people in their own ways. 

Here are two variant customer specifications.

| First Name | Middle Name | Last Name | Phone | Address 1 | Address 2 | City | State | Country |
| ------------- |:-------------:| -----:| ------------- |:-------------:| -----:|-----:|-----:|-----:|
| Mr. Adam | R. | Smith | 1-777-888-9999 | 112 Bell Street | Near Church Tower | Oakland | California | US | 
| Dr. Raksha | | Ranganathan |+91-8383838383| 21, Brigade Road| Opposite McD | Bengaluru | Karnataka | India |

|**Name**|**Age**|**Address**|**State**|**Phone**|
--------|------|--------|--------|--------|
|Smith, Adam | 33 | 112, Bell Street, Near Church Tower, Oakland | CA | +1-777-888-9999|
| PhD. Raksha Rangnathan | 40 | 21, Brigade Road, Opp. McDonalds, Bangalore | Karnataka, India | 918383838383

In the above records, names are either divided into first, middle, last name, initials, or are combined. Names are sometimes clubbed with salutations, titles are different, middle names are missing, phone numbers are written differently, addresses are split and combined, the old and the new name of a city have been recorded for the same address, abbreviations are used, punctuations vary, and some fields are omitted.

As you read these records, you would have understood that both the data sources describe the two same people. But would a computer — which only understands the equality or the lack of it — mark these data values to be the same?

For understanding the data, computers need to identify records that refer to the same real-world entity\*. This process to recognize who is who — irrespective of the myriad representations — is Fuzzy Matching or Record Matching. Fuzzy matching is also known as entity resolution, merge purge and data matching. When used in the context of a person, fuzzy matching is referred to as identity resolution. When a single source of data is involved and the purpose is to remove the duplicate entries, fuzzy data matching is termed as record deduplication or deduplication. 

A corporate service provider can use fuzzy matching to resolve organization names despite the different representations, misspellings, abbreviations, and typographical errors. 
| | |
--- | --- |
Susheel Enterprises | Sushil Enterprises
Joylukkas | Joylukkas
McDonald’s | McD
Walls Limited Liability Partnership | Walls LLP
Google India Pvt Ltd | Google India

Udhyam Foundation

Udhyam Inc.

An insurance company needs to reconcile spelling variations, short forms, synonyms, and different word orders to correlate its data. 

Prozac

Prozec

Schizophrenia

Schizo

only with prescription

prescription mandatory

Senior Citizen Health Insurance Plans

Health Insurance Plans for Senior Citizens

alzheimer patient groups

alzheimer patient communities

Finance and banking firms require to match different numbers and percentage representations.

27 to 35

27 – 35

95 percent

95%

$40-$78

$40 to 78$

Income Tax Return 2018

ITR 2018

Leading and trailing white spaces, hyphens, case differences, and dots can make any two addresses look different when they are not.

[\[email protected\]](/cdn-cgi/l/email-protection)

[\[email protected\]](/cdn-cgi/l/email-protection)

near     Golden temple

near Golden Temple.

Times-Square

Times Square

Additionally, an international client database may contain data in different languages. All in all, because of the non-standardization of the data, each of the attributes can vary in numerous ways in different records of the same entity, and databases are filled with these variations.

Companies use fuzzy record matching to connect disparate data sources to clean data, see non-obvious relationships across multiple data silos, and get a unified view of data. In the process, they may build a master data management system (MDM) by combining the data from the various sources and thus develop a single source of truth.

**Fuzzy Matching For Enterprises**
----------------------------------

We all know the 1-10-100 rule of data quality, and [Gartner puts](https://www.data.com/export/sites/data/common/assets/pdf/DS_Gartner.pdf) the rule like this: It takes $1 to verify a record as it’s entered, $10 to cleanse and dedupe it and $100 if nothing is done, as the ramifications of the mistakes are felt over and over again. 

Fuzzy matching is crucial as it matches non-identical records despite all the data inconsistencies without the constant need for formulating rules. By joining databases using fuzzy matching, we can clean the data and analyze the information. We can draw patterns over the unified data, derive conclusions, and see the bigger picture. 

This cleaner, leaner, and more-connected data is also crucial to operate efficiently and prevent fraud. And to top it all, we get a single customer view at lesser operating costs.

Let us look at some of the various scenarios where fuzzy data matching enables organizations to provide personalized customer experiences, drive marketing efforts efficiently, increase sales, and stay compliant.

### **Industry Use Cases** 

#### **Life Sciences and Healthcare**

[Financial times referred](https://www.ft.com/content/21a6e7d8-b479-11e3-a09a-00144feabdc0#axzz32CdBNqMs) to a McKinsey Global Institute report that said the US healthcare system could save $300 billion a year — $1,000 per American — through better integration and analysis of the data produced by everything from clinical trials to health insurance transactions to smart running shoes.

The life sciences and healthcare industry need the most current and connected data about health care organizations and providers, patients and their interactions and treatments, hospitals, physicians, labs, and products to not only have a clear understanding about these inter-dependent stakeholders but also to provide unified and personalized information to all of them at the right time on their preferred platforms. 

 For instance, a pharmaceutical corporation could use fuzzy matching to aggregate data about drugs dispatched from all its locations to understand how much of each medicine was prescribed and sold throughout the year. As the data is entered in different systems, the drug names would have differences, and fuzzy matching will be able to reconcile the names. The company can also use a single view of all its purchasers(hospitals and clinics) to see who would be needing new supplies and who owes them money.

Pharmaceutical companies also have to consolidate biological and medicinal data from the studies done by scientists in global labs to conduct their research and make new drugs. Fuzzy matching comes in handy here to connect and match compounds and reactions across thousands of spreadsheets with millions of rows, all with various column names, formats, and inconsistencies(such as insulin glargine and insulin-glargine). 

On top of general research and analysis, health care systems require a connected and comprehensive picture(an MDM platform perhaps)of every patient’s medical journey. They use this real-time 360° view of their patients to provide personalized intelligent medical recommendations, improve the patient experience to establish a strong customer relationship, and create better products. Clean and matched patient data can also be used to create health indicators. Fuzzy matching enables the consolidation of a patient’s data from various data sources, matching data from hospitals and private clinics, insurance providers and claims, personal profiles, and from the internet and social platforms to create a unique profile of each patient. 

For instance, the healthcare industry can analyze the compiled data to know if the patient had any allergic reactions in the past or the exact ibuprofen mix that suited the patient the last time. Using the data, the health care providers can figure out statistics like the average heartbeat rate of the males in India in the 55-60 age group during the regular cardio health checkup and draw conclusions.  

Pharmaceutical firms also use connected data to conduct market research and do competition and pricing analysis before they launch any new product. If they don’t get a unified view of the market, product launches would either fail or get delayed.  

The life sciences and health industry have complex compliance requirements. If a company fails to be compliant with any one of the various health accountability acts or consumer data privacy regulations, it may be penalized heavily or shut down. 

Previously, companies spent an unprecedented number of days aggregating reports to present to regulatory entities. But agile record matching connects and reconciles various voluminous influxes of life science data, such as from hospitals, internal records, third-party sources, offline mediums, and social media, to keep the regulatory compliances forever ready. 

#### **Insurance**

Insurance companies often struggle with fragmented data silos because different policies(car, health, home, etc) maintain their own record of the individual. Thus they cannot reconcile a customer’s demographics, preferences, past insurance policies, and credit ratings. 

In the absence of an integrated view over a customer, the insurance industry lacks an understanding of her ideal coverage, risk exposure, and other analytics. Personalized marketing and opportunities for cross-selling are missed, too. 

For instance, if the insurance provider doesn’t know that an interested customer applying for health insurance is the husband of a present insurance holder with the company, they won’t be able to offer a combined scheme and will compromise on the customer service and/or the best returns on those clients. The insurance takers might not know their options but the company should be able to tell them. In this situation, integrating and matching all internal data with possible leads can give a unified view of a lot of customers in a specific geography. 

Fuzzy matching is pivotal for customer-centricity in the insurance industry. If the insurance executives have a unified view of their potential leads, they can provide them highly personalized insurance plans and will thus beat the competition easily. 

Fraud detection is one of the biggest use cases of fuzzy record matching in the insurance industry. The insurers might incur huge losses if they fail to identify people who have committed insurance frauds in the past and have now applied for heavy insurance schemes with slight changes in their details.

Claims management, policy management, and regulatory compliance also depend on the consolidation of policy and regulatory data.

#### **Manufacturing**

With fuzzy data matching, manufacturers can analyze their expenditure over large geographies and optimize suppliers and raw material procurement. This analysis will allow the companies to reduce costs by closing gaps in their spending and onboarding the cheapest suppliers and purchasing from them. An analysis of inventory can help remove blind spots in inventory and distribution.

For instance, if a manufacturing unit already has a past inventory of 100,000 units, then they do not need to produce more units until new orders are received. But if their parts supplier chain from China is broken due to political unrest, then they need to quickly look into their consolidated supplier databases to identify the next cheapest supplier and place an order immediately.

A unified view over the customer will allow the manufacturing firms to provide consistent customer experience and cross-sell and up-sell their products. Cross-connecting data from research, past design projects, competition, manufacturing quality, failure, and maintenance reports can also pave the way for superior quality. 

Fuzzy matching also allows the merging of product catalogs and price lists. Multiple entries and, hence, different pricing for the same product can cause chaos in any company. Reconciliation of pricing is crucial to show consistent and correct product information to every buyer. 

#### **Financial Services**

Due to their high-risk nature, financial services could lose a lot of customers if they experience poor service or a careless attitude from the relationship managers. Disjoint and unclean data could also expose the business to unwarranted risks.

A single source of customer data that enables unified portfolio preparation and auditing is pivotal to the smooth operations and success of every financial firm. So for instance, the variant client data entered by different relationship managers for the same client (typos in names, phone numbers, address, etc) has to be aggregated and cleaned using fuzzy matching to get a holistic view of customers. 

Only a unified view of a customer can enable the finance companies to provide good customer service, which would lead to higher customer retention and thus a higher portfolio value. If you have a trustworthy relationship with an investment bank, you would want them to invest your yearly savings on an ongoing basis rather than finding a new financial service. 

Connected customer data is also vital for establishing personal credit ratings and financial stability. These ratings could be used to make informed decisions on issuing loans, handing over credits, and detecting fraudulent activities. Fuzzy data matching is employed to connect the KYC data with customer investments. The KYC information along with the credit ratings can prove to be very helpful in detecting and avoiding debt frauds. 

[A McKinsey article on anti-money laundering states](https://www.mckinsey.com/business-functions/risk/our-insights/the-new-frontier-in-anti-money-laundering) , “In the United States, anti-money laundering (AML) compliance staff have increased up to tenfold at major banks over the past five years or so.” The article emphasizes that fuzzy logic(one of the many tools) can allow banks to validate more customer identities and map how specific customers are connected to higher risk individuals and legal entities. 

#### **Energy, Oil, and Mining**

Global energy, mining, and oil companies need to clean, categorize, unify, and understand disparate data from various sources, geographies, and engineering perspectives to reduce operational costs and smoothen their operations. These firms need to categorize parts and materials, understand where they are spending their money, and discover better and cheaper machinery suppliers. 

Energy organizations require unified data to not only reduce costs but to stay compliant and to report efficiently and correctly. Fuzzy matching helps these firms consolidate their data and give them a robust data management platform that can serve all the above functions. 

As the list of use cases gets longer, the realization that fuzzy record matching has endless benefits dawns gloriously.

Our failure to understand two records as one entity can corrupt the master data. Relationships and patterns would be missed. Aggregations and calculations won’t make any sense. Crucial information will lie unused. Critical decisions would go haywire. Every aspect of lives, countries, organizations, and communities might be threatened.

In all scenarios, we can understand the situation by linking records and can track anything from a person to a disease to a new scheme. There is exponential value in integrating the data silos across various industries.

### **Functional Use Cases**

#### **Sales And Marketing**

Recommendations to a customer and an effective marketing scheme cannot be predicted based on distinct data silos.

If the buyer data is not viewed in consolidation, most marketing efforts would be wasted as the outreach programs and customer interactions won’t be personalized and shoppers just wouldn’t buy. In the absence of a useful customer experience, the company would be shunned for poor customer service and thus would lose to the competitors. 

Often, a customer is listed multiple times with various purchases across two different store databases due to different spellings of their name or a typing error in the phone number. A duplicate email from the company would only be a missed sale opportunity, or worse could cause the customer to mark the company’s marketing emails as spam.

Customer deduplication in CRMs is another area where fuzzy matching is mighty effective. Customer Relationship Management systems often have multiple entries of the same customer, due to different entries by separate salespersons, or prospect lead list acquisition from multiple channels. During outreach, it is wasteful to contact the same lead multiple times, as this leads to poor brand experience and discomfort to the recipient. Valuable sales cycles are wasted too, as more than one salesperson is reaching out to the same individual. Customer deduplication through fuzzy matching really helps in sales and marketing operational excellence.

In another scenario, an automobile provider needs to reconcile all the leads through websites and mobile applications to eliminate the duplicate ones and then focus on the compact list. 

Or for instance, if a customer returns a product but buys a bigger size the next day, the company sends the below message, 

Option 1: You returned the product and bought a bigger one. Rest assured we will send it to you as fast as we can.

Option 2: You have just returned a product. Go here to shop again. 

Message 2 would feel redundant to the customer. In this case, the return details had to be reconciled with the customer profile first. 

In all scenarios, the customer data has to be unified through fuzzy matching before engaging in any marketing.

By fuzzy matching and unifying customer data from different internal and external data silos such as webstores, mobile applications, offline stores, loyalty programs, and personal profiles, businesses can create a 360-degree view of the customer and take their marketing efforts to the next level by providing a unique personalized experience to each customer.

A 360 view of the customer is also a very effective tool for understanding opportunities to cross-sell and upsell products and services. Increasing customer lifetime value at a much lower customer acquisition cost would be possible now. 

If a customer checks out an overcoat at a store, this offline information could be used to send her a discount code for winter wear. Regardless of the touchpoint, it is the same brand for the customer.

This customer unification can be achieved by updating and matching real-time customer interaction data from all sources on top of the customer’s unique journey.

All data-driven and customer-facing businesses such as hospitality, health providers, insurance companies, retail brands, automobile firms would benefit highly from this personalized marketing approach. [As per Mckinsey](https://www.mckinsey.com/business-functions/marketing-and-sales/our-insights/what-matters-in-customer-experience-cx-transformations), a fundamental change of mindset focusing on the customer, along with operational and IT improvements, can generate a 20 to 30 percent uplift in customer satisfaction. 

By connecting various marketing initiatives and the resulting sales, the firms can understand the channels and forms of advertisement that bring the highest ROI. Buyer behavior can also be used to modify the marketing campaigns and budgets. 

#### **Supply Chain**

Accurate and connected supplier data is essential for maintaining an agile supply chain. In the absence of supplier unification, a supply chain will be less competitive and a lot of money would be spent on coping with supply and inventory problems. 

Matching suppliers and building a unified supplier profile isn’t that easy. Apart from volume complexities, variations in descriptions could be immense, and names would be misspelled or represented differently. There would be new purchases with different descriptions every quarter.

Fuzzy matching is used to maintain a robust supply chain by consolidating supplier data across data silos spread over various business units, regions, geographies, and parts and materials categories. 

Supplier matching would also enable companies to understand pricing across divisions, find the best supplier for a given product easily, negotiate prices, onboard new suppliers faster, get rid of duplicate suppliers, and manage risk associated with vendor geographies. 

Let us say a CPG company wants to compare two vendors of similar products to figure out who sells the majority of the products at a cheaper price. Both vendors would express product names differently. While one vendor may call the product tissue paper, another may call it paper handkerchief or tissue or sanitary paper.

As product titles will not match exactly, fuzzy matching is used to reconcile the products, compare their prices, and decide which vendor is selling for cheaper. Fuzzy matching will also block fraudulent sellers who enroll again with slight variations in their details.

**Challenges**
--------------

Comparing big data records filled with non-standard and inconsistent data from diverse data sources that do not provide any unique identifiers is a complex problem.

Computers can assess equality and do mathematical comparisons but cannot understand fuzzy matching on their own. Though humans might call the two people represented by the records Mrs. Kamala Harris and Harris Kamala the same, a programmed system wouldn’t know what to do.

Same data can be represented in different formats by various users, technological systems, geographies, regions, years, and organizations. Records vary in terms of typos, salutations, spaces, abbreviations, languages, spellings, completion, omissions, phonetics, order, suffixes, prefixes, etc(as shown above). Errors in data entry can lead to multiple records of an entity even within a single source. 

Given the representation variations, an unprecedented number of permutations and combinations of the same data exist. Name matching, Address matching, Location matching, supplier matching, product matching all have distinctive matching criteria depending on the types of the respective entities.

A Data Management system should understand all criteria and match various records to represent one customer or product or vendor or supplier. Name, address, location, part, product, phone number, date matching are critical to reconciling data from different silos together.

And we are talking about a massive amount of data. The sheer volume of data coupled with the abundant formats makes matching a formidable task. Fine-tuning any algorithm to fit every entity type, record type, and value would be a herculean job.

Legacy data management methods can’t handle this disparate data in practical time boundaries. 

To understand the complexity of a data matching solution, let us analyze rule-based matching more closely. To match any two records of the same entity, various string-based comparison rules will be defined. Each record would run with every other record on all these rules to deduce if the two are identical.

Here are only some of the limitations of this rule-based matching.

#### **Completeness**

Given the unfathomable number of variations for a single data value of an attribute, defining matching rules for a field isn’t easy and requires a deep understanding of data profiles. Can you possibly cover all spellings of Alzheimer’s or all different descriptions of thin-long fibers ever written? For example, until we wrote this article and researched fuzzy matching for the automotive industry, we didn’t know that chassis means the vehicle frame. Forget the synonym equality rule then.

Setting matching rules is a cumbersome, expensive, and lengthy process.

#### **Complexity**

Apart from defining rules, combining matching rules of different attributes of a record would be even more challenging. Should the records be the same if street 1 and street 2 seem similar but the phone numbers don’t match or are disjoint? 

#### **Maintenance**

An unmanageable number of rules would have to be defined to cover the full variety of data representations. And still rules will be missed, and corner cases would get away uncaught. 

Updating rules and adapting them to changing data would be tricky, too. As the number of data sources increases and the format and data types grow along with the volume of data, defining the rules becomes an increasingly complicated endeavor. 

And no matter how many rules you create, a fixed set of rules can’t handle all the unprecedented data varieties making rule-based matching very hard and expensive to implement and maintain. 

#### **Scale**

As most entities lack a unique key that can be compared, every record is matched with every other record in a brute force manner. For n number of records, there exists n\*(n-1)/2 unique pairs. To establish equality, every rule will run on every pair.  

**Number of records**

**Unique Pairs**

**No. of comparisons(assuming just one rule)**

10,000

10,000\*(9999)/2

~  50 million

1,00,000

100,000(99999)/2

~  5 billion

 As the number of records increases 10 times, the number of comparisons increases 100 times. So as the number of records grows, the number of comparisons goes exponential. Hence the time to compare goes exponential even if we assume the fastest comparison technique. 

Rule-based data matching is a computationally challenging and unscalable solution. The time to run those rules would also go exponential in no time.

#### **Precision**

Even after deducing the matching logic, what would be the optimum matching threshold to differentiate matches from non-matches?  Due to the nature of rules, many false positives and false negatives would definitely find their way into the results. 

#### **Recall**

You make one mistake in the matching criteria and the whole data matching would be uprooted. Many true positives can be missed. Understanding how the attributes and records can be said to be the same is a game-changer.

#### **Other challenges**

Apart from the complications of rule-based integration solutions, here are many more complexities applicable to all data matching solutions that shows why matching is one of the toughest problems to solve, 

##### **Variant Data Stores**

Data will be saved in various data stores such as relational databases, NoSQL datastore, cloud storage, and local file systems. The different databases imply that data would come in disparate formats — text, proprietary, JSON, XML, CSV, Parquet, Avro, and others.

##### **Schema Variations in Data Sources**

Entity representation often varies across systems. The number of attributes could be different and their representations could vary largely. The address attribute in one data system could be split into Address 1 and Address 2 in another. Or the columns may be named differently for even the same attribute. For example, instead of Address 1 and Address 2, Street 1, and Street 2 could be written. Price could be written as MRP or Sale price. Aligning the various schemas is a challenging task. 

\*Entity Definition: An entity, as we know, is a unique thing — a person, a business, a product, a supplier, a drug, an organization. Every entity comes with its describing attributes such as name, address, date, color, shape, price, age, website, brand, model, capacity et cetera. 

Fuzzy matching is a great technique to match non-identical data but it comes with its challenges. At Nube, we solve the problem of fuzzy matching by employing artificial intelligence. Do reach out to us if you need help in reconciling your organization’s data. 

