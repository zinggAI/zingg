---
description: >-
  Complete guide for running Zingg Enterprise natively on Snowflake using Snowpark.
  Covers installation, configuration, SQL/Python APIs, incremental matching, and
  B2B customer/vendor entity resolution examples.
parent: Installing Zingg Enterprise for Snowflake
nav_order: 1
---

# Zingg Enterprise Snowflake: Complete Instructions

{% hint style="info" icon="right-long" %}
**Enterprise only.** Zingg Enterprise runs natively inside Snowflake using Snowpark — no EC2, no Docker, no Lambda, no external Spark clusters. All compute executes on your Snowflake warehouse.
{% endhint %}

---

## Why Snowflake Native?

| Traditional (Community) | Zingg Enterprise Snowflake |
|-------------------------|----------------------------|
| Provision EC2 / Databricks / EMR | Zero infrastructure — runs on your Snowflake warehouse |
| Manage Spark jars, versions, clusters | Snowpark handles runtime; you only configure warehouse size |
| Data moves out of Snowflake for processing | **Zero data egress** — compute pushes to data |
| Lambda + API Gateway for async phases | Native Snowflake procedures / Snowpark Container Services |
| Separate monitoring (CloudWatch, Spark UI) | Unified in Snowflake Query History & Task Graphs |

---

## Prerequisites

| Requirement | Details |
|-------------|---------|
| **Snowflake Edition** | Enterprise Edition or higher (Snowpark required) |
| **Warehouse** | X-Small to 4X-Large; auto-scaling recommended |
| **Database/Schema** | Dedicated database for Zingg models (e.g., `ZINGG_ENTITY_RESOLUTION`) |
| **Stage** | Internal named stage for model artifacts (e.g., `@ZINGG_MODELS`) |
| **License** | Zingg Enterprise license file (`zingg.license`) |
| **Python** | 3.8+ with `snowflake-snowpark-python` ≥ 1.12 |
| **Network** | Snowflake account allows outbound to `pypi.org` (for pip install) or use internal PyPI mirror |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        YOUR SNOWFLAKE ACCOUNT                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  WAREHOUSE   │  │   DATABASE   │  │      NAMED STAGE     │  │
│  │  (Compute)   │◄─┤  ZINGG_ER    │◄─┤  @ZINGG_MODELS       │  │
│  │  AUTO-SCALE  │  │  (Tables)    │  │  (Model Artifacts)   │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘  │
│         │                 │                      │             │
│         ▼                 ▼                      ▼             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              SNOWPARK CONTAINER SERVICES                  │  │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────────┐  │  │
│  │  │  FIND   │  │ LABEL   │  │ TRAIN   │  │   MATCH     │  │  │
│  │  │ TRAINING│  │  (UI)   │  │         │  │             │  │  │
│  │  └────┬────┘  └────┬────┘  └────┬────┘  └──────┬──────┘  │  │
│  └───────┼────────────┼────────────┼────────────┼───────────┘  │
│          │            │            │            │              │
└──────────┼────────────┼────────────┼────────────┼──────────────┘
           │            │            │            │
    ┌──────┴──────┐ ┌───┴────┐ ┌─────┴────┐ ┌────┴─────┐
    │  Snowsight  │ │ Jupyter│ │  SQL/    │ │  Python  │
    │  Labeling   │ │ Notebook│ │  Python  │ │  Worksheets│
    └─────────────┘ └────────┘ └──────────┘ └───────────┘
```

---

## Installation Methods

### Method 1: Snowflake Marketplace (Recommended — 5 minutes)

1. Log into Snowsight → **Marketplace** → Search **"Zingg Enterprise"**
2. Click **Get** → Select target database/schema → **Install**
3. Run the setup wizard (configures warehouse, stage, roles automatically)
4. Verify: `CALL ZINGG.SYSTEM$VERIFY_SETUP();`

### Method 2: Manual Package Install (For air-gapped / custom deployments)

```bash
# 1. Download Enterprise package (provided by Zingg after license purchase)
# Package name: zingg-enterprise-snowflake-<version>.tar.gz

