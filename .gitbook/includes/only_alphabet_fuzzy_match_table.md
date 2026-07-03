---
title: only_alphabet_fuzzy_match_table
---

| Value A          | Value B             | Match?                                                                                                                                      |
| ---------------- | ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| 42 St James Road | 44 Saint James Road | <p>Yes - strips to "St James Road" vs<br>"Saint James Road," fuzzy match on<br>alphabetic parts (numbers stripped<br>before comparison)</p> |
| 123 Main Street  | 123 Main St         | <p>Yes - "Main Street" vs "Main St",<br>abbreviation handled by fuzzy</p>                                                                   |
| 42 Oak Avenue    | 42 Elm Avenue       | <p>No - "Oak "Avenue" vs "Elm Avenue",<br>alphabetic portions too different</p>                                                             |
| Flat 3B, Tower A | Flat 7C, Tower A    | <p>Yes - "Flat B Tower A" vs<br>"Flat C Tower A"...<br>Confirm with team -<br>alphabetic letters in unit codes</p>                          |
