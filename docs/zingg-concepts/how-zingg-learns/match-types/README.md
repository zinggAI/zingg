---
description: >-
  The 12 similarity functions Zingg supports what each does, when to use it, and
  which are  Enterprise only.
---

# Match Types

Every field in your Zingg configuration gets a match type assigned to it. The match type tells\
Zingg which similarity function to apply when comparing values in that field across two records.\
Choosing the right match type for each field is one of the highest-impact configuration decisions you make as it directly affects matching accuracy.

Multiple match types can be assigned to a single field, separated by commas.

<table><thead><tr><th width="139.8359375" valign="top">Match Type</th><th width="172.765625" valign="top">What it does</th><th valign="top">Data types</th><th valign="top">Best for</th><th width="102.7313232421875" valign="top">Edition</th><th width="187.5281982421875">Example</th></tr></thead><tbody><tr><td valign="top"><a href="fuzzy-match.md"><code>FUZZY</code></a></td><td valign="top">Broad matching tolerant to typos,<br>abbreviations and variations. Uses <code>fuzzy</code> string similarity features.</td><td valign="top">string, integer, long, double, date</td><td valign="top">Names, addresses, free-text fields</td><td valign="top">All</td><td>"Jon Smith", "Jonathan Smith", and<br>"J. Smith" all match the same record.</td></tr><tr><td valign="top"><a href="fuzzy_optimised-match.md"><code>FUZZY_OPTIMISED</code></a></td><td valign="top">Same accuracy as <code>FUZZY</code>, approximately 2X faster. Lower CPU and memory usage on large datasets. Use this in production when <code>FUZZY</code> accuracy is needed at scale</td><td valign="top">string, integer, long, double, date</td><td valign="top">Same as <code>FUZZY</code> - use when dataset<br>is large and performance matters</td><td valign="top">Enterprise only</td><td>Same matching behaviour as <code>FUZZY</code>. Processes a 10M-record dataset in approximately half the time.</td></tr><tr><td valign="top"><a href="exact-match.md"><code>EXACT</code></a></td><td valign="top">Provides an exact-match signal to the classifier. A strong indicator when two values align perfectly. Records may still match based on other fields and how training pairs were labeled.</td><td valign="top">string, integer, long, date, boolean</td><td valign="top">Country codes, boolean flags,<br>categorical fields where no<br>variation is expected</td><td valign="top">All</td><td>"US" and "US"—strong match signal. "US" and ""us"—no exact signal, but the record pair may still match if other fields score highly.</td></tr><tr><td valign="top"><a href="email-match.md"><code>EMAIL</code></a></td><td valign="top">Matches only the local part before<br>the <code>@</code> - ignores the domain entirely.</td><td valign="top">string</td><td valign="top">Email address fields</td><td valign="top">All</td><td>"john.smith@gmail.com" and<br>"john.smith@company.com" — match.<br>"john.smith@gmail.com" and<br>"jsmith@gmail.com" — no match.</td></tr><tr><td valign="top"><a href="email_optimised-match.md"><code>EMAIL_OPTIMISED</code></a></td><td valign="top">Same as <code>EMAIL</code> but significantly faster<br>evaluation on large datasets.</td><td valign="top">string</td><td valign="top">Email fields at production scale</td><td valign="top">Enterprise only</td><td>Same behavior as <code>EMAIL</code>.<br>Use when comparing millions of<br>email addresses.</td></tr><tr><td valign="top"><a href="pincode-match.md"><code>PINCODE</code></a></td><td valign="top">Matches postal and pin codes across<br>common format variants (e.g. xxxxx<br>and xxxxx-xxxx).</td><td valign="top">string</td><td valign="top">Postal/ZIP code fields</td><td valign="top">All</td><td>"94102" and "94102-1234" — match.<br>"94102" and "94103" — no match.</td></tr><tr><td valign="top"><a href="numeric-match.md"><code>NUMERIC</code></a></td><td valign="top">Extracts numbers from strings and<br>compares how many are the same<br>across both strings.</td><td valign="top">string</td><td valign="top">Street numbers, apartment numbers,<br>building numbers</td><td valign="top">All</td><td>"42B Main St" and "42 Main Street" —<br>the number 42 is extracted and matched<br>from both. Alphabetic variation ignored.</td></tr><tr><td valign="top"><a href="numeric_with_units-match.md"><code>NUMERIC_WITH_UNITS</code></a></td><td valign="top">Extracts product codes or numbers<br>with units (e.g. 16gb, 500ml) and<br>compares how many are the same.</td><td valign="top">string</td><td valign="top">Product specification fields,<br>size field</td><td valign="top">All</td><td>16GB" and "16 GB DDR4" — the 16 and "GB" tokens match. "16gb" and "32gb" — the units match but the numbers do not.</td></tr><tr><td valign="top"><a href="text-match.md"><code>TEXT</code></a></td><td valign="top">Compares overlapping words between two strings. Good for longer descriptive text without many typos.</td><td valign="top">string</td><td valign="top">Product descriptions, notes,<br>long free-text fields</td><td valign="top">All</td><td>"Enterprise data management platform"<br>and "data management platform for<br>enterprise" — high overlap, likely match.<br>"Enterprise software" and<br>"consumer hardware" — low overlap,<br>likely no match.</td></tr><tr><td valign="top"><a href="only_alphabets_exact-match.md"><code>ONLY_ALPHABETS_EXACT</code></a></td><td valign="top">Ignores all numbers, then requires an exact letter match.</td><td valign="top">string</td><td valign="top">Building names where unit numbers<br>should be ignored</td><td valign="top">All</td><td>"Tower A, Flat 12" and "Tower A, Flat 7"<br>— "Tower A" matches exactly after<br>numbers are stripped.<br>"Tower A" and "Tower B" — no match.</td></tr><tr><td valign="top"><a href="only_alphabets_fuzzy-match.md"><code>ONLY_ALPHABETS_FUZZY</code></a></td><td valign="top">Ignores all numbers, then applies fuzzy matching to alphabetic characters only</td><td valign="top">string</td><td valign="top">Address fields where street names<br>need fuzzy matching and numbers<br>are handled separately via <code>NUMERIC</code></td><td valign="top">All</td><td>"St. James Rd" and "Saint James Road" —<br>alphabetic characters compared fuzzily.<br>Combine with <code>NUMERIC</code> for the<br>street number.</td></tr><tr><td valign="top"><a href="only_alphabets_fuzzy_optimised-match.md"><code>ONLY_ALPHABETS_FUZZY_OPTIMISED</code></a></td><td valign="top">Same as <code>ONLY_ALPHABETS_FUZZY</code> but optimised for production scale.</td><td valign="top">string</td><td valign="top">Same as <code>ONLY_ALPHABETS_FUZZY</code> -use when dataset is large</td><td valign="top">Enterprise only</td><td>Same matching behaviour.<br>Use for large address datasets.</td></tr><tr><td valign="top"><a href="null_or_blank-match.md"><code>NULL_OR_BLANK</code></a></td><td valign="top">By default Zingg treats nulls as<br>matches. Add this alongside another<br>match type to build an explicit feature for null/blank values so the model can learn their effect.</td><td valign="top">string, integer, long, date, boolean</td><td valign="top">Any field frequently null or blank<br>across source systems -combine<br>with <code>FUZZY</code> or <code>EXACT</code></td><td valign="top">All</td><td>Field A: null, Field B: "John" — with <code>NULL_OR_BLANK</code> added, these are NOT treated as matching. Without it, null matches anything.</td></tr><tr><td valign="top"><a href="dont_use-match.md"><code>DONT_USE</code></a></td><td valign="top">Appears in output but no computation<br>is done. Not shown in the labeller<br>when <code>showConcise</code> is <code>true</code>.</td><td valign="top">any</td><td valign="top">Record IDs, internal keys,<br>any field needed in output<br>but not in matching</td><td valign="top">All</td><td>A <code>customer_id</code> field that must appear<br>in output for traceability but should<br>not influence whether two records<br>are the same entity.</td></tr><tr><td valign="top"><a href="mapping_-filename-match.md"><code>MAPPING_(FILENAME)</code></a></td><td valign="top">Maps input field values to canonical<br>values using a user-supplied lookup file. Handles nicknames, abbreviations,<br>company name variants, and categorical<br>normalisation. Matching is tolerant<br>to common variations defined in the mapping file.</td><td valign="top">string</td><td valign="top">Name fields (nicknames), company<br>fields, categorical fields with<br>different representations across<br>systems</td><td valign="top">Enterprise only</td><td>"Jon", "Jonathan", "Johnny" all map<br>to canonical "John" via nicknames.json.<br>"M", "Male", "1" all map to "M"<br>via gender.json. Matched before<br>similarity scoring runs.</td></tr></tbody></table>

