package net.mehvahdjukaar.fastpaintings;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.block.state.BlockState;
import subaraki.paintings.Paintings;

public class SetPaintingMessage implements Message {


    private final ResourceLocation paintingName;
    private final BlockPos pos;

    public SetPaintingMessage(ResourceLocation paintingVariant, BlockPos paintingPos) {
        this.paintingName = paintingVariant;
        this.pos = paintingPos;
    }

    public SetPaintingMessage(FriendlyByteBuf buf) {
        this.paintingName = buf.readResourceLocation();
        this.pos = buf.readBlockPos();
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.paintingName);
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
      var level =  context.getSender().level();
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() instanceof PaintingBlock){
          level.removeBlock(pos, false);
          var p = Painting.create(level, pos, state.getValue(PaintingBlock.FACING));
          if(p.isPresent()){
              var painting = p.get();
              PaintingVariant variant =  BuiltInRegistries.PAINTING_VARIANT.get(paintingName);
              Paintings.UTILITY.setArt(painting, variant);
              Paintings.UTILITY.updatePaintingBoundingBox(painting);
              level.addFreshEntity(p.get());
          }
      }
    }
}
