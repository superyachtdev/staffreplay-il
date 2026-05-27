package com.example.staffreplay;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaffReplay implements ModInitializer {

    public static final Logger LOGGER =
            LoggerFactory.getLogger("staffreplay");

    @Override
    public void onInitialize() {
        LOGGER.info("Staff Replay initialized");
    }
}
