package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;

public class ShutdownThread extends Thread {

  private static Logger logger = LoggerFactory.getLogger(ShutdownThread.class);

  private ServiceBusProcessorClient processorClient;

  public ShutdownThread(ServiceBusProcessorClient processorClient) {
    this.processorClient = processorClient;
  }

  public void run() {
    logger.info("Closing Service Bus processor...");
    processorClient.close();
    logger.info("Finished closing Service Bus processor");
  }

}
