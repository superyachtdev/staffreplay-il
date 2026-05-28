package com.example.staffreplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import java.io.InputStream;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import java.util.concurrent.CompletableFuture;
import org.lwjgl.opengl.GL32;
import java.util.concurrent.LinkedBlockingQueue;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

public class BackgroundRecorder {

       private static final int[] FPS_OPTIONS = {
    10,
    15,
    20,
    25,
    30,
    35
};

private int fpsIndex = 2;

private int currentRecordingFps =
        FPS_OPTIONS[fpsIndex];

private long frameTimeNs =
        1_000_000_000L / currentRecordingFps;

    private static final BackgroundRecorder INSTANCE =
            new BackgroundRecorder();

    private Process ffmpegProcess;

    private OutputStream ffmpegInput;

    private WritableByteChannel ffmpegChannel;

    private Path currentOutput;

    private boolean recording = false;
    private boolean enabled = true;

    private long chunkStartTime = 0L;

    private int chunkIndex = 0;

    public int getCurrentRecordingFps() {

    return currentRecordingFps;
}

    private final LinkedList<ChunkInfo>
        chunkFiles =
        new LinkedList<>();

private final List<Integer> pendingUnmaps =

        new ArrayList<>();

private final BlockingQueue<java.nio.ByteBuffer>

        availableBuffers =

        new LinkedBlockingQueue<>();

        private float captureScale = 0.5f;




    private static final long
        CHUNK_DURATION_NS =
        15_000_000_000L;

        

    /*
 * TARGET RECORDING FPS
 */

    private long lastFrameTime = 0L;

    /*
     * OUTPUT SIZE
     */

   private int outputWidth;

private int outputHeight;

        /*
 * INTERNAL CAPTURE SCALE
 */

    /*
     * REUSABLE MEMORY
     */
private int[] pboIds = new int[8];

private int currentPboIndex = 0;


private int frameByteSize;

  private final BlockingQueue<MappedFrame>
        frameQueue =
        new LinkedBlockingQueue<>(128);
        

private Thread encoderThread;
private volatile boolean rotatingChunk = false;

/*
 * DEDICATED REPLAY FRAMEBUFFER
 */

private static final List<Marker>
        markers =
        new ArrayList<>();

private static class ChunkInfo {

    Path path;

    long durationMs;

    ChunkInfo(
            Path path,
            long durationMs
    ) {

        this.path = path;
        this.durationMs = durationMs;
    }
}

private static class MappedFrame {

    java.nio.ByteBuffer mappedBuffer;

    int pboIndex;

    MappedFrame(

            java.nio.ByteBuffer mappedBuffer,

            int pboIndex

    ) {

        this.mappedBuffer = mappedBuffer;

        this.pboIndex = pboIndex;

    }

}

private static long
        recordingStartTime;

    public static BackgroundRecorder getInstance() {

        return INSTANCE;
    }

private void ensureFFmpegInstalled() {

    try {

        String os =
                System.getProperty("os.name")
                        .toLowerCase();

        String resourcePath;

        String outputName;

        /*
         * WINDOWS
         */

        if (os.contains("win")) {

            resourcePath =
                    "/ffmpeg/windows/ffmpeg.exe";

            outputName =
                    "ffmpeg.exe";

        /*
         * MAC
         */

        } else if (os.contains("mac")) {

            resourcePath =
                    "/ffmpeg/mac/ffmpeg";

            outputName =
                    "ffmpeg";

        /*
         * LINUX
         */

        } else {

            resourcePath =
                    "/ffmpeg/linux/ffmpeg";

            outputName =
                    "ffmpeg";
        }

        Path ffmpegDir =
                MinecraftClient.getInstance()
                        .runDirectory
                        .toPath()
                        .resolve("staffreplay")
                        .resolve("ffmpeg");

        Files.createDirectories(ffmpegDir);

        Path outputPath =
                ffmpegDir.resolve(outputName);

        /*
         * ALREADY EXISTS
         */

        if (Files.exists(outputPath)) {

            return;
        }

        /*
         * EXTRACT FFMPEG
         */

        try (
                InputStream input =
                        getClass().getResourceAsStream(
                                resourcePath
                        )
        ) {

            if (input == null) {

                throw new RuntimeException(
                        "Missing bundled FFmpeg: " +
                        resourcePath
                );
            }

            Files.copy(
                    input,
                    outputPath
            );
        }

        /*
         * MAKE EXECUTABLE
         */

        outputPath.toFile().setExecutable(true);

        StaffReplay.LOGGER.info(
                "Installed bundled FFmpeg"
        );

    } catch (Exception e) {

        throw new RuntimeException(
                "Failed installing FFmpeg",
                e
        );
    }
}

