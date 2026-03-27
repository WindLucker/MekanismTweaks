package dev.felnull.mekanismtweaks.mixin;

import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TileEntitySolarNeutronActivator.class, remap = false)
public abstract class MixinSolarNeutronActivator {

    @Shadow
    private float peakProductionRate;

    /**
     * @author Sakyraisu
     * @reason Remove day/night check - only require sky visibility and redstone control
     */
    @Overwrite
    private boolean canFunction() {
        TileEntitySolarNeutronActivator self = (TileEntitySolarNeutronActivator) (Object) this;
        World world = ((WorldAccessor) this).getWorld();
        BlockPos pos = ((WorldAccessor) this).getPos();

        return MekanismUtils.canFunction(self)
                && world != null
                && world.getDimensionType().hasSkyLight()
                && world.canBlockSeeSky(pos.up());
    }

    /**
     * @author Sakyraisu
     * @reason Force Solar Neutron Activator to work 24/7 at max speed, ignoring day/night cycle and weather
     */
    @Overwrite
    private float recalculateProductionRate() {
        return canFunction() ? peakProductionRate : 0;
    }
}
