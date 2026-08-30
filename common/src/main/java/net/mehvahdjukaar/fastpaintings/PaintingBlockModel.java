package net.mehvahdjukaar.fastpaintings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomUnbakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PaintingBlockModel implements CustomBlockModel {

    private final BlockStateModel[] models = new BlockStateModel[16];

    public PaintingBlockModel(Map<String, BlockStateModel> paintingModels) {
        for (var e : paintingModels.entrySet()) {
            String k = e.getKey();
            models[getIndex(k.contains("top"), k.contains("bottom"), k.contains("left"), k.contains("right"))] = e.getValue();
        }
    }

    private static int getIndex(boolean top, boolean bottom, boolean left, boolean right) {
        int index = 0;

        index |= (top ? 1 : 0) << 3;
        index |= (bottom ? 1 : 0) << 2;
        index |= (left ? 1 : 0) << 1;
        index |= right ? 1 : 0;

        return index;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        if (state == null) return;
        PaintingVariant variant = getVariant(data, level, pos, state);
        if (variant == null) return;

        int paintingW = variant.width();
        int paintingH = variant.height();
        int rightOffset = state.getValue(PaintingBlock.RIGHT_OFFSET);
        int downOffset = state.getValue(PaintingBlock.DOWN_OFFSET);
        TextureAtlasSprite sprite = getPaintingSprite(variant);

        emitSlice(emitter, this.models[0], level, pos, state, random, sprite,
                paintingW, paintingH, rightOffset, downOffset);

        int index = getIndex(downOffset == 0, downOffset == paintingH - 1,
                rightOffset == 0, rightOffset == paintingW - 1);
        if (index != 0) {
            emitSlice(emitter, this.models[index], level, pos, state, random, sprite,
                    paintingW, paintingH, rightOffset, downOffset);
        }
    }

    @Nullable
    private static PaintingVariant getVariant(ExtraModelData data, @Nullable BlockAndTintGetter level,
                                              @Nullable BlockPos pos, BlockState state) {
        PaintingVariant variant = data.get(PaintingBlockEntity.MIMIC_KEY);
        if (variant == null && level != null && pos != null && !PaintingBlock.isMaster(state)) {
            PaintingBlockEntity master = PaintingBlock.getMaster(state, pos, level);
            if (master != null) variant = master.getVariant().value();
        }
        return variant;
    }

    private static TextureAtlasSprite getPaintingSprite(PaintingVariant variant) {
        Identifier assetId = variant.assetId();
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(Identifier.fromNamespaceAndPath(assetId.getNamespace(), "painting/" + assetId.getPath()));
    }

    private static void emitSlice(QuadEmitter emitter, @Nullable BlockStateModel model,
                                  @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                  @Nullable BlockState state, RandomSource random, TextureAtlasSprite sprite,
                                  int paintingW, int paintingH, int rightOffset, int downOffset) {
        if (model == null) return;
        List<BlockStateModelPart> parts = new ArrayList<>();
        ClientHelper.collectModelParts(model, level, pos, state, random, parts);
        for (BlockStateModelPart part : parts) {
            for (Direction dir : Direction.values()) {
                emitter.cullFace(dir);
                emitQuads(emitter, part.getQuads(dir), sprite, paintingW, paintingH, rightOffset, downOffset);
            }
            emitter.cullFace(null);
            emitQuads(emitter, part.getQuads(null), sprite, paintingW, paintingH, rightOffset, downOffset);
        }
    }

    private static void emitQuads(QuadEmitter emitter, List<BakedQuad> quads, TextureAtlasSprite sprite,
                                  int paintingW, int paintingH, int rightOffset, int downOffset) {
        for (BakedQuad q : quads) {
            emitter.fromQuad(q);
            TextureAtlasSprite oldSprite = q.materialInfo().sprite();
            if (oldSprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                emitter.sprite(sprite);
                for (int i = 0; i < QuadEmitter.VERTICES; i++) {
                    long uv = q.packedUV(i);
                    float u = unlerp(UVPair.unpackU(uv), oldSprite.getU0(), oldSprite.getU1());
                    float v = unlerp(UVPair.unpackV(uv), oldSprite.getV0(), oldSprite.getV1());
                    emitter.uv(i, (u + rightOffset) / paintingW, (v + downOffset) / paintingH);
                }
            }
            emitter.emit();
        }
    }

    private static float unlerp(float value, float min, float max) {
        float range = max - min;
        return range == 0 ? 0 : (value - min) / range;
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        return this.models[0].particleMaterial().sprite();
    }

    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return getVariant(data, level, pos, state);
    }

    public record Unbaked(Map<String, BlockStateModel.Unbaked> models) implements CustomUnbakedModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.unboundedMap(Codec.STRING, BlockStateModel.Unbaked.CODEC).fieldOf("models").forGetter(Unbaked::models)
        ).apply(i, Unbaked::new));

        @Override
        public CustomBlockModel bake(ModelBaker baker) {
            Map<String, BlockStateModel> baked = new HashMap<>();
            this.models.forEach((key, unbaked) -> baked.put(key, unbaked.bake(baker)));
            return new PaintingBlockModel(baked);
        }

        @Override
        public MapCodec<? extends CustomUnbakedModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.models.values().forEach(m -> m.resolveDependencies(resolver));
        }
    }
}
