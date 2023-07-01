package net.mehvahdjukaar.fastpaintings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class PaintingBlockModelLoader implements CustomModelLoader {


    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new Geometry(ClientPlatformHelper.parseBlockModel(json.get("model")));
    }


    private record Geometry(BlockModel model) implements CustomGeometry {

        @Override
        public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<Material> l = new ArrayList<>();
            for (var v : Registry.PAINTING_VARIANT) {
                var r = Registry.PAINTING_VARIANT.getKey(v);
                l.add(new Material(TextureAtlas.LOCATION_BLOCKS,
                        new ResourceLocation(r.getNamespace(), "painting/" + r.getPath())));
            }
            l.add(new Material(TextureAtlas.LOCATION_BLOCKS,
                    PaintingBlockModel.BACK_TEXTURE));
            return l;
        }

        @Override
        public CustomBakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
            BakedModel bakedModel = this.model.bake(modelBakery, model, spriteGetter, transform, location, true);
            return new PaintingBlockModel(bakedModel);
        }
    }
}
