---
description: >-
  Complete guide for running Zingg Enterprise natively on Snowflake using Snowpark. Covers installation, configuration, SQL/Python APIs, incremental matching, and B2B customer/vendor entity resolution examples.
parent: Installing Zingg Enterprise for Snowflake
nav_order: 1
---

# Zingg Enterprise on Snowflake

{% hint style="success" icon="right-long" %}
**Resolve customer and vendor master data natively inside Snowflake — no EC2, no Docker, no Lambda, no external Spark clusters.** All compute runs on your Snowflake warehouse, so your data never leaves.
{% endhint %}

{% embed url="https://www.youtube.com/watch?v=zOabyZxN9b0" %}
Watch Zingg's entity resolution demo to see the end-to-end pipeline in action.
{% endembed %}

* **Zero infrastructure** — runs on your existing Snowflake warehouse
* **Zero data egress** — customer/vendor data never leaves Snowflake
* **Persistent `ZINGG_ID`** — globally unique entity IDs that survive incremental runs
* **Incremental matching** — add new records without full reprocessing
* **Snowsight labeling UI** — Match / Non-Match / Can't Say for data stewards

---

## What You'll Build

By the end of this guide you'll have a working **B2B Entity Resolution pipeline** that resolves:

* **B2B Customer Accounts** — parent companies + subsidiaries ("Alphabet Inc." + "Alphabet LLC" + "Google LLC" → one entity)
* **Enterprise Vendor Profiles** — suppliers across SAP, Oracle, Coupa, Ariba into a unified golden record

The full flow: **Find Training Data → Label → Train → Match → Incremental**.

---

## Architecture At A Glance

```
         ┌─────────────────────────────────────────────────────────┐
         │                     SNOWFLAKE ACCOUNT                     │
         │                                                           │
         │   WAREHOUSE (compute)    DATABASE (tables)    STAGE       │
         │   ZINGG_WH (auto-scale)  ZINGG_ENTITY_        @ZINGG_     │
         │                          RESOLUTION           MODELS     │
         │         │                     │                │          │
         │         ▼                     ▼                ▼          │
         │   ┌──────────────────── SNOWPARK ────────────────────┐    │
         │   │  findTrainingData  →  label  →  train  →  match  │    │
         │   └──────────────────────────────────────────────────┘    │
         └─────────────────────────────────────────────────────────┘
```

{% hint style="info" %}
**No infrastructure to manage.** Zingg Enterprise runs inside Snowpark Container Services, so you configure a warehouse size and go. There's nothing to patch, version, or scale yourself.
{% endhint %}

> **Product screenshot placeholder:** Zingg Enterprise pipeline overview (Snowsight). Add the screenshot here.

---

## 1. Prerequisites

Before you begin, make sure you have:

| Requirement | Details |
|-------------|---------|
| **Snowflake Edition** | Enterprise Edition or higher (Snowpark required) |
| **Warehouse** | X-Small to 4X-Large; auto-scaling recommended |
| **Database/Schema** | Dedicated database for Zingg models (e.g., `ZINGG_ENTITY_RESOLUTION`) |
| **Stage** | Internal named stage for model artifacts (e.g., `@ZINGG_MODELS`) |
| **License** | Zingg Enterprise license file (`zingg.license`) |
| **Python** | 3.8+ with `snowflake-snowpark-python` ≥ 1.12 |

{% hint style="warning" icon="warning" %}
Some organizations can't reach `pypi.org` directly. If you're air-gapped or restricted, use your internal PyPI mirror during the manual install (see Step 3 below).
{% endhint %}

---

## 2. Install Zingg Enterprise

Two ways to install — pick the one that fits your environment.

{% stepper %}
{% step %}
### Choose an installation method

* **Snowflake Marketplace (Recommended)** — ~5 minutes, fully automated setup of warehouse, stage, and roles. Best for most users.
* **Manual Package Install** — ~15 minutes. For air-gapped, restricted, or highly customized environments.
{% endstep %}

{% step %}
### Method A: Snowflake Marketplace

1. Log into **Snowsight** → **Marketplace** → search **"Zingg Enterprise"**
2. Click **Get** → select your target database/schema → **Install**
3. Run the setup wizard (configures warehouse, stage, and roles automatically)
4. Verify with: `CALL ZINGG.SYSTEM$VERIFY_SETUP();`

