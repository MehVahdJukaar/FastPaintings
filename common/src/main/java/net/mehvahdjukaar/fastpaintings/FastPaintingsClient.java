package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.renderer.RenderType;

public class FastPaintingsClient {

    public static void init() {
        ClientPlatformHelper.addClientSetup(FastPaintingsClient::setup);
        ClientPlatformHelper.addModelLoaderRegistration(FastPaintingsClient::registerModelLoaders);
    }

    public static void setup() {
        ClientPlatformHelper.registerRenderType(FastPaintings.PAINTING_BLOCK.get(), RenderType.cutout());
    }

    private static void registerModelLoaders(ClientPlatformHelper.ModelLoaderEvent event) {
        event.register(FastPaintings.res("painting"), new PaintingBlockModelLoader());
    }

}
