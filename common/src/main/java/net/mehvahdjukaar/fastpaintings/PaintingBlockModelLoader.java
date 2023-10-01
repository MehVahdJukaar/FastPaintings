package net.mehvahdjukaar.fastpaintings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
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

        var models = l.stream().collect(Collectors.toMap(s -> s, json::get));
        return (modelBakery, spriteGetter, transform, location) -> {
            var map = models.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    e -> {
                        var j = e.getValue();
                        BlockModel model;
                        if (j.isJsonPrimitive()) {
                            model = (BlockModel) modelBakery.getModel(ResourceLocation.tryParse(j.getAsString()));
                        } else {
                            model = ClientHelper.parseBlockModel(j);
                        }
                        model.resolveParents(modelBakery::getModel);
                        return model.bake(modelBakery, model, spriteGetter, transform, location, true);
                    }));
            return new PaintingBlockModel(map);
        };
    }
}
