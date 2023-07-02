package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;

public class FastPaintingsClient {

    public static void init() {
        ClientHelper.addClientSetup(FastPaintingsClient::setup);
        ClientHelper.addModelLoaderRegistration(FastPaintingsClient::registerModelLoaders);
    }

    public static void setup() {
    }

    private static void registerModelLoaders(ClientHelper.ModelLoaderEvent event) {
        event.register(FastPaintings.res("painting"), new PaintingBlockModelLoader());
    }

}
