/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package moscow.rockstar.utility.sounds;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricsFetcher {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    public static String fetchFromGenius(String artist, String title) {
        if (artist == null || title == null || artist.isBlank() || title.isBlank()) {
            System.err.println("Artist or title cannot be empty");
            return null;
        }
        try {
            SongInfo info = LyricsFetcher.searchSong(artist, title);
            if (info == null) {
                System.err.println("Song not found on Genius");
                return null;
            }
            String lyrics = LyricsFetcher.fetchLyricsFromApi(info.id);
            if (lyrics == null && info.url != null) {
                lyrics = LyricsFetcher.fetchLyricsFromPage(info.url);
            }
            return lyrics;
        }
        catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Request interrupted");
        }
        catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
        return null;
    }

    private static SongInfo searchSong(String artist, String title) throws IOException, InterruptedException {
        String query = URLEncoder.encode(artist + " " + title, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.genius.com/search?q=" + query)).header("Authorization", "Bearer batnaM4ixvdL448SIofj6I6aqLsRZ2RuLowRA8tXoWYUAse55DoAX7Xf7MT0vjy5").header("Accept", "application/json").GET().build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("API returned status: " + response.statusCode());
            return null;
        }
        JsonObject jsonResponse = JsonParser.parseString((String)response.body()).getAsJsonObject();
        JsonObject meta = jsonResponse.getAsJsonObject("meta");
        if (meta != null && meta.has("status") && meta.get("status").getAsInt() != 200) {
            System.err.println("API error: " + meta.get("message").getAsString());
            return null;
        }
        JsonObject responseObj = jsonResponse.getAsJsonObject("response");
        if (responseObj != null && responseObj.has("hits")) {
            for (JsonElement hit : responseObj.getAsJsonArray("hits")) {
                JsonObject result = hit.getAsJsonObject().getAsJsonObject("result");
                if (result == null || !result.has("url") || !result.has("id")) continue;
                return new SongInfo(result.get("url").getAsString(), result.get("id").getAsInt());
            }
        }
        return null;
    }

    private static String fetchLyricsFromApi(int id) throws IOException, InterruptedException {
        JsonObject lyricsObj;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.genius.com/songs/" + id + "?text_format=plain")).header("Authorization", "Bearer batnaM4ixvdL448SIofj6I6aqLsRZ2RuLowRA8tXoWYUAse55DoAX7Xf7MT0vjy5").header("Accept", "application/json").GET().build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        JsonObject jsonResponse = JsonParser.parseString((String)response.body()).getAsJsonObject();
        JsonObject song = Optional.ofNullable(jsonResponse.getAsJsonObject("response")).map(obj -> obj.getAsJsonObject("song")).orElse(null);
        if (song != null && song.has("lyrics") && (lyricsObj = song.getAsJsonObject("lyrics")).has("plain")) {
            return lyricsObj.get("plain").getAsString();
        }
        if (song != null && song.has("lyrics_body")) {
            return song.get("lyrics_body").getAsString();
        }
        return null;
    }

    private static String fetchLyricsFromPage(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", USER_AGENT).header("Accept-Language", "en-US,en;q=0.9").GET().build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        Pattern pattern = Pattern.compile("(<div[^>]*class=\"[^\"]*Lyrics__Container[^\"]*\"[^>]*>.*?</div>)", 32);
        Matcher matcher = pattern.matcher(response.body());
        StringBuilder lyrics = new StringBuilder();
        while (matcher.find()) {
            String snippet = matcher.group(1).replaceAll("<br\\s*/?>", "\n").replaceAll("<.*?>", "").replaceAll("&quot;", "\"").trim();
            if (snippet.isEmpty()) continue;
            lyrics.append(snippet).append("\n\n");
        }
        return lyrics.isEmpty() ? null : lyrics.toString().trim();
    }

    record SongInfo(String url, int id) {
    }
}

