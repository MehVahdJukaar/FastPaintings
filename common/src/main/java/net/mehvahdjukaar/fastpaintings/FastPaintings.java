package net.mehvahdjukaar.fastpaintings;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.minecraft.core.BlockPos;
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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;


public class FastPaintings {
    public static final String MOD_ID = "fastpaintings";
    public static final Logger LOGGER = LogManager.getLogger("Fast Paintings");

    public static Supplier<DropMode> SPECIAL_DROP;

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static final ChannelHandler CHANNEL = ChannelHandler.createChannel(res("channel"));


    public static final SoundType PAINTING = new SoundType(1.0F, 1.0F,
            SoundEvents.PAINTING_BREAK, SoundEvents.GRASS_STEP,
            SoundEvents.PAINTING_PLACE, SoundEvents.WOOD_HIT,
            SoundEvents.WOOD_FALL);

    public static final Supplier<Block> PAINTING_BLOCK = RegHelper.registerBlock(
            res("painting"),
            () -> new PaintingBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
                    .mapColor(MapColor.NONE)
                    .noOcclusion()
                    .instabreak()
                    .sound(PAINTING))


    );

    public static final Supplier<BlockEntityType<PaintingBlockEntity>> PAINTING_TILE = RegHelper.registerBlockEntityType(
            res("painting"),
            () -> PlatHelper.newBlockEntityType(PaintingBlockEntity::new, PAINTING_BLOCK.get())
    );

    public static void init() {
        CHANNEL.register(NetworkDir.PLAY_TO_SERVER, SetPaintingMessage.class, SetPaintingMessage::new);

        ConfigBuilder builder =  ConfigBuilder.create(MOD_ID, ConfigType.COMMON);
        builder.push("general");
        SPECIAL_DROP = builder.comment("Makes paintings always drop with their NBT")
                        .define("nbt_drop", DropMode.OFF);
        builder.pop();

        builder.buildAndRegister();
    }


    public static final LoadingCache<Integer, BlockPos> LAST_KNOWN_ENTITY_POS = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public BlockPos load(Integer key) {
                    return null;
                }
            });

}
