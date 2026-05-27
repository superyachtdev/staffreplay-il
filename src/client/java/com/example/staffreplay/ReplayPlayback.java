package com.example.staffreplay;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReplayPlayback {

    private static final ReplayPlayback INSTANCE =
            new ReplayPlayback();

    private final Gson gson =
            new Gson();

    private List<ReplaySnapshot> snapshots;

    private boolean playing = false;

    private boolean finished = false;

    private int currentIndex = 0;

    public static ReplayPlayback getInstance() {

        return INSTANCE;
    }

    public void loadReplay(
            Path replayFile
    ) {

        try {

            Reader reader =
                    Files.newBufferedReader(
                            replayFile
                    );

            Type type =
                    new TypeToken<
                            List<ReplaySnapshot>
                    >() {}.getType();

            snapshots =
                    gson.fromJson(
                            reader,
                            type
                    );

            reader.close();

            currentIndex = 0;

            finished = false;

            StaffReplay.LOGGER.info(
                    "Loaded replay with {} snapshots",
                    snapshots.size()
            );

        } catch (Exception e) {

            StaffReplay.LOGGER.error(
                    "Failed loading replay",
                    e
            );
        }
    }

    public void startPlayback() {

        if (
                snapshots == null ||
                snapshots.isEmpty()
        ) {

            StaffReplay.LOGGER.warn(
                    "No replay loaded"
            );

            return;
        }

        playing = true;

        finished = false;

        currentIndex = 0;

        StaffReplay.LOGGER.info(
                "Started replay playback"
        );
    }

    public void tick(
            MinecraftClient client
    ) {

        if (!playing) {

            return;
        }

        if (
                snapshots == null ||
                currentIndex >= snapshots.size()
        ) {

            playing = false;

            finished = true;

            StaffReplay.LOGGER.info(
                    "Replay playback finished"
            );

            return;
        }

        ClientPlayerEntity player =
                client.player;

        if (player == null) {

            return;
        }

        ReplaySnapshot snapshot =
                snapshots.get(currentIndex);

        player.setPosition(
                snapshot.x,
                snapshot.y,
                snapshot.z
        );

        player.setYaw(snapshot.yaw);

        player.setPitch(snapshot.pitch);

        currentIndex++;
    }

    public boolean isPlaying() {

        return playing;
    }

    public boolean isFinished() {

        return finished;
    }
}