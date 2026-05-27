package com.example.staffreplay;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

public class Keybinds {

    public static KeyBinding SAVE_REPLAY;

    public static KeyBinding CYCLE_LENGTH;

    public static KeyBinding TOGGLE_BUFFER;

    public static KeyBinding OPEN_SETTINGS;

    public static KeyBinding ADD_MARKER;

    public static KeyBinding CYCLE_RECORDING_FPS;

    public static void register() {

        SAVE_REPLAY =
                KeyBindingHelper.registerKeyBinding(
                        new KeyBinding(
                                "key.staffreplay.save",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_R,
                                "category.staffreplay"
                        )
                );

        CYCLE_LENGTH =
                KeyBindingHelper.registerKeyBinding(
                        new KeyBinding(
                                "key.staffreplay.cycle_length",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_O,
                                "category.staffreplay"
                        )
                );

        TOGGLE_BUFFER =
                KeyBindingHelper.registerKeyBinding(
                        new KeyBinding(
                                "key.staffreplay.toggle_buffer",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_P,
                                "category.staffreplay"
                        )
                );

        ADD_MARKER =
                KeyBindingHelper.registerKeyBinding(
                        new KeyBinding(
                                "key.staffreplay.add_marker",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_M,
                                "category.staffreplay"
                        )
                );

        /*
         * CYCLE RECORDING FPS
         */

        OPEN_SETTINGS =
        KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.staffreplay.settings",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_O,
                        "category.staffreplay"
                )
        );

        CYCLE_RECORDING_FPS =
                KeyBindingHelper.registerKeyBinding(
                        new KeyBinding(
                                "key.staffreplay.cycle_recording_fps",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_G,
                                "category.staffreplay"
                        )
                );
    }

    

    public static void handleKeybinds(
            MinecraftClient client
    ) {

        /*
         * CYCLE RECORDING FPS
         */

        while (CYCLE_RECORDING_FPS.wasPressed()) {

            BackgroundRecorder recorder =
                    BackgroundRecorder.getInstance();

            recorder.cycleRecordingFps();

            if (client.player != null) {

                client.player.sendMessage(

                        Text.literal(
                                "§e§l[STAFF-REPLAY] §7Recording FPS: §f" +
                                recorder.getCurrentRecordingFps()
                        ),

                        false
                );
            }
        }

        /*
         * ADD TIMELINE MARKER
         */

        while (ADD_MARKER.wasPressed()) {

            if (client.player == null) {

                return;
            }

            /*
             * PREVENT DOUBLE PROMPTS
             */

            if (MarkerChatListener.waitingForMarker) {

                return;
            }

            MarkerChatListener.waitingForMarker =
                    true;

            client.player.sendMessage(

                    Text.literal(
                            "§e§l[STAFF-REPLAY] §7Type marker note in chat..."
                    ),

                    false
            );

            StaffReplay.LOGGER.info(
                    "Waiting for marker note..."
            );
        }
    }
}