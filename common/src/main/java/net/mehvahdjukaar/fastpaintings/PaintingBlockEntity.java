package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class PaintingBlockEntity extends BlockEntity implements IExtraModelDataProvider { //implements VariantHolder<PaintingVariant>

    public static final ModelDataKey<PaintingVariant> MIMIC_KEY = new ModelDataKey<>(PaintingVariant.class);

    private Holder<PaintingVariant> variant;

    public PaintingBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(FastPaintings.PAINTING_TILE.get(), blockPos, blockState);
        this.variant = getDefaultVariant();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        ResourceKey<PaintingVariant> resourceKey = ResourceKey.create(Registry.PAINTING_VARIANT_REGISTRY,
                ResourceLocation.tryParse(compound.getString("variant")));
        this.setVariant(Registry.PAINTING_VARIANT.getHolder(resourceKey).orElseGet(PaintingBlockEntity::getDefaultVariant));

    }

    @NotNull
    private static Holder<PaintingVariant> getDefaultVariant() {
        return Registry.PAINTING_VARIANT.getHolderOrThrow(PaintingVariants.KEBAB);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("variant", this.getVariant().unwrapKey().orElse(PaintingVariants.KEBAB).location().toString());
    }

    public void setVariant(Holder<PaintingVariant> variant) {
        this.variant = variant;
        this.setChanged();
    }

    public Holder<PaintingVariant> getVariant() {
        return variant;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(MIMIC_KEY, this.getVariant().value())
                .build();
    }

}
