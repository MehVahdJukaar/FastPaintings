package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class FastPaintingsClient {

    public static final ResourceLocation PAINTING_SHEET = new ResourceLocation("paintings");

    public static void init() {
        ClientHelper.addClientSetup(FastPaintingsClient::setup);
        ClientHelper.addModelLoaderRegistration(FastPaintingsClient::registerModelLoaders);
    }

    public static void setup() {
        ClientHelper.registerRenderType(FastPaintings.PAINTING_BLOCK.get(), RenderType.cutout());
    }

    private static void registerModelLoaders(ClientHelper.ModelLoaderEvent event) {
        event.register(FastPaintings.res("painting"), new PaintingBlockModelLoader());
    }

}
