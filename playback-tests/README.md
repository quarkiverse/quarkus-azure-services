# Quarkus Azure Services - PlayBack Tests

The Azure SDK uses a mechanism called "PlayBack Testing".
Basically it records the network calls when invoking the Azure services, stores the requests/responses in JSon, and then runs the tests against these JSon files.

## Running the test in Record Mode

When running the test in record mode, you need to create the Azure services that the test needs, execute the tests in `RECORD` mode and store the JSON files.

```
AZURE_TEST_MODE=RECORD 

mvn test
```

## Running the test in PlayBack Mode

```
AZURE_TEST_MODE=PLAYBACK 

mvn test
```
