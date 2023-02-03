# Quarkus Azure Services - PlayBack Tests

The Azure SDK uses a mechanism called "PlayBack Testing".
Basically it records the network calls when invoking the Azure services, stores the requests/responses in JSon, and then runs the tests against these JSon files.

## Running the test in Record Mode

When running the test in record mode, you need to create the Azure services that the test needs, execute the tests in `RECORD` mode and store the JSON files (set the `AZURE_LOG_LEVEL` variable if you want to see the logs of the Azure SDK).

```
mvn -DAZURE_TEST_MODE=RECORD -DAZURE_LOG_LEVEL=debug clean test 
```

This will generate the JSON files in the `target/test-classes` directory. You need to copy these files to the `src/test/resources` directory to then run the test in `PLAYBACK` mode.

## Running the test in PlayBack Mode

By default, when executing a `mvn test` the tests are executed in `PLAYBACK` mode.
If you want to run the tests in `PLAYBACK` mode explicitly, you can do it with the following command:

```
mvn -DAZURE_TEST_MODE=PLAYBACK clean test
```
