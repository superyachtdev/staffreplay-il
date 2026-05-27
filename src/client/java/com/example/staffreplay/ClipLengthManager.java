package com.example.staffreplay;

public class ClipLengthManager {

    private static final int[] LENGTHS =
            {
                    5,
                    15,
                    30,
                    45,
                    60,
                    90,
                    120
                    
            };

    private static int currentIndex = 2;

    public static void cycleLength() {

        currentIndex++;

        if (currentIndex >= LENGTHS.length) {

            currentIndex = 0;
        }

        int length =
                getCurrentLength();

        String display =
                length >= 60
                        ? (length / 60) + "m"
                        : length + "s";

        StaffReplay.LOGGER.info(
                "Clip length set to {}",
                display
        );
    }

    public static int getCurrentLength() {

        return LENGTHS[currentIndex];
    }

    public static String getFormattedLength() {

        int length =
                getCurrentLength();

        if (length >= 60) {

            return (length / 60) + "m";
        }

        return length + "s";
    }
}