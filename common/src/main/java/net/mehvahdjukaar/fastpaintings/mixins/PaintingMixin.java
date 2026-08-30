package net.mehvahdjukaar.fastpaintings.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.fastpaintings.PaintingBlock;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Painting.class)
public abstract class PaintingMixin extends Entity {


    @Shadow
    public abstract Holder<PaintingVariant> getVariant();

    public PaintingMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "dropItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/decoration/painting/Painting;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public ItemEntity fastPaintings$betterDrop(Painting instance, ServerLevel level, ItemLike itemLike, Operation<ItemEntity> original) {
        if (this.getType() == EntityType.PAINTING) {
            return this.spawnAtLocation(level, PaintingBlock.getPaintingItem(getVariant(), true));
        } else {
            return original.call(instance, level, itemLike);
        }
    }
}
