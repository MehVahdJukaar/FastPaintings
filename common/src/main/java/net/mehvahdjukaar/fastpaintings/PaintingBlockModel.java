package net.mehvahdjukaar.fastpaintings;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaintingBlockModel implements CustomBakedModel {

    public static final ResourceLocation BACK_TEXTURE = new ResourceLocation("painting/back");

    private final BakedModel paintingModel;

    public PaintingBlockModel(BakedModel paintingModel) {
        this.paintingModel = paintingModel;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side,
                                         RandomSource rand, RenderType renderType,
                                         ExtraModelData data) {

        PaintingVariant variant = data.get(PaintingBlockEntity.MIMIC_KEY);
        if (variant == null) {
            return List.of();
        }

        List<BakedQuad> quads = paintingModel.getQuads(null, side, rand);
        PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();

        ResourceLocation name = paintingTextureManager.get(variant).getName();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(name);
        float segmentWScale = sprite.getWidth() / (float) variant.getWidth();
        float segmentHScale = sprite.getHeight() / (float) variant.getHeight();

        float rightOff = state.getValue(PaintingBlock.RIGHT_OFFSET)
                * (sprite.getU1() - sprite.getU0()) / (variant.getWidth() / 16f);
        float downOff = state.getValue(PaintingBlock.DOWN_OFFSET)
                * (sprite.getV1() - sprite.getV0()) / (variant.getHeight() / 16f);

        List<BakedQuad> newList = new ArrayList<>();
        for (BakedQuad q : quads) {
            TextureAtlasSprite oldSprite = q.getSprite();
            if (oldSprite.getName().equals(MissingTextureAtlasSprite.getLocation())) {
                int stride = DefaultVertexFormat.BLOCK.getIntegerSize();
                int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);
                for (int i = 0; i < v.length / stride; i++) {
                    float originalU = Float.intBitsToFloat(v[i * stride + 4]);
                    float originalV = Float.intBitsToFloat(v[i * stride + 5]);

                    float u1 = (originalU - oldSprite.getU0()) * segmentWScale + rightOff;
                    v[i * stride + 4] = Float.floatToRawIntBits(u1 + sprite.getU0());

                    float v1 = (originalV - oldSprite.getV0()) * segmentHScale + downOff;
                    v[i * stride + 5] = Float.floatToRawIntBits(v1 + sprite.getV0());
                }
                newList.add(new BakedQuad(v, q.getTintIndex(), q.getDirection(), sprite, q.isShade()));
            } else newList.add(q);

        }
        return newList;
    }


    @Override
    public ExtraModelData getModelData(@Nullable ExtraModelData originalTileData, BlockPos pos, BlockState state, BlockAndTintGetter level) {
        if (state.getBlock() instanceof PaintingBlock && !PaintingBlock.isMaster(state)) {
            var tile = PaintingBlock.getMaster(state, pos, level);
            if (tile != null) return tile.getExtraModelData();
        }
        return originalTileData;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        return paintingModel.getParticleIcon();
    }


    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }
}
