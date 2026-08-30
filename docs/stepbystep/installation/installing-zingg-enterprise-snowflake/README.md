---
description: >-
  Complete guide for installing and running Zingg Enterprise natively on Snowflake using Snowpark. Covers Marketplace installation, manual package setup, configuration, and B2B customer/vendor entity resolution examples.
nav_order: 1
---

# Installing Zingg on Snowflake for Enterprise

{% hint style="info" icon="right-long" %}
**Enterprise only.** Zingg Enterprise runs natively inside Snowflake using Snowpark — no EC2, no Docker, no Lambda, no external Spark clusters. All compute executes on your Snowflake warehouse.
{% endhint %}

## Prerequisites

| Requirement | Details |
|-------------|---------|
| **Snowflake Edition** | Enterprise Edition or higher (Snowpark required) |
| **Warehouse** | X-Small to 4X-Large; auto-scaling recommended |
| **Database/Schema** | Dedicated database for Zingg models (e.g., `ZINGG_ENTITY_RESOLUTION`) |
| **Stage** | Internal named stage for model artifacts (e.g., `@ZINGG_MODELS`) |
| **License** | Zingg Enterprise license file (`zingg.license`) |
| **Python** | 3.8+ with `snowflake-snowpark-python` ≥ 1.12 |

## Quick Start

Choose your installation method:

| Method | Time | Best For |
|--------|------|----------|
| [Snowflake Marketplace](enterprise-snowflake-instructions.md#method-1-snowflake-marketplace-recommended-5-minutes) | 5 minutes | Most users — automated setup |
| [Manual Package Install](enterprise-snowflake-instructions.md#method-2-manual-package-install-for-air-gapped--custom-deployments) | 15 minutes | Air-gapped environments, custom deployments |

## Installation Methods

### Method 1: Snowflake Marketplace (Recommended)
1. Log into Snowsight → **Marketplace** → Search **"Zingg Enterprise"**
2. Click **Get** → Select target database/schema → **Install**
3. Run the setup wizard (configures warehouse, stage, roles automatically)
4. Verify: `CALL ZINGG.SYSTEM$VERIFY_SETUP();`

### Method 2: Manual Package Install
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

## Next Steps

| Step | Description | Link |
|------|-------------|------|
| **1** | Complete configuration with B2B examples | [Enterprise Snowflake Instructions](enterprise-snowflake-instructions.md#configuration-b2b-customer--vendor-entity-resolution) |
| **2** | Set up Snowflake objects (warehouse, database, stage, roles) | [Setting Up Zingg Enterprise for Snowflake](setting-up-zingg-enterprise-for-snowflake.md) |
| **3** | Configure connection properties | [Snowflake Properties](snowflake-properties.md) |
| **4** | Define match configuration for your schema | [Match Configuration](match-configuration.md) |
| **5** | Verify installation with sample data | [Verifying the Installation](verifying-the-installation.md) |
| **6** | Run long jobs asynchronously | [Running Asynchronously](running-asynchronously.md) |

## B2B Entity Resolution Examples

The [Enterprise Snowflake Instructions](enterprise-snowflake-instructions.md) include complete configuration examples for:

- **B2B Customer Accounts** — Resolves parent companies and subsidiaries (e.g., "Alphabet Inc." + "Alphabet LLC" + "Google LLC" → same entity)
- **Enterprise Vendor Profiles** — Resolves vendors across SAP, Oracle, Coupa, Ariba using D-U-N-S, tax IDs, and email domains

## Key Features

- **Zero Infrastructure**: Runs natively on Snowflake warehouse — no EC2, Databricks, or EMR
- **Zero Data Egress**: Compute pushes to data; customer/vendor data never leaves Snowflake
- **Persistent ZINGG_ID**: Globally unique entity identifiers that survive incremental runs
- **Snowsight Labeling UI**: Interactive Match/Non-Match/Can't Say widget for data stewards
- **SQL & Python APIs**: Call Zingg phases from Snowflake SQL or Snowpark Python
- **Incremental Matching**: Daily/weekly loads with human decision preservation

## Security

- **Key-pair authentication** (production required)
- **Network policies** to restrict service user IPs
- **Column-level masking** for PII (Tax ID, SSN)
- **Audit logging** via Snowflake Query History

## Support

- **Enterprise License Required**: Contact [Zingg Sales](https://www.zingg.ai/company/contact/contact)
- **Technical Support**: Included with Enterprise license — dedicated Slack channel
- **Documentation Issues**: GitHub Issues on `zinggAI/zingg` with label `docs:snowflake`