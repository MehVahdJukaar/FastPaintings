package net.mehvahdjukaar.fastpaintings.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingRenderer.class)
public class PaintingRendererHackMixin {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    public void cancelFirstRenderTick(PaintingRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                      CameraRenderState camera, CallbackInfo ci) {
        if (state.ageInTicks < 2) ci.cancel();
    }

}
