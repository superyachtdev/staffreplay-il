package com.example.staffreplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.MinecraftClient;

import java.io.Writer;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

public class ReplaySerializer {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    public static Path saveReplay(
            List<ReplaySnapshot> snapshots,
            MinecraftClient client
    ) {

        try {

            Path replayDir =
                    client.runDirectory
                            .toPath()
                            .resolve("staffreplay")
                            .resolve("replays");

            Files.createDirectories(
                    replayDir
            );

            String timestamp =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyy-MM-dd_HH-mm-ss"
                                    )
                            );

            Path output =
                    replayDir.resolve(
                            "replay_" +
                            timestamp +
                            ".json"
                    );

            Writer writer =
                    Files.newBufferedWriter(
                            output
                    );

            GSON.toJson(
                    snapshots,
                    writer
            );

            writer.close();

            StaffReplay.LOGGER.info(
                    "Replay saved: {}",
                    output.toAbsolutePath()
            );

            return output;

        } catch (Exception e) {

            StaffReplay.LOGGER.error(
                    "Failed saving replay",
                    e
            );
        }

        return null;
    }
}