> **Product screenshot placeholder:** the Snowsight Marketplace listing for **Zingg Enterprise** with the Get / Install flow. Add the screenshot here.
{% endstep %}

{% step %}
### Method B: Manual Package Install

```bash
# 1. Download the Enterprise package (provided by Zingg after license purchase)
# Package name: zingg-enterprise-snowflake-<version>.tar.gz

# 2. Extract
gzip -d zingg-enterprise-snowflake-<version>.tar.gz
tar xvf zingg-enterprise-snowflake-<version>.tar
cd zingg-enterprise-snowflake-<version>

# 3. Install Python dependencies
pip install snowflake-snowpark-python==1.12.*
pip install -r requirements.txt   # includes the zingg-enterprise-snowflake wheel

# 4. Place your license file
cp /path/to/zingg.license .
```
{% endstep %}
{% endstepper %}

{% hint style="success" icon="right-long" %}
**Done with install?** Skip ahead to **Step 4 – Configure & Run** for the pipeline walkthrough, or follow the full configuration below.
{% endhint %}

---

## 3. Configure Snowflake Objects & Connection

### 3.1 Create required Snowflake objects

Run this **once per environment** (as DBA or ACCOUNTADMIN):

```sql
-- 1. Dedicated warehouse with auto-scaling
CREATE WAREHOUSE IF NOT EXISTS ZINGG_WH
  WAREHOUSE_SIZE = 'MEDIUM'
  AUTO_SUSPEND = 60
  AUTO_RESUME = TRUE
  MIN_CLUSTER_COUNT = 1
  MAX_CLUSTER_COUNT = 4
  SCALING_POLICY = 'ECONOMY';

-- 2. Database & schema for Zingg models
CREATE DATABASE IF NOT EXISTS ZINGG_ENTITY_RESOLUTION;
CREATE SCHEMA IF NOT EXISTS ZINGG_ENTITY_RESOLUTION.PUBLIC;

-- 3. Named stage for model artifacts (encrypted, versioned)
CREATE STAGE IF NOT EXISTS ZINGG_ENTITY_RESOLUTION.PUBLIC.ZINGG_MODELS
  ENCRYPTION = (TYPE = 'SNOWFLAKE_SSE')
  COMMENT = 'Zingg Enterprise model artifacts — blocking models, similarity models, training data';

-- 4. Service role with minimal grants
CREATE ROLE IF NOT EXISTS ZINGG_ER_ROLE;
GRANT USAGE ON WAREHOUSE ZINGG_WH TO ROLE ZINGG_ER_ROLE;
GRANT USAGE ON DATABASE ZINGG_ENTITY_RESOLUTION TO ROLE ZINGG_ER_ROLE;
GRANT USAGE ON SCHEMA ZINGG_ENTITY_RESOLUTION.PUBLIC TO ROLE ZINGG_ER_ROLE;
GRANT CREATE TABLE, CREATE VIEW, CREATE STAGE ON SCHEMA ZINGG_ENTITY_RESOLUTION.PUBLIC TO ROLE ZINGG_ER_ROLE;
GRANT READ, WRITE ON STAGE ZINGG_ENTITY_RESOLUTION.PUBLIC.ZINGG_MODELS TO ROLE ZINGG_ER_ROLE;

-- 5. Grant to the service user
GRANT ROLE ZINGG_ER_ROLE TO USER ZINGG_SERVICE_USER;
ALTER USER ZINGG_SERVICE_USER SET DEFAULT_ROLE = ZINGG_ER_ROLE;
```

### 3.2 Create a Snowpark session

```python
from snowflake.snowpark import Session

# Use key-pair auth for production (see Security below)
connection_parameters = {
    "account": "xy12345.us-east-1",            # Your Snowflake account identifier
    "user": "ZINGG_SERVICE_USER",              # Dedicated service user
    "private_key_file": "/path/to/rsa_key.p8", # Key-pair auth (recommended)
    # "password": "********",                  # Or password auth for dev only
    "role": "ZINGG_ER_ROLE",                   # Custom role with required grants
    "warehouse": "ZINGG_WH",                   # Dedicated auto-scale warehouse
    "database": "ZINGG_ENTITY_RESOLUTION",     # Target database
    "schema": "PUBLIC"                         # Target schema
}

session = Session.builder.configs(connection_parameters).create()
session.sql("USE WAREHOUSE ZINGG_WH").collect()
```

