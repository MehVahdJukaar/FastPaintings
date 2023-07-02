package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
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

    public static ResourceLocation res(String name) {

        return new ResourceLocation(MOD_ID, name);
    }

   public static final SoundType PAINTING = new SoundType(1.0F, 1.0F,
           SoundEvents.PAINTING_BREAK, SoundEvents.GRASS_STEP,
           SoundEvents.PAINTING_PLACE, SoundEvents.WOOD_HIT,
           SoundEvents.WOOD_FALL);

    public static final Supplier<Block> PAINTING_BLOCK = RegHelper.registerBlock(
            res("painting"),
            () -> new FastPaintingBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
                    .mapColor(MapColor.NONE)
                    .instabreak()
                    .sound(PAINTING))


    );

    public static final Supplier<BlockEntityType<PaintingBlockEntity>> PAINTING_TILE = RegHelper.registerBlockEntityType(
            res("painting"),
            () -> PlatHelper.newBlockEntityType(PaintingBlockEntity::new, PAINTING_BLOCK.get())
    );

    public static void init() {
    }
}
