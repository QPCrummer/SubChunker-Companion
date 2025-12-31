package com.github.qpcrummer.subchunker_companion.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.Proxy;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServer {
    @Shadow
    @Final
    static Logger LOGGER;

    public DedicatedServerMixin(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, LevelLoadListener levelLoadListener) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, levelLoadListener);
    }

    /**
     * @author QPCrummer
     * @reason Minimum functional server for benchmarking
     */
    @Overwrite
    public boolean initServer() {
        this.setPlayerList(new DedicatedPlayerList((DedicatedServer)(Object)this, this.registries(), this.playerDataStorage));
        this.loadLevel();
        LOGGER.info("Done ()! For help, type \"help\"");
        this.notificationManager().serverStarted();
        return true;
    }
}