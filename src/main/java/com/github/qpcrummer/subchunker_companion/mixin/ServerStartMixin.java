package com.github.qpcrummer.subchunker_companion.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketProcessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public abstract class ServerStartMixin extends ReentrantBlockableEventLoop<TickTask> {
    @Shadow
    @Final
    private PacketProcessor packetProcessor;

    @Shadow
    @Final
    private ServerTickRateManager tickRateManager;

    @Shadow
    @Final
    private ServerConnectionListener connection;

    public ServerStartMixin(String string) {
        super(string);
    }

    @Shadow
    public abstract ResourceManager getResourceManager();

    @Shadow
    private volatile boolean running;

    @Shadow
    @Final
    protected WorldData worldData;

    @Shadow
    @Final
    private LayeredRegistryAccess<RegistryLayer> registries;

    @Shadow
    @Final
    private Executor executor;

    @Shadow
    @Final
    protected LevelStorageSource.LevelStorageAccess storageSource;

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject(method = "prepareLevels", at = @At("TAIL"))
    private void onWorldLoadFinished(CallbackInfo ci) {
        this.packetProcessor.close();
        this.tickRateManager.setFrozen(true);
        this.connection.stop();
        ((CloseableResourceManager)this.getResourceManager()).close();
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;processPacketsAndTick(Z)V"))
    private void onServerStart(MinecraftServer instance, boolean bl) {
        this.running = false;
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;stopServer()V"))
    private void onServerStop(MinecraftServer instance) {
        // Don't stop
    }

    @Inject(method = "shouldRun(Lnet/minecraft/server/TickTask;)Z",at = @At("HEAD"), cancellable = true)
    private void onShouldRun(TickTask tickTask, CallbackInfoReturnable<Boolean> cir) {
        if (!this.running) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "waitForTasks", at = @At("HEAD"), cancellable = true)
    private void onWaitForTasks(CallbackInfo ci) {
        if (!this.running) {
            ci.cancel();
        }
    }

    @Inject(method = "haveTime", at = @At("HEAD"), cancellable = true)
    private void onHaveTime(CallbackInfoReturnable<Boolean> cir) {
        if (!this.running) {
            cir.setReturnValue(true);
        }
    }

    /**
     * @author QPCrummer
     * @reason Reduce bloat
     */
    @Overwrite
    public void loadLevel() {
        createLevelsRewritten();
    }


    private void createLevelsRewritten() {
        ServerLevelData data = this.worldData.overworldData();

        Registry<LevelStem> stems =
                this.registries.compositeAccess()
                        .lookupOrThrow(Registries.LEVEL_STEM);

        LevelStem overworldStem = stems.getValue(LevelStem.OVERWORLD);

        long seed = this.worldData.worldGenOptions().seed();
        long biomeSeed = BiomeManager.obfuscateSeed(seed);

        ServerLevel overworld = new ServerLevel(
                (MinecraftServer)(Object)this,
                this.executor,
                this.storageSource,
                data,
                Level.OVERWORLD,
                overworldStem,
                this.worldData.isDebugWorld(),
                biomeSeed,
                ImmutableList.of(),
                true,
                null
        );

        this.levels.put(Level.OVERWORLD, overworld);
    }
}