---

## 4. Configure & Run the Pipeline

The steps below use a **B2B Customer Accounts** example end-to-end. A **Vendor Profiles** example is included in the expandable section at the end.

### Step 4.1 Define your field configuration

```python
from zingg_enterprise import ZinggEnterprise

# ─── B2B CUSTOMER ACCOUNTS EXAMPLE ───
# Resolves: "Alphabet Inc." + "Alphabet LLC" + "Google LLC" → same entity
customer_config = {
    "data": [{
        "name": "customer_input",
        "table": "RAW_CUSTOMERS",            # Source table in ZINGG_ENTITY_RESOLUTION.PUBLIC
        "database": "ZINGG_ENTITY_RESOLUTION",
        "schema": "PUBLIC"
    }],
    "output": [{
        "name": "customer_resolved",
        "table": "RESOLVED_CUSTOMERS",       # Output table (created by Zingg)
        "database": "ZINGG_ENTITY_RESOLUTION",
        "schema": "PUBLIC"
    }],
    "modelId": "b2b_customer_v1",
    "zinggDir": "@ZINGG_MODELS/b2b_customer_v1",  # Stage path for model artifacts
    "numPartitions": 8,                      # Tune: 2–4 × cluster count
    "labelDataSampleSize": 0.3,              # 30% sample for training pair generation
    "fieldDefinition": [
        # Company name — fuzzy with MAPPING for known aliases
        {
            "fieldName": "company_name",
            "matchType": "MAPPING_company_aliases,FUZZY",
            "fields": "company_name",
            "dataType": "string"
        },
        # Legal entity suffix — exact on normalized suffix
        {
            "fieldName": "legal_suffix",
            "matchType": "EXACT",
            "fields": "legal_suffix",
            "dataType": "string"
        },
        # Address — fuzzy, optimized for street variations
        {
            "fieldName": "address",
            "matchType": "FUZZY_OPTIMISED",
            "fields": "address_line1,address_line2,city,state,postal_code,country",
            "dataType": "string"
        },
        # Website domain — exact match (strong B2B signal)
        {
            "fieldName": "website_domain",
            "matchType": "EXACT",
            "fields": "website_domain",
            "dataType": "string"
        },
        # Parent company ID — deterministic when available
        {
            "fieldName": "parent_company_id",
            "matchType": "EXACT",
            "fields": "parent_company_id",
            "dataType": "string"
        },
        # D-U-N-S number — exact, primary key for vendors
        {
            "fieldName": "duns_number",
            "matchType": "EXACT",
            "fields": "duns_number",
            "dataType": "string"
        },
        # Record ID — excluded from matching, kept in output
        {
            "fieldName": "record_id",
            "matchType": "DONT_USE",
            "fields": "record_id",
            "dataType": "string"
        }
    ],
    # Deterministic rules for high-confidence auto-merge
    "deterministicMatching": [
        {"fields": ["duns_number"]},                    # Same D-U-N-S = same vendor
        {"fields": ["website_domain", "company_name"]}, # Same domain + name = same
        {"fields": ["parent_company_id", "legal_suffix"]}
    ],
    # Pass-through for inactive records
    "passthroughExpr": "status = 'INACTIVE' AND status IS NOT NULL"
}

zingg_customer = ZinggEnterprise(session, customer_config)
```

> **Tip:** When a field maps one-to-one to a source column, set `fields` to the same value as `fieldName`. When you want to combine multiple columns into a single derived field (like `address` above), list them comma-separated in `fields`.

<details>
<summary><strong>Enterprise Vendor Profiles example (SAP, Oracle, Coupa, Ariba)</strong></summary>