    public boolean isRecording() {

        return recording;
    }

    public boolean isEnabled() {

    return enabled;
}

public Path getCurrentOutput() {

    return currentOutput;
}

public void toggleEnabled(
        MinecraftClient client
) {

    /*
     * DISABLE
     */

    if (enabled) {

        enabled = false;

        stop();

        StaffReplay.LOGGER.info(
                "Replay buffer disabled"
        );

        return;
    }

    /*
     * ENABLE
     */

    try {

        start(client);

        enabled = true;

        StaffReplay.LOGGER.info(
                "Replay buffer enabled"
        );

    } catch (Exception e) {

        enabled = false;

        StaffReplay.LOGGER.error(
                "Failed enabling replay buffer",
                e
        );
    }
}

public void stop() {

    try {

        recording = false;

        if (encoderThread != null) {

            encoderThread.join(2000);

            encoderThread = null;
        }

        if (ffmpegInput != null) {

         
            ffmpegInput.close();
        }

        for (ChunkInfo chunk : chunkFiles) {

            try {

                Files.deleteIfExists(
                        chunk.path
                );

            } catch (Exception ignored) {}
        }

        chunkFiles.clear();

        frameQueue.clear();
    

        for (int pboId : pboIds) {

            if (pboId != 0) {

                GL15.glDeleteBuffers(pboId);
            }
        }

        if (currentOutput != null) {

            try {

                Files.deleteIfExists(
                        currentOutput
                );

            } catch (Exception ignored) {}
        }

    } catch (Exception e) {

        StaffReplay.LOGGER.error(
                "Failed stopping recorder",
                e
        );
    }
}

    public LinkedList<Path> getChunkFiles() {

    LinkedList<Path> paths =
            new LinkedList<>();

    for (ChunkInfo info : chunkFiles) {

        paths.add(info.path);
    }

    return paths;
}

    public void start(
            MinecraftClient client
    ) {
        if (recording) {

               

        return;

    }

        try {

                 ensureFFmpegInstalled();
                 
            Path tempDir =
                    client.runDirectory
                            .toPath()
                            .resolve("staffreplay")
                            .resolve("buffer");

            Files.createDirectories(
                    tempDir
            );

            currentOutput =
                    tempDir.resolve(
                            "chunk_0.mp4"
                    );

Framebuffer framebuffer =
    client.getFramebuffer();

outputWidth =  framebuffer.viewportWidth;
outputHeight =  framebuffer.viewportHeight;

     frameByteSize =
        outputWidth *
        outputHeight *
        4;


      for (int i = 0; i < 16; i++) {

    availableBuffers.offer(
            java.nio.ByteBuffer.allocateDirect(
                    frameByteSize
            )
    );
}
     

for (int i = 0; i < pboIds.length; i++) {

    pboIds[i] = GL15.glGenBuffers();

    GL15.glBindBuffer(
            GL21.GL_PIXEL_PACK_BUFFER,
            pboIds[i]
    );

   GL15.glBufferData(
    GL21.GL_PIXEL_PACK_BUFFER,
    frameByteSize,
    GL15.GL_STREAM_READ
);
}

GL15.glBindBuffer(
        GL21.GL_PIXEL_PACK_BUFFER,
        0
);

GL11.glPixelStorei(
        GL11.GL_PACK_ALIGNMENT,
        1
);

            startFFmpegProcess(
                    outputWidth,
                    outputHeight
            );

          chunkStartTime =
    System.nanoTime();

recordingStartTime =
        System.currentTimeMillis();

markers.clear();

recording = true;
lastFrameTime = System.nanoTime();
            startEncoderThread();

            

            StaffReplay.LOGGER.info(
                    "Background recorder started"
            );

        } catch (Exception e) {

    StaffReplay.LOGGER.error(
            "Failed starting recorder",
            e
    );

    throw new RuntimeException(e);
}
    }

private void startEncoderThread() {

    encoderThread = new Thread(() -> {

        while (recording || !frameQueue.isEmpty()) {

            try {

                MappedFrame queued =
                        frameQueue.poll(
                                100,
                                java.util.concurrent.TimeUnit.MILLISECONDS
                        );

                if (queued == null) {

                    continue;
                }

                java.nio.ByteBuffer mapped =
                        queued.mappedBuffer;

                java.nio.ByteBuffer frameCopy =
                        availableBuffers.poll();

                if (frameCopy == null) {

                    continue;
                }

                frameCopy.clear();

                mapped.limit(frameByteSize);

                frameCopy.put(mapped);

                frameCopy.flip();

                /*
                 * UNMAP PBO
                 */

              synchronized (pendingUnmaps) {

    pendingUnmaps.add(
            queued.pboIndex
    );
}

                /*
                 * WRITE TO FFMPEG
                 */

                WritableByteChannel channel =
                        ffmpegChannel;

                if (channel != null) {

                    while (frameCopy.hasRemaining()) {

                        channel.write(frameCopy);
                    }
                }

                frameCopy.clear();

                availableBuffers.offer(frameCopy);

            } catch (Exception e) {

                StaffReplay.LOGGER.error(
                        "Encoder thread failed",
                        e
                );
            }
        }

    });

    encoderThread.setDaemon(true);

    encoderThread.setPriority(Thread.NORM_PRIORITY);

    encoderThread.start();
}

