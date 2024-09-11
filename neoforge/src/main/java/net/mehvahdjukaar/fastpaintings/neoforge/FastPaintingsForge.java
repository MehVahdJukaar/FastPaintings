package net.mehvahdjukaar.fastpaintings.neoforge;

import net.mehvahdjukaar.fastpaintings.FastPaintings;
import net.mehvahdjukaar.fastpaintings.FastPaintingsClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.IModBusEvent;

import static net.mehvahdjukaar.fastpaintings.FastPaintings.MOD_ID;

/**
 * Author: MehVahdJukaar
 */
@Mod(MOD_ID)
public class FastPaintingsForge {


    public FastPaintingsForge(IModBusEvent busEvent) {
        RegHelper.startRegisteringFor(busEvent);
        FastPaintings.init();
        if (PlatHelper.getPhysicalSide().isClient()) {
            FastPaintingsClient.init();
        }

    }

}
