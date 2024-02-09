package net.mehvahdjukaar.fastpaintings.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.fastpaintings.PaintingBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HangingEntityItem.class)
public class PaintingItemMixin {

    @WrapOperation(method = "useOn", at = @At(target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
            value = "INVOKE"))
    public boolean fastPaintings$convertImmediately(Level instance, Entity entity, Operation<Boolean> original,
                                                    @Local ItemStack stack) {
        if (entity.getType() == EntityType.PAINTING) {
            if (PaintingBlock.tryConverting((Painting) entity, stack)) {
                return true;
            }
        }
        return original.call(instance, entity);
    }
}