    /*
     * GPU ENCODERS
     */

    private String getEncoder() {

    String os =
            System.getProperty(
                    "os.name"
            ).toLowerCase();

    /*
     * MACOS
     */

    if (os.contains("mac")) {

        return "h264_videotoolbox";
    }

    /*
     * WINDOWS
     */

    if (os.contains("win")) {

        try {

            Process process =
                    new ProcessBuilder(

                            getFFmpegPath(),

                            "-hide_banner",

                            "-encoders"
                    ).start();

            String encoders =
                    new String(
                            process
                                    .getInputStream()
                                    .readAllBytes()
                    );

            /*
             * NVIDIA
             */

            if (encoders.contains("h264_nvenc")) {

                StaffReplay.LOGGER.info(
                        "Using NVIDIA NVENC"
                );

                return "h264_nvenc";
            }

            /*
             * AMD
             */

            if (encoders.contains("h264_amf")) {

                StaffReplay.LOGGER.info(
                        "Using AMD AMF"
                );

                return "h264_amf";
            }

            /*
             * INTEL
             */

            if (encoders.contains("h264_qsv")) {

                StaffReplay.LOGGER.info(
                        "Using Intel QuickSync"
                );

                return "h264_qsv";
            }

        } catch (Exception ignored) {}

        /*
         * FALLBACK
         */

        StaffReplay.LOGGER.info(
                "Using libx264 fallback"
        );

        return "libx264";
    }

    /*
     * LINUX
     */

    try {

        Process process =
                new ProcessBuilder(

                        getFFmpegPath(),

                        "-hide_banner",

                        "-encoders"
                ).start();

        String encoders =
                new String(
                        process
                                .getInputStream()
                                .readAllBytes()
                );

        /*
         * NVIDIA
         */

        if (encoders.contains("h264_nvenc")) {

            return "h264_nvenc";
        }

        /*
         * VAAPI
         */

        if (encoders.contains("h264_vaapi")) {

            return "h264_vaapi";
        }

    } catch (Exception ignored) {}

    /*
     * FALLBACK
     */

    return "libx264";
}

    /*
     * CROSS PLATFORM FFMPEG
     */

