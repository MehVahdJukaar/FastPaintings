package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariants;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;


public class PaintingBlockEntity extends BlockEntity implements IExtraModelDataProvider {

    public static final ModelDataKey<PaintingVariant> MIMIC_KEY = new ModelDataKey<>(PaintingVariant.class);

    private Holder<PaintingVariant> variant;
    private boolean placedWithNbt = false;

    public PaintingBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(FastPaintings.PAINTING_TILE.get(), blockPos, blockState);
        this.variant = VariantUtils.getDefaultOrAny(Utils.hackyGetRegistryAccess(), PaintingVariants.KEBAB);
        Item.BY_BLOCK.put(FastPaintings.PAINTING_BLOCK.get(), Items.PAINTING);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        VariantUtils.readVariant(input, Registries.PAINTING_VARIANT).ifPresent(this::setVariant);
        placedWithNbt = input.getBooleanOr("placed_with_nbt", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        VariantUtils.writeVariant(output, this.variant);
        output.putBoolean("placed_with_nbt", placedWithNbt);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.hasDroppedItemHack) {
            this.hasDroppedItemHack = true;
            Vec3 v = this.getPaintingDropLocation();
            Containers.dropItemStack(this.level, v.x, v.y, v.z,
                    PaintingBlock.getPaintingItem(this.variant, this.placedWithNbt));
        }
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

    public Vec3 getPaintingDropLocation() {
        PaintingVariant painting = this.getVariant().value();
        Direction alongWidth = this.getBlockState().getValue(PaintingBlock.FACING).getCounterClockWise();
        double halfWidth = (painting.width() - 1) / 2.0;
        BlockPos pos = this.getBlockPos();
        return new Vec3(
                pos.getX() + 0.5 + alongWidth.getStepX() * halfWidth,
                pos.getY() + 0.5 - (painting.height() - 1) / 2.0,
                pos.getZ() + 0.5 + alongWidth.getStepZ() * halfWidth);
    }

    boolean hasDroppedItemHack = false;
}