# 2. Extract
gzip -d zingg-enterprise-snowflake-<version>.tar.gz
tar xvf zingg-enterprise-snowflake-<version>.tar
cd zingg-enterprise-snowflake-<version>

# 3. Install Python dependencies
pip install snowflake-snowpark-python==1.12.*
pip install -r requirements.txt  # includes zingg-enterprise-snowflake wheel

# 4. Place license file
cp /path/to/zingg.license .
```

---

## Configuration: B2B Customer & Vendor Entity Resolution

The following examples use **B2B Customer Accounts** (parent companies + subsidiaries) and **Enterprise Vendor Profiles** (suppliers across ERPs) as the canonical entities.

### 1. Snowpark Session Setup

```python
from snowflake.snowpark import Session

# Use key-pair auth for production (see Security section below)
connection_parameters = {
    "account": "xy12345.us-east-1",           # Your Snowflake account identifier
    "user": "ZINGG_SERVICE_USER",             # Dedicated service user
    "private_key_file": "/path/to/rsa_key.p8", # Key-pair auth (recommended)
    # "password": "********",                 # Or password auth for dev only
    "role": "ZINGG_ER_ROLE",                  # Custom role with required grants
    "warehouse": "ZINGG_WH",                  # Dedicated warehouse (auto-scale)
    "database": "ZINGG_ENTITY_RESOLUTION",    # Target database
    "schema": "PUBLIC"                        # Target schema
}

session = Session.builder.configs(connection_parameters).create()
session.sql("USE WAREHOUSE ZINGG_WH").collect()
```

### 2. Create Required Snowflake Objects

Run once per environment (DBA or ACCOUNTADMIN):

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

-- 5. Grant to service user
GRANT ROLE ZINGG_ER_ROLE TO USER ZINGG_SERVICE_USER;
ALTER USER ZINGG_SERVICE_USER SET DEFAULT_ROLE = ZINGG_ER_ROLE;
```

### 3. Zingg Configuration (Python Dict)

```python
from zingg_enterprise import ZinggEnterprise

# ─── B2B CUSTOMER ACCOUNTS EXAMPLE ───
# Resolves: "Alphabet Inc." + "Alphabet LLC" + "Google LLC" → same entity
customer_config = {
    "data": [{
        "name": "customer_input",
        "table": "RAW_CUSTOMERS",           # Source table in ZINGG_ENTITY_RESOLUTION.PUBLIC
        "database": "ZINGG_ENTITY_RESOLUTION",
        "schema": "PUBLIC"
    }],
    "output": [{
        "name": "customer_resolved",
        "table": "RESOLVED_CUSTOMERS",      # Output table (created by Zingg)
        "database": "ZINGG_ENTITY_RESOLUTION",
        "schema": "PUBLIC"
    }],
    "modelId": "b2b_customer_v1",
    "zinggDir": "@ZINGG_MODELS/b2b_customer_v1",  # Stage path for model artifacts
    "numPartitions": 8,                     # Tune to warehouse: 2-4 × cluster count
    "labelDataSampleSize": 0.3,             # 30% sample for training pair generation
    "fieldDefinition": [
        # Company name — fuzzy with MAPPING for known aliases
        {
            "fieldName": "company_name",
            "matchType": "MAPPING_company_aliases,FUZZY",
            "fields": "company_name",
            "dataType": "string"
        },
        # Legal entity suffix — exact match on normalized suffix
        {
            "fieldName": "legal_suffix",
            "matchType": "EXACT",
            "fields": "legal_suffix",
            "dataType": "string"
        },
        # Address — fuzzy optimized for street variations
        {
            "fieldName": "address",
            "matchType": "FUZZY_OPTIMISED",
            "fields": "address_line1,address_line2,city,state,postal_code,country",
            "dataType": "string"
        },
        # Website domain — exact match (strong signal for B2B)
        {
            "fieldName": "website_domain",
            "matchType": "EXACT",
            "fields": "website_domain",
            "dataType": "string"
        },
        # Parent company ID — deterministic match when available
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
    # Optional: Deterministic rules for high-confidence auto-merge
    "deterministicMatching": [
        {"fields": ["duns_number"]},                           # Same D-U-N-S = same vendor
        {"fields": ["website_domain", "company_name"]},        # Same domain + name = same
        {"fields": ["parent_company_id", "legal_suffix"]}      # Same parent + suffix
    ],
    # Optional: Pass-through for inactive records
    "passthroughExpr": "status = 'INACTIVE' AND status IS NOT NULL"
}

# ─── ENTERPRISE VENDOR PROFILES EXAMPLE ───
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

# Initialize Zingg Enterprise
zingg_customer = ZinggEnterprise(session, customer_config)
zingg_vendor = ZinggEnterprise(session, vendor_config)
```

