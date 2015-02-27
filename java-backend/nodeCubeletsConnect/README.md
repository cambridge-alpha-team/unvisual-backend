To install, run `mvn install`. Note that the unit tests here are testing
a multi-threaded, timeout sensitive piece of code (the watchdog), so
under heavy workloads if the JVM does not get scheduled by the OS
(enough) then the unit test may fail. In this case you can do one of:
1. Skip unit tests with `mvn -DskipTests install`.
2. Increase scalingFactor at the top of
`src/test/java/com/github/cambridgeAlphaTeam/watchdog/WatchDogTest.java`
to have larger timeouts.
3. Try running unit tests when your system is less loaded.
