# Ignite 3 tables amount capacity test
How to run:
1. Manually start 2 nodes.
2. Manually init cluster.
3. In file `build.gradle` change field `ext.ignite3Version` to required one (if you publish your ignite3 version into local maven repository you can use it).
4. In file `src/main/java/lunigorn/ignite3test/Test.java` set correct value into field `DB_URL`.
5. Assemble distributive via command `./gradlew clean assembleDist`.
6. Change directory to `build/distributions`.
7. Untar archive `tar -xvf ignite3test.tar`.
8. Change directory to `build/distributions/ignite3test`.
9. Run the executable `bash ./bin/ignite3test > test.log`
10. Enjoy!
