package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;

public class FastPaintingsClient {

    public static void init() {
        ClientHelper.addBlockModelRegistration(FastPaintingsClient::registerBlockModels);
    }

    private static void registerBlockModels(ClientHelper.BlockModelEvent event) {
        event.register(FastPaintings.res("painting"), PaintingBlockModel.Unbaked.CODEC);
    }

}
