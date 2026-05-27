package com.example.staffreplay;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HudOverlay {
        private static long bufferStartTime = 0;

    private static final DateTimeFormatter
            TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "HH:mm:ss"
            );

    public static void register() {

        HudRenderCallback.EVENT.register(
                HudOverlay::render
        );
    }


    private static void render(
            DrawContext context,
            RenderTickCounter counter
    ) {

        MinecraftClient client =
                MinecraftClient.getInstance();

        if (
                client.player == null
        ) {

            return;
        }

        int x = 12;
        int y = 12;

        int lineHeight = 12;

        BackgroundRecorder recorder =
                BackgroundRecorder.getInstance();

        /*
         * BUFFER STATUS
         */

        boolean enabled =
                recorder.isRecording();

        /*
         * CONTENT
         */

        String header =
                "● STAFF REPLAY";

        String status =
                enabled
                        ? "Status: ACTIVE"
                        : "Status: DISABLED";

        String clip =
                "Clip Length: " +
                ClipLengthManager.getCurrentLength() +
                "s";

        int fps =
                client.getCurrentFps();

        String fpsText =
                "Game FPS: " + fps;

        String recordingFps =
                "Recording FPS: " +
                recorder.getCurrentRecordingFps();

      int clipLength =
        ClipLengthManager.getCurrentLength();

int currentSecond = 0;

if (enabled) {

    if (bufferStartTime == 0) {

        bufferStartTime =
                System.currentTimeMillis();
    }

    long elapsed =
            System.currentTimeMillis() -
            bufferStartTime;

    currentSecond =
            (int)(
                    (elapsed / 1000)
                    % clipLength
            );

} else {

    bufferStartTime = 0;
}

String bufferTimer =
        "Clip Time: " +
        currentSecond +
        "s/" +
        clipLength +
        "s";
        String time =
                "Time: " +
                LocalTime.now()
                        .format(
                                TIME_FORMAT
                        );

        /*
         * AUTO PANEL WIDTH
         */

        int maxWidth = 0;

        maxWidth = Math.max(
                maxWidth,
                client.textRenderer.getWidth(
                        header
                )
        );

        maxWidth = Math.max(
                maxWidth,
                client.textRenderer.getWidth(
                        status
                )
        );

        maxWidth = Math.max(
                maxWidth,
                client.textRenderer.getWidth(
                        clip
                )
        );

        maxWidth = Math.max(
                maxWidth,
                client.textRenderer.getWidth(
                        fpsText
                )
        );

        maxWidth = Math.max(
                maxWidth,
                client.textRenderer.getWidth(
                        recordingFps
                )
        );

        maxWidth = Math.max(
        maxWidth,
        client.textRenderer.getWidth(
                bufferTimer
        )
);
        maxWidth = Math.max(
                maxWidth,
                client.textRenderer.getWidth(
                        time
                )
        );

        /*
         * PANEL SIZE
         */

        int panelWidth =
                maxWidth + 16;

        int panelHeight =
                94;

        /*
         * BACKGROUND PANEL
         */

        context.fill(
                x - 5,
                y - 5,
                x + panelWidth,
                y + panelHeight,
                0x78000000
        );

        /*
         * HEADER
         */

        context.drawText(
                client.textRenderer,
                header,
                x,
                y,
                enabled
                        ? 0xFF4040
                        : 0x888888,
                true
        );

        /*
         * STATUS
         */

        context.drawText(
                client.textRenderer,
                status,
                x,
                y + (lineHeight * 1),
                enabled
                        ? 0x55FF55
                        : 0xFF5555,
                false
        );

        /*
         * CLIP LENGTH
         */

        context.drawText(
                client.textRenderer,
                clip,
                x,
                y + (lineHeight * 3),
                0x00FFCC,
                false
        );

        /*
         * GAME FPS
         */

        int fpsColor =
                fps >= 100
                        ? 0x55FF55
                        : fps >= 60
                        ? 0xFFFF55
                        : 0xFF5555;

        context.drawText(
                client.textRenderer,
                fpsText,
                x,
                y + (lineHeight * 4),
                fpsColor,
                false
        );

        /*
         * RECORDING FPS
         */

        int recordingColor =
                recorder.getCurrentRecordingFps() >= 30
                        ? 0x55FF55
                        : recorder.getCurrentRecordingFps() >= 20
                        ? 0xFFFF55
                        : 0xFFAA55;

        context.drawText(
                client.textRenderer,
                recordingFps,
                x,
                y + (lineHeight * 5),
                recordingColor,
                false
        );

        /*
         * CHUNKS
         */

        context.drawText(
        client.textRenderer,
        bufferTimer,
        x,
        y + (lineHeight * 6),
        0x55FFFF,
        false
);

        /*
         * TIME
         */

        context.drawText(
                client.textRenderer,
                time,
                x,
                y + (lineHeight * 7),
                0xCCCCCC,
                false
        );

        /*
         * PULSING RECORD DOT
         */

        if (enabled) {

            int pulse =
                    (int)(
                            (
                                    Math.sin(
                                            System.currentTimeMillis() /
                                            220.0
                                    ) + 1
                            ) * 40
                    );

            int red =
                    180 + pulse;

            int pulseColor =
                    (255 << 24) |
                    (red << 16);

            context.fill(
                    x + panelWidth - 16,
                    y + 4,
                    x + panelWidth - 10,
                    y + 10,
                    pulseColor
            );
        }
    }
}