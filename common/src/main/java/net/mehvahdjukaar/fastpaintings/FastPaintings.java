package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;


public class FastPaintings {
    public static final String MOD_ID = "fastpaintings";
    public static final Logger LOGGER = LogManager.getLogger("Fast Paintings");

    public static final Supplier<NBTDropMode> SPECIAL_DROP;
    public static final ModConfigHolder CONFIG;

    public static Identifier res(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static final SoundType PAINTING = new SoundType(1.0F, 1.0F,
            SoundEvents.PAINTING_BREAK, SoundEvents.GRASS_STEP,
            SoundEvents.PAINTING_PLACE, SoundEvents.WOOD_HIT,
            SoundEvents.WOOD_FALL);

    public static final Supplier<PaintingBlock> PAINTING_BLOCK = RegHelper.registerBlock(
            res("painting"),
            PaintingBlock::new,
            BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
                    .mapColor(MapColor.NONE)
                    .noOcclusion()
                    .instabreak()
                    .sound(PAINTING)
    );

    public static final Supplier<BlockEntityType<PaintingBlockEntity>> PAINTING_TILE = RegHelper.registerBlockEntityType(
            res("painting"),
            PaintingBlockEntity::new,
            PAINTING_BLOCK
    );


    static {
        ConfigBuilder builder = ConfigBuilder.create(MOD_ID, ConfigType.COMMON);
        builder.push("general");
        SPECIAL_DROP = PlatHelper.isModLoaded("easel_does_it") ? () -> NBTDropMode.ALWAYS :
                builder.comment("Makes paintings always drop with their painting variant")
                        .define("nbt_drop", NBTDropMode.OFF);
        builder.pop();

        CONFIG = builder.build();
    }


    public static void init() {

    }
}
