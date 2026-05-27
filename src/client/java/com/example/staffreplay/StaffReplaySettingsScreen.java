package com.example.staffreplay;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.text.Text;

import java.util.List;

public class StaffReplaySettingsScreen
        extends Screen {

    private final Screen parent;

    private List<AudioDevice> devices;

    private int currentDevice = 0;

    protected StaffReplaySettingsScreen(
            Screen parent
    ) {

        super(
                Text.literal(
                        "Staff Replay Settings"
                )
        );

        this.parent = parent;
    }

    @Override
    protected void init() {

        devices =
                AudioDeviceDetector
                        .detectDevices();

        int startY =
                height / 2 - 120;

        /*
         * AUDIO TOGGLE
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal(
                                "Audio: " +
                                (
                                        SettingsManager.isAudioEnabled()
                                        ? "ON"
                                        : "OFF"
                                )
                        ),

                        button -> {

                            SettingsManager.setAudioEnabled(

                                    !SettingsManager
                                            .isAudioEnabled()
                            );

                            button.setMessage(

                                    Text.literal(
                                            "Audio: " +
                                            (
                                                    SettingsManager.isAudioEnabled()
                                                    ? "ON"
                                                    : "OFF"
                                            )
                                    )
                            );
                        }
                ).dimensions(

                        width / 2 - 100,
                        startY,

                        200,
                        20
                ).build()
        );

        /*
         * AUDIO DEVICE
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal(
                                getCurrentDeviceName()
                        ),

                        button -> {

                            if (devices.isEmpty()) {

                                return;
                            }

                            currentDevice++;

                            if (
                                    currentDevice >= devices.size()
                            ) {

                                currentDevice = 0;
                            }

                            AudioDevice device =
                                    devices.get(
                                            currentDevice
                                    );

                            SettingsManager.setAudioDevice(
                                    device.getFfmpegId()
                            );

                            button.setMessage(
                                    Text.literal(
                                            device.getName()
                                    )
                            );
                        }

                ).dimensions(

                        width / 2 - 100,
                        startY + 30,

                        200,
                        20
                ).build()
        );

        /*
         * AUTO EVIDENCE
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal(
                                "Auto Evidence: " +
                                (
                                        SettingsManager.isAutoEvidenceEnabled()
                                        ? "ON"
                                        : "OFF"
                                )
                        ),

                        button -> {

                            SettingsManager.setAutoEvidenceEnabled(

                                    !SettingsManager
                                            .isAutoEvidenceEnabled()
                            );

                            button.setMessage(

                                    Text.literal(
                                            "Auto Evidence: " +
                                            (
                                                    SettingsManager.isAutoEvidenceEnabled()
                                                    ? "ON"
                                                    : "OFF"
                                            )
                                    )
                            );
                        }

                ).dimensions(

                        width / 2 - 100,
                        startY + 60,

                        200,
                        20
                ).build()
        );

        /*
         * XRAY
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal(
                                "Xray: " +
                                (
                                        SettingsManager.isDetectXray()
                                        ? "ON"
                                        : "OFF"
                                )
                        ),

                        button -> {

                            SettingsManager.setDetectXray(

                                    !SettingsManager
                                            .isDetectXray()
                            );

                            button.setMessage(

                                    Text.literal(
                                            "Xray: " +
                                            (
                                                    SettingsManager.isDetectXray()
                                                    ? "ON"
                                                    : "OFF"
                                            )
                                    )
                            );
                        }

                ).dimensions(

                        width / 2 - 100,
                        startY + 90,

                        200,
                        20
                ).build()
        );

        /*
         * CHEATING
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal(
                                "Cheating: " +
                                (
                                        SettingsManager.isDetectCheating()
                                        ? "ON"
                                        : "OFF"
                                )
                        ),

                        button -> {

                            SettingsManager.setDetectCheating(

                                    !SettingsManager
                                            .isDetectCheating()
                            );

                            button.setMessage(

                                    Text.literal(
                                            "Cheating: " +
                                            (
                                                    SettingsManager.isDetectCheating()
                                                    ? "ON"
                                                    : "OFF"
                                            )
                                    )
                            );
                        }

                ).dimensions(

                        width / 2 - 100,
                        startY + 120,

                        200,
                        20
                ).build()
        );

        /*
         * MUTE EVASION
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal(
                                "Mute Evasion: " +
                                (
                                        SettingsManager.isDetectMuteEvasion()
                                        ? "ON"
                                        : "OFF"
                                )
                        ),

                        button -> {

                            SettingsManager.setDetectMuteEvasion(

                                    !SettingsManager
                                            .isDetectMuteEvasion()
                            );

                            button.setMessage(

                                    Text.literal(
                                            "Mute Evasion: " +
                                            (
                                                    SettingsManager.isDetectMuteEvasion()
                                                    ? "ON"
                                                    : "OFF"
                                            )
                                    )
                            );
                        }

                ).dimensions(

                        width / 2 - 100,
                        startY + 150,

                        200,
                        20
                ).build()
        );

        /*
         * GLITCH ABUSE
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal(
                                "Glitch Abuse: " +
                                (
                                        SettingsManager.isDetectGlitchAbuse()
                                        ? "ON"
                                        : "OFF"
                                )
                        ),

                        button -> {

                            SettingsManager.setDetectGlitchAbuse(

                                    !SettingsManager
                                            .isDetectGlitchAbuse()
                            );

                            button.setMessage(

                                    Text.literal(
                                            "Glitch Abuse: " +
                                            (
                                                    SettingsManager.isDetectGlitchAbuse()
                                                    ? "ON"
                                                    : "OFF"
                                            )
                                    )
                            );
                        }

                ).dimensions(

                        width / 2 - 100,
                        startY + 180,

                        200,
                        20
                ).build()
        );

        /*
         * INAPPROPRIATE COSMETIC
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal(
                                "Inappropriate Cosmetic: " +
                                (
                                        SettingsManager.isDetectInappropriateCosmetic()
                                        ? "ON"
                                        : "OFF"
                                )
                        ),

                        button -> {

                            SettingsManager.setDetectInappropriateCosmetic(

                                    !SettingsManager
                                            .isDetectInappropriateCosmetic()
                            );

                            button.setMessage(

                                    Text.literal(
                                            "Inappropriate Cosmetic: " +
                                            (
                                                    SettingsManager.isDetectInappropriateCosmetic()
                                                    ? "ON"
                                                    : "OFF"
                                            )
                                    )
                            );
                        }

                ).dimensions(

                        width / 2 - 100,
                        startY + 210,

                        200,
                        20
                ).build()
        );

        /*
         * DONE BUTTON
         */

        addDrawableChild(

                ButtonWidget.builder(

                        Text.literal("Done"),

                        button -> {

                            client.setScreen(
                                    parent
                            );
                        }

                ).dimensions(

                        width / 2 - 100,
                        startY + 250,

                        200,
                        20
                ).build()
        );
    }

    private String getCurrentDeviceName() {

        if (devices == null || devices.isEmpty()) {

            return "No Audio Devices";
        }

        return devices
                .get(currentDevice)
                .getName();
    }
}