---
title: exact-match-behavior-table
---

| Value A                         | Value B                | Match                                                              |
| ------------------------------- | ---------------------- | ------------------------------------------------------------------ |
| john.smith@gmail.com            | john.smith@company.com | <p>Yes - same local part,<br>different domain (domain ignored)</p> |
| john.smith@gmail.com            | john.smith@outlook.com | Yes - same local part                                              |
| jsmith@gmail.com                | john.smith@gmail.com   | No - different local parts                                         |
| <p>john.smith@gmail.com<br></p> | JOHN.SMITH@gmail.com   | Confirm with team - case sensitivity of local part                 |
| johnsmith@gmail.com             | john.smith@gmail.com   | Confirm with team - dot handling in local part                     |
| not-an-email                    | john.smith@gmail.com   | Confirm with team - no `@` present in Value A                      |
