package com.example.staffreplay;

public class SettingsManager {

    private static boolean audioEnabled = true;

    private static String audioDevice = "";

    private static boolean autoEvidenceEnabled = true;

private static boolean detectXray = true;
private static boolean detectCheating = true;
private static boolean detectMuteEvasion = true;
private static boolean detectGlitchAbuse = true;
private static boolean detectInappropriateCosmetic = true;

    public static boolean isAudioEnabled() {

        return audioEnabled;
    }

    public static boolean isAutoEvidenceEnabled() {
    return autoEvidenceEnabled;
}

public static void setAutoEvidenceEnabled(boolean enabled) {
    autoEvidenceEnabled = enabled;
}

public static boolean isDetectXray() {
    return detectXray;
}

public static void setDetectXray(boolean enabled) {
    detectXray = enabled;
}

public static boolean isDetectCheating() {
    return detectCheating;
}

public static void setDetectCheating(boolean enabled) {
    detectCheating = enabled;
}

public static boolean isDetectMuteEvasion() {
    return detectMuteEvasion;
}

public static void setDetectMuteEvasion(boolean enabled) {
    detectMuteEvasion = enabled;
}

public static boolean isDetectGlitchAbuse() {
    return detectGlitchAbuse;
}

public static void setDetectGlitchAbuse(boolean enabled) {
    detectGlitchAbuse = enabled;
}

public static boolean isDetectInappropriateCosmetic() {
    return detectInappropriateCosmetic;
}

public static void setDetectInappropriateCosmetic(boolean enabled) {
    detectInappropriateCosmetic = enabled;
}

    public static void setAudioEnabled(
            boolean enabled
    ) {

        audioEnabled = enabled;
    }

    public static String getAudioDevice() {

        return audioDevice;
    }

    public static void setAudioDevice(
            String device
    ) {

        audioDevice = device;
    }
}