    public String getFFmpegPath() {

    String os =
            System.getProperty(
                    "os.name"
            ).toLowerCase();

    Path ffmpegDir =
            MinecraftClient.getInstance()
                    .runDirectory
                    .toPath()
                    .resolve("staffreplay")
                    .resolve("ffmpeg");

    /*
     * WINDOWS
     */

    if (os.contains("win")) {

        return ffmpegDir
                .resolve("ffmpeg.exe")
                .toAbsolutePath()
                .toString();
    }

    /*
     * MAC / LINUX
     */

    return ffmpegDir
            .resolve("ffmpeg")
            .toAbsolutePath()
            .toString();
}
private List<String> getAudioInputArgs() {

    List<String> args =
            new ArrayList<>();

    /*
     * AUDIO DISABLED
     */

    if (
            !SettingsManager.isAudioEnabled()
    ) {

        return args;
    }

    String device =
            SettingsManager.getAudioDevice();

    if (
            device == null ||
            device.isBlank()
    ) {

        return args;
    }

    String os =
            System.getProperty(
                    "os.name"
            ).toLowerCase();

    /*
     * MACOS
     */

    if (os.contains("mac")) {

        args.add("-f");
        args.add("avfoundation");

        args.add("-i");
        args.add(device);

        return args;
    }

    /*
     * WINDOWS
     */

    if (os.contains("win")) {

        args.add("-f");
        args.add("dshow");

        args.add("-i");
        args.add(device);

        return args;
    }

    /*
     * LINUX
     */

    args.add("-f");
    args.add("pulse");

    args.add("-i");
    args.add(device);

    return args;
}

    private void startFFmpegProcess(
        int width,
        int height
) throws Exception {

    String encoder =
            getEncoder();

    List<String> command =
        new ArrayList<>();

command.add(
        getFFmpegPath()
);

command.add("-y");

/*
 * VIDEO INPUT
 */

command.add("-f");
command.add("rawvideo");

command.add("-pix_fmt");
command.add("bgra");


command.add("-video_size");
command.add(
        outputWidth + "x" + outputHeight
);

command.add("-framerate");
command.add(
        String.valueOf(
                currentRecordingFps
        )
);

command.add("-thread_queue_size");
command.add("8");

command.add("-fflags");
command.add("+genpts");

command.add("-analyzeduration");

command.add("0");

command.add("-probesize");

command.add("32");

command.add("-i");
command.add("-");

/*
 * AUDIO INPUT
 */

command.addAll(
        getAudioInputArgs()
);

/*
 * VIDEO FILTER
 */

command.add("-filter:v");

command.add(
    "vflip,scale=iw*" +
    captureScale +
    ":ih*" +
    captureScale
);

/*
 * VIDEO ENCODER
 */

command.add("-c:v");
command.add(encoder);

command.add("-preset");
command.add("ultrafast");

if (encoder.equals("h264_videotoolbox")) {

    command.add("-realtime");
    command.add("true");

    command.add("-prio_speed");
    command.add("true");
}

command.add("-b:v");
command.add("12M");

command.add("-maxrate");
command.add("16M");

command.add("-bufsize");
command.add("12M");

command.add("-g");
command.add("20");

/*
 * AUDIO ENCODER
 */

if (
        SettingsManager.isAudioEnabled()
) {

    command.add("-c:a");
    command.add("aac");

    command.add("-b:a");
    command.add("192k");
}

/*
 * COMPATIBILITY
 */

command.add("-pix_fmt");
command.add("yuv420p");

/*
 * FAST WEB PLAYBACK
 */

command.add("-movflags");
command.add("+faststart");

/*
 * OUTPUT FILE
 */

command.add(
        currentOutput
                .toAbsolutePath()
                .toString()
);

ProcessBuilder builder =
        new ProcessBuilder(
                command
        );

ffmpegProcess =
        builder.start();

        new Thread(() -> {

    try (

        InputStream errorStream =
                ffmpegProcess.getErrorStream()

    ) {

        errorStream.transferTo(
                OutputStream.nullOutputStream()
        );

    } catch (Exception ignored) {}

}).start();

   ffmpegInput =
        ffmpegProcess.getOutputStream();

        ffmpegChannel =
        Channels.newChannel(ffmpegInput);

if (!ffmpegProcess.isAlive()) {

    throw new RuntimeException(
            "FFmpeg failed to start"
    );
}

StaffReplay.LOGGER.info(
        "Using encoder: {}",
        encoder
);
}

