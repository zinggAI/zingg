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

The parameter CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY is the number of seconds in-between client attempts to update the token for the session - it can vary from 900 to 3600.
