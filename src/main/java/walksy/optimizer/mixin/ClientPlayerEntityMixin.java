package walksy.optimizer.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.optimizer.WalksyCrystalOptimizerMod;
import walksy.optimizer.command.EnableOptimizerCommand;

import java.util.List;

import static walksy.optimizer.WalksyCrystalOptimizerMod.mc;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {

    /**
     * @Author Walksy
     */

    @Inject(at = @At("HEAD"), method = "tick()V")
    private void useOwnTicks(CallbackInfo ci) {
        if (EnableOptimizerCommand.fastCrystal) {
            WalksyCrystalOptimizerMod.useOwnTicks();
        }
    }
}