```python
# Resolves vendors across SAP, Oracle, Coupa, Ariba
vendor_config = {
    "data": [{
        "name": "vendor_input",
        "table": "RAW_VENDORS",
        "database": "ZINGG_ENTITY_RESOLUTION",
        "schema": "PUBLIC"
    }],
    "output": [{
        "name": "vendor_resolved",
        "table": "RESOLVED_VENDORS",
        "database": "ZINGG_ENTITY_RESOLUTION",
        "schema": "PUBLIC"
    }],
    "modelId": "enterprise_vendor_v1",
    "zinggDir": "@ZINGG_MODELS/enterprise_vendor_v1",
    "numPartitions": 8,
    "labelDataSampleSize": 0.25,
    "fieldDefinition": [
        {"fieldName": "vendor_name", "matchType": "MAPPING_vendor_aliases,FUZZY", "fields": "vendor_name", "dataType": "string"},
        {"fieldName": "tax_id", "matchType": "EXACT", "fields": "tax_id", "dataType": "string"},
        {"fieldName": "address", "matchType": "FUZZY_OPTIMISED", "fields": "address_line1,city,state,postal_code,country", "dataType": "string"},
        {"fieldName": "email_domain", "matchType": "EMAIL", "fields": "contact_email", "dataType": "string"},
        {"fieldName": "payment_terms", "matchType": "EXACT", "fields": "payment_terms", "dataType": "string"},
        {"fieldName": "vendor_id", "matchType": "DONT_USE", "fields": "vendor_id", "dataType": "string"}
    ],
    "deterministicMatching": [
        {"fields": ["tax_id"]},
        {"fields": ["email_domain", "vendor_name"]}
    ]
}
zingg_vendor = ZinggEnterprise(session, vendor_config)
```
</details>

<details>
<summary><strong>Mapping files for B2B aliases (MAPPING match type)</strong></summary>

Create JSON mapping files and upload them to `@ZINGG_MODELS/mappings/`. These map different name variants to a canonical value so they match even before similarity scoring runs.

**`company_aliases.json`** — parent/subsidiary relationships:
```json
[
  ["Alphabet Inc.", "Alphabet LLC", "Google LLC", "Google Inc.", "Google"],
  ["Microsoft Corporation", "Microsoft Corp", "MSFT", "Microsoft"],
  ["Amazon.com Inc.", "Amazon.com LLC", "Amazon", "AWS", "Amazon Web Services"],
  ["Meta Platforms Inc.", "Meta Platforms", "Facebook Inc.", "Facebook", "FB"],
  ["International Business Machines", "IBM Corp", "IBM", "International Business Machines Corp"]
]
```

**`vendor_aliases.json`** — common vendor name variations:
```json
[
  ["Dell Technologies", "Dell Inc.", "Dell", "Dell EMC"],
  ["Hewlett Packard Enterprise", "HPE", "HP Enterprise", "Hewlett Packard"],
  ["VMware Inc.", "VMware", "VMware LLC"],
  ["ServiceNow Inc.", "ServiceNow", "NOW"],
  ["Salesforce Inc.", "Salesforce", "SFDC", "Salesforce.com"]
]
```

Upload to stage:
```sql
PUT file:///local/path/company_aliases.json @ZINGG_MODELS/mappings/ AUTO_COMPRESS=FALSE;
PUT file:///local/path/vendor_aliases.json @ZINGG_MODELS/mappings/ AUTO_COMPRESS=FALSE;
```

Then reference the file by its base name (without `.json`) in your match type:
`"matchType": "MAPPING_company_aliases,FUZZY"`
</details>

### Step 4.2 Run the phases

{% stepper %}
{% step %}
### 1. Find Training Data

Generates candidate record pairs for labeling.

```python
zingg_customer.find_training_data()
# → candidate pairs written to @ZINGG_MODELS/b2b_customer_v1/trainingData/unmarked/
```
{% endstep %}

{% step %}
### 2. Label Pairs (Human Input)

The only step requiring a human. No ML knowledge needed — just your domain understanding.

**Option A — Snowsight Labeling UI (recommended for data stewards):**
```python
zingg_customer.label()   # opens interactive labeling in Snowsight
```
* See record pairs side-by-side with **Match / Non-Match / Can't Say** buttons
* Auto-saves labels to `@ZINGG_MODELS/b2b_customer_v1/trainingData/marked/`
* **Target: 40–60 Match + 40–60 Non-Match pairs** before training

