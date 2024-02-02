package net.mehvahdjukaar.fastpaintings.mixins;

import net.mehvahdjukaar.fastpaintings.FastPaintings;
import net.mehvahdjukaar.fastpaintings.SetPaintingMessage;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import subaraki.paintings.gui.PaintingScreen;

@Pseudo
@Mixin(PaintingScreen.class)
public class PaintingPlusPlusCompatMixin {

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    public void sendFastPaintingsPacket(ResourceLocation variantName, int entityId, CallbackInfo ci){
       BlockPos pos = FastPaintings.LAST_KNOWN_ENTITY_POS.getIfPresent(entityId);
       if(pos != null){
           NetworkHelper.sendToServer(new SetPaintingMessage(variantName, pos));
           ci.cancel();
       }
    }

}
