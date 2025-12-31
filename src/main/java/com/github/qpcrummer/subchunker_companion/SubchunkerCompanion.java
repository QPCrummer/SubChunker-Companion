package com.github.qpcrummer.subchunker_companion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.api.ChunkyAPI;

public class SubchunkerCompanion implements ModInitializer {
    private static final double RADIUS = 1500;
    private static MinecraftServer server;
    public static boolean STOP = false;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> server = minecraftServer);
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

        api.onGenerationComplete(generationCompleteEvent -> {
            SubchunkerCompanion.STOP = true;
            server.stopServer();
        });

        System.out.println("Chunky registered");
    }
}
