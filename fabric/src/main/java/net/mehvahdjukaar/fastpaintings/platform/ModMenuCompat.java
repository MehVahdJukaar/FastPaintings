package net.mehvahdjukaar.fastpaintings.platform;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.fastpaintings.FastPaintings;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ClientHelper.makeConfigScreen(FastPaintings.CONFIG, parent, null);
    }
}
