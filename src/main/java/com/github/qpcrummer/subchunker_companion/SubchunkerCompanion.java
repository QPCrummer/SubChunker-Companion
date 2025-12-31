package com.github.qpcrummer.subchunker_companion;

import net.fabricmc.api.ModInitializer;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.api.ChunkyAPI;

public class SubchunkerCompanion implements ModInitializer {
    private static final double RADIUS = 1500;

    @Override
    public void onInitialize() {
    }

    public static void registerTasks(Chunky instance) {
        ChunkyAPI api = instance.getApi();

        api.startTask(
                "overworld",
                "square",
                0.0,
                0.0,
                SubchunkerCompanion.RADIUS,
                SubchunkerCompanion.RADIUS,
                "region"
        );
    }
}
