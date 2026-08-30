---
description: Connection Details
---

# Snowflake Connection Properties

Zingg needs details about accessing Snowflake which can be provided through a properties file.

> `touch snowEnv.txt `

### SnowEnv.txt format:

```
    URL={snowflake_url}
    USER={snowflake_user_name}
    PASSWORD={snowflake_password}
    ROLE={role}
    WAREHOUSE={warehouse}
    DB={database_name}
    SCHEMA={schema}
    CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY=900
```

The parameter `CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY` is the number of seconds in-between client attempts to update the token for the session - it can vary from 900 to 3600.

### Parameter Descriptions

| Parameter | Required | Description | Example |
|-----------|----------|-------------|---------|
| `URL` | Yes | Snowflake account URL | `xy12345.us-east-1.snowflakecomputing.com` |
| `USER` | Yes | Snowflake username | `zingg_user` |
| `PASSWORD` | Yes | Snowflake password | `********` |
| `ROLE` | No | Snowflake role to use | `SYSADMIN` |
| `WAREHOUSE` | Yes | Snowflake warehouse name | `COMPUTE_WH` |
| `DB` | Yes | Database name | `CUSTOMER_DATA` |
| `SCHEMA` | Yes | Schema name | `PUBLIC` |
| `CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY` | No | Session keep-alive interval (seconds) | `900` |

### Example snowEnv.txt

```
    URL=xy12345.us-east-1.snowflakecomputing.com
    USER=zingg_user
    PASSWORD=mySecurePassword123
    ROLE=SYSADMIN
    WAREHOUSE=COMPUTE_WH
    DB=CUSTOMER_DATA
    SCHEMA=PUBLIC
    CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY=900
```

### Usage with Zingg

Pass the properties file to Zingg using the `--properties-file` flag:

```bash
./zingg.sh --phase findTrainingData --conf configSnow.json --properties-file snowEnv.txt
```

Or in Python:

```python
args.setPropertiesFile("snowEnv.txt")
```

### Security Best Practices

1. **Never commit `snowEnv.txt` to version control** - Add it to `.gitignore`
2. **Use Snowflake key-pair authentication** for production instead of password
3. **Rotate passwords regularly** and use strong passwords
4. **Limit the Snowflake role** to only required privileges
5. **Use environment variables** in CI/CD pipelines instead of files

### Key-Pair Authentication (Alternative)

For production, use key-pair authentication instead of password:

1. Generate RSA key pair:
```bash
openssl genrsa 2048 | openssl pkcs8 -topk8 -inform PEM -out rsa_key.p8 -nocrypt
openssl rsa -in rsa_key.p8 -pubout -out rsa_key.pub
```

2. Assign public key to Snowflake user:
```sql
ALTER USER zingg_user SET RSA_PUBLIC_KEY='MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...';
```

3. Update `snowEnv.txt`:
```
    URL=xy12345.us-east-1.snowflakecomputing.com
    USER=zingg_user
    PRIVATE_KEY_FILE=rsa_key.p8
    PRIVATE_KEY_FILE_PWD=  # if key is encrypted
    ROLE=SYSADMIN
    WAREHOUSE=COMPUTE_WH
    DB=CUSTOMER_DATA
    SCHEMA=PUBLIC
    CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY=900
```

### Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection timeout | Check network/firewall, verify URL format |
| Authentication failed | Verify username/password or key pair |
| Session expired | Increase `CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY` |
| Warehouse not found | Verify warehouse name and permissions |
| Database/schema not found | Verify database and schema names |

### Related Documentation

* [Platform Guide for Snowflake](../../../platform-guides/platform-guide-for-snowflake.md)
* [Connect Snowflake](../../../connect-your-data/connect-cloud-warehouses/connect-snowflake.md)
* [Zingg Runtime Properties](../../../reference/runtime-properties.md)