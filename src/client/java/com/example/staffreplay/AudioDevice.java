package com.example.staffreplay;

public class AudioDevice {

    private final String name;

    private final String ffmpegId;

    public AudioDevice(
            String name,
            String ffmpegId
    ) {

        this.name = name;
        this.ffmpegId = ffmpegId;
    }

    public String getName() {

        return name;
    }

    public String getFfmpegId() {

        return ffmpegId;
    }

    @Override
    public String toString() {

        return name;
    }
}