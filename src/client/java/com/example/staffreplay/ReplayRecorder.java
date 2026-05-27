package com.example.staffreplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ReplayRecorder {

    private static final ReplayRecorder INSTANCE =
            new ReplayRecorder();

    private final Deque<ReplaySnapshot> snapshots =
            new ArrayDeque<>();

    public static ReplayRecorder getInstance() {

        return INSTANCE;
    }

    private int getMaxSnapshots() {

        return 20 *
                ClipLengthManager.getCurrentLength();
    }

    public void recordTick(
            MinecraftClient client
    ) {

        ClientPlayerEntity player =
                client.player;

        if (player == null) {

            return;
        }

        snapshots.addLast(
                new ReplaySnapshot(

                        System.currentTimeMillis(),

                        player.getX(),
                        player.getY(),
                        player.getZ(),

                        player.getYaw(),
                        player.getPitch()
                )
        );

        while (
                snapshots.size() >
                getMaxSnapshots()
        ) {

            snapshots.removeFirst();
        }
    }

    public List<ReplaySnapshot> getSnapshots() {

        return new ArrayList<>(snapshots);
    }

    public void clear() {

        snapshots.clear();

        StaffReplay.LOGGER.info(
                "Cleared replay snapshots"
        );
    }
}