{% tabs %}
{% tab title="Python" %}
{% code overflow="wrap" %}
```python
from zingg.client import * 
from zingg.pipes import *

#Community
fname = FieldDefinition("fname", "string", MatchType.FUZZY)
lname = FieldDefinition("lname", "string", MatchType.FUZZY)

stNo = FieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = FieldDefinition("add1", "string", MatchType.FUZZY)

dob = FieldDefinition("dob", "string", MatchType.EXACT)
ssn = FieldDefinition("ssn", "string", MatchType.EXACT)

fieldDefs = [ fname, lname, stNo, add1, dob, ssn ]
args.setFieldDefinition(fieldDefs)
```
{% endcode %}
{% endtab %}

{% tab title="JSON" %}
```json
{
  "fieldDefinition" : [
    {
      "fieldName" : "fname",
      "matchType" : "fuzzy",
      "fields" : "fname",
      "dataType" : "string"
    },
    {
      "fieldName" : "lname",
      "matchType" : "fuzzy",
      "fields" : "lname",
      "dataType" : "string"
    },
    {
      "fieldName" : "stNo",
      "matchType" : "fuzzy",
      "fields" : "stNo",
      "dataType" : "string"
    },
    {
      "fieldName" : "add1",
      "matchType" : "fuzzy",
      "fields" : "add1",
      "dataType" : "string"
    },
    {
      "fieldName" : "dob",
      "matchType" : "exact",
      "fields" : "dob",
      "dataType" : "string"
    },
    {
      "fieldName" : "ssn",
      "matchType" : "exact",
      "fields" : "ssn",
      "dataType" : "string"
    }
  ]
}
```
{% endtab %}
{% endtabs %}

