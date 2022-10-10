# Service Bus prefetch lock sample

Isolated code sample to simulate the issue below where a processor client gets stuck when using `prefetch`:

- https://github.com/Azure/azure-sdk-for-java/issues/31356

## Running the code

Requirements:

- JDK 17
- Latest Maven
- Service Bus namespace with a queue

Set an environment variable with your connection string:

```sh
export CONNECTION_STRING="Endpoint=sb://{NAMESPACE_NAME}.servicebus.windows.net/;SharedAccessKeyName={KEY_NAME};SharedAccessKey={ACCESS_KEY}="
export QUEUE_NAME="demoQueue"
```

Start the app by running Maven or on your favorite IDE:

```sh
mvn install
mvn exec:java
```

To test it, add 1,000 messages to the queue using the Service Bus web explorer. When the value set on `.prefetchCount(int:)` is set, the service bus processor get's stuck at some point when receiving messages. Restarting solves the issue temporarily until the queue locks again.

Several errors will be thrown by the processor, such as these:

```none
WARN  c.a.m.s.i.ManagementChannel - {"az.sdk.message":"Operation not successful.","entityPath":"demoQueue","status":"GONE","description":"The lock supplied is invalid. Either the lock expired, or the message has already been removed from the queue. ....

Caused by: java.lang.IllegalStateException: Cannot perform operations on a disposed receiver.

ERROR c.a.m.s.LockRenewalOperation - {"az.sdk.message":"Error occurred while renewing lock token.","exception":"The lock supplied is invalid. Either the lock expired, or the message has already been removed from the queue. For more information please see https://aka.ms/ServiceBusExceptions ....

18:26:18.845 [reactor-executor-1] ERROR reactor.core.publisher.Operators - Operator called default onErrorDropped
``

The log level can be set in the `logback.xml` file.

## Environment

Here are my specific versions when running my local tests:

```sh
$ java --version
openjdk 17.0.4 2022-07-19
OpenJDK Runtime Environment (build 17.0.4+8-Ubuntu-122.04)
OpenJDK 64-Bit Server VM (build 17.0.4+8-Ubuntu-122.04, mixed mode, sharing)

$ mvn --version
Apache Maven 3.8.6 (84538c9988a25aec085021c365c560670ad80f63)
Maven home: /usr/share/maven
Java version: 17.0.4, vendor: Private Build, runtime: /usr/lib/jvm/java-17-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.10.102.1-microsoft-standard-wsl2", arch: "amd64", family: "unix"

$ lsb_release -a
No LSB modules are available.
Distributor ID: Ubuntu
Description:    Ubuntu 22.04.1 LTS
Release:        22.04
Codename:       jammy
```

## Code snippet: create a Service Bus instance

Shorthand commands to create a Service Bus namespace and get the root connection string:

```sh
location="eastus2"
group="rg-demo"
namespace="bus-<YOUR NAMESPACE NAME>"

az group create -n $group -l $location
az servicebus namespace create -n $namespace -g $group -l $location
az servicebus queue create -n "demoQueue" --namespace-name $namespace -g $group --enable-partitioning

az servicebus namespace authorization-rule keys list -g $group --namespace-name $namespace --name "RootManageSharedAccessKey" --query "primaryConnectionString" -o tsv
```