    private void rotateChunk(
            MinecraftClient client
    ) { if (!recording) {

    return;
}

        try {

            if (ffmpegInput != null) {

  

                ffmpegInput.close();
                ffmpegInput = null;
            }

       if (ffmpegProcess != null) {

    Process oldProcess = ffmpegProcess;

  oldProcess.destroy();
}

            if (currentOutput != null) {

                long durationMs =
        (System.nanoTime() - chunkStartTime)
        / 1_000_000L;

if (Files.exists(currentOutput)) {

    chunkFiles.add(
            new ChunkInfo(
                    currentOutput,
                    durationMs
            )
    );

} else {

    StaffReplay.LOGGER.error(
            "Chunk file missing after rotation: {}",
            currentOutput
    );

    return;
}

           int chunkDurationSeconds = 15;

int maxChunks =
    (int) Math.ceil(
        (double)
        ClipLengthManager.getCurrentLength()
        / chunkDurationSeconds
    ) + 1;

while (chunkFiles.size() > maxChunks) {

    ChunkInfo oldChunk =
            chunkFiles.removeFirst();

    try {

        Files.deleteIfExists(oldChunk.path);

    } catch (Exception ignored) {}
} 

            chunkIndex++;

            Path tempDir =
                    client.runDirectory
                            .toPath()
                            .resolve("staffreplay")
                            .resolve("buffer");

            currentOutput =
                    tempDir.resolve(
                            "chunk_" +
                            chunkIndex +
                            ".mp4"
                    );

Framebuffer framebuffer =
        client.getFramebuffer();

outputWidth =
      framebuffer.viewportWidth;

outputHeight =
          framebuffer.viewportHeight;

int width = outputWidth;
int height = outputHeight;

startFFmpegProcess(
        width,
        height
);

           chunkStartTime =
    System.nanoTime();
    lastFrameTime = System.nanoTime();

            StaffReplay.LOGGER.info(
                    "Rotated chunk: {}",
                    chunkIndex
            );

            }
        } catch (Exception e) {

            StaffReplay.LOGGER.error(
                    "Chunk rotation failed",
                    e
            );
        }
    }

    public void cycleRecordingFps() {

    fpsIndex++;

    if (fpsIndex >= FPS_OPTIONS.length) {

        fpsIndex = 0;
    }

    currentRecordingFps =
            FPS_OPTIONS[fpsIndex];

    frameTimeNs =
            1_000_000_000L /
            currentRecordingFps;

    StaffReplay.LOGGER.info(
            "Recording FPS set to {}",
            currentRecordingFps
    );
}

