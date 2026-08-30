package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaintingBlock extends WaterBlock implements EntityBlock {

    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);
    protected static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final IntegerProperty DOWN_OFFSET = IntegerProperty.create("y_offset", 0, 15);
    protected static final IntegerProperty RIGHT_OFFSET = IntegerProperty.create("x_offset", 0, 15);

    public PaintingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(DOWN_OFFSET, 0)
                .setValue(RIGHT_OFFSET, 0)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        var m = getMaster(state, pos, level);
        if (m != null && !m.hasDroppedItemHack) {
            m.hasDroppedItemHack = true; //hack to prevent double drops
            if (!player.isCreative()) {
                ItemStack painting = getPaintingItem(m.getVariant(), m.isPlacedWithNbt());
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, painting);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        PaintingBlockEntity m = getMaster(state, pos, level);
        if (m != null) {
            return getPaintingItem(m.getVariant(), m.isPlacedWithNbt());
        }
        return new ItemStack(Items.PAINTING);
    }

    public static ItemStack getPaintingItem(Holder<PaintingVariant> variant, boolean wasPlacedWithNbt) {
        NBTDropMode mode = FastPaintings.SPECIAL_DROP.get();
        ItemStack itemStack = new ItemStack(Items.PAINTING);
        if (mode == NBTDropMode.OFF || (mode == NBTDropMode.WHEN_PLACED_WITH_NBT && !wasPlacedWithNbt)) {
            return itemStack;
        }
        itemStack.set(DataComponents.PAINTING_VARIANT, variant);
        return itemStack;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState blockState) {
        return true;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ec && !(ec.getEntity() instanceof HangingEntity)) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, DOWN_OFFSET, RIGHT_OFFSET);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState,
                                     RandomSource random) {
        Direction dir = state.getValue(FACING);
        if (directionToNeighbour.getOpposite() == dir &&
                !neighbourState.isSolid() && !DiodeBlock.isDiode(neighbourState)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        PaintingBlockEntity m = getMaster(state, pos, level);
        if (m != null) {
            pos = m.getBlockPos();
            Direction dir = state.getValue(FACING);
            var variant = m.getVariant();

            int width = getWidth(variant);
            int height = getHeight(variant);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    BlockPos p = pos.below(y).relative(dir.getCounterClockWise(), x);
                    var b = level.getBlockState(p);
                    if (!b.is(this) || b.getValue(FACING) != dir ||
                            b.getValue(DOWN_OFFSET) != y ||
                            b.getValue(RIGHT_OFFSET) != x) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!canSurvive(state, level, pos)) {
            var m = getMaster(state, pos, level);
            if (m != null) {
                pos = m.getBlockPos();
                Direction dir = state.getValue(FACING);
                var variant = m.getVariant();

                int width = getWidth(variant);
                int height = getHeight(variant);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        BlockPos p = pos.below(y).relative(dir.getCounterClockWise(), x);
                        if (level.getBlockState(p).is(this)) level.removeBlock(p, false);
                    }
                }
            } else level.removeBlock(pos, false);
        }
    }

    private static int getHeight(Holder<PaintingVariant> variant) {
        return variant.value().height();
    }

    private static int getWidth(Holder<PaintingVariant> variant) {
        return variant.value().width();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isMaster(state) ? new PaintingBlockEntity(pos, state) : null;
    }


    public static boolean tryConverting(Painting entity, @Nullable ItemStack stack) {
        Level level = entity.level();
        Direction dir = entity.getDirection();
        var variant = entity.getVariant();

        int width = getWidth(variant);
        int height = getHeight(variant);
        if (width > 5 || height > 5) return false;
        var bb = entity.getBoundingBox();
        //bad code ahead
        BlockPos pos = switch (dir) {
            case SOUTH -> BlockPos.containing(bb.minX, bb.maxY - 0.5, bb.maxZ);
            case WEST -> BlockPos.containing(bb.minX, bb.maxY - 0.5, bb.minZ);
            case EAST -> BlockPos.containing(bb.maxX, bb.maxY - 0.5, bb.maxZ - 0.5);
            default -> BlockPos.containing(bb.maxX - 0.5, bb.maxY - 0.5, bb.minZ);
        };


        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                BlockPos p = pos.below(y).relative(dir.getCounterClockWise(), x);
                if (!level.getBlockState(p).isAir()) {
                    return false;
                }
            }
        }
        if (level.getBlockState(pos).isAir()) {
            BlockState state = FastPaintings.PAINTING_BLOCK.get().defaultBlockState()
                    .setValue(FACING, dir);
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(pos) instanceof PaintingBlockEntity pe) {
                pe.setVariant(variant);

                if (stack != null) {
                    var chosenVariant = stack.get(DataComponents.PAINTING_VARIANT);
                    if (chosenVariant != null && chosenVariant.value() == variant.value()) {
                        pe.setPlacedWithNbt(true);
                    }
                }

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (x == 0 && y == 0) continue;
                        BlockPos p = pos.below(y).relative(dir.getCounterClockWise(), x);
                        level.setBlock(p, state
                                        .setValue(DOWN_OFFSET, y)
                                        .setValue(RIGHT_OFFSET, x),
                                Block.UPDATE_CLIENTS);
                    }
                }

                pe.requestModelReload();

                entity.discard();

                return true;
            }
        }
        return false;
    }

    public static boolean isMaster(BlockState state) {
        return state.getValue(DOWN_OFFSET) == 0 && state.getValue(RIGHT_OFFSET) == 0;
    }

    public static PaintingBlockEntity getMaster(BlockState state, BlockPos pos, BlockGetter level) {
        BlockPos masterPos = getMasterPos(state, pos);
        if (level instanceof Level l && !l.isLoaded(pos)) {
            return null;
        }
        var e = level.getBlockEntity(masterPos);
        if (e instanceof PaintingBlockEntity pe) return pe;
        return null;
    }

    @NotNull
    private static BlockPos getMasterPos(BlockState state, BlockPos pos) {
        int y = state.getValue(DOWN_OFFSET);
        int x = state.getValue(RIGHT_OFFSET);
        Direction facing = state.getValue(FACING);
        return pos.above(y).relative(facing.getClockWise(), x);
    }

    //so block behind is not culled in case painting is transparent
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction direction) {
        if (adjacentBlockState.is(this) && adjacentBlockState.getValue(FACING) == state.getValue(FACING)) return true;
        return state.getValue(FACING) == direction || super.skipRendering(state, adjacentBlockState, direction);
    }
}
