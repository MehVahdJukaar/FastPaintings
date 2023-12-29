package net.mehvahdjukaar.fastpaintings.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.mehvahdjukaar.fastpaintings.FastPaintingsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = SpriteLoader.class, priority = 900)
public class ModelManagerMixin {

    //paintings are already on block atlas!
    @Deprecated(forRemoval = true)
    @Inject(method = "loadAndStitch", at = @At(value = "HEAD"))
    public void addPaintingSheet(ResourceManager resouceManager, ResourceLocation location, int mipLevel, Executor executor,
                                 CallbackInfoReturnable<CompletableFuture<SpriteLoader.Preparations>> cir,
                                 @Local(ordinal = 0) LocalIntRef mutableMip) {
       // if (location.equals(FastPaintingsClient.PAINTING_SHEET)) {
         //   mutableMip.set(Minecraft.getInstance().options.mipmapLevels().get());
        //}
    }
}