   public void captureFrame(
        MinecraftClient client
) {

    if (
        !enabled ||
        !recording ||
        ffmpegInput == null
) {

    return;
}

if (frameQueue.size() > 4) {

    return;
}

    try {
        synchronized (pendingUnmaps) {

    for (int pboIndex : pendingUnmaps) {

        GL15.glBindBuffer(
                GL21.GL_PIXEL_PACK_BUFFER,
                pboIds[pboIndex]
        );

        GL15.glUnmapBuffer(
                GL21.GL_PIXEL_PACK_BUFFER
        );
    }

    pendingUnmaps.clear();

    GL15.glBindBuffer(
            GL21.GL_PIXEL_PACK_BUFFER,
            0
    );
}

        long now =
                System.nanoTime();

   if (lastFrameTime == 0L) {

    lastFrameTime = now;
}

if (now < lastFrameTime + frameTimeNs) {

    return;
}

lastFrameTime += frameTimeNs;

/*
 * RESYNC IF TOO FAR BEHIND
 */

if (now - lastFrameTime > frameTimeNs * 2L) {

    lastFrameTime = now;
}
        /*
         * ROTATE CHUNKS
         */

     if (
        now -
        chunkStartTime >=
        CHUNK_DURATION_NS &&
        !rotatingChunk
) {

    rotatingChunk = true;
CompletableFuture.runAsync(() -> {

    try {

        rotateChunk(client);

    } finally {

        rotatingChunk = false;
    }

});
}

        Framebuffer framebuffer =
                client.getFramebuffer();

        /*
         * DOUBLE BUFFERING
         */

       int nextPboIndex =
        (currentPboIndex + 1) % pboIds.length;

int mapPboIndex =
        (currentPboIndex + 4) % pboIds.length;
        /*
         * READ CURRENT FRAME INTO CURRENT PBO
         */

        /*
 * READ CURRENT FRAME INTO CURRENT PBO
 */

framebuffer.beginRead();



GL15.glBindBuffer(
        GL21.GL_PIXEL_PACK_BUFFER,
        pboIds[currentPboIndex]
);

GL11.glPixelStorei(
    GL11.GL_PACK_ALIGNMENT,
    1
);

GL11.glReadPixels(
        0,
        0,
        outputWidth,
        outputHeight,
        GL12.GL_BGRA,
        GL11.GL_UNSIGNED_BYTE,
        0
);


/*
 * RESTORE MAIN FRAMEBUFFER
 */

framebuffer.endRead();

        /*
         * MAP PREVIOUS PBO
         */

  if (frameQueue.size() > 2) {

    currentPboIndex = nextPboIndex;

    GL15.glBindBuffer(
            GL21.GL_PIXEL_PACK_BUFFER,
            0
    );

    return;
}

        GL15.glBindBuffer(
                GL21.GL_PIXEL_PACK_BUFFER,
                pboIds[mapPboIndex]
        );

        java.nio.ByteBuffer buffer =
                GL30.glMapBufferRange(
                        GL21.GL_PIXEL_PACK_BUFFER,
                        0,
                        frameByteSize,
                        GL30.GL_MAP_READ_BIT
                );

        if (buffer != null) {

           buffer.limit(frameByteSize);

frameQueue.offer(
        new MappedFrame(
                buffer,
                mapPboIndex
        )
);

        }



        GL15.glBindBuffer(
                GL21.GL_PIXEL_PACK_BUFFER,
                0
        );

        currentPboIndex =
                nextPboIndex;

    } catch (Exception e) {

        StaffReplay.LOGGER.error(
                "Capture failed",
                e
        );
    }
}

public void finalizeCurrentChunk(
        MinecraftClient client
) {

    try {

        /*
         * SAVE OLD REFERENCES
         */

        Process oldProcess =
                ffmpegProcess;

        OutputStream oldInput =
                ffmpegInput;

        Path oldOutput =
                currentOutput;

        long finalizedDurationMs =
                (System.nanoTime() - chunkStartTime)
                / 1_000_000L;

        /*
         * ROTATE TO NEW CHUNK
         */

        

        /*
         * FINALIZE OLD FILE SYNCHRONOUSLY
         */

        if (oldInput != null) {

            

            oldInput.close();
        }

        if (oldProcess != null) {

            int exit =
                    oldProcess.waitFor();

            StaffReplay.LOGGER.info(
                    "Replay finalized with exit code {}",
                    exit
            );
        }

        /*
         * ADD FINALIZED CHUNK
         */

        if (
        oldOutput != null &&
        Files.exists(oldOutput)
) {

    chunkFiles.add(
            new ChunkInfo(
                    oldOutput,
                    finalizedDurationMs
            )
    );

} else {

    StaffReplay.LOGGER.error(
            "Chunk file missing: {}",
            oldOutput
    );

    return;
}

            int chunkDurationSeconds = 15;

int maxChunks =
    (int) Math.ceil(
        (double)
        ClipLengthManager.getCurrentLength()
        / chunkDurationSeconds
    ) + 1;

            while (chunkFiles.size() > maxChunks) {

                ChunkInfo oldChunk =
                        chunkFiles.removeFirst();

                try {

                    Files.deleteIfExists(
                            oldChunk.path
                    );

                } catch (Exception ignored) {}
            }
        

        StaffReplay.LOGGER.info(
                "Replay finalized synchronously"
        );

    } catch (Exception e) {

        StaffReplay.LOGGER.error(
                "Failed finalizing chunk",
                e
        );
    }
}
    public static void addMarker(
        String note
) {

    long currentTime =
            System.currentTimeMillis();

    long timestamp =
            currentTime -
            recordingStartTime;

    markers.add(

            new Marker(
                    timestamp,
                    note
            )
    );

    StaffReplay.LOGGER.info(
            "Added marker '{}' at {}ms",
            note,
            timestamp
    );
}

public static List<Marker> getMarkers() {

    return markers;
}
}