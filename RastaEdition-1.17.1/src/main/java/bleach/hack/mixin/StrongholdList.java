package bleach.hack.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChunkGenerator.class)
public class StrongholdList{
    @Shadow
    private List<ChunkPos> strongholds;

    @Inject(at = @At("RETURN"), method = "locateStructure")
    public void printAll(ServerWorld world, StructureFeature<?> feature, BlockPos center, int radius, boolean skipExistingChunks, CallbackInfoReturnable info) {
        for (ChunkPos strong : strongholds) {
            BlockPos p = new BlockPos(strong.x*16, 0, strong.z*16);
            System.out.println(p);
        }
    }
}
