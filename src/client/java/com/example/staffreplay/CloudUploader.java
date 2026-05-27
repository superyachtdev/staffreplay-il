package com.example.staffreplay;

import net.minecraft.client.MinecraftClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.cert.X509Certificate;
import java.net.URLEncoder;
import javax.net.ssl.X509TrustManager;

import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;

import java.security.cert.X509Certificate;

public class CloudUploader {

    private static final String UPLOAD_URL =
    "https://gateway.staffreplay.lol/upload";

    static {

        try {

            TrustManager[] trustAllCerts =
                    new TrustManager[] {

                            new X509TrustManager() {

                                public X509Certificate[] getAcceptedIssuers() {

                                    return new X509Certificate[0];
                                }

                                public void checkClientTrusted(
                                        X509Certificate[] certs,
                                        String authType
                                ) {}

                                public void checkServerTrusted(
                                        X509Certificate[] certs,
                                        String authType
                                ) {}
                            }
                    };

            SSLContext sslContext =
                    SSLContext.getInstance(
                            "TLS"
                    );

            sslContext.init(
                    null,
                    trustAllCerts,
                    new java.security.SecureRandom()
            );

            HttpsURLConnection.setDefaultSSLSocketFactory(
                    sslContext.getSocketFactory()
            );

            HostnameVerifier allHostsValid =
                    (hostname, session) -> true;

            HttpsURLConnection.setDefaultHostnameVerifier(
                    allHostsValid
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void uploadReplay(
            Path videoFile
    ) {

        new Thread(() -> {

            try {

                if (
                        videoFile == null ||
                        !Files.exists(videoFile)
                ) {

                    StaffReplay.LOGGER.error(
                            "Video file does not exist"
                    );

                    return;
                }

                String originalFileName =
        videoFile
                .getFileName()
                .toString();

String fileName =
        originalFileName;

         Path thumbnailFile =
    videoFile.resolveSibling(

            originalFileName.replace(
                    ".mp4",
                    "_thumb.jpg"
            )
    );
        /*
 * GENERATE THUMBNAIL
 */

try {

    Process thumbnailProcess =
            new ProcessBuilder(

                    BackgroundRecorder
                            .getInstance()
                            .getFFmpegPath(),

                    "-y",

                    "-ss",
                    "00:00:01",

                    "-i",
                    videoFile.toString(),

                    "-frames:v",
                    "1",

                    "-q:v",
                    "2",

                    thumbnailFile.toString()

            ).start();

    thumbnailProcess.waitFor();

    StaffReplay.LOGGER.info(
            "Generated thumbnail"
    );

} catch (Exception e) {

    StaffReplay.LOGGER.error(
            "Failed generating thumbnail",
            e
    );
}

                long fileSize =
                        Files.size(
                                videoFile
                        );

                StaffReplay.LOGGER.info(
                        "Uploading clip: {} ({} bytes)",
                        fileName,
                        fileSize
                );

                /*
 * UPLOAD THUMBNAIL
 */

if (Files.exists(thumbnailFile)) {

    try {

        String thumbnailFileName =
                originalFileName.replace(
                        ".mp4",
                        "_thumb.jpg"
                );

        byte[] thumbnailData =
                Files.readAllBytes(
                        thumbnailFile
                );

        URL thumbnailUrl =
                new URL(

                        UPLOAD_URL +
                        "?name=" +
                        URLEncoder.encode(
                                thumbnailFileName,
                                StandardCharsets.UTF_8
                        )
                );

        HttpURLConnection thumbnailConnection =
                (HttpURLConnection)
                        thumbnailUrl.openConnection();

        thumbnailConnection.setRequestMethod(
                "POST"
        );

        thumbnailConnection.setDoOutput(
                true
        );

        thumbnailConnection.setUseCaches(
                false
        );

        thumbnailConnection.setRequestProperty(
                "Content-Type",
                "image/jpeg"
        );

        OutputStream thumbnailOutput =
                thumbnailConnection.getOutputStream();

        thumbnailOutput.write(
                thumbnailData
        );

        thumbnailOutput.flush();

        thumbnailOutput.close();

        int thumbnailResponse =
                thumbnailConnection.getResponseCode();

        StaffReplay.LOGGER.info(
                "Thumbnail upload response: {}",
                thumbnailResponse
        );

    } catch (Exception e) {

        StaffReplay.LOGGER.error(
                "Thumbnail upload failed",
                e
        );
    }
}

                byte[] data =
                        Files.readAllBytes(
                                videoFile
                        );

                        Path markerFile =
        videoFile.resolveSibling(

                originalFileName.replace(
                        ".mp4",
                        ".json"
                )
        );

       

                URL url =
        new URL(
                UPLOAD_URL +
                "?name=" +
URLEncoder.encode(
        fileName,
        StandardCharsets.UTF_8
)
        );

                HttpURLConnection connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "POST"
                );

                connection.setDoOutput(
                        true
                );

                connection.setUseCaches(
                        false
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "video/mp4"
                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        data
                );

                output.flush();

                output.close();

                int responseCode =
                        connection.getResponseCode();

                InputStream input =
                        connection.getInputStream();

                String responseBody =
                        new String(
                                input.readAllBytes(),
                                StandardCharsets.UTF_8
                        );

                input.close();

                StaffReplay.LOGGER.info(
                        "Upload response: {}",
                        responseCode
                );

                StaffReplay.LOGGER.info(
                        "Upload body: {}",
                        responseBody
                );

                if (responseCode == 200) {

                    StaffReplay.LOGGER.info(
                            "Replay uploaded successfully"
                    );

                    /*
 * UPLOAD MARKERS
 */

if (Files.exists(markerFile)) {

    try {

       String markerFileName =
        originalFileName.replace(
                ".mp4",
                ".json"
        );

        byte[] markerData =
                Files.readAllBytes(
                        markerFile
                );

        URL markerUrl =
                new URL(

                        UPLOAD_URL +
                        "?name=" +
                        URLEncoder.encode(
                                markerFileName,
                                StandardCharsets.UTF_8
                        )
                );

        HttpURLConnection markerConnection =
                (HttpURLConnection)
                        markerUrl.openConnection();

        markerConnection.setRequestMethod(
                "POST"
        );

        markerConnection.setDoOutput(
                true
        );

        markerConnection.setUseCaches(
                false
        );

        markerConnection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        OutputStream markerOutput =
                markerConnection.getOutputStream();

        markerOutput.write(
                markerData
        );

        markerOutput.flush();

        markerOutput.close();

        int markerResponse =
                markerConnection.getResponseCode();

        StaffReplay.LOGGER.info(
                "Marker upload response: {}",
                markerResponse
        );

        if (markerResponse == 200) {

            StaffReplay.LOGGER.info(
                    "Uploaded marker file"
            );

        } else {

            StaffReplay.LOGGER.error(
                    "Failed uploading markers"
            );
        }

    } catch (Exception e) {

        StaffReplay.LOGGER.error(
                "Marker upload failed",
                e
        );
    }
}

                    /*
                     * PARSE FILE NAME
                     */

                    String cleanName =
                            originalFileName.replace(
                                    ".mp4",
                                    ""
                            );

                    String[] split =
                            cleanName.split(
                                    "__"
                            );

                    if (split.length >= 2) {

                        String staff =
                                split[0];

                        String player =
                                split[1]
                                        .replace(
                                                "_",
                                                " "
                                        );

                        MinecraftClient client =
                                MinecraftClient.getInstance();

                        if (
                                client.player != null &&
                                client.getNetworkHandler() != null
                        ) {

                            client.player.sendMessage(

        net.minecraft.text.Text.literal(
                "§e§l[STAFF-REPLAY] " +
                "§e§o" + staff +
                " §7just uploaded a clip for the player " +
                "§f" + player +
                "§7."
        ),

        false
);

                            StaffReplay.LOGGER.info(
                                    "Sent global upload message"
                            );
                        }
                    }

                    Files.deleteIfExists(
                            videoFile
                    );

                    Files.deleteIfExists(
        markerFile
);

                    StaffReplay.LOGGER.info(
                            "Deleted local file"
                    );

                } else {

                    StaffReplay.LOGGER.error(
                            "Upload failed"
                    );
                }

            } catch (Exception e) {

                StaffReplay.LOGGER.error(
                        "Failed uploading replay",
                        e
                );
            }

        }).start();
    }
}