# Deterministic Matching

### Deterministic Matching - _Zingg Enterprise Feature_

Zingg Enterprise allows the ability to plug rule-based deterministic matching along with already Zingg AI's probabilistic matching. If the data contains _sure_ identifiers like emails, SSNs, passport-ids etc, we can use these attributes to resolve records.\
\
The deterministic matching flow is weaved into Zingg's flow to ensure that each record which has a match finds one, probabilistically, deterministically or both. If the data has known identifiers, Zingg Enterprise's deterministic matching highly improves both matching accuracy and performance.

### Example For Configuring In JSON:

```json
    "deterministicMatching":[  
        {  
           "matchCondition":[{"fieldName":"fname"},{"fieldName":"stNo"},{"fieldName":"add1"}]  
        },  
        {  
           "matchCondition":[{"fieldName":"fname"},{"fieldName":"dob"},{"fieldName":"ssn"}]  
        },   
        {  
           "matchCondition":[{"fieldName":"fname"},{"fieldName":"email"}]  
        }  
    ]  
```

#### Python Code Example:

```{python}
detMatchNameAdd = DeterministicMatching('fname','stNo','add1')  
detMatchNameDobSsn = DeterministicMatching('fname','dob','ssn')  
detMatchNameEmail = DeterministicMatching('fname','email')  
args.setDeterministicMatchingCondition(detMatchNameAdd,detMatchNameDobSsn,detMatchNameEmail)  
```

#### How Will It Work:

The above conditions would translate into the following:

1. Those rows which have **exactly** same `fname`, `stNo` and `add1` => exact match with max score 1\
   _OR_
2. Those rows which have **exactly** same `fname`, `dob` and `ssn` => exact match with max score 1\
   _OR_
3. Those rows which have **exactly** same `fname` and `email` => exact match with max score 1
