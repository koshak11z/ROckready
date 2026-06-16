/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  lombok.Generated
 *  net.minecraft.client.MinecraftClient
 */
package moscow.rockstar.systems.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.file.ClientFile;
import moscow.rockstar.systems.file.impl.ClientDataFile;
import net.minecraft.client.MinecraftClient;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.Initialization;

public class FileManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final File DIRECTORY = new File(MinecraftClient.getInstance().runDirectory, "Rockstar");
    public static final String DEFAULT_FILE_TYPE = "rock";
    private final List<ClientFile> clientFiles = new ArrayList<ClientFile>();

    public FileManager() {
        try {
            if (!DIRECTORY.exists()) {
                Files.createDirectories(Path.of(DIRECTORY.toURI()), new FileAttribute[0]);
            }
        }
        catch (IOException e) {
            System.err.println("Error creating directory: " + e.getMessage());
        }
    }

    @Initialization
    public void registerClientFiles() {
        this.clientFiles.add(new ClientDataFile());
    }

    public ClientFile getClientFile(String clientFileName) {
        return this.clientFiles.stream().filter(clientFile -> clientFile.getInfoAnnotation().name().equalsIgnoreCase(clientFileName)).findFirst().orElse(null);
    }

    public void readFile(ClientFile clientFile) {
        try {
            if (clientFile.getFile().exists()) {
                clientFile.read();
            }
        }
        catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public void readFile(String clientFileName) {
        ClientFile clientFile = this.getClientFile(clientFileName);
        if (clientFile != null) {
            this.readFile(clientFile);
        }
    }

    public void writeFile(ClientFile clientFile) {
        try {
            if (!clientFile.getFile().exists()) {
                clientFile.getFile().createNewFile();
            }
            clientFile.write();
        }
        catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public void writeFile(String clientFileName) {
        ClientFile clientFile = this.getClientFile(clientFileName);
        if (clientFile != null) {
            clientFile.write();
        }
    }

    @Compile
    @Initialization
    public void loadClientFiles() {
        for (ClientFile file : this.clientFiles) {
            this.readFile(file);
        }
    }

    public void saveClientFiles() {
        for (ClientFile file : this.clientFiles) {
            this.writeFile(file);
        }
    }

    @Generated
    public List<ClientFile> getClientFiles() {
        return this.clientFiles;
    }
}

