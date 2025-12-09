# Azure Service Bus Dev Services Tutorial

This is a demonstration project for the Quarkus Azure Service Bus extension.
It showcases different ways to invoke the Dev Services propvided by this extension.

## Prerequisites

To successfully run this tutorial, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* A container runtime like Docker or Podman with the compose extension installed

You also need to clone the repository and switch to the directory of the tutorial.

```
git clone https://github.com/quarkiverse/quarkus-azure-services.git
cd quarkus-azure-services/integration-tests/azure-servicebus-devservices
```

### Use Development Iteration Version

By default, the tutorial is kept in sync with the development iteration version, which is `999-SNAPSHOT`.
To install the development iteration version, build it locally.
Run the following command in the root directory of the repository:

```
mvn clean install -DskipTests --file ../../pom.xml
```

### Use Release Version

If you want to use the release version, you need to update the version of dependencies in the `pom.xml` file.

First, you need to find out the latest release version of the Quarkus Azure services extensions from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.2.1`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml
<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.2.1</version>
    <relativePath></relativePath>
</parent>
```

## Run the Application

The application does not have a connection to an Azure Service Bus configured.
Neither `quarkus.azure.servicebus.connection-string` nor `quarkus.azure.servicebus.namespace` is configured.
This causes the Azure Service Bus Dev Services to launch an Azure Service Bus emulator in dev and test mode.

Run the application in dev mode with:

```
mvn quarkus:dev
```

On their first invocation, the Dev Services will download the Azure Service Bus emulator container and a MSSQL database and start them.
This may take a few minutes.
Watch the console logs to see the progress.
The following launches will be faster but still require about 30 seconds.

Once the application is started, test it by sending requests to it via `curl`:

```shell
# Send message "Hello Azure Service Bus!" to the service, which will send it to and receive it from the Azure Service Bus emulator.
curl http://localhost:8080/quarkus-azure-servicebus/messages -X POST -d '{"message": "Hello Azure Service Bus!"}' -H "Content-Type: application/json"

# Retrieve the cached message that wass received from Azure Service Bus. You should see {"messages":["Hello Azure Service Bus!"],"count":1,"status":"success"} in the response.
curl http://localhost:8080/quarkus-azure-servicebus/messages
```

### Run the Tests

While the application is running, run the tests in another terminal:

```shell
mvn test
```

This will launch a second instance of the Azure Service Bus emulator for the duration of the testsuite.

### Share the Dev Services

The Azure Service Bus Dev Services can be shared between multiple Quarkus applications.
This feature is especially useful when developing multiple Quarkus applications where one acts as
a message producer and the other as a consumer.

While the first instance of the application is running,
open a new terminal and run the following command to start a second instance of the application
on a different port:

```
mvn quarkus:dev -Dquarkus.http.port=8088
```

Sharing can be disabled by setting `quarkus.azure.servicebus.devservices.shared=false` in `application.properties`.

## Compose Support

The Azure Service Bus Dev Services also support the [Compose Dev Services](https://quarkus.io/guides/compose-dev-services).

Instead of using the default Azure Service Bus emulator provided by the Azure Service Bus Dev Services,
you can define a custom service stack in a compose file and let the Dev Services runtime manage it for you.

The tutorial application ships with the compose file `compose-devservices.yaml`.
A compose file with this name is automatically picked up by the Quarkus Compose Dev Services.
For demonstration purposes, we deactivated compose support.
To activate it, edit `src/main/resources/application.properties`.

Change the line

```properties
quarkus.compose.devservices.enabled=false
```
to

```properties
#quarkus.compose.devservices.enabled=false
```

Then run the application and its test again as described above.
Watch the console logs for a line containing _Compose Dev Service container found_.

### Pick Up Existing Containers

To speed up application startup in `dev` mode, you can manually manage the lifecycle of the
Dev Service stack.

Make sure compose support is not deactivated in `application.properties`.
Then, manually launch the compose stack with

```shell
docker compose -f compose-devservices.yaml up
```

Wait for the line _Emulator Service is Successfully Up!_ to show up in the console logs.

Then launch the sample application again with `mvn quarkus:dev`.
The application should now connect to the existing containers instead of launching new ones.

> This only works for dev mode.
> In test mode, the Azure Service Bus Dev Services will always launch new containers.
