package im.zov4ik.features.impl.misc.telegram;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TelegramBotBridge {
    private static final long POLL_INTERVAL_MS = 2500L;
    private static final long RETRY_INTERVAL_MS = 4500L;

    private final TelegramBotClient client = new TelegramBotClient();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "zov-telegram-bot");
        thread.setDaemon(true);
        return thread;
    });
    private final Queue<TelegramCommand> pendingCommands = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean pollInFlight = new AtomicBoolean(false);

    private volatile long nextPollAtMs;
    private volatile long nextOffset;
    private volatile String sessionKey = "";
    private volatile String lastError = "";

    public void tick(String token, long chatId) {
        if (token == null || token.isBlank()) {
            resetSession();
            return;
        }

        String currentSessionKey = token.trim();
        if (!currentSessionKey.equals(sessionKey)) {
            sessionKey = currentSessionKey;
            nextOffset = 0L;
            pendingCommands.clear();
            nextPollAtMs = 0L;
        }

        long now = System.currentTimeMillis();
        if (now < nextPollAtMs || !pollInFlight.compareAndSet(false, true)) {
            return;
        }

        nextPollAtMs = now + POLL_INTERVAL_MS;
        ioExecutor.submit(() -> pollInternal(token.trim()));
    }

    public List<TelegramCommand> drainCommands() {
        List<TelegramCommand> commands = new ArrayList<>();
        TelegramCommand command;
        while ((command = pendingCommands.poll()) != null) {
            commands.add(command);
        }
        return commands;
    }

    public void sendMessageAsync(String token, long chatId, String message) {
        if (token == null || token.isBlank() || chatId == 0L || message == null || message.isBlank()) {
            return;
        }

        ioExecutor.submit(() -> {
            try {
                client.sendMessage(token.trim(), chatId, message);
            } catch (Exception exception) {
                reportError(exception);
            }
        });
    }

    public void sendPhotoAsync(String token, long chatId, Path imagePath, String caption, boolean deleteAfterSend) {
        if (token == null || token.isBlank() || chatId == 0L || imagePath == null) {
            return;
        }

        ioExecutor.submit(() -> {
            try {
                client.sendPhoto(token.trim(), chatId, imagePath, caption);
            } catch (Exception exception) {
                reportError(exception);
            } finally {
                if (deleteAfterSend) {
                    try {
                        Files.deleteIfExists(imagePath);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    public String consumeLastError() {
        String error = lastError;
        lastError = "";
        return error;
    }

    private void pollInternal(String token) {
        try {
            TelegramBotClient.PollResult result = client.pollUpdates(token, nextOffset);
            nextOffset = Math.max(nextOffset, result.nextOffset());
            pendingCommands.addAll(result.commands());
        } catch (Exception exception) {
            reportError(exception);
            nextPollAtMs = System.currentTimeMillis() + RETRY_INTERVAL_MS;
        } finally {
            pollInFlight.set(false);
        }
    }

    private void resetSession() {
        pendingCommands.clear();
        nextOffset = 0L;
        nextPollAtMs = 0L;
        sessionKey = "";
        lastError = "";
    }

    private void reportError(Exception exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            lastError = "Unknown Telegram error";
            return;
        }
        lastError = exception.getMessage();
    }
}