---

## Mapping Files for B2B Aliases (MAPPING Match Type)

Create JSON mapping files and upload to `@ZINGG_MODELS/mappings/`:

**`company_aliases.json`** — Parent/subsidiary relationships
```json
[
  ["Alphabet Inc.", "Alphabet LLC", "Google LLC", "Google Inc.", "Google"],
  ["Microsoft Corporation", "Microsoft Corp", "MSFT", "Microsoft"],
  ["Amazon.com Inc.", "Amazon.com LLC", "Amazon", "AWS", "Amazon Web Services"],
  ["Meta Platforms Inc.", "Meta Platforms", "Facebook Inc.", "Facebook", "FB"],
  ["International Business Machines", "IBM Corp", "IBM", "International Business Machines Corp"]
]
```

**`vendor_aliases.json`** — Common vendor name variations
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

Reference in config: `"matchType": "MAPPING_company_aliases,FUZZY"` (filename without `.json`)

---

## Running the Full Pipeline

### Phase 1: Find Training Data
```python
# Generates candidate pairs for labeling
zingg_customer.find_training_data()
# Output: candidate pairs written to @ZINGG_MODELS/b2b_customer_v1/trainingData/unmarked/
```

### Phase 2: Label Pairs (Interactive)

**Option A: Snowsight Labeling UI (Recommended for Data Stewards)**
```python
# Opens interactive labeling in Snowsight
zingg_customer.label()
```
- Shows record pairs side-by-side with Match / Non-Match / Can't Say buttons
- Auto-saves labels to `@ZINGG_MODELS/b2b_customer_v1/trainingData/marked/`
- Target: 40-60 Match pairs + 40-60 Non-Match pairs before training

**Option B: Jupyter / Python Worksheet (For ML Engineers)**
```python
zingg_customer.label()  # Returns widget in Jupyter
# Or use programmatic labeling for bulk review
```

### Phase 3: Generate Documentation (Audit Before Train)
```python
# Produces HTML report of all labeled pairs
zingg_customer.generate_docs()
# Output: @ZINGG_MODELS/b2b_customer_v1/docs/model.html + data.html
```
**Review checklist:**
- [ ] False positives < 2% (different entities labeled as Match)
- [ ] False negatives < 2% (same entities labeled as Non-Match)
- [ ] All field types represented in training pairs
- [ ] Edge cases covered: abbreviations, missing fields, transliterations

### Phase 4: Train Model
```python
# Builds blocking + similarity models from labels
zingg_customer.train()
# Model artifacts saved to @ZINGG_MODELS/b2b_customer_v1/model/
```

