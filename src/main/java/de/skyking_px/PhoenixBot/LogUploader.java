package de.skyking_px.PhoenixBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LogUploader extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        Map<File, String> fileMap = new HashMap<>();

        event.getMessage().getAttachments().stream()
                .filter(attachment -> attachment.getFileName().endsWith(".log") || attachment.getFileName().contains("crash"))
                .forEach(attachment -> {
                    File tempFile = new File("temp-" + System.currentTimeMillis() + ".log");
                    fileMap.put(tempFile, attachment.getFileName());

                    attachment.getProxy().download().thenAccept(inputStream -> {
                        try {
                            saveInputStreamToFile(inputStream, tempFile);

                            if (fileMap.size() == event.getMessage().getAttachments().size()) {
                                event.getChannel().sendTyping().queue();
                                CompletableFuture.runAsync(() -> uploadAndSendLinks(fileMap, event));
                            }

                        } catch (IOException e) {
                            event.getChannel().sendMessage("❌ Error saving file `" + attachment.getFileName() + "`").queue();
                            e.printStackTrace();
                        }
                    });
                });
    }

    private void saveInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private void uploadAndSendLinks(Map<File, String> fileMap, MessageReceivedEvent event) {
        List<String> uploadedUrls = new ArrayList<>();

        for (Map.Entry<File, String> entry : fileMap.entrySet()) {
            File tempFile = entry.getKey();
            String originalName = entry.getValue();

            try {
                String url = uploadToMclogs(tempFile);
                uploadedUrls.add("`" + originalName + "` → " + url);
                tempFile.delete();
            } catch (IOException e) {
                event.getChannel().sendMessage("❌ Error uploading file `" + originalName + "`").queue();
                e.printStackTrace();
            }
        }

        if (!uploadedUrls.isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("📄 Uploaded Log Files")
                    .setColor(0x00FF00)
                    .setTimestamp(Instant.now());

            for (String uploadedFile : uploadedUrls) {
                embed.addField("File", uploadedFile, false);
            }

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }

    private String uploadToMclogs(File file) throws IOException {
        StringBuilder logContent = new StringBuilder();

        // Read file safely
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logContent.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Error reading file";
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.mclo.gs/1/log");
            post.setEntity(MultipartEntityBuilder.create().addTextBody("content", logContent.toString()).build());

            try (CloseableHttpResponse response = client.execute(post)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = new String(entity.getContent().readAllBytes());
                    JsonNode jsonNode = new ObjectMapper().readTree(result);
                    return jsonNode.has("url") ? jsonNode.get("url").asText() : "Error uploading file";
                }
            }
        }
        return "Error uploading file";
    }
}
