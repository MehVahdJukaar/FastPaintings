package net.mehvahdjukaar.fastpaintings.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.fastpaintings.FastPaintings;
import net.mehvahdjukaar.fastpaintings.FastPaintingsClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;

public class FastPaintingsFabric implements ModInitializer {

    public void onInitialize() {
        FastPaintings.init();
        if (PlatformHelper.getEnv().isClient()) {
            FastPaintingsClient.init();
        }
    }

}
