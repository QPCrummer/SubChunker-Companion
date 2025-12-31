package com.github.qpcrummer.subchunker_companion.mixin;

import net.minecraft.network.PacketProcessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.TickTask;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
}
