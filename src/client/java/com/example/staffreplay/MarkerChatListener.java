package com.example.staffreplay;

import net.minecraft.client.MinecraftClient;

import net.minecraft.text.Text;

public class MarkerChatListener {

    public static boolean waitingForMarker =
            false;

    public static void handleChatMessage(
            String message
    ) {

        waitingForMarker = false;

        /*
         * ADD MARKER
         */

        BackgroundRecorder.addMarker(
                message
        );

        MinecraftClient client =
                MinecraftClient.getInstance();

        if (client.player != null) {

            client.player.sendMessage(

                    Text.literal(
                            "§a§l[STAFF-REPLAY] §7Added marker: §f" +
                            message
                    ),

                    false
            );
        }

        StaffReplay.LOGGER.info(
                "Added marker: {}",
                message
        );
    }
}