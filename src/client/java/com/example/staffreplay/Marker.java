package com.example.staffreplay;

public class Marker {

    private final long timestamp;

    private final String note;

    public Marker(
            long timestamp,
            String note
    ) {

        this.timestamp =
                timestamp;

        this.note =
                note;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public String getNote() {

        return note;
    }
}