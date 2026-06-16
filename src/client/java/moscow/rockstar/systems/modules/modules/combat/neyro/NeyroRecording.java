package moscow.rockstar.systems.modules.modules.combat.neyro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NeyroRecording {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type FRAME_LIST_TYPE = new TypeToken<List<NeyroFrame>>() {}.getType();
    private String name;
    private List<NeyroFrame> frames = new ArrayList<>();

    public NeyroRecording() { this("unnamed"); }
    public NeyroRecording(String name) { this.name = name; }
    public void addFrame(NeyroFrame frame) { this.frames.add(frame); }
    public void clear() { this.frames.clear(); }
    public int size() { return this.frames.size(); }
    public boolean isEmpty() { return this.frames.isEmpty(); }
    public List<NeyroFrame> getFrames() { return this.frames; }
    public void setFrames(List<NeyroFrame> frames) { this.frames = frames == null ? new ArrayList<>() : frames; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public void saveToFile(File directory) throws IOException {
        if (!directory.exists()) directory.mkdirs();
        File file = new File(directory, this.name + ".json");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(GSON.toJson(this.frames));
        }
    }

    public static NeyroRecording loadFromFile(File directory, String name) throws IOException {
        File file = new File(directory, name + ".json");
        if (!file.exists()) throw new FileNotFoundException("Neyro recording '" + name + "' not found");
        NeyroRecording recording = new NeyroRecording(name);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            List<NeyroFrame> loaded = GSON.fromJson(reader, FRAME_LIST_TYPE);
            recording.setFrames(loaded);
        }
        return recording;
    }

    public static boolean deleteFile(File directory, String name) {
        File file = new File(directory, name + ".json");
        return file.exists() && file.delete();
    }

    public static List<String> listRecordings(File directory) {
        List<String> names = new ArrayList<>();
        File[] files = directory.listFiles((dir, fileName) -> fileName.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                names.add(fileName.substring(0, fileName.length() - 5));
            }
        }
        return names;
    }
}
