---
layout: default
nav_order: 6
---
## Output Scores

For each field(FNAME, NAME, EMAIL..), Zingg computes multiple features and feeds them to a classifier. 
These features are typically different ways to compare strings. We consider string lengths and their differences, character differences and which characters actually differed. 
- The shorter string pair ABCD and ABCE will be less similar than ABCDEF and ABCEEF. 
- Common typos, for eg m instead of n are penalized less severely and will be scored higher than a word with a replaced by non obvious character b. 
- The more changes you need to make to the strings to make them match, the less similar they will be. 
- Differences in the middle are penalized more than prefixes and suffixes. 

No individual feature is perfect, but the whole is greater than the sum of its parts. That's where the strength of Zingg matching and the accuracy comes from.  

The classifier finds the best curve to represent the data(features) and gives a final score. This score is dependent on the individual features, but is not a linear function but a curve fit. The deterioration in similarity does happen as scores get lower, but we try very hard to find as many possible matches and hence you will continue to get good quality matches at lower scores. However, the number of good matches at lower scores will be lesser than those at higher scores.

The threshold is automatically chosen so that you can pick up most of the results, with a balance toward accuracy(what is a right match) and recall(not missing out on potential matches). Unlike traditional systems, we do not want the user to worry too much about the cut-off, We optimize the cutoff and hence you may see records with scores less than the the conventional 0.5. 

A few things to keep in mind while interpreting the scores
- Matching is transitive, so if record A matches Record B and Record B matches C, we put records A, B and C in the same cluster. That is how different records in a cluster get matched at various confidence levels to the rest and all these records showed up together. 
- Our recommendation is to keep a threshold below x% of the max score as suspect records - manual review/ to be visited later/flow through but will less confidence. 
x will depend on how accurate you find the results and how much you want to control the outcome. 
- Keep cluster size above 4 or 5 for inspection. You could keep it irrespective of the score, or look at only those clusters whose z_minScore is 0. This will depend on what the results look like to you.