### Phase 5: Match (Resolve Entities)
```python
# Full resolution on input table
results = zingg_customer.match()
results.show()

# Output table RESOLVED_CUSTOMERS contains:
# - All input columns
# - ZINGG_ID: Persistent entity GUID (e.g., "ZNGG_7f3a2b1c...")
# - Z_MINSCORE: Weakest link confidence in cluster
# - Z_MAXSCORE: Strongest link confidence in cluster
```

### Phase 6: Incremental Matching (Production)
```python
# For daily/weekly incremental loads
incremental_config = {
    **customer_config,
    "incremental": True,
    "lastRunTimestamp": "2024-01-15 00:00:00"  # Process records modified since
}

zingg_incremental = ZinggEnterprise(session, incremental_config)
results = zingg_incremental.match()
# Merges new records into existing ZINGG_ID clusters
# Preserves human-approved decisions from label phase
```

---

## SQL API (For Analysts / No-Code Workflows)

After Marketplace install or manual stored procedure deployment:

```sql
-- 1. Find training data
CALL ZINGG.FIND_TRAINING_DATA('b2b_customer_v1');

-- 2. Label interactively in Snowsight (opens labeling UI)
CALL ZINGG.LABEL('b2b_customer_v1');

-- 3. Train model
CALL ZINGG.TRAIN('b2b_customer_v1');

-- 4. Run match
CALL ZINGG.MATCH('b2b_customer_v1');

-- 5. Incremental run
CALL ZINGG.MATCH('b2b_customer_v1', incremental => TRUE, last_run_ts => '2024-01-15');

-- 6. View statistics
SELECT * FROM TABLE(ZINGG.GET_STATISTICS('b2b_customer_v1'));
```

---

## Interpreting Results: B2B Score Bands

| Score Range | Tier | Action | B2B Example |
|-------------|------|--------|-------------|
| **0.90 – 1.00** | 🎯 **Auto-Merge** | Flow directly to golden record | Same D-U-N-S + exact company name |
| **0.75 – 0.89** | ⚠️ **Steward Review** | Route to Data Steward queue | "Google LLC" vs "Alphabet Inc." (alias mapping) |
| **0.50 – 0.74** | 🔍 **Deep Review** | Senior steward + SME consult | Partial address match, missing fields |
| **Below 0.50** | 🛑 **Separate Entities** | Keep as distinct records | Different tax IDs, different domains |

```sql
-- Auto-route to review queues
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

## Monitoring & Operations

### Query History (Built-in Observability)
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

### Model Health Metrics
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
        print(f"Cluster {cluster['z_cluster']}: Low determinism ({cluster['z_cluster_determinism']:.2f}) — review recommended")
```

---

## Security Best Practices

### 1. Key-Pair Authentication (Production Required)
```bash
# Generate RSA key pair
openssl genrsa 2048 | openssl pkcs8 -topk8 -inform PEM -out rsa_key.p8 -nocrypt
openssl rsa -in rsa_key.p8 -pubout -out rsa_key.pub

# Assign public key to service user
ALTER USER ZINGG_SERVICE_USER SET RSA_PUBLIC_KEY='MIIBIjANBgkqh...';
```

```python
# Use in connection parameters
connection_parameters = {
    "account": "...",
    "user": "ZINGG_SERVICE_USER",
    "private_key_file": "/secure/path/rsa_key.p8",
    # No password needed
    ...
}
```

### 2. Network Policy
```sql
-- Restrict Zingg service user to corporate IPs
CREATE NETWORK POLICY ZINGG_NETWORK_POLICY
  ALLOWED_IP_LIST = ('203.0.113.0/24', '198.51.100.0/24');  # Your office/CI/CD ranges
ALTER USER ZINGG_SERVICE_USER SET NETWORK_POLICY = ZINGG_NETWORK_POLICY;
```

