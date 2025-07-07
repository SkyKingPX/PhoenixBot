package de.skyking_px.PhoenixBot.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LogUploader extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // Collect all attachments we want to handle
        Map<File, String> filesToUpload = new HashMap<>();
        event.getMessage().getAttachments().stream()
                .filter(att -> att.getFileName().endsWith(".log") || att.getFileName().toLowerCase().contains("crash"))
                .forEach(att -> {
                    try {
                        File tmp = File.createTempFile("phoenix-log", ".tmp");
                        att.getProxy().downloadToFile(tmp).join();
                        filesToUpload.put(tmp, att.getFileName());
                    } catch (IOException e) {
                        event.getChannel().sendMessage("❌ Couldn't save " + att.getFileName() + ".").queue();
                    }
                });

        if (filesToUpload.isEmpty()) return;

        // 1) send placeholder message
        EmbedBuilder loading = new EmbedBuilder()
                .setTitle("⏳ Uploading Logs …")
                .setDescription("Please be patient.")
                .setColor(HexFormat.fromHexDigits("2073cb"))
                .setTimestamp(Instant.now());

        event.getChannel().sendMessageEmbeds(loading.build()).queue(placeholder ->
                // 2) run IO heavy work asynchronously so we don't block the gateway thread
                CompletableFuture.supplyAsync(() -> uploadAll(filesToUpload))
                        .thenAccept(result -> editSuccess(placeholder, result, filesToUpload))
                        .exceptionally(ex -> { // any unhandled exception ends up here
                            editFailure(placeholder, ex);
                            return null;
                        }));
    }

    private List<String> uploadAll(Map<File, String> files) {
        List<String> urls = new ArrayList<>();
        for (Map.Entry<File, String> entry : files.entrySet()) {
            File tmp = entry.getKey();
            String original = entry.getValue();

            try {
                String url = uploadToMclogs(tmp);
                urls.add("`" + original + "` → " + url);
            } catch (IOException ex) {
                throw new RuntimeException("Error while uploading " + original, ex);
            } finally {
                // We are done with the temp file either way
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            }
        }
        return urls;
    }

    // ────────────────────────────────────────────────────────────────────────────────
    //  Message editing helpers
    // ────────────────────────────────────────────────────────────────────────────────

    private void editSuccess(Message placeholder, List<String> uploaded, Map<File, String> files) {
        EmbedBuilder success = new EmbedBuilder()
                .setTitle("📄 Log-Files uploaded")
                .addField("Information", "Use the Button(s) below to navigate through the logs", false)
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now());

        List<Button> buttons = new ArrayList<>();
        if (!uploaded.isEmpty()) {
            int maxButtons = Math.min(uploaded.size(), 5);
            for (int i = 0; i < maxButtons; i++) {
                String pretty = uploaded.get(i);
                String url = pretty.substring(pretty.indexOf("→") + 1).trim();
                buttons.add(Button.link(url, "Open " + files.values().iterator().next()));
            }
        }

        if (buttons.isEmpty()) {
            placeholder.editMessageEmbeds(success.build()).queue();
        } else {
            placeholder.editMessageEmbeds(success.build()).setActionRow(buttons).queue();
        }
    }

    private void editFailure(Message placeholder, Throwable ex) {
        EmbedBuilder fail = new EmbedBuilder()
                .setTitle("❌ Upload failed")
                .setDescription("Error: " + ex.getMessage())
                .setColor(Color.RED)
                .setTimestamp(Instant.now());

        placeholder.editMessageEmbeds(fail.build()).queue();
    }

    // ────────────────────────────────────────────────────────────────────────────────
    //  Low‑level HTTP helper
    // ────────────────────────────────────────────────────────────────────────────────

    private String uploadToMclogs(File file) throws IOException {
        // Read log file (10MiB API limit – we trust users to send something reasonable)
        StringBuilder log = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.append(line).append('\n');
            }
        }

        // Build x‑www‑form‑urlencoded entity
        List<NameValuePair> params = List.of(new BasicNameValuePair("content", log.toString()));
        HttpEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.mclo.gs/1/log");
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String result = new String(resEntity.getContent().readAllBytes(), StandardCharsets.UTF_8);
                    JsonNode json = new ObjectMapper().readTree(result);

                    if (json.path("success").asBoolean(false)) {
                        if (json.has("url")) return json.get("url").asText();
                    }
                    throw new IOException(json.path("error").asText("mclo.gs API Error"));
                }
            }
        }
        throw new IOException("mclo.gs API Error – leere Antwort");
    }
}
