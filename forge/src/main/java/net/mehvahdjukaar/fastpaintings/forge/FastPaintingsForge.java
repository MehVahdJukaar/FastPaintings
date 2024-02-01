package net.mehvahdjukaar.fastpaintings.forge;

import net.mehvahdjukaar.fastpaintings.FastPaintings;
import net.mehvahdjukaar.fastpaintings.FastPaintingsClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.neoforged.fml.common.Mod;

import static net.mehvahdjukaar.fastpaintings.FastPaintings.MOD_ID;

/**
 * Author: MehVahdJukaar
 */
@Mod(MOD_ID)
public class FastPaintingsForge {


    public FastPaintingsForge() {
        FastPaintings.init();
        if (PlatHelper.getPhysicalSide().isClient()) {
            FastPaintingsClient.init();
        }

    }

}
