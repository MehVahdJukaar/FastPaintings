package net.mehvahdjukaar.fastpaintings;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaintingBlockModel implements CustomBakedModel {

    public static final ResourceLocation BACK_TEXTURE = new ResourceLocation("painting/back");

    private final BakedModel[] models = new BakedModel[16];

    public PaintingBlockModel(Map<String, BakedModel> paintingModels) {
        for (var e : paintingModels.entrySet()) {
            String k = e.getKey();
            int i = getIndex(k.contains("top"), k.contains("bottom"), k.contains("left"), k.contains("right"));
            models[i] = e.getValue();
        }
    }

    public int getIndex(boolean top, boolean bottom, boolean left, boolean right) {
        int index = 0;

        index |= (top ? 1 : 0) << 3;
        index |= (bottom ? 1 : 0) << 2;
        index |= (left ? 1 : 0) << 1;
        index |= right ? 1 : 0;

        return index;
    }


    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side,
                                         RandomSource rand, RenderType renderType,
                                         ExtraModelData data) {

        PaintingVariant variant = data.get(PaintingBlockEntity.MIMIC_KEY);
        if (variant == null) {
            return List.of();
        }

        PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
        ResourceLocation paintingTexture = paintingTextureManager.get(variant).contents().name();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(new ResourceLocation(paintingTexture.getNamespace(), "painting/"+paintingTexture.getPath()));

        float segmentWScale = sprite.contents().width() / (float) variant.getWidth();
        float segmentHScale = sprite.contents().height() / (float) variant.getHeight();

        int rightOffset = state.getValue(PaintingBlock.RIGHT_OFFSET);
        int downOffset = state.getValue(PaintingBlock.DOWN_OFFSET);
        int paintingW = variant.getWidth() / 16;
        int paintingH = variant.getHeight() / 16;

        float spriteRightOff = rightOffset * (sprite.getU1() - sprite.getU0()) / paintingW;
        float spriteDownOff = downOffset * (sprite.getV1() - sprite.getV0()) / paintingH;


        List<BakedQuad> combinedQuads = new ArrayList<>();


        List<BakedModel> bakedModels = new ArrayList<>();
        bakedModels.add(this.models[0]);
        int index = getIndex(downOffset == 0, downOffset == paintingH - 1, rightOffset == 0, rightOffset == paintingW - 1);
        bakedModels.add(this.models[index]);

        for (var model : bakedModels) {
            if (model == null) continue;
            List<BakedQuad> quads = model.getQuads(null, side, rand);

            for (BakedQuad q : quads) {
                TextureAtlasSprite oldSprite = q.getSprite();
                if (oldSprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                    int stride = DefaultVertexFormat.BLOCK.getIntegerSize();
                    int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);
                    for (int i = 0; i < v.length / stride; i++) {
                        float originalU = Float.intBitsToFloat(v[i * stride + 4]);
                        float originalV = Float.intBitsToFloat(v[i * stride + 5]);

                        float u1 = (originalU - oldSprite.getU0()) * segmentWScale + spriteRightOff;
                        v[i * stride + 4] = Float.floatToRawIntBits(u1 + sprite.getU0());

                        float v1 = (originalV - oldSprite.getV0()) * segmentHScale + spriteDownOff;
                        v[i * stride + 5] = Float.floatToRawIntBits(v1 + sprite.getV0());
                    }
                    combinedQuads.add(new BakedQuad(v, q.getTintIndex(), q.getDirection(), sprite, q.isShade()));
                } else combinedQuads.add(q);
            }
        }
        return combinedQuads;
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
        return models[0].getParticleIcon();
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
