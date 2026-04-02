package walksy.optimizer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.optimizer.WalksyCrystalOptimizerMod;
import walksy.optimizer.command.EnableOptimizerCommand;

import static walksy.optimizer.WalksyCrystalOptimizerMod.limitPackets;
import static walksy.optimizer.WalksyCrystalOptimizerMod.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "startUseItem", cancellable = true)
    private void onDoItemUse(CallbackInfo ci) {
        if (EnableOptimizerCommand.fastCrystal) {
            ItemStack mainHand = mc.player.getMainHandItem();
            if (mainHand.is(Items.END_CRYSTAL))
                if (WalksyCrystalOptimizerMod.hitCount != limitPackets())
                    ci.cancel();
        }
    }
}
