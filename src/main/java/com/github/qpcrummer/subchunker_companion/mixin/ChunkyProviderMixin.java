package com.github.qpcrummer.subchunker_companion.mixin;

import com.github.qpcrummer.subchunker_companion.SubchunkerCompanion;
import com.llamalad7.mixinextras.sugar.Local;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkyProvider.class, remap = false)
public class ChunkyProviderMixin {
    @Inject(method = "register", at = @At("TAIL"))
    private static void registerTasks(CallbackInfo ci, @Local(argsOnly = true) Chunky instance) {
        SubchunkerCompanion.registerTasks(instance);
    }
}
