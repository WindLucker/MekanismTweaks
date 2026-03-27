package dev.felnull.mekanismtweaks.mixin;

import dev.felnull.mekanismtweaks.Temp;
import mekanism.api.Upgrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Upgrade.class)
public abstract class MixinUpgrade {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, name = "arg3")
    private static String getName(String s) {
        Temp.name = s;
        return s;
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, name = "arg6")
    private static int toFullStack(int i) {
        return Temp.name.equals("speed") || Temp.name.equals("energy") ? 32 : i;
    }
}
