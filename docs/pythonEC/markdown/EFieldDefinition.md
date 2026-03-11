# EFieldDefinition

## zinggEC.enterprise.common.EFieldDefinition

This module is to work with the extended functionality of field definitions

### *class* zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition(name, dataType, \*matchType)

Bases: `FieldDefinition`

This class defines each field that we use in matching. We can use this to configure the properties of each field we use for matching in Zingg.

* **Parameters:**
  * **name** (*String*) – name of the field
  * **dataType** (*String*) – type of the data e.g. string, float, etc.
  * **matchType** (*MatchType*) – match type of this field e.g. FUSSY, EXACT, etc. including user-defined mapping match types

---

#### getMatchTypeArray(matchType)

Purpose:

Return a Java list of match type objects suitable for passing to the underlying Java API. The method converts any Python wrapper types (for example `MappingMatchType`) to their Java equivalents.

Parameters:

* **matchType** (*List[IMatchType]*) – list of match type objects or wrappers provided to the constructor.

Returns:

* **java.util.ArrayList** of match type objects .

---

#### getPrimaryKey()

Purpose:

Check whether this field is configured as a primary key. Primary key fields are excluded from postprocessing and are typically marked with `MatchType.DONT_USE`.

Parameters:

* None

Returns:

* **boolean** — `True` if the field is the primary key; otherwise `False`.

Example:

```python
id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)
assert id.getPrimaryKey() is True
```

---

#### setPrimaryKey(primaryKey)

Purpose:

Mark or unmark the field as the primary key for records. When set to `True`, the field will be treated as the primary identifier and will not be postprocessed.

Parameters:

* **primaryKey** (*boolean*) — `True` to mark as primary key; `False` to clear primary key.

Returns:

* None

Example:

```python
id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)
```

---

### Postprocessors (added)

Fields may be configured with postprocessors which transform output values after matching. Two helper methods are provided in the Python API; they are thin wrappers over the Java field-definition API.

#### setPostProcessors(postProcessors)

Purpose:

Attach one or more postprocessor definitions to the field. Postprocessors are applied after matching and before output is written.

Parameters:

* **postProcessors** (*List[IPostprocessor]*) — list of postprocessor objects or wrappers .

Returns:

* None

Example:

```python
from zinggEC.enterprise.common.StandardisePostprocessorType import StandardisePostprocessorType
job_title = EFieldDefinition("job_title", "string", MatchType.FUZZY)
job_title.setPostProcessors([StandardisePostprocessorType("STANDARDISE", "jobtitles")])
```

#### getPostProcessors()

Purpose:

Retrieve the postprocessors configured for the field.

Parameters:

* None

Returns:

* **List[IPostprocessor]** — the list of configured postprocessor objects.

