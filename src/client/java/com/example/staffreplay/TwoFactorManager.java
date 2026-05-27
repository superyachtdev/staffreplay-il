package com.example.staffreplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.StandardCharsets;

public class TwoFactorManager {

    private static final String API_URL =
            "https://staffreplay.lol/generate-code";

    private static final String MOD_SECRET =
            "my-super-secret-key-123";

    public static void generateCode(
            MinecraftClient client
    ) {

        new Thread(() -> {

            try {

                if (
                        client.player == null
                ) {

                    return;
                }

                String username =
                        client.player
                                .getGameProfile()
                                .getName();

                String uuid =
                        client.player
                                .getUuidAsString();

                URL url =
                        new URL(API_URL);

                HttpURLConnection connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "POST"
                );

                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                connection.setRequestProperty(
                        "x-mod-secret",
                        MOD_SECRET
                );

                String body =
                        "{"
                        + "\"username\":\"" +
                        username +
                        "\","
                        + "\"uuid\":\"" +
                        uuid +
                        "\""
                        + "}";

                try (
                        OutputStream os =
                                connection
                                        .getOutputStream()
                ) {

                    os.write(
                            body.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );
                }

                String response =
                        new String(
                                connection
                                        .getInputStream()
                                        .readAllBytes(),
                                StandardCharsets.UTF_8
                        );

                String code =
                        response
                                .split("\"code\":\"")[1]
                                .split("\"")[0];

                client.execute(() -> {

                    client.player.sendMessage(

                            Text.literal(
                                    "2FA Code: " + code
                            ).formatted(
                                    Formatting.GREEN,
                                    Formatting.BOLD
                            ),

                            false
                    );
                });

            } catch (Exception e) {

                StaffReplay.LOGGER.error(
                        "Failed generating 2FA code",
                        e
                );

                client.execute(() -> {

                    if (
                            client.player != null
                    ) {

                        client.player.sendMessage(

                                Text.literal(
                                        "Failed generating 2FA code"
                                ).formatted(
                                        Formatting.RED
                                ),

                                false
                        );
                    }
                });
            }

        }).start();
    }
}