> **Product screenshot placeholder:** the Snowsight labeling UI showing two candidate customer records with Match / Non-Match / Can't Say buttons. Add the screenshot here.

**Option B — Jupyter / Python worksheet (for ML engineers):**
```python
zingg_customer.label()   # returns an interactive widget in Jupyter
```
{% endstep %}

{% step %}
### 3. Generate Documentation (Audit Before Train)

Produces an HTML review report of all labeled pairs.

```python
zingg_customer.generate_docs()
# → @ZINGG_MODELS/b2b_customer_v1/docs/model.html + data.html
```

**Review checklist:**
- [ ] False positives < 2% (different entities labeled as Match)
- [ ] False negatives < 2% (same entities labeled as Non-Match)
- [ ] All field types represented in training pairs
- [ ] Edge cases covered: abbreviations, missing fields, transliterations
{% endstep %}

{% step %}
### 4. Train the Model

Builds the blocking + similarity models from your labels.

```python
zingg_customer.train()
# → model artifacts saved to @ZINGG_MODELS/b2b_customer_v1/model/
```
{% endstep %}

{% step %}
### 5. Match (Resolve Entities)

Runs full resolution on the input table.

```python
results = zingg_customer.match()
results.show()
```

The `RESOLVED_CUSTOMERS` output table contains:
* All your input columns
* **`ZINGG_ID`** — persistent entity GUID (e.g., `ZNGG_7f3a2b1c...`)
* **`Z_MINSCORE`** — weakest link confidence within the cluster
* **`Z_MAXSCORE`** — strongest link confidence within the cluster

> **Product screenshot placeholder:** the `RESOLVED_CUSTOMERS` match output in Snowsight showing the `ZINGG_ID`, `Z_MINSCORE`, and `Z_MAXSCORE` columns. Add the screenshot here.
{% endstep %}

{% step %}
### 6. Incremental Matching (Production)

Add new/modified records **without reprocessing everything**.

```python
incremental_config = {
    **customer_config,
    "incremental": True,
    "lastRunTimestamp": "2024-01-15 00:00:00"  # Process records modified since
}

zingg_incremental = ZinggEnterprise(session, incremental_config)
results = zingg_incremental.match()
# → merges new records into existing ZINGG_ID clusters
# → preserves human-approved decisions from the label phase
```
{% endstep %}
{% endstepper %}

{% hint style="info" %}
**Prefer SQL?** Zingg Enterprise also exposes a SQL API for analysts and no-code workflows — see **Step 5 – SQL API** below.
{% endhint %}

---

## 5. SQL API (For Analysts / No-Code Workflows)

After the Marketplace install or manual stored-procedure deployment, run the whole pipeline from SQL:

```sql
-- 1. Find training data
CALL ZINGG.FIND_TRAINING_DATA('b2b_customer_v1');

-- 2. Label interactively in Snowsight (opens the labeling UI)
CALL ZINGG.LABEL('b2b_customer_v1');

-- 3. Train the model
CALL ZINGG.TRAIN('b2b_customer_v1');

-- 4. Run match
CALL ZINGG.MATCH('b2b_customer_v1');

-- 5. Incremental run
CALL ZINGG.MATCH('b2b_customer_v1', incremental => TRUE, last_run_ts => '2024-01-15');

-- 6. View statistics
SELECT * FROM TABLE(ZINGG.GET_STATISTICS('b2b_customer_v1'));
```

---

## 6. Interpreting Results: B2B Score Bands

Use these score bands to decide what happens with each resolved cluster:

| Score Range | Tier | Action | B2B Example |
|-------------|------|--------|-------------|
| **0.90 – 1.00** | **Auto-Merge** | Flow directly to golden record | Same D-U-N-S + exact company name |
| **0.75 – 0.89** | **Steward Review** | Route to Data Steward queue | "Google LLC" vs "Alphabet Inc." (alias mapping) |
| **0.50 – 0.74** | **Deep Review** | Senior steward + SME consult | Partial address match, missing fields |
| **Below 0.50** | **Separate Entities** | Keep as distinct records | Different tax IDs, different domains |

Route results automatically using a view:

