package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import bleach.hack.BleachHack;
import bleach.hack.event.events.EventBlockShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;

@Mixin(BlockCollisionSpliterator.class)
public class MixinBlockCollisionSpliterator {

    @Redirect(method = "offerBlockShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape calculatePushVelocity_getCollisionShape(BlockState blockState, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = blockState.getCollisionShape(world, pos, context);
        EventBlockShape event = new EventBlockShape((BlockState) blockState, pos, shape);
        BleachHack.eventBus.post(event);

        if (event.isCancelled()) {
            return VoxelShapes.empty();
        }

        return event.getShape();
    }
}