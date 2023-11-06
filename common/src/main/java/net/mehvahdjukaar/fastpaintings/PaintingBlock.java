package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class PaintingBlock extends WaterBlock implements EntityBlock {

    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);
    protected static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final IntegerProperty DOWN_OFFSET = IntegerProperty.create("y_offset", 0, 5);
    protected static final IntegerProperty RIGHT_OFFSET = IntegerProperty.create("x_offset", 0, 5);

    public PaintingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(DOWN_OFFSET, 0)
                .setValue(RIGHT_OFFSET, 0)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState blockState) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        return switch (state.getValue(FACING)) {
            default -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction updateDir, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        Direction dir = stateIn.getValue(FACING);
        if (updateDir.getOpposite() == dir) {
            if (!facingState.isSolid() && !DiodeBlock.isDiode(facingState)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(stateIn, updateDir, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
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
        return variant.value().getHeight() / 16;
    }

    private static int getWidth(Holder<PaintingVariant> variant) {
        return variant.value().getWidth() / 16;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isMaster(state) ? new PaintingBlockEntity(pos, state) : null;
    }


    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return Items.PAINTING.getDefaultInstance();
        //TODO: proper way with block item map
    }


    public static void tryConverting(Painting entity) {
        Level level = entity.level();
        Direction dir = entity.getDirection();
        var variant = entity.getVariant();

        int width = getWidth(variant);
        int height = getHeight(variant);
        if (width > 5 || height > 5) return;
        var bb = entity.getBoundingBox();
        //bad code ahead
        BlockPos pos = switch (dir) {
            default -> BlockPos.containing(bb.maxX - 0.5, bb.maxY - 0.5, bb.minZ);
            case SOUTH -> BlockPos.containing(bb.minX, bb.maxY - 0.5, bb.maxZ);
            case WEST -> BlockPos.containing(bb.minX, bb.maxY - 0.5, bb.minZ);
            case EAST -> BlockPos.containing(bb.maxX, bb.maxY - 0.5, bb.maxZ - 0.5);
        };


        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                BlockPos p = pos.below(y).relative(dir.getCounterClockWise(), x);
                if (!level.getBlockState(p).isAir()) {
                    return;
                }
            }
        }
        if (level.getBlockState(pos).isAir()) {
            BlockState state = FastPaintings.PAINTING_BLOCK.get().defaultBlockState()
                    .setValue(FACING, dir);
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(pos) instanceof PaintingBlockEntity pe) {
                pe.setVariant(variant);


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

                FastPaintings.LAST_KNOWN_ENTITY_POS.put(entity.getId(), entity.getPos());
            }
        }
    }

    public static boolean isMaster(BlockState state) {
        return state.getValue(DOWN_OFFSET) == 0 && state.getValue(RIGHT_OFFSET) == 0;
    }

    public static PaintingBlockEntity getMaster(BlockState state, BlockPos pos, BlockAndTintGetter level) {
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

    //@Override
    public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
        return state.getValue(FACING).getOpposite() != dir;
    }

}
