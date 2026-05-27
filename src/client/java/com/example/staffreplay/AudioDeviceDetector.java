package com.example.staffreplay;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;

public class AudioDeviceDetector {

    public static List<AudioDevice> detectDevices() {

        List<AudioDevice> devices =
                new ArrayList<>();

        try {

            String os =
                    System.getProperty(
                            "os.name"
                    ).toLowerCase();

            ProcessBuilder builder;

            /*
             * MACOS
             */

            if (os.contains("mac")) {

                builder =
                        new ProcessBuilder(

                                "/opt/homebrew/bin/ffmpeg",

                                "-f",
                                "avfoundation",

                                "-list_devices",
                                "true",

                                "-i",
                                ""
                        );
            }

            /*
             * WINDOWS
             */

            else if (os.contains("win")) {

                builder =
                        new ProcessBuilder(

                                "ffmpeg",

                                "-list_devices",
                                "true",

                                "-f",
                                "dshow",

                                "-i",
                                "dummy"
                        );
            }

            /*
             * LINUX
             */

            else {

                return devices;
            }

            builder.redirectErrorStream(
                    true
            );

            Process process =
                    builder.start();

            BufferedReader reader =
                    new BufferedReader(

                            new InputStreamReader(
                                    process.getInputStream()
                            )
                    );

            String line;

            while (
                    (line = reader.readLine()) != null
            ) {

                StaffReplay.LOGGER.info(
                        "[AUDIO] {}",
                        line
                );

                /*
                 * MAC PARSE
                 */

                if (
                        os.contains("mac") &&
                        line.contains("]")
                ) {

                    try {

                        int start =
                                line.indexOf("[") + 1;

                        int end =
                                line.indexOf("]");

                        String index =
                                line.substring(
                                        start,
                                        end
                                );

                        String name =
                                line.substring(
                                        end + 1
                                ).trim();

                        devices.add(

                                new AudioDevice(
                                        name,
                                        ":" + index
                                )
                        );

                    } catch (Exception ignored) {}
                }
            }

        } catch (Exception e) {

            StaffReplay.LOGGER.error(
                    "Failed detecting audio devices",
                    e
            );
        }

        return devices;
    }
}