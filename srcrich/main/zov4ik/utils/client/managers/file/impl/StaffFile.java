package im.zov4ik.utils.client.managers.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import im.zov4ik.common.repository.staff.StaffRepository;
import im.zov4ik.utils.client.managers.file.ClientFile;
import im.zov4ik.utils.client.managers.file.exception.FileLoadException;
import im.zov4ik.utils.client.managers.file.exception.FileSaveException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StaffFile extends ClientFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public StaffFile() {
        super("Staff");
    }

    @Override
    public void loadFromFile(File directory) throws FileLoadException {
        if (tryMigrateLegacyCfg(directory)) {
            return;
        }

        File file = new File(directory, getName() + ".json");
        if (!file.exists()) {
            return;
        }

        try {
            String content = Files.readString(file.toPath());
            if (content.isEmpty()) {
                return;
            }

            List<StaffRepository.Staff> staff = GSON.fromJson(content, new TypeToken<List<StaffRepository.Staff>>() {}.getType());
            if (staff == null) {
                return;
            }
            StaffRepository.getStaff().clear();
            StaffRepository.getStaff().addAll(staff);
        } catch (IOException e) {
            throw new FileLoadException("Failed to read staff file", e);
        }
    }

    private boolean tryMigrateLegacyCfg(File directory) throws FileLoadException {
        File legacyFile = resolveLegacyCfg(directory);
        if (legacyFile == null || !legacyFile.exists()) {
            return false;
        }

        try {
            String content = Files.readString(legacyFile.toPath());
            if (content == null || content.isBlank()) {
                return false;
            }

            List<String> legacyNames = GSON.fromJson(content, new TypeToken<List<String>>() {}.getType());
            if (legacyNames == null || legacyNames.isEmpty()) {
                return false;
            }

            StaffRepository.getStaff().clear();
            Set<String> dedupe = new HashSet<>();
            for (String rawName : legacyNames) {
                if (rawName == null) {
                    continue;
                }
                String name = rawName.trim();
                if (name.isEmpty()) {
                    continue;
                }
                String key = name.toLowerCase(Locale.ROOT);
                if (!dedupe.add(key)) {
                    continue;
                }
                StaffRepository.getStaff().add(new StaffRepository.Staff(name));
            }

            saveToFile(directory);
            Files.deleteIfExists(legacyFile.toPath());
            return true;
        } catch (IOException e) {
            throw new FileLoadException("Failed to migrate legacy staffs.cfg", e);
        } catch (Exception ignored) {
            return false;
        }
    }

    private File resolveLegacyCfg(File directory) {
        if (directory == null) {
            return null;
        }

        File parent = directory.getParentFile();
        File runDirectory = parent != null ? parent.getParentFile() : null;
        if (runDirectory == null) {
            return null;
        }
        return new File(runDirectory, "staffs.cfg");
    }

    @Override
    public void saveToFile(File directory) throws FileSaveException {
        File file = new File(directory, getName() + ".json");
        try {
            Files.writeString(file.toPath(), GSON.toJson(StaffRepository.getStaff()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new FileSaveException("Failed to save staff file", e);
        }
    }
}
