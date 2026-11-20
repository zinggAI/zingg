## Design Critique

**Overview:** This class orchestrates Zingg's human-in-the-loop CLI labeling pipeline. While the high-level flow covers reading inputs, preprocessing, interactive human labeling, post-processing, and output persistence, the original design mixes data retrieval, state mutation, and CLI I/O inside broad methods.

### Design Flaws

1. `Labeller.getUnmarkedRecords()` duplicated `ZinggBase` logic while adding a marked-records stat update, violating SRP. This mixes data retrieval with state updates and gives a `get*` method an unexpected side effect.
2. `QUIT_LABELING` handling occurred after state updates; The exit condition should be checked before modifying records or stats.
3. Multiple `Scanner(System.in)` instances were created during CLI prompts. Also, `currentPair` lookup and handling were spread across the labeling loop.
4. `processRecordsCli(...)` carried too many responsibilities in one large method, making it harder to read, test, and maintain.
5. Assumed only two records exist per cluster ID without explicit validation or guardrails.
6. Did not bound or paginate how many candidate pairs are shown in a single CLI session, relying entirely on upstream batch sizing.
7. The catch blocks handled general `Exception` before `ZinggClientException`, making the specific exception handler unreachable.
8. The result of `cache()` was assigned back to the same variable, making it less clear that caching returns a `ZFrame` reference that should be retained.
9. `readCliInput()` risked an infinite or unbounded loop.

## How We Improved It

1. **Separated state updates from data retrieval:** Moved marked-records stat updates into `execute()` when `markedRecords != null`, and removed the redundant `getUnmarkedRecords()` override so `ZinggBase` handles retrieval without side effects in a `get*` method.
2. **Checked `QUIT_LABELING` early:** Moved the `selectedOption == QUIT_LABELING` check and `break` before updating labeller stats, updating records, or printing stats.
3. **Reused a `transient` `Scanner`:** Added a lazy-initialized `transient Scanner inputScanner` via `getInputScanner()` instead of creating a new `Scanner(System.in)` on every prompt.
4. **Broke `processRecordsCli(...)` into smaller helpers:** Extracted `labelSinglePair(...)`, `validatePairRowCount(...)`, and `printCurrentStats()` so loop orchestration, display/input, validation, and stat printing are separated.
5. **Added pair validation:** Introduced `validatePairRowCount()` to check whether currentPair is null or contains exactly two records for a cluster ID, and log a warning when the expected structure is not found.
6. **Made cache handling clearer:** Stored the result of `lines.cache()` in a separate `cachedLines` variable to make the cached reference explicit.
7. **Added input limits:** Updated `readCliInput()` with a `hasNext()` check and a `MAX_INVALID_ATTEMPTS` `limit`. It now throws `ZinggClientException` when the input stream is closed or the maximum number of invalid attempts is reached.