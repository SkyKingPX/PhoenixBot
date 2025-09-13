package de.skyking_px.PhoenixBot.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TicketStorage {

    private final File file = new File("tickets.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private ObjectNode root;

    public TicketStorage() throws IOException {
        if (!file.exists()) {
            root = mapper.createObjectNode();
            root.put("globalTicketCount", 0);
            root.set("tickets", mapper.createObjectNode());
            save();
        } else {
            root = (ObjectNode) mapper.readTree(file);
            // Ensure structure exists for older versions
            if (!root.has("globalTicketCount")) {
                root.put("globalTicketCount", 0);
            }
            if (!root.has("tickets")) {
                root.set("tickets", mapper.createObjectNode());
            }
        }
    }

    public synchronized String generateTicketName(String username) throws IOException {
        int currentCount = root.path("globalTicketCount").asInt(0);
        int newCount = currentCount + 1;
        root.put("globalTicketCount", newCount);
        save();
        
        // Clean username to be thread-safe (remove spaces, special chars)
        String cleanUsername = username.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (cleanUsername.length() > 20) {
            cleanUsername = cleanUsername.substring(0, 20);
        }
        
        return "ticket-" + cleanUsername + "-" + newCount;
    }

    public synchronized void saveTicket(String threadId, String ticketName, String userId, String guildId, long creationTime) throws IOException {
        ObjectNode ticketNode = mapper.createObjectNode();
        ticketNode.put("ticketName", ticketName);
        ticketNode.put("userId", userId);
        ticketNode.put("guildId", guildId);
        ticketNode.put("creationTime", creationTime);
        ticketNode.put("responded", false);

        ObjectNode tickets = (ObjectNode) root.get("tickets");
        tickets.set(threadId, ticketNode);
        save();
    }

    public synchronized void markTicketResponded(String threadId) throws IOException {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        ObjectNode ticketNode = (ObjectNode) tickets.get(threadId);
        if (ticketNode != null) {
            ticketNode.put("responded", true);
            save();
        }
    }

    public synchronized void removeTicket(String threadId) throws IOException {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        if (tickets.has(threadId)) {
            tickets.remove(threadId);
            save();
        }
    }

    public synchronized boolean isTicketPending(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        if (ticketNode == null) return false;
        return !ticketNode.path("responded").asBoolean(true);
    }

    public synchronized String getTicketName(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        return ticketNode != null ? ticketNode.path("ticketName").asText(null) : null;
    }

    public synchronized String getTicketUserId(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        return ticketNode != null ? ticketNode.path("userId").asText(null) : null;
    }

    public synchronized String getTicketGuildId(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        return ticketNode != null ? ticketNode.path("guildId").asText(null) : null;
    }

    public synchronized long getTicketCreationTime(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        return ticketNode != null ? ticketNode.path("creationTime").asLong(0) : 0;
    }

    public synchronized boolean isTicketThread(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        return tickets.has(threadId);
    }

    public synchronized int getGlobalTicketCount() {
        return root.path("globalTicketCount").asInt(0);
    }

    public synchronized Map<String, TicketInfo> loadAllPendingTickets() {
        Map<String, TicketInfo> result = new HashMap<>();
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        
        tickets.fields().forEachRemaining(entry -> {
            JsonNode node = entry.getValue();
            boolean responded = node.path("responded").asBoolean(true);
            if (!responded) {
                String ticketName = node.path("ticketName").asText("Unknown");
                String userId = node.path("userId").asText();
                String guildId = node.path("guildId").asText();
                long creationTime = node.path("creationTime").asLong(0);
                result.put(entry.getKey(), new TicketInfo(ticketName, userId, guildId, creationTime));
            }
        });
        return result;
    }

    private void save() throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
    }

    public static class TicketInfo {
        public final String ticketName;
        public final String userId;
        public final String guildId;
        public final long creationTime;

        public TicketInfo(String ticketName, String userId, String guildId, long creationTime) {
            this.ticketName = ticketName;
            this.userId = userId;
            this.guildId = guildId;
            this.creationTime = creationTime;
        }
    }
}