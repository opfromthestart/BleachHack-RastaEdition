package bleach.hack.mixin;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChunkGenerator.class)
public interface IStronghold
{
    @Accessor
    List<ChunkPos> getStrongholds();
}