```sql
CREATE OR REPLACE VIEW RESOLVED_CUSTOMERS_REVIEW AS
SELECT *,
  CASE
    WHEN Z_MINSCORE >= 0.90 THEN 'AUTO_MERGE'
    WHEN Z_MINSCORE >= 0.75 THEN 'STEWARD_REVIEW'
    WHEN Z_MINSCORE >= 0.50 THEN 'DEEP_REVIEW'
    ELSE 'SEPARATE'
  END AS ACTION_TIER
FROM ZINGG_ENTITY_RESOLUTION.PUBLIC.RESOLVED_CUSTOMERS;
```

---

## 7. Monitoring & Operations

<details>
<summary><strong>Query History — built-in observability</strong></summary>

```sql
-- All Zingg job runs
SELECT QUERY_ID, QUERY_TEXT, EXECUTION_STATUS,
       TOTAL_ELAPSED_TIME/1000 AS seconds,
       BYTES_SCANNED, BYTES_WRITTEN
FROM TABLE(INFORMATION_SCHEMA.QUERY_HISTORY())
WHERE QUERY_TEXT ILIKE '%ZINGG%' OR QUERY_TEXT ILIKE '%zingg_enterprise%'
ORDER BY START_TIME DESC;

-- Warehouse utilization during Zingg runs
SELECT WAREHOUSE_NAME, AVG(RUNNING_QUERY_COUNT) AS avg_concurrent,
       SUM(EXECUTION_TIME)/1000 AS total_seconds
FROM SNOWFLAKE.ACCOUNT_USAGE.QUERY_HISTORY
WHERE QUERY_TEXT ILIKE '%ZINGG%'
GROUP BY WAREHOUSE_NAME;
```
</details>

<details>
<summary><strong>Model health metrics</strong></summary>

```python
# Get comprehensive statistics
stats = zingg_customer.get_statistics()
print(f"Total clusters: {stats['summary']['total_clusters']}")
print(f"Avg cluster size: {stats['summary']['avg_cluster_size']}")
print(f"Singleton rate: {stats['summary']['singleton_rate']:.1%}")
print(f"Max cluster size: {stats['summary']['max_cluster_size']}")

# Per-cluster details for review
cluster_stats = stats['cluster']
for cluster in cluster_stats:
    if cluster['z_cluster_determinism'] < 0.3:
        print(f"Cluster {cluster['z_cluster']}: Low determinism "
              f"({cluster['z_cluster_determinism']:.2f}) — review recommended")
```
</details>

---

## 8. Security Best Practices

<details>
<summary><strong>1. Key-pair authentication (production required)</strong></summary>

```bash
# Generate an RSA key pair
openssl genrsa 2048 | openssl pkcs8 -topk8 -inform PEM -out rsa_key.p8 -nocrypt
openssl rsa -in rsa_key.p8 -pubout -out rsa_key.pub

# Assign the public key to the service user
ALTER USER ZINGG_SERVICE_USER SET RSA_PUBLIC_KEY='MIIBIjANBgkqh...';
```

```python
# Use it in connection parameters
connection_parameters = {
    "account": "...",
    "user": "ZINGG_SERVICE_USER",
    "private_key_file": "/secure/path/rsa_key.p8",
    # No password needed
    ...
}
```
</details>

<details>
<summary><strong>2. Network policy</strong></summary>

```sql
-- Restrict the Zingg service user to corporate IPs
CREATE NETWORK POLICY ZINGG_NETWORK_POLICY
  ALLOWED_IP_LIST = ('203.0.113.0/24', '198.51.100.0/24');  -- Your office/CI ranges
ALTER USER ZINGG_SERVICE_USER SET NETWORK_POLICY = ZINGG_NETWORK_POLICY;
```
</details>

<details>
<summary><strong>3. Column-level security (PII)</strong></summary>

```sql
-- Mask SSN / Tax ID for non-privileged roles
CREATE MASKING POLICY MASK_TAX_ID AS (val STRING) RETURNS STRING ->
  CASE WHEN CURRENT_ROLE() IN ('ZINGG_ER_ROLE', 'ACCOUNTADMIN') THEN val ELSE '*****' END;

ALTER TABLE RESOLVED_VENDORS MODIFY COLUMN TAX_ID SET MASKING POLICY MASK_TAX_ID;
```
</details>

