package de.skyking_px.PhoenixBot.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON-based storage system for managing support ticket data.
 * Handles ticket creation, tracking, response status, and automatic cleanup.
 * Thread-safe implementation using synchronized methods.
 * 
 * @author SkyKing_PX
 */
public class TicketStorage {

    /** JSON file for storing ticket data */
    private final File file = new File("tickets.json");
    /** Jackson ObjectMapper for JSON operations */
    private final ObjectMapper mapper = new ObjectMapper();
    /** Root JSON node containing all ticket data */
    private ObjectNode root;

    /**
     * Initializes the ticket storage system.
     * Creates a new storage file with proper structure if one doesn't exist,
     * otherwise loads existing data and ensures backward compatibility.
     * 
     * @throws IOException If there is an error reading or creating the storage file
     */
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

    /**
     * Generates a unique ticket name using username and global counter.
     * Sanitizes username by removing special characters and limiting length.
     * 
     * @param username Discord username to include in ticket name
     * @return Generated ticket name in format "ticket-{username}-{counter}"
     * @throws IOException If there is an error saving the updated counter
     */
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

    /**
     * Saves a new ticket to storage with all metadata.
     * 
     * @param threadId Discord thread ID for the ticket
     * @param ticketName Generated name for the ticket
     * @param userId Discord user ID who created the ticket
     * @param guildId Discord guild ID where the ticket was created
     * @param creationTime Timestamp when the ticket was created
     * @throws IOException If there is an error saving to the storage file
     */
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

    /**
     * Marks a ticket as responded to by the user.
     * This prevents automatic deletion and enables moderation.
     * 
     * @param threadId Discord thread ID of the ticket
     * @throws IOException If there is an error saving to the storage file
     */
    public synchronized void markTicketResponded(String threadId) throws IOException {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        ObjectNode ticketNode = (ObjectNode) tickets.get(threadId);
        if (ticketNode != null) {
            ticketNode.put("responded", true);
            save();
        }
    }

    /**
     * Removes a ticket from storage completely.
     * Used when tickets are closed or automatically cleaned up.
     * 
     * @param threadId Discord thread ID of the ticket to remove
     * @throws IOException If there is an error saving to the storage file
     */
    public synchronized void removeTicket(String threadId) throws IOException {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        if (tickets.has(threadId)) {
            tickets.remove(threadId);
            save();
        }
    }

    /**
     * Checks if a ticket is pending (not yet responded to by the user).
     * 
     * @param threadId Discord thread ID to check
     * @return true if ticket exists and has not been responded to, false otherwise
     */
    public synchronized boolean isTicketPending(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        if (ticketNode == null) return false;
        return !ticketNode.path("responded").asBoolean(true);
    }

    /**
     * Retrieves the ticket name for a specific thread.
     * 
     * @param threadId Discord thread ID to look up
     * @return Ticket name or null if not found
     */
    public synchronized String getTicketName(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        return ticketNode != null ? ticketNode.path("ticketName").asText(null) : null;
    }

    /**
     * Retrieves the user ID who created a specific ticket.
     * 
     * @param threadId Discord thread ID to look up
     * @return Discord user ID or null if not found
     */
    public synchronized String getTicketUserId(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        return ticketNode != null ? ticketNode.path("userId").asText(null) : null;
    }

    /**
     * Retrieves the guild ID where a specific ticket was created.
     * 
     * @param threadId Discord thread ID to look up
     * @return Discord guild ID or null if not found
     */
    public synchronized String getTicketGuildId(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        return ticketNode != null ? ticketNode.path("guildId").asText(null) : null;
    }

    /**
     * Retrieves the creation timestamp for a specific ticket.
     * 
     * @param threadId Discord thread ID to look up
     * @return Creation timestamp in milliseconds or 0 if not found
     */
    public synchronized long getTicketCreationTime(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        JsonNode ticketNode = tickets.get(threadId);
        return ticketNode != null ? ticketNode.path("creationTime").asLong(0) : 0;
    }

    /**
     * Checks if a thread ID corresponds to a known ticket.
     * 
     * @param threadId Discord thread ID to check
     * @return true if this is a ticket thread, false otherwise
     */
    public synchronized boolean isTicketThread(String threadId) {
        ObjectNode tickets = (ObjectNode) root.get("tickets");
        return tickets.has(threadId);
    }

    /**
     * Gets the current global ticket counter.
     * 
     * @return Current ticket count used for generating unique names
     */
    public synchronized int getGlobalTicketCount() {
        return root.path("globalTicketCount").asInt(0);
    }

    /**
     * Loads all pending (unanswered) tickets from storage.
     * Used for restoring automatic deletion timers on bot restart.
     * 
     * @return Map of thread IDs to TicketInfo objects for pending tickets
     */
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

    /**
     * Saves the current ticket data to the JSON file.
     * 
     * @throws IOException If there is an error writing to the storage file
     */
    private void save() throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
    }

    /**
     * Immutable data class holding ticket information.
     * Used for transferring ticket data between storage operations.
     */
    public static class TicketInfo {
        /** Name of the ticket */
        public final String ticketName;
        /** Discord user ID who created the ticket */
        public final String userId;
        /** Discord guild ID where ticket was created */
        public final String guildId;
        /** Timestamp when ticket was created */
        public final long creationTime;

        /**
         * Creates a new TicketInfo instance.
         * 
         * @param ticketName Name of the ticket
         * @param userId Discord user ID of the ticket creator
         * @param guildId Discord guild ID where ticket was created
         * @param creationTime Timestamp when ticket was created
         */
        public TicketInfo(String ticketName, String userId, String guildId, long creationTime) {
            this.ticketName = ticketName;
            this.userId = userId;
            this.guildId = guildId;
            this.creationTime = creationTime;
        }
    }
}