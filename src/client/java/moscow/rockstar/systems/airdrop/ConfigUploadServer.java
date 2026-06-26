/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  fi.iki.elonen.NanoHTTPD
 *  fi.iki.elonen.NanoHTTPD$IHTTPSession
 *  fi.iki.elonen.NanoHTTPD$Method
 *  fi.iki.elonen.NanoHTTPD$Response
 *  fi.iki.elonen.NanoHTTPD$Response$IStatus
 *  fi.iki.elonen.NanoHTTPD$Response$Status
 *  lombok.Generated
 */
package moscow.rockstar.systems.airdrop;

import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.config.ConfigManager;
import moscow.rockstar.utility.interfaces.IMinecraft;

public class ConfigUploadServer
extends NanoHTTPD
implements IMinecraft {
    private static final int DEFAULT_PORT = 5656;
    private static final int MAX_PORT = 5666;
    private final File directory;
    private final int port;
    private String name;
    private boolean render;

    public ConfigUploadServer() throws IOException {
        super(ConfigUploadServer.findFreePort());
        this.directory = new File(ConfigUploadServer.mc.runDirectory, "Rockstar" + File.separator + "configs");
        if (!this.directory.exists() && !this.directory.mkdirs()) {
            throw new IOException("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u043f\u0430\u043f\u043a\u0443 \u0434\u043b\u044f \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432: " + String.valueOf(this.directory));
        }
        this.start(5000, false);
        this.port = this.getListeningPort();
        System.out.println("\u0421\u0435\u0440\u0432\u0435\u0440 \u0437\u0430\u043f\u0443\u0449\u0435\u043d \u043d\u0430 \u043f\u043e\u0440\u0442\u0443 " + this.port + ", \u043a\u043e\u043d\u0444\u0438\u0433\u0438 \u0432 " + this.directory.getAbsolutePath());
    }

    private static int findFreePort() throws IOException {
        for (int candidate = DEFAULT_PORT; candidate <= MAX_PORT; ++candidate) {
            try (ServerSocket socket = new ServerSocket()) {
                socket.setReuseAddress(false);
                socket.bind(new InetSocketAddress("127.0.0.1", candidate));
                return candidate;
            } catch (IOException ignored) {
            }
        }
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        if (NanoHTTPD.Method.POST.equals((Object)session.getMethod())) {
            try {
                HashMap files = new HashMap();
                session.parseBody(files);
                String tmpPath = (String)files.get("file");
                if (tmpPath != null) {
                    String originalName = (String)session.getParms().get("file");
                    if (originalName == null || originalName.isEmpty()) {
                        originalName = "client.rock";
                    }
                    this.name = originalName;
                    this.render = true;
                    File dest = new File(this.directory, originalName);
                    Files.copy(Path.of(tmpPath, new String[0]), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("\u041a\u043e\u043d\u0444\u0438\u0433 \u043f\u043e\u043b\u0443\u0447\u0435\u043d, \u043e\u0436\u0438\u0434\u0430\u0435\u043c \u043f\u043e\u044f\u0432\u043b\u0435\u043d\u0438\u044f " + dest.getName());
                    long start = System.currentTimeMillis();
                    while (!dest.exists() && System.currentTimeMillis() - start < 5000L) {
                        try {
                            Thread.sleep(50L);
                        }
                        catch (InterruptedException ie) {
                            // empty catch block
                            break;
                        }
                    }
                    if (!dest.exists()) {
                        return ConfigUploadServer.newFixedLengthResponse((NanoHTTPD.Response.IStatus)NanoHTTPD.Response.Status.INTERNAL_ERROR, (String)"text/plain", (String)"\u0424\u0430\u0439\u043b \u043d\u0435 \u043f\u043e\u044f\u0432\u0438\u043b\u0441\u044f \u0432 \u043f\u0430\u043f\u043a\u0435 \u043f\u043e\u0441\u043b\u0435 \u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u044f");
                    }
                    System.out.println("\u0424\u0430\u0439\u043b \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0451\u043d, \u043b\u043e\u0430\u0434\u0438\u043c \u043a\u043e\u043d\u0444\u0438\u0433 " + dest.getName());
                    ConfigManager mgr = Rockstar.getInstance().getConfigManager();
                    ConfigFile cfg = mgr.getConfig(originalName);
                    if (cfg == null) {
                        cfg = new ConfigFile(originalName);
                        mgr.getConfigFiles().add(cfg);
                    }
                    cfg.load();
                    return ConfigUploadServer.newFixedLengthResponse((String)"\u041a\u043e\u043d\u0444\u0438\u0433 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d \u0438 \u043f\u0440\u0438\u043c\u0435\u043d\u0451\u043d");
                }
            }
            catch (Exception e) {
                return ConfigUploadServer.newFixedLengthResponse((String)("\u041e\u0448\u0438\u0431\u043a\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0438: " + e.getMessage()));
            }
        }
        this.render = false;
        String html = "<!DOCTYPE html> <html lang=\"ru\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u043a\u043e\u043d\u0444\u0438\u0433\u0430</title>\n  <style>\n    body {\n      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n      display: flex;\n      flex-direction: column;\n      align-items: center;\n      justify-content: center;\n      height: 100vh;\n      margin: 0;\n      padding: 20px;\n      background: #f2f2f2;\n    }\n\n    h1 {\n      font-size: 1.8em;\n      margin-bottom: 1em;\n      text-align: center;\n    }\n\n    form {\n      display: flex;\n      flex-direction: column;\n      gap: 15px;\n      width: 100%;\n      max-width: 400px;\n      background: #fff;\n      padding: 20px;\n      border-radius: 12px;\n      box-shadow: 0 4px 10px rgba(0,0,0,0.1);\n    }\n\n    input[type=\"file\"] {\n      font-size: 1.1em;\n    }\n\n    input[type=\"submit\"] {\n      padding: 12px;\n      font-size: 1.2em;\n      border: none;\n      border-radius: 8px;\n      background: #007aff;\n      color: white;\n      cursor: pointer;\n      transition: background 0.3s ease;\n    }\n\n    input[type=\"submit\"]:hover {\n      background: #005fcc;\n    }\n\n    @media (max-width: 400px) {\n      h1 {\n        font-size: 1.4em;\n      }\n      input[type=\"submit\"] {\n        font-size: 1em;\n        padding: 10px;\n      }\n    }\n  </style>\n</head>\n<body>\n  <h1>\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433 Rockstar</h1>\n  <form method=\"POST\" enctype=\"multipart/form-data\">\n    <input type=\"file\" name=\"file\" accept=\".rock\" required />\n    <input type=\"submit\" value=\"\u041e\u0442\u043f\u0440\u0430\u0432\u0438\u0442\u044c \u0432 \u043a\u043b\u0438\u0435\u043d\u0442\" />\n  </form>\n</body> </html>";
        return ConfigUploadServer.newFixedLengthResponse((NanoHTTPD.Response.IStatus)NanoHTTPD.Response.Status.OK, (String)"text/html", (String)html);
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public boolean isRender() {
        return this.render;
    }

    @Generated
    public int getPort() {
        return this.port;
    }
}
