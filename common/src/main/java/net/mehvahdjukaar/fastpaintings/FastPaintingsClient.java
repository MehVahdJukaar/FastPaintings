package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;

public class FastPaintingsClient {

    public static void init() {
        ClientPlatformHelper.addClientSetup(FastPaintingsClient::setup);
        ClientPlatformHelper.addModelLoaderRegistration(FastPaintingsClient::registerModelLoaders);
    }

    public static void setup() {
    }

    private static void registerModelLoaders(ClientPlatformHelper.ModelLoaderEvent event) {
        event.register(FastPaintings.res("painting"), new PaintingBlockModelLoader());
    }

}