<details>

<summary><strong>Which match type should I use for each field type?</strong></summary>

A quick reference for the most common fields:

* **Name fields (first name, last name, company name)** → `FUZZY`. Handles spelling variations and abbreviations. Use MAPPING for known alias or nickname lists (Enterprise only).
* **Email address** → `EMAIL`. Matches before the `@` ; only avoids mismatches from different email domains for the same person.
* **Date of birth / registration date** → `EXACT`. Dates should not have fuzzy tolerance.
* **Postal / ZIP code** → `PINCODE`. Handles common format variants.
* **Street address (full address line)** → `FUZZY` or `ONLY_ALPHABETS_FUZZY` combined with NUMERIC for the street number as a separate field.
* **Street number / apartment number** → `NUMERIC`. Extracts and compares the number portion only.
* **Internal record ID (not used for matching)** → `DONT_USE`. Appears in output but excluded from comparison.
* **Any field frequently null across source systems** → Add `NULL_OR_BLANK` alongside the main match type.
* **Product descriptions / notes** → TEXT. `Word` overlap comparison for longer free-text fields.

</details>

{% hint style="success" icon="right-long" %}
**Read More:**

Configure Zingg (full field definition setup in notebook 01) | Match types reference (Section 7) | Concepts glossary
{% endhint %}
