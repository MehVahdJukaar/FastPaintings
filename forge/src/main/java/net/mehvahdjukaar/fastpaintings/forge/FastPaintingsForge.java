package net.mehvahdjukaar.fastpaintings.forge;

import net.mehvahdjukaar.fastpaintings.FastPaintings;
import net.mehvahdjukaar.fastpaintings.FastPaintingsClient;
import net.mehvahdjukaar.moonlight.api.client.model.forge.BakedQuadBuilderImpl;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraftforge.fml.common.Mod;

import static net.mehvahdjukaar.fastpaintings.FastPaintings.MOD_ID;

/**
 * Author: MehVahdJukaar
 */
@Mod(MOD_ID)
public class FastPaintingsForge {


    public FastPaintingsForge() {
        FastPaintings.init();
        if (PlatformHelper.getEnv().isClient()) {
            FastPaintingsClient.init();
        }

    }
}
