package net.mehvahdjukaar.fastpaintings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PaintingBlockModelLoader implements CustomModelLoader {

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        JsonElement jsonModels = json.get("models");
        var modelsMap = new HashMap<String, JsonElement>();
        if (jsonModels instanceof JsonObject j) {
            modelsMap.putAll(j.asMap());
        }
        return (modelBakery, spriteGetter, transform, location) -> {
            var map = modelsMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
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
