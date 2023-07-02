package net.mehvahdjukaar.fastpaintings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaintingBlockModelLoader implements CustomModelLoader {


    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        List<String> l = List.of(
                "center",
                "top_bottom_left_right",
                "top_bottom_left",
                "top_bottom_right",
                "top_bottom",
                "top_left",
                "top_right",
                "top",
                "bottom_left",
                "bottom_right",
                "bottom_right_left",
                "bottom_right_top",
                "bottom",
                "left",
                "right",
                "right_left"
        );


        var map = l.stream().collect(Collectors.toMap(s -> s, json::get));
        return new Geometry(map);
    }


    private record Geometry(Map<String, JsonElement> models) implements CustomGeometry {

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
            var map = models.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    e -> {
                        var j = e.getValue();
                        BlockModel model;
                        if (j.isJsonPrimitive()) {
                            model = (BlockModel) modelBakery.getModel(ResourceLocation.tryParse(j.getAsString()));
                        } else {
                            model = ClientPlatformHelper.parseBlockModel(j);
                        }

                        return model.bake(modelBakery, model, spriteGetter, transform, location, true);
                    }));
            return new PaintingBlockModel(map);
        }
    }
}
