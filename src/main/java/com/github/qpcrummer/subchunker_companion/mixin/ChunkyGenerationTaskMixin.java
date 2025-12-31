package com.github.qpcrummer.subchunker_companion.mixin;

import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GenerationTask.Progress.class, remap = false)
public class ChunkyGenerationTaskMixin {
    @Inject(method = "sendUpdate", at = @At(value = "INVOKE", target = "Lorg/popcraft/chunky/platform/Sender;sendMessagePrefixed(Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 0))
    private void sendUpdate(Sender sender, CallbackInfo ci) {
        // End the JVM
        Runtime.getRuntime().halt(0);
    }
}
