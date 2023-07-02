package net.mehvahdjukaar.fastpaintings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
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
        var map = l.stream().collect(Collectors.toMap(s -> s, s -> ClientHelper.parseBlockModel(json.get(s))));
        return new Geometry(map);
    }


    private record Geometry(Map<String, BlockModel> models) implements CustomGeometry {

        @Override
        public CustomBakedModel bake(ModelBaker modelBakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
            var map = models.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, m -> m.getValue().bake(modelBakery, m.getValue(), spriteGetter, transform, location, true)));
            return new PaintingBlockModel(map);
        }
    }
}
