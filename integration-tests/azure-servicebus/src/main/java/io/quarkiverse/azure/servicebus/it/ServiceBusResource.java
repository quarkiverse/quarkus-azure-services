package io.quarkiverse.azure.servicebus.it;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

@Path("/quarkus-azure-servicebus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceBusResource {

    private static final Logger LOG = Logger.getLogger(ServiceBusResource.class);

    @Inject
    ServiceBusManager serviceBusManager;

    @POST
    @Path("/messages")
    public Response sendMessage(Map<String, String> request) {
        String message = request.get("message");
        LOG.infof("REST request to send message: %s", message);
        serviceBusManager.sendMessage(message);

        return Response.ok(Map.of(
                "status", "success",
                "message", "Message sent successfully")).build();
    }

    @GET
    @Path("/messages")
    public Response getMessages() {
        List<String> messages = serviceBusManager.getReceivedMessages();
        LOG.infof("REST request to get messages, returning %d messages", messages.size());

        return Response.ok(Map.of(
                "status", "success",
                "count", messages.size(),
                "messages", messages)).build();
    }

    @GET
    @Path("/messages/count")
    public Response getMessageCount() {
        int count = serviceBusManager.getReceivedMessageCount();
        LOG.infof("REST request to get message count: %d", count);

        return Response.ok(Map.of(
                "status", "success",
                "count", count)).build();
    }

    @DELETE
    @Path("/messages")
    public Response clearMessages() {
        LOG.info("REST request to clear all messages");
        serviceBusManager.clearReceivedMessages();

        return Response.ok(Map.of(
                "status", "success",
                "message", "All messages cleared successfully")).build();
    }
}
