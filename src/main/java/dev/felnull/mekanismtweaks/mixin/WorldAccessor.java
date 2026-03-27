package dev.felnull.mekanismtweaks.mixin;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TileEntity.class)
public interface WorldAccessor {
    @Accessor("world")
    World getWorld();

    @Accessor("pos")
    BlockPos getPos();
}
