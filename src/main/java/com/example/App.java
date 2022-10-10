package com.example;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Starting the client...");

        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                var payload = messageContext.getMessage().getBody().toString();
                logger.info(String.format("Message payload received: %s", payload));
                messageContext.complete();
            } catch (Exception ex) {
                messageContext.abandon();
            }
        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            errorContext.getException().printStackTrace();
        };

        String connectionString = System.getenv("CONNECTION_STRING");
        String queueName = System.getenv("QUEUE_NAME");

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .prefetchCount(100)
                .maxConcurrentCalls(100)
                .queueName(queueName)
                .processMessage(processMessage)
                .processError(processError)
                .disableAutoComplete()
                .buildProcessorClient();

        processorClient.start();

        logger.info("Service Bus client started.");

        // Adds a hook to close the Service Bus processor on shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(processorClient));
    }
}