### 3. Column-Level Security (PII)
```sql
-- Mask SSN / Tax ID in output for non-privileged roles
CREATE MASKING POLICY MASK_TAX_ID AS (val STRING) RETURNS STRING ->
  CASE WHEN CURRENT_ROLE() IN ('ZINGG_ER_ROLE', 'ACCOUNTADMIN') THEN val ELSE '*****' END;

ALTER TABLE RESOLVED_VENDORS MODIFY COLUMN TAX_ID SET MASKING POLICY MASK_TAX_ID;
```

### 4. Audit Logging
```sql
-- Enable query audit for Zingg role
ALTER ACCOUNT SET LOG_LEVEL = 'FULL';  -- Requires ACCOUNTADMIN
-- All Zingg queries now logged in SNOWFLAKE.ACCOUNT_USAGE.QUERY_HISTORY
```

---

## Troubleshooting Quick Reference

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Session creation failed: Invalid private key` | Key format / path wrong | Ensure PKCS#8 DER format, correct path, no passphrase |
| `Stage @ZINGG_MODELS does not exist` | Setup wizard skipped | Run `CREATE STAGE` SQL from Configuration section |
| `Out of memory` on train phase | Warehouse too small | Increase warehouse size or `numPartitions` |
| `Labeling UI won't load` | Snowsight permissions | Grant `SNOWSIGHT_USER` role to service user |
| `Incremental run duplicates ZINGG_ID` | `lastRunTimestamp` incorrect | Use `CURRENT_TIMESTAMP() - INTERVAL '1 DAY'` pattern |
| `MAPPING match type not found` | Mapping file not on stage | `PUT` mapping JSON to `@ZINGG_MODELS/mappings/` |
| `Deterministic rule not firing` | Field type mismatch | Ensure `EXACT` match type on deterministic fields |

---

## Performance Tuning Guide

| Parameter | Starting Value | Tuning Rule |
|-----------|----------------|-------------|
| `numPartitions` | 8 | 2–4 × warehouse cluster count (MAX_CLUSTER_COUNT) |
| `labelDataSampleSize` | 0.3 | Reduce to 0.1 for >1M records; increase for <100K |
| Warehouse size | MEDIUM | Scale up for train/match; down for label |
| `MAX_CLUSTER_COUNT` | 4 | Increase for parallel phase execution |

---

## Version Upgrade Procedure

```bash
# 1. Backup current model stage
CREATE STAGE ZINGG_ENTITY_RESOLUTION.PUBLIC.ZINGG_MODELS_BACKUP
  CLONE ZINGG_ENTITY_RESOLUTION.PUBLIC.ZINGG_MODELS;

# 2. Download new Enterprise package
# 3. Install new Python wheel
pip install --upgrade zingg-enterprise-snowflake-<new_version>.whl

# 4. Re-run train (models are version-compatible forward)
zingg_customer.train()  # Rebuilds with new version

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
| Snowflake Connector (Community) | [Connect Snowflake](../connect-your-data/connect-cloud-warehouses/connect-snowflake.md) |
| Platform Guide: Snowflake | [Platform Guide for Snowflake](../platform-guides/platform-guide-for-snowflake.md) |
| Python API Reference | [Enterprise ZinggES Python API](../zingg-python-api/enterprise-zingges-python-api.md) |
| Match Types Reference | [Field Definition Match Types](../zingg-concepts/zingg-configuration/field-definition/match-types/) |
| Deterministic Matching | [Set Deterministic Matching Conditions](../running-zingg/set-deterministic-matching-conditions.md) |
| Pass-Through Records | [Pass Through](../running-zingg/pass-through.md) |
| Incremental Matching | [Run Incremental Matching](../running-zingg/run-incremental-matching.md) |

---

## Support & Licensing

- **Enterprise License Required**: Contact [Zingg Sales](https://www.zingg.ai/company/contact/contact)
- **Documentation Issues**: GitHub Issues on `zinggAI/zingg` with label `docs:snowflake`
- **Technical Support**: Included with Enterprise license — dedicated Slack channel
- **Training**: Zingg offers on-site Data Steward labeling workshops for B2B/Vendor ER