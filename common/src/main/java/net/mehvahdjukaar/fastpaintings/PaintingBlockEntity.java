package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PaintingBlockEntity extends BlockEntity implements IExtraModelDataProvider, VariantHolder<Holder<PaintingVariant>> {

    public static final ModelDataKey<PaintingVariant> MIMIC_KEY = new ModelDataKey<>(PaintingVariant.class);

    private Holder<PaintingVariant> variant;
    private boolean placedWithNbt = false;

    public PaintingBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(FastPaintings.PAINTING_TILE.get(), blockPos, blockState);
        this.variant = getDefaultVariant(Utils.hackyGetRegistryAccess().registryOrThrow(Registries.PAINTING_VARIANT).asLookup());
        Item.BY_BLOCK.put(FastPaintings.PAINTING_BLOCK.get(), Items.PAINTING);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ResourceKey<PaintingVariant> resourceKey = ResourceKey.create(Registries.PAINTING_VARIANT,
                ResourceLocation.tryParse(tag.getString("variant")));
        var paintingsReg = registries.lookupOrThrow(Registries.PAINTING_VARIANT);
        this.setVariant(paintingsReg.get(resourceKey)
                .orElseGet(() -> getDefaultVariant(paintingsReg)));
        placedWithNbt = tag.getBoolean("placed_with_nbt");
    }

    @NotNull
    private static Holder.Reference<PaintingVariant> getDefaultVariant(HolderLookup.RegistryLookup<PaintingVariant> reg) {
        return reg.getOrThrow(PaintingVariants.KEBAB);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("variant", this.getVariant().unwrapKey().orElse(PaintingVariants.KEBAB).location().toString());
        tag.putBoolean("placed_with_nbt", placedWithNbt);
    }

    public void setVariant(Holder<PaintingVariant> variant) {
        this.variant = variant;
        this.setChanged();
    }

    public void setPlacedWithNbt(boolean bool) {
        placedWithNbt = bool;
    }

    public boolean isPlacedWithNbt() {
        return placedWithNbt;
    }

    public Holder<PaintingVariant> getVariant() {
        return variant;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(MIMIC_KEY, this.getVariant().value());
    }
}
