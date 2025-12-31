package com.github.qpcrummer.subchunker_companion.mixin;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(SimplePreparableReloadListener.class)
public class SimplePreparableReloadListenerMixin {
    @Redirect(method = "reload", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<?> supplyAsync(Supplier<?> supplier, Executor executor) {
        // Remove Recipes and Advancements
        return CompletableFuture.runAsync(() -> {});
    }
}
