# Ignite 3 rows amount capacity test
## The algorithm:
1. Create JDBC connection to the cluster via `DB_URL` (default `jdbc:ignite:thin://127.0.1.1:10800`).
2. Create the table for next insertions.
3. Create prepared statement for insert row.
4. Add `BATCH_SIZE` (default 1_000) insert batches to prepared statement.
5. Execute added batches.
6. Assert amount of inserted rows equals to expected.
7. If step 6 throw exception from Ignite repeat step 6 `RETRIES_COUNT` times (default 3) with sleep `RETRIES_SLEEP` (default 100) milliseconds between retries.
8. Sleep `SLEEP` (default 30) milliseconds.
9. Repeat steps 4-8 until total rows amount reaches `ROWS_COUNT` (default 1_000_000).

## How to run:
1. Manually start 2 nodes.
2. Manually init cluster.
3. In file `build.gradle` change field `ext.ignite3Version` to required one (if you publish your ignite3 version into local maven repository you can use it).
4. In file `src/main/java/lunigorn/ignite3test/Test.java` set correct value into field `DB_URL`.
5. Assemble distributive via command `./gradlew clean assembleDist`.
6. Change directory to `build/distributions`.
7. Untar archive `tar -xf ignite3test.tar`.
8. Change directory to `build/distributions/ignite3test`.
9. Run the executable `bash ./bin/ignite3test > test.log`
10. Enjoy!
