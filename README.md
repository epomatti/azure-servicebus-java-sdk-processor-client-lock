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

The log level can be set in the `logback.xml` file.

```none
18:21:10.143 [boundedElastic-2] INFO  c.a.m.s.i.ServiceBusReceiveLinkProcessor - {"az.sdk.message":"Adding credits.","prefetch":10,"requested":2,"linkCredits":0,"expectedTotalCredit":10,"queuedMessages":0,"creditsToAdd":10,"messageQueueSize":0}
18:21:10.145 [boundedElastic-2] INFO  com.example.App - Message payload received: Test message
18:21:10.516 [boundedElastic-2] INFO  c.a.m.s.i.ServiceBusReceiveLinkProcessor - {"az.sdk.message":"Adding credits.","prefetch":10,"requested":2,"linkCredits":0,"expectedTotalCredit":10,"queuedMessages":1,"creditsToAdd":9,"messageQueueSize":0}
18:21:10.516 [boundedElastic-2] INFO  c.a.m.s.i.ServiceBusReceiveLinkProcessor - {"az.sdk.message":"Adding credits.","prefetch":10,"requested":2,"linkCredits":0,"expectedTotalCredit":10,"queuedMessages":0,"creditsToAdd":10,"messageQueueSize":0}
18:21:10.518 [boundedElastic-2] INFO  com.example.App - Message payload received: Test message
18:21:56.509 [parallel-4] INFO  c.a.m.s.LockRenewalOperation - {"az.sdk.message":"Starting lock renewal.","isSession":false,"lockToken":"20b2e3b2-7d1b-46ad-a739-319a7b8b0051"}
18:21:56.511 [parallel-4] INFO  c.a.m.s.i.ServiceBusReactorAmqpConnection - {"az.sdk.message":"Creating management node.","linkName":"demoQueue-mgmt","entityPath":"demoQueue","address":"demoQueue/$management"}
18:21:56.643 [reactor-executor-1] INFO  c.a.c.a.i.handler.SessionHandler - {"az.sdk.message":"onSessionRemoteOpen","connectionId":"MF_72f731_1665436756481","sessionName":"demoQueue-mgmt-session","sessionIncCapacity":0,"sessionOutgoingWindow":2147483647}
18:21:56.644 [reactor-executor-1] INFO  c.a.c.a.i.ReactorConnection - {"az.sdk.message":"Emitting new response channel.","connectionId":"MF_72f731_1665436756481","entityPath":"demoQueue/$management","linkName":"demoQueue-mgmt"}
18:21:56.644 [reactor-executor-1] INFO  c.a.c.a.i.AmqpChannelProcessor - {"az.sdk.message":"Setting next AMQP channel.","connectionId":"MF_72f731_1665436756481","entityPath":"demoQueue/$management"}
18:21:56.646 [reactor-executor-1] INFO  c.a.c.a.i.ActiveClientTokenManager - {"az.sdk.message":"Scheduling refresh token task.","scopes":"amqp://bus-demo12345.servicebus.windows.net/demoQueue"}
18:21:56.772 [reactor-executor-1] INFO  c.a.c.a.i.handler.SendLinkHandler - {"az.sdk.message":"onLinkRemoteOpen","connectionId":"MF_72f731_1665436756481","linkName":"demoQueue-mgmt:sender","entityPath":"demoQueue/$management","remoteTarget":"Target{address='demoQueue/$management', durable=NONE, expiryPolicy=SESSION_END, timeout=0, dynamic=false, dynamicNodeProperties=null, capabilities=null}"}
18:21:56.772 [reactor-executor-1] INFO  c.a.c.a.i.AmqpChannelProcessor - {"az.sdk.message":"Channel is now active.","connectionId":"MF_72f731_1665436756481","entityPath":"demoQueue/$management"}
18:21:56.772 [reactor-executor-1] INFO  c.a.c.a.i.handler.ReceiveLinkHandler - {"az.sdk.message":"onLinkRemoteOpen","connectionId":"MF_72f731_1665436756481","entityPath":"demoQueue/$management","linkName":"demoQueue-mgmt:receiver","remoteSource":"Source{address='demoQueue/$management', durable=NONE, expiryPolicy=SESSION_END, timeout=0, dynamic=false, dynamicNodeProperties=null, distributionMode=null, filter=null, defaultOutcome=null, outcomes=null, capabilities=null}"}
18:21:57.337 [reactor-executor-1] WARN  c.a.m.s.i.ManagementChannel - {"az.sdk.message":"Operation not successful.","entityPath":"demoQueue","status":"GONE","description":"The lock supplied is invalid. Either the lock expired, or the message has already been removed from the queue. For more information please see https://aka.ms/ServiceBusExceptions . Reference:b5b66653-9cbe-4f34-8c6d-12ed014281cd, TrackingId:94cf0350-7a2e-40aa-9a42-4cff21187c15_B7, SystemTracker:bus-demo12345:queue:demoqueue~95, Timestamp:2022-10-10T21:21:58","condition":"com.microsoft:message-lock-lost"}
18:21:57.341 [reactor-executor-1] ERROR c.a.m.s.LockRenewalOperation - {"az.sdk.message":"Error occurred while renewing lock token.","exception":"The lock supplied is invalid. Either the lock expired, or the message has already been removed from the queue. For more information please see https://aka.ms/ServiceBusExceptions . Reference:b5b66653-9cbe-4f34-8c6d-12ed014281cd, TrackingId:94cf0350-7a2e-40aa-9a42-4cff21187c15_B7, SystemTracker:bus-demo12345:queue:demoqueue~95, Timestamp:2022-10-10T21:21:58, errorContext[NAMESPACE: bus-demo12345.servicebus.windows.net. ERROR CONTEXT: N/A, PATH: demoQueue/$management, REFERENCE_ID: demoQueue-mgmt:receiver, LINK_CREDIT: 0]","isSession":false,"lockToken":"20b2e3b2-7d1b-46ad-a739-319a7b8b0051"}
```

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
