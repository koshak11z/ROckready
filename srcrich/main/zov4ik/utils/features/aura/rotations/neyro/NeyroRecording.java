package im.zov4ik.utils.features.aura.rotations.neyro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NeyroRecording {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Type FRAME_LIST_TYPE = new TypeToken<List<NeyroFrame>>() {}.getType();

    String name;
    List<NeyroFrame> frames = new ArrayList<>();

    public NeyroRecording(String name) {
        this.name = name;
    }

    public NeyroRecording() {
        this("unnamed");
    }

    public void addFrame(NeyroFrame frame) {
        frames.add(frame);
    }

    public void clear() {
        frames.clear();
    }

    public int size() {
        return frames.size();
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }

    public void saveToFile(File directory) throws IOException {
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, name + ".json");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(GSON.toJson(frames));
        }
    }

    public static NeyroRecording loadFromFile(File directory, String name) throws IOException {
        File file = new File(directory, name + ".json");
        if (!file.exists()) {
            throw new FileNotFoundException("Neyro recording '" + name + "' not found!");
        }

        NeyroRecording recording = new NeyroRecording(name);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            List<NeyroFrame> loadedFrames = GSON.fromJson(builder.toString(), FRAME_LIST_TYPE);
            if (loadedFrames != null) {
                recording.setFrames(loadedFrames);
            }
        }
        return recording;
    }

    public static boolean deleteFile(File directory, String name) {
        File file = new File(directory, name + ".json");
        return file.exists() && file.delete();
    }

    public static List<String> listRecordings(File directory) {
        List<String> names = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, fileName) -> fileName.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    names.add(fileName.substring(0, fileName.length() - 5));
                }
            }
        }
        return names;
    }
}
