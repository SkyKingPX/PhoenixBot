package de.skyking_px.PhoenixBot.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VoteStorage {

    private final File file = new File("votes.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private ObjectNode root;

    public VoteStorage() throws IOException {
        if (!file.exists()) {
            root = mapper.createObjectNode();
            save();
        } else {
            root = (ObjectNode) mapper.readTree(file);
        }
    }

    public synchronized String getUserVote(String messageId, String userId) {
        JsonNode msg = root.path(messageId).path("voters");
        return msg.path(userId).asText(null); // "up", "down" or null
    }

    public synchronized void saveUserVote(String messageId, String userId, String voteType) throws IOException {
        ObjectNode msgNode = (ObjectNode) root.get(messageId);
        if (msgNode == null) {
            msgNode = mapper.createObjectNode();
            msgNode.put("up", 0);
            msgNode.put("down", 0);
            msgNode.set("voters", mapper.createObjectNode());
            root.set(messageId, msgNode);
        }

        ObjectNode voters = (ObjectNode) msgNode.with("voters");
        voters.put(userId, voteType);
        save();
    }

    public synchronized void setVoteCount(String messageId, int up, int down) throws IOException {
        ObjectNode msgNode = (ObjectNode) root.get(messageId);
        if (msgNode == null) {
            msgNode = mapper.createObjectNode();
            root.set(messageId, msgNode);
        }
        msgNode.put("up", up);
        msgNode.put("down", down);
        save();
    }


    public synchronized void saveVote(String messageId, int upvotes, int downvotes) throws IOException {
        ObjectNode voteData = mapper.createObjectNode();
        voteData.put("up", upvotes);
        voteData.put("down", downvotes);
        root.set(messageId, voteData);
        save();
    }

    public synchronized Map<String, int[]> loadAllVotes() {
        Map<String, int[]> result = new HashMap<>();
        root.fields().forEachRemaining(entry -> {
            JsonNode node = entry.getValue();
            int up = node.path("up").asInt(0);
            int down = node.path("down").asInt(0);
            result.put(entry.getKey(), new int[]{up, down});
        });
        return result;
    }

    private void save() throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
    }
}
