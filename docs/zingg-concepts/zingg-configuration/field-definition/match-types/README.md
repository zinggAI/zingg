---
description: Abstraction for similarity algorithms in human understandable terms
---

# Match Type

Every field in your Zingg configuration gets a match type assigned to it. The match type tells Zingg which similarity function to apply when comparing values in that field across two records. Each `dataType` has predefined features for the given `matchType`. For example, the `string` type with `FUZZY` match is configured for Affine and Jaro-Winkler string comparison.

Choosing the right match type for each field is one of the highest-impact configuration decisions you make as it directly affects matching accuracy.

Multiple match types can be assigned to a single field, separated by commas.

| Match Type | What it does | Data types | Best for | Edition | Example |
|------------|--------------|------------|----------|---------|---------|
| [`FUZZY`](fuzzy-match.md) | Broad matching tolerant to typos, abbreviations and variations. Uses `fuzzy` string similarity features. | string, integer, long, double, date | Names, addresses, free-text fields | All | "Jon Smith", "Jonathan Smith", and "J. Smith" all match the same record. |
| [`FUZZY_OPTIMISED`](fuzzy_optimised-match.md) | Same accuracy as `FUZZY`, approximately 2X faster. Lower CPU and memory usage on large datasets. Use this in production when `FUZZY` accuracy is needed at scale. | string, integer, long, double, date | Same as `FUZZY` — use when dataset is large and performance matters | Enterprise only | Same matching behaviour as `FUZZY`. Processes a 10M-record dataset in approximately half the time. |
| [`EXACT`](exact-match.md) | Provides an exact-match signal to the classifier. A strong indicator when two values align perfectly. Records may still match based on other fields and how training pairs were labeled. | string, integer, long, date, boolean | Country codes, boolean flags, categorical fields where no variation is expected | All | "US" and "US" — strong match signal. "US" and "us" — no exact signal, but the record pair may still match if other fields score highly. |
| [`EMAIL`](email-match.md) | Matches only the local part before the `@` — ignores the domain entirely. | string | Email address fields | All | "john.smith@gmail.com" and "john.smith@company.com" — match. "john.smith@gmail.com" and "jsmith@gmail.com" — no match. |
| [`EMAIL_OPTIMISED`](email_optimised-match.md) | Same as `EMAIL` but significantly faster evaluation on large datasets. | string | Email fields at production scale | Enterprise only | Same behavior as `EMAIL`. Use when comparing millions of email addresses. |
| [`PINCODE`](pincode-match.md) | Matches postal and pin codes across common format variants (e.g. `xxxxx` and `xxxxx-xxxx`). | string | Postal/ZIP code fields | All | "94102" and "94102-1234" — match. "94102" and "94103" — no match. |
| [`NUMERIC`](numeric-match.md) | Extracts numbers from strings and compares how many are the same across both strings. | string | Street numbers, apartment numbers, building numbers | All | "42B Main St" and "42 Main Street" — the number 42 is extracted and matched from both. Alphabetic variation ignored. |
| [`NUMERIC_WITH_UNITS`](numeric_with_units-match.md) | Extracts product codes or numbers with units (e.g. 16gb, 500ml) and compares how many are the same. | string | Product specification fields, size fields | All | "16GB" and "16 GB DDR4" — the 16 and "GB" tokens match. "16gb" and "32gb" — the units match but the numbers do not. |
| [`TEXT`](text-match.md) | Compares overlapping words between two strings. Good for longer descriptive text without many typos. | string | Product descriptions, notes, long free-text fields | All | "Enterprise data management platform" and "data management platform for enterprise" — high overlap, likely match. "Enterprise software" and "consumer hardware" — low overlap, likely no match. |
| [`ONLY_ALPHABETS_EXACT`](only_alphabets_exact-match.md) | Ignores all numbers, then requires an exact letter match. | string | Building names where unit numbers should be ignored | All | "Tower A, Flat 12" and "Tower A, Flat 7" — "Tower A" matches exactly after numbers are stripped. "Tower A" and "Tower B" — no match. |
| [`ONLY_ALPHABETS_FUZZY`](only_alphabets_fuzzy-match.md) | Ignores all numbers, then applies fuzzy matching to alphabetic characters only. | string | Address fields where street names need fuzzy matching and numbers are handled separately via `NUMERIC` | All | "St. James Rd" and "Saint James Road" — alphabetic characters compared fuzzily. Combine with `NUMERIC` for the street number. |
| [`ONLY_ALPHABETS_FUZZY_OPTIMISED`](only_alphabets_fuzzy_optimised-match.md) | Same as `ONLY_ALPHABETS_FUZZY` but optimised for production scale. | string | Same as `ONLY_ALPHABETS_FUZZY` — use when dataset is large | Enterprise only | Same matching behaviour. Use for large address datasets. |
| [`NULL_OR_BLANK`](null_or_blank-match.md) | By default Zingg treats nulls as matches. Add this alongside another match type to build an explicit feature for null/blank values so the model can learn their effect. | string, integer, long, date, boolean | Any field frequently null or blank across source systems — combine with `FUZZY` or `EXACT` | All | Field A: null, Field B: "John" — with `NULL_OR_BLANK` added, these are NOT treated as matching. Without it, null matches anything. |
| [`DONT_USE`](dont_use-match.md) | Appears in output but no computation is done. Not shown in the labeller when `showConcise` is `true`. | any | Record IDs, internal keys, any field needed in output but not in matching | All | A `customer_id` field that must appear in output for traceability but should not influence whether two records are the same entity. |
| [`MAPPING_(FILENAME)`](mapping_-filename-match.md) | Maps input field values to canonical values using a user-supplied lookup file. Handles nicknames, abbreviations, company name variants, and categorical normalisation. Matching is tolerant to common variations defined in the mapping file. | string | Name fields (nicknames), company fields, categorical fields with different representations across systems | Enterprise only | "Jon", "Jonathan", "Johnny" all map to canonical "John" via nicknames.json. "M", "Male", "1" all map to "M" via gender.json. Matched before similarity scoring runs. |

{% hint style="success" icon="book-open" %}
**Read More:**

Configure Zingg (full field definition setup in notebook 01) | Match types reference (Section 7) | Concepts glossary
{% endhint %}
