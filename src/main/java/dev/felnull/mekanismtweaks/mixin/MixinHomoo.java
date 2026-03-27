package dev.felnull.mekanismtweaks.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import mekanism.api.Upgrade;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.lib.HashList;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;

@Mixin(value = TileEntityDigitalMiner.class, remap = false)
public abstract class MixinHomoo {

    @Shadow
    public Long2ObjectMap<BitSet> oresToMine;

    @Shadow
    private HashList<MinerFilter<?>> filters;

    @Shadow
    public boolean inverse;

    @Shadow
    public ItemStack missingStack;

    @Shadow
    public int delay;

    @Shadow
    public abstract void recalculateUpgrades(Upgrade upgrade);

    @Shadow
    public abstract int getDelay();

    @Shadow
    protected abstract BlockPos getPosFromIndex(int index);

    @Shadow
    protected abstract boolean canMine(BlockPos pos);

    @Shadow
    protected abstract List<ItemStack> getDrops(BlockState state, BlockPos pos);

    @Shadow
    public abstract boolean canInsert(List<ItemStack> toInsert);

    @Shadow
    protected abstract boolean setReplace(BlockPos pos, int index);

    @Shadow
    protected abstract void add(List<ItemStack> drops);

    @Shadow
    protected abstract void updateCachedToMine();

    @Inject(method = "onUpdateServer",
            at = @At(value = "INVOKE", target = "Lmekanism/common/tile/machine/TileEntityDigitalMiner;updateCachedToMine()V", shift = At.Shift.AFTER))
    public void injected(CallbackInfo ci) {
        if (delay < 0) {
            for (int i = delay; i < 0; i++) {
                tryMineBlock$compat();
            }
            recalculateUpgrades(Upgrade.SPEED);
            ((DelayAccessor) this).setDelay(getDelay());
        }
    }

    @SuppressWarnings({"deprecation", "IfStatementWithIdenticalBranches"})
    @Unique
    private void tryMineBlock$compat() {
        World world = ((WorldAccessor) this).getWorld();
        boolean did = false;
        LongIterator it = oresToMine.keySet().iterator();

        while (it.hasNext()) {
            long chunk = it.nextLong();
            BitSet set = oresToMine.get(chunk);
            int next = 0;

            while (!did) {
                int index = set.nextSetBit(next);
                BlockPos pos = getPosFromIndex(index);
                if (index == -1) {
                    it.remove();
                    break;
                }

                Optional<BlockState> blockState = mekanism.common.util.WorldUtils.getBlockState(world, pos);
                if (blockState.isPresent() && !blockState.get().isAir(world, pos)) {
                    boolean hasFilter = false;
                    BlockState state = blockState.get();

                    for (MinerFilter<?> filter : filters) {
                        if (filter.canFilter(state)) {
                            hasFilter = true;
                            break;
                        }
                    }

                    if (inverse != hasFilter && canMine(pos)) {
                        List<ItemStack> drops = getDrops(state, pos);
                        if (canInsert(drops) && setReplace(pos, index)) {
                            did = true;
                            add(drops);
                            set.clear(index);
                            if (set.cardinality() == 0) {
                                it.remove();
                            }

                            world.playEvent(2001, pos, Block.getStateId(state));
                            missingStack = ItemStack.EMPTY;
                        }
                        break;
                    }

                    set.clear(index);
                    if (set.cardinality() == 0) {
                        it.remove();
                        break;
                    }

                    next = index + 1;
                } else {
                    set.clear(index);
                    if (set.cardinality() == 0) {
                        it.remove();
                        break;
                    }

                    next = index + 1;
                }
            }
        }

        updateCachedToMine();
    }
}

