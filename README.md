# Service Bus prefetch lock sample

Isolated code sample to simulate the issue below where a processor client gets stuck when using `prefetch`:

- https://github.com/Azure/azure-sdk-for-java/issues/31356

Requirements:

- JDK 17
- Latest Maven (must be compatible with Java 17)
- Service Bus namespace with a Queue

Set an environment variable with your connection string:

```sh
export CONNECTION_STRING="Endpoint=sb://{NAMESPACE_NAME}.servicebus.windows.net/;SharedAccessKeyName={KEY_NAME};SharedAccessKey={ACCESS_KEY}="
```

```sh
mvn install
mvn exec:java
```

### Create Service Bus

Shorthand commands to create a SB namespace and get the root connection string:

```sh
location="eastus2"
group="rg-demo"
namespace="bus-<YOUR NAMESPACE NAME>"

az group create -n $group -l $location
az servicebus namespace create -n $namespace -g $group -l $location
az servicebus queue create -n "demoQueue" --namespace-name $namespace -g $group --enable-partitioning

az servicebus namespace authorization-rule keys list -g $group --namespace-name $namespace --name "RootManageSharedAccessKey" --query "primaryConnectionString" -o tsv
```