<details>
<summary><strong>4. Audit logging</strong></summary>

```sql
-- Enable query audit for the Zingg role
ALTER ACCOUNT SET LOG_LEVEL = 'FULL';  -- Requires ACCOUNTADMIN
-- All Zingg queries now logged in SNOWFLAKE.ACCOUNT_USAGE.QUERY_HISTORY
```
</details>

---

## 9. Troubleshooting Quick Reference

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Session creation failed: Invalid private key` | Key format / path wrong | Ensure PKCS#8 DER format, correct path, no passphrase |
| `Stage @ZINGG_MODELS does not exist` | Setup wizard skipped | Run the `CREATE STAGE` SQL from the configuration section |
| `Out of memory` on train phase | Warehouse too small | Increase warehouse size or tune `numPartitions` |
| `Labeling UI won't load` | Snowsight permissions | Grant `SNOWSIGHT_USER` role to the service user |
| `Incremental run duplicates ZINGG_ID` | `lastRunTimestamp` wrong | Use a `CURRENT_TIMESTAMP() - INTERVAL '1 DAY'` pattern |
| `MAPPING match type not found` | Mapping file not on stage | `PUT` the mapping JSON to `@ZINGG_MODELS/mappings/` |
| `Deterministic rule not firing` | Field type mismatch | Ensure `EXACT` match type on deterministic fields |

---

## 10. Performance Tuning

| Parameter | Starting Value | Tuning Rule |
|-----------|----------------|-------------|
| `numPartitions` | 8 | 2–4 × warehouse cluster count (`MAX_CLUSTER_COUNT`) |
| `labelDataSampleSize` | 0.3 | Reduce to 0.1 for >1M records; increase for <100K |
| Warehouse size | MEDIUM | Scale up for train/match; scale down for label |
| `MAX_CLUSTER_COUNT` | 4 | Increase for parallel phase execution |

---

## 11. Version Upgrade Procedure

```bash
# 1. Back up your current model stage
CREATE STAGE ZINGG_ENTITY_RESOLUTION.PUBLIC.ZINGG_MODELS_BACKUP
  CLONE ZINGG_ENTITY_RESOLUTION.PUBLIC.ZINGG_MODELS;

# 2. Download the new Enterprise package
# 3. Install the new Python wheel
pip install --upgrade zingg-enterprise-snowflake-<new_version>.whl

# 4. Re-run train (models are version-compatible forward)
zingg_customer.train()   # Rebuilds with the new version

# 5. Verify output
results = zingg_customer.match()
assert results.filter("ZINGG_ID IS NOT NULL").count() > 0

# 6. Rollback if needed
DROP STAGE ZINGG_MODELS;
ALTER STAGE ZINGG_MODELS_BACKUP RENAME TO ZINGG_MODELS;
```

---

## Related Documentation

| Topic | Link |
|-------|------|
| Snowflake Connector (Community) | [Connect Snowflake](../../../connect-your-data/connect-cloud-warehouses/connect-snowflake.md) |
| Platform Guide: Snowflake | [Platform Guide for Snowflake](../../../platform-guides/platform-guide-for-snowflake.md) |
| Python API Reference | [Enterprise ZinggES Python API](../../../zingg-python-api/enterprise-zingges-python-api.md) |
| Match Types Reference | [Field Definition Match Types](../../../zingg-concepts/zingg-configuration/field-definition/match-types/) |
| Deterministic Matching | [Set Deterministic Matching Conditions](../../../running-zingg/set-deterministic-matching-conditions.md) |
| Pass-Through Records | [Pass Through](../../../running-zingg/pass-through.md) |
| Incremental Matching | [Run Incremental Matching](../../../running-zingg/run-incremental-matching.md) |

---

## Support & Licensing

* **Enterprise License Required** — contact [Zingg Sales](https://www.zingg.ai/company/contact/contact)
* **Technical Support** — included with your Enterprise license (dedicated Slack channel)
* **Documentation Issues** — GitHub Issues on `zinggAI/zingg` with label `docs:snowflake`
* **Training** — Zingg offers on-site Data Steward labeling workshops for B2B/Vendor ER
