package com.example.staffreplay;

import net.minecraft.client.MinecraftClient;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.nio.charset.StandardCharsets;

import java.util.LinkedList;

public class ReplayManager {

    private static final ReplayManager INSTANCE =
            new ReplayManager();

    public static ReplayManager getInstance() {

        return INSTANCE;
    }

    public void startRecording(
            MinecraftClient client
    ) {

        ReplayRecorder
                .getInstance()
                .clear();

        StaffReplay.LOGGER.info(
                "Started replay recording"
        );
    }

    public Path saveReplay(
            MinecraftClient client,
            String replayName
    ) {

        try {

            BackgroundRecorder recorder =
                    BackgroundRecorder.getInstance();

                    recorder.finalizeCurrentChunk(client);

                    Thread.sleep(300);
            LinkedList<Path> chunks =
                    recorder.getChunkFiles();

            if (chunks.isEmpty()) {

                StaffReplay.LOGGER.warn(
                        "No chunks available"
                );

                return null;
            }

            Path outputDir =
                    client.runDirectory
                            .toPath()
                            .resolve("staffreplay")
                            .resolve("clips");

            Files.createDirectories(
                    outputDir
            );

            String timestamp =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyy-MM-dd_HH-mm-ss"
                                    )
                            );

            Path concatFile =
                    outputDir.resolve(
                            "concat_" +
                            timestamp +
                            ".txt"
                    );

            StringBuilder builder =
                    new StringBuilder();

            for (Path chunk : chunks) {

                builder.append(
                        "file '"
                );

                builder.append(
                        chunk.toAbsolutePath()
                );

                builder.append(
                        "'\n"
                );
            }

            Files.writeString(
                    concatFile,
                    builder.toString()
            );

            Path finalOutput =
                    outputDir.resolve(
                            "clip_" +
                            timestamp +
                            ".mp4"
                    );

         ProcessBuilder processBuilder =
        new ProcessBuilder(

                BackgroundRecorder
                        .getInstance()
                        .getFFmpegPath(),

                "-y",

                "-f",
                "concat",

                "-safe",
                "0",

                "-i",
                concatFile
                        .toAbsolutePath()
                        .toString(),

                "-c",
                "copy",

                "-movflags",
                "+faststart",

                finalOutput
                        .toAbsolutePath()
                        .toString()
        );

            processBuilder.redirectErrorStream(
                    true
            );

            Process process =
                    processBuilder.start();

                    String ffmpegOutput =
        new String(
                process.getInputStream()
                        .readAllBytes()
        );

StaffReplay.LOGGER.info(
        "FFmpeg merge output: {}",
        ffmpegOutput
);

            int exitCode =
                    process.waitFor();

            StaffReplay.LOGGER.info(
                    "FFmpeg merge exit code: {}",
                    exitCode
            );

            StaffReplay.LOGGER.info(
        "Created clip: {}",
        finalOutput
);

/*
 * SAVE MARKERS
 */

try {

    List<Marker> markers =
            BackgroundRecorder.getMarkers();

    Path markerFile =
            outputDir.resolve(

                    finalOutput
                            .getFileName()
                            .toString()
                            .replace(
                                    ".mp4",
                                    ".json"
                            )
            );

    StringBuilder markerJson =
            new StringBuilder();

    markerJson.append("[");

    for (
            int i = 0;
            i < markers.size();
            i++
    ) {

        Marker marker =
                markers.get(i);

        markerJson.append("{");

        markerJson.append(
                "\"timestamp\":"
        );

        markerJson.append(
                marker.getTimestamp()
        );

        markerJson.append(",");

        markerJson.append(
                "\"note\":\""
        );

        markerJson.append(

                marker.getNote()
                        .replace("\"", "'")
        );

        markerJson.append("\"");

        markerJson.append("}");

        if (
                i < markers.size() - 1
        ) {

            markerJson.append(",");
        }
    }

    markerJson.append("]");

    Files.writeString(

            markerFile,

            markerJson.toString(),

            StandardCharsets.UTF_8
    );

    StaffReplay.LOGGER.info(
            "Saved markers: {}",
            markerFile
    );

} catch (Exception e) {

    StaffReplay.LOGGER.error(
            "Failed saving markers",
            e
    );
}

if (!Files.exists(finalOutput)) {

    StaffReplay.LOGGER.error(
            "Final merged replay missing: {}",
            finalOutput
    );

    return null;
}

return finalOutput;

        } catch (Exception e) {

            StaffReplay.LOGGER.error(
                    "Failed creating clip",
                    e
            );

            return null;
        }
    }
}