package com.example.staffreplay;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.client.MinecraftClient;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.concurrent.atomic.AtomicBoolean;

public class StaffReplayClient
        implements ClientModInitializer {

    private static final ReplayManager MANAGER =
            ReplayManager.getInstance();

    private static final ReplayRecorder RECORDER =
            ReplayRecorder.getInstance();

    private static final BackgroundRecorder
            BACKGROUND_RECORDER =
            BackgroundRecorder.getInstance();

    private final AtomicBoolean savePressed =
            new AtomicBoolean(false);

    private boolean startedRecording = false;
    private boolean bufferingEnabled = true;

    /*
     * REPLAY NAMING
     */

    private static boolean waitingForReplayName =
            false;

            private static long lastAutoEvidence = 0L;


    @Override
    public void onInitializeClient() {

        StaffReplay.LOGGER.info(
                "Staff Replay Client Loaded"
        );

        Keybinds.register();

        HudOverlay.register();

        /*
         * CHAT INTERCEPT
         */

        ClientReceiveMessageEvents.GAME.register(
    (message, overlay) -> {

        try {

            if (
                !SettingsManager.isAutoEvidenceEnabled()
            ) {
                return;
            }

            String raw =
                    message.getString();

            MinecraftClient client =
                    MinecraftClient.getInstance();

            if (
                client.player == null
            ) {
                return;
            }

            String username =
                    client.player
                            .getGameProfile()
                            .getName()
                            .toLowerCase();

            String lower =
                    raw.toLowerCase();

            /*
             * MUST BE YOUR PUNISHMENT
             */

            if (
                !lower.contains(
                    "by " + username
                )
            ) {
                return;
            }

            /*
             * DETECT TYPES
             */

            boolean matched = false;

            if (
                lower.contains("xray") &&
                SettingsManager.isDetectXray()
            ) {
                matched = true;
            }

            if (
                lower.contains("cheating") &&
                SettingsManager.isDetectCheating()
            ) {
                matched = true;
            }

            if (
                lower.contains("mute evasion") &&
                SettingsManager.isDetectMuteEvasion()
            ) {
                matched = true;
            }

            if (
                lower.contains("glitch abuse") &&
                SettingsManager.isDetectGlitchAbuse()
            ) {
                matched = true;
            }

            if (
                lower.contains("inappropriate cosmetic") &&
                SettingsManager.isDetectInappropriateCosmetic()
            ) {
                matched = true;
            }

            if (!matched) {
                return;
            }

            if (
    System.currentTimeMillis() -
    lastAutoEvidence < 5000L
) {
    return;
}

lastAutoEvidence =
        System.currentTimeMillis();

            /*
             * EXTRACT PLAYER NAME
             */

            String punishedPlayer =
                    "unknown";

            if (
                raw.contains(" was ")
            ) {

                punishedPlayer =
                        raw.substring(
                                0,
                                raw.indexOf(" was ")
                        ).trim();

                punishedPlayer =
                        punishedPlayer.replaceAll(
                                "§[0-9a-fk-or]",
                                ""
                        );

                punishedPlayer =
                        punishedPlayer.replaceAll(
                                "\\[[^]]*\\]",
                                ""
                        ).trim();
            }

            final String finalPlayer =
        punishedPlayer
                .replaceAll(
                        "[^a-zA-Z0-9-_]",
                        "_"
                );

            client.player.sendMessage(

                Text.literal(
                    "[STAFFREPLAY] Auto evidence triggered for " +
                    finalPlayer
                ).formatted(
                    Formatting.GREEN,
                    Formatting.BOLD
                ),

                false
            );

            /*
             * SAVE IN BACKGROUND
             */

            new Thread(() -> {

                try {

                    String replayName =
                            username +
                            "__" +
                            finalPlayer;

                    Path replayFile =
                            MANAGER.saveReplay(
                                    client,
                                    replayName
                            );

                    if (replayFile == null) {
                        return;
                    }

                    Path renamedFile =
                            replayFile.resolveSibling(
                                    replayName + ".mp4"
                            );

                    Files.move(
        replayFile,
        renamedFile
);

/*
 * RENAME MARKERS
 */

Path oldMarkerFile =
        replayFile.resolveSibling(

                replayFile
                        .getFileName()
                        .toString()
                        .replace(
                                ".mp4",
                                ".json"
                        )
        );

Path newMarkerFile =
        renamedFile.resolveSibling(

                renamedFile
                        .getFileName()
                        .toString()
                        .replace(
                                ".mp4",
                                ".json"
                        )
        );

if (Files.exists(oldMarkerFile)) {

    Files.move(
            oldMarkerFile,
            newMarkerFile
    );
}

CloudUploader.uploadReplay(
        renamedFile
);

                } catch (Exception e) {

                    StaffReplay.LOGGER.error(
                            "Auto evidence failed",
                            e
                    );
                }

            }).start();

        } catch (Exception e) {

            StaffReplay.LOGGER.error(
                    "Punishment detection failed",
                    e
            );
        }
    }
);

        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {

    if (
            command.equalsIgnoreCase(
                    "replayauth"
            )
    ) {

        TwoFactorManager.generateCode(
                MinecraftClient.getInstance()
        );

        return false;
    }

    return true;
});

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {

            StaffReplay.LOGGER.info(
                    "CHAT MESSAGE: {}",
                    message
            );
        

            /*
             * NOT WAITING
             */
          /*
 * MARKER CHAT INPUT
 */

if (MarkerChatListener.waitingForMarker) {

    MarkerChatListener.handleChatMessage(
            message
    );

    MinecraftClient client =
            MinecraftClient.getInstance();

    if (client.player != null) {

        client.player.sendMessage(

                Text.literal(
                        "[STAFFREPLAY] Marker added"
                ).formatted(
                        Formatting.AQUA,
                        Formatting.BOLD
                ),

                false
        );
    }

    return false;
}

/*
 * NOT WAITING FOR REPLAY NAME
 */

if (!waitingForReplayName) {

    return true;
} 

            try {

                MinecraftClient client =
                        MinecraftClient.getInstance();

                if (
                        client.player == null
                ) {

                    return false;
                }

                /*
                 * CLEAN NAME
                 */

                String cleanName =
                        message
                                .trim()
                                .replaceAll(
                                        "[^a-zA-Z0-9-_]",
                                        "_"
                                );

                /*
                 * STAFF USERNAME
                 */

                String playerName =
                        client.player
                                .getGameProfile()
                                .getName();

                /*
                 * FINAL FILE
                 */

                waitingForReplayName = false;

MinecraftClient.getInstance().execute(() -> {

    if (client.player != null) {

        client.player.sendMessage(
                Text.literal(
                        "[STAFFREPLAY] Saving replay in background..."
                ).formatted(
                        Formatting.YELLOW,
                        Formatting.BOLD
                ),
                false
        );
    }
});

new Thread(() -> {

    try {

        String replayName =
                playerName +
                "__" +
                cleanName;

        Path replayFile =
                MANAGER.saveReplay(
                        client,
                        replayName
                );

        if (replayFile == null) {

            return;
        }

        Path renamedFile =
                replayFile.resolveSibling(
                        replayName + ".mp4"
                );

        Files.move(
                replayFile,
                renamedFile
        );

        /*
         * RENAME MARKERS
         */

        Path oldMarkerFile =
                replayFile.resolveSibling(

                        replayFile
                                .getFileName()
                                .toString()
                                .replace(
                                        ".mp4",
                                        ".json"
                                )
                );

        Path newMarkerFile =
                renamedFile.resolveSibling(

                        renamedFile
                                .getFileName()
                                .toString()
                                .replace(
                                        ".mp4",
                                        ".json"
                                )
                );

        if (Files.exists(oldMarkerFile)) {

            Files.move(
                    oldMarkerFile,
                    newMarkerFile
            );
        }

        CloudUploader.uploadReplay(
                renamedFile
        );

        MinecraftClient.getInstance().execute(() -> {

            if (client.player != null) {

                client.player.sendMessage(
                        Text.literal(
                                "[STAFFREPLAY] Replay processing started"
                        ).formatted(
                                Formatting.GREEN,
                                Formatting.BOLD
                        ),
                        false
                );
            }
        });

    } catch (Exception e) {

        StaffReplay.LOGGER.error(
                "Failed processing replay",
                e
        );
    }

}).start();

            } catch (Exception e) {

                StaffReplay.LOGGER.error(
                        "Failed naming replay",
                        e
                );
            }

            /*
             * BLOCK NORMAL CHAT
             */

            return false;
        });

        /*
         * MAIN LOOP
         */

        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {

            if (
                    !startedRecording &&
                    clientTick.world != null &&
                    clientTick.player != null
            ) {

                MANAGER.startRecording(
                        clientTick
                );

                BACKGROUND_RECORDER.start(
                        clientTick
                );

                startedRecording = true;

                StaffReplay.LOGGER.info(
                        "Replay recording started"
                );
            }

            RECORDER.recordTick(
                    clientTick
            );

            BACKGROUND_RECORDER.captureFrame(
                    clientTick
            );

            Keybinds.handleKeybinds(
        clientTick
);

while (
        Keybinds.OPEN_SETTINGS.wasPressed()
) {

    clientTick.setScreen(

            new StaffReplaySettingsScreen(
                    clientTick.currentScreen
            )
    );
}

/*
 * TOGGLE BUFFER
 */

while (
        Keybinds.TOGGLE_BUFFER.wasPressed()
) {

    bufferingEnabled =
            !bufferingEnabled;

    if (bufferingEnabled) {

        BACKGROUND_RECORDER.start(
                clientTick
        );


    } else {

        BACKGROUND_RECORDER.stop();

        if (clientTick.player != null) {

            clientTick.player.sendMessage(
                    Text.literal(
                            "[STAFFREPLAY] Buffer DISABLED"
                    ).formatted(
                            Formatting.RED,
                            Formatting.BOLD
                    ),
                    true
            );
        }
    }
}
            
            /*
             * CHANGE CLIP LENGTH
             */

            while (
                    Keybinds.CYCLE_LENGTH.wasPressed()
            ) {

                ClipLengthManager.cycleLength();

                if (clientTick.player != null) {

                    clientTick.player.sendMessage(
                            Text.literal(
                                    "[STAFFREPLAY] Clip Length: " +
                                    ClipLengthManager.getCurrentLength() +
                                    "s"
                            ).formatted(
                                    Formatting.YELLOW,
                                    Formatting.BOLD
                            ),
                            true
                    );
                }
            }

            /*
             * SAVE REPLAY
             */

            boolean currentlyPressed =
                    Keybinds.SAVE_REPLAY.isPressed();

            if (
                    currentlyPressed &&
                    !savePressed.get()
            ) {

                StaffReplay.LOGGER.info(
                        "R PRESSED"
                );




 waitingForReplayName = true;

MinecraftClient.getInstance().execute(() -> {

    if (clientTick.player != null) {

        clientTick.player.sendMessage(
                Text.literal(
                        "[STAFFREPLAY] Type player name you clipped for"
                ).formatted(
                        Formatting.YELLOW,
                        Formatting.BOLD
                ),
                false
        );
    }
});
            }

            savePressed.set(
                    currentlyPressed
            );
        });
    }
}