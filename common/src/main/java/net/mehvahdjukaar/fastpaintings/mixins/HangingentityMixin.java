package net.mehvahdjukaar.fastpaintings.mixins;

import net.mehvahdjukaar.fastpaintings.PaintingBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HangingEntity.class)
public abstract class HangingentityMixin extends Entity {

    protected HangingentityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci){
        if(this.tickCount < 3 && !level.isClientSide && this.isAlive() && this.getType() == EntityType.PAINTING){
            PaintingBlock.tryConverting((Painting)(Object)this);
        }
    }
}
