package net.mehvahdjukaar.fastpaintings.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.mehvahdjukaar.fastpaintings.FastPaintings;
import net.mehvahdjukaar.fastpaintings.FastPaintingsClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;

public class FastPaintingsFabric implements ModInitializer {

    public void onInitialize() {
        FastPaintings.init();
        if (PlatHelper.getPhysicalSide().isClient()) {
            FastPaintingsClient.init();
        }
    }

}
