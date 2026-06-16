package im.zov4ik.features.impl.misc.telegram;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TelegramBotClient {
    private static final String API_BASE = "https://api.telegram.org/bot";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    public PollResult pollUpdates(String botToken, long offset) throws IOException, InterruptedException {
        String safeToken = botToken == null ? "" : botToken.trim();
        String endpoint = API_BASE + safeToken + "/getUpdates";
        String query = "?timeout=0&offset=" + Math.max(0L, offset) + "&allowed_updates=%5B%22message%22%5D";

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint + query))
                .GET()
                .timeout(REQUEST_TIMEOUT)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject root = parseJson(response.body());
        ensureOk(root, response.statusCode(), "getUpdates");

        long nextOffset = offset;
        List<TelegramCommand> commands = new ArrayList<>();

        JsonArray result = root.has("result") && root.get("result").isJsonArray() ? root.getAsJsonArray("result") : null;
        if (result == null) {
            return new PollResult(nextOffset, commands);
        }

        for (JsonElement element : result) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject update = element.getAsJsonObject();
            if (update.has("update_id")) {
                long updateId = update.get("update_id").getAsLong();
                nextOffset = Math.max(nextOffset, updateId + 1L);
            }

            JsonObject message = update.has("message") && update.get("message").isJsonObject()
                    ? update.getAsJsonObject("message")
                    : null;
            if (message == null) {
                continue;
            }

            JsonObject chat = message.has("chat") && message.get("chat").isJsonObject()
                    ? message.getAsJsonObject("chat")
                    : null;
            if (chat == null || !chat.has("id") || !message.has("text")) {
                continue;
            }

            long chatId = chat.get("id").getAsLong();
            String text = message.get("text").getAsString();
            if (text == null || text.isBlank()) {
                continue;
            }

            commands.add(new TelegramCommand(chatId, text.trim()));
        }

        return new PollResult(nextOffset, commands);
    }

    public void sendMessage(String botToken, long chatId, String text) throws IOException, InterruptedException {
        if (text == null || text.isBlank()) {
            return;
        }
        String safeText = recoverMojibakeIfNeeded(text);

        String endpoint = API_BASE + botToken.trim() + "/sendMessage";
        String form = "chat_id=" + urlEncode(Long.toString(chatId))
                + "&text=" + urlEncode(safeText)
                + "&disable_web_page_preview=true";

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject root = parseJson(response.body());
        ensureOk(root, response.statusCode(), "sendMessage");
    }

    public void sendPhoto(String botToken, long chatId, Path imagePath, String caption) throws IOException, InterruptedException {
        if (imagePath == null || !Files.exists(imagePath)) {
            throw new IOException("Image file not found");
        }

        String endpoint = API_BASE + botToken.trim() + "/sendPhoto";
        String boundary = "----ZOVBoundary" + Long.toUnsignedString(System.nanoTime(), 36).toUpperCase(Locale.ROOT);
        byte[] body = buildMultipartBody(boundary, chatId, imagePath, caption);

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject root = parseJson(response.body());
        ensureOk(root, response.statusCode(), "sendPhoto");
    }

    private byte[] buildMultipartBody(String boundary, long chatId, Path imagePath, String caption) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeFormPart(output, boundary, "chat_id", Long.toString(chatId));
        if (caption != null && !caption.isBlank()) {
            writeFormPart(output, boundary, "caption", recoverMojibakeIfNeeded(caption));
        }
        writeFilePart(output, boundary, "photo", imagePath.getFileName().toString(), "image/png", Files.readAllBytes(imagePath));
        output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private void writeFormPart(ByteArrayOutputStream output, String boundary, String name, String value) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(value.getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeFilePart(ByteArrayOutputStream output, String boundary, String name, String filename, String contentType, byte[] data) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(data);
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private JsonObject parseJson(String body) throws IOException {
        try {
            JsonElement parsed = JsonParser.parseString(body);
            if (!parsed.isJsonObject()) {
                throw new IOException("Invalid Telegram response");
            }
            return parsed.getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new IOException("Failed to parse Telegram response", exception);
        }
    }

    private void ensureOk(JsonObject root, int statusCode, String method) throws IOException {
        boolean ok = root.has("ok") && root.get("ok").getAsBoolean();
        if (ok) {
            return;
        }

        String description = root.has("description") ? root.get("description").getAsString() : "unknown error";
        throw new IOException("Telegram " + method + " failed (" + statusCode + "): " + description);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String recoverMojibakeIfNeeded(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String best = value;
        int bestScore = scoreTextQuality(best);
        String normalized = value;
        for (int i = 0; i < 8; i++) {
            String repaired = decodeUtf8FromWindows1251(normalized);
            if (repaired == null || repaired.isBlank() || repaired.equals(normalized)) {
                break;
            }
            int repairedScore = scoreTextQuality(repaired);
            if (repairedScore > bestScore) {
                best = repaired;
                bestScore = repairedScore;
            }
            normalized = repaired;
        }
        return best;
    }

    private int scoreTextQuality(String value) {
        if (value == null || value.isBlank()) {
            return Integer.MIN_VALUE / 4;
        }
        int russianCyrillicCount = 0;
        int otherCyrillicCount = 0;
        int latinCount = 0;
        int digitCount = 0;
        int whitespaceCount = 0;
        int markerCount = 0;
        int questionCount = 0;
        int replacementCount = 0;
        int weirdControlCount = 0;
        int suspiciousSequenceCount = 0;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ((ch >= '\u0410' && ch <= '\u044F') || ch == '\u0401' || ch == '\u0451') {
                russianCyrillicCount++;
            } else if (ch >= '\u0400' && ch <= '\u04FF') {
                otherCyrillicCount++;
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                latinCount++;
            } else if (ch >= '0' && ch <= '9') {
                digitCount++;
            } else if (Character.isWhitespace(ch)) {
                whitespaceCount++;
            }
            if (ch == '\u0420' || ch == '\u0421' || ch == '\u00D0' || ch == '\u00D1' || ch == '\u00C2' || ch == '\u00C3') {
                markerCount++;
            }
            if (ch == '?') {
                questionCount++;
            }
            if (ch == '\uFFFD') {
                replacementCount++;
            }
            if (ch < 32 && ch != '\n' && ch != '\r' && ch != '\t') {
                weirdControlCount++;
            }
        }

        String lower = value.toLowerCase(Locale.ROOT);
        suspiciousSequenceCount += countSubstring(lower, "\u0432\u0402");
        suspiciousSequenceCount += countSubstring(lower, "\u0440\u045F");
        suspiciousSequenceCount += countSubstring(lower, "\u0440\u045E");
        suspiciousSequenceCount += countSubstring(lower, "\u0440\u0441");
        suspiciousSequenceCount += countSubstring(lower, "\u0441\u045F");
        suspiciousSequenceCount += countSubstring(lower, "\u0453");
        suspiciousSequenceCount += countSubstring(lower, "\u045C");
        suspiciousSequenceCount += countSubstring(lower, "\u0459");

        return russianCyrillicCount * 8
                + latinCount * 2
                + digitCount
                + whitespaceCount
                - otherCyrillicCount * 6
                - markerCount * 8
                - questionCount * 9
                - replacementCount * 20
                - weirdControlCount * 16
                - suspiciousSequenceCount * 14;
    }

    private int countSubstring(String text, String needle) {
        if (text == null || needle == null || needle.isEmpty()) {
            return 0;
        }
        int count = 0;
        int from = 0;
        while (true) {
            int index = text.indexOf(needle, from);
            if (index < 0) {
                return count;
            }
            count++;
            from = index + needle.length();
        }
    }

    private String decodeUtf8FromWindows1251(String value) {
        try {
            String decoded = new String(value.getBytes("windows-1251"), StandardCharsets.UTF_8);
            if (decoded.indexOf('\uFFFD') >= 0) {
                return value;
            }
            return decoded;
        } catch (Exception ignored) {
            return value;
        }
    }

    public record PollResult(long nextOffset, List<TelegramCommand> commands) {
    }
}

