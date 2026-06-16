package im.zov4ik.utils.client.managers.file.impl;

import antidaunleak.api.annotation.Native;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import im.zov4ik.common.proxy.Config;
import im.zov4ik.common.proxy.ProxyServer;
import im.zov4ik.utils.client.managers.file.ClientFile;
import im.zov4ik.utils.client.managers.file.exception.FileLoadException;
import im.zov4ik.utils.client.managers.file.exception.FileSaveException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ProxyFile extends ClientFile {

    public ProxyFile() {
        super("Proxy/Proxyconfig");
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        Config.saveConfig();
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        Config.loadConfig();
    }
}