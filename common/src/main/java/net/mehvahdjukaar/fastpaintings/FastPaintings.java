package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.function.Supplier;


public class FastPaintings {
    public static final String MOD_ID = "fastpaintings";

    public static ResourceLocation res(String name) {

        return new ResourceLocation(MOD_ID, name);
    }

   public static SoundType PAINTING = new SoundType(1.0F, 1.0F,
           SoundEvents.PAINTING_BREAK, SoundEvents.GRASS_STEP,
           SoundEvents.PAINTING_PLACE, SoundEvents.WOOD_HIT,
           SoundEvents.WOOD_FALL);

    public static final Supplier<Block> PAINTING_BLOCK = RegHelper.registerBlock(
            res("painting"),
            () -> new PaintingBlock(BlockBehaviour.Properties.of(Material.POWDER_SNOW)
                    .color(MaterialColor.NONE)
                    .instabreak()
                    .sound(PAINTING))


    );

    public static final Supplier<BlockEntityType<PaintingBlockEntity>> PAINTING_TILE = RegHelper.registerBlockEntityType(
            res("painting"),
            () -> PlatformHelper.newBlockEntityType(PaintingBlockEntity::new, PAINTING_BLOCK.get())
    );

    public static void init() {
    }
}
