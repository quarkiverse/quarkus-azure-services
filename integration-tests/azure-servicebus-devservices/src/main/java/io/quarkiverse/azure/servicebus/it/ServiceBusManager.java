package io.quarkiverse.azure.servicebus.it;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

@ApplicationScoped
public class ServiceBusManager {

    private static final Logger LOG = Logger.getLogger(ServiceBusManager.class);

    @ConfigProperty(name = "quarkus.azure.servicebus.queue-name", defaultValue = "test-queue")
    private String queueName;

    @Inject
    private ServiceBusClientBuilder clientBuilder;

    private ServiceBusSenderClient senderClient;
    private ServiceBusProcessorClient processorClient;

    // Thread-safe list to store received messages for testing
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();

    @PostConstruct
    void initialize() {
        LOG.info("Initializing Azure Service Bus clients");

        // Initialize sender client
        senderClient = clientBuilder
                .sender()
                .queueName(queueName)
                .buildClient();

        // Initialize processor client with message handler
        processorClient = clientBuilder
                .processor()
                .queueName(queueName)
                .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                .processMessage(context -> {
                    String body = context.getMessage().getBody().toString();
                    receivedMessages.add(body);
                    LOG.infof("Received message: %s", body);
                })
                .processError(context -> {
                    LOG.errorf("Error occurred: %s", context.getException());
                })
                .disableAutoComplete()
                .buildProcessorClient();

        // Start processing messages
        processorClient.start();

        LOG.info("Azure Service Bus clients initialized successfully");
    }

    @PreDestroy
    void cleanup() {
        LOG.info("Cleaning up Azure Service Bus clients");

        if (processorClient != null) {
            processorClient.close();
        }

        if (senderClient != null) {
            senderClient.close();
        }

        LOG.info("Azure Service Bus clients cleaned up successfully");
    }

    public void sendMessage(String messageBody) {
        LOG.infof("Sending message: %s", messageBody);

        ServiceBusMessage message = new ServiceBusMessage(messageBody);
        senderClient.sendMessage(message);

        LOG.info("Message sent successfully");
    }

    public int getReceivedMessageCount() {
        return receivedMessages.size();
    }

    public List<String> getReceivedMessages() {
        return List.copyOf(receivedMessages);
    }

    public void clearReceivedMessages() {
        receivedMessages.clear();
        LOG.info("Cleared all received messages");
    }
}
