package walksy.optimizer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static walksy.optimizer.WalksyCrystalOptimizerMod.mc;

@Mixin({EndCrystalItem.class})
public class EndCrystalItemMixin {


    /**
     * @Author Walksy
     */


    /**
     * Stops crystals from decreasing too much
     * PS: does not work on singleplayer
     */
    @Inject(method = {"useOn"}, at = {@At("HEAD")}, cancellable = true)
    private void modifyDecrementAmount(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack mainHandStack = mc.player.getMainHandItem();
        if (mainHandStack.is(Items.END_CRYSTAL)) {
            if (isLookingAt(Blocks.OBSIDIAN, generalLookPos().getBlockPos())
                    || isLookingAt(Blocks.BEDROCK, generalLookPos().getBlockPos())) {
                HitResult hitResult = mc.hitResult;
                if (hitResult instanceof BlockHitResult) {
                    BlockHitResult hit = (BlockHitResult)hitResult;
                    BlockPos block = hit.getBlockPos();
                    if (canPlaceCrystalServer(block))
                        context.getItemInHand().shrink(-1);
                }
            }
        }
    }


    private BlockState getBlockState(BlockPos pos) {
        return mc.level.getBlockState(pos);
    }
    private boolean isLookingAt(Block block, BlockPos pos) {
        return getBlockState(pos).getBlock() == block;
    }
    private BlockHitResult generalLookPos() {
        Vec3 camPos = mc.player.getEyePosition();
        Vec3 clientLookVec = lookVec();
        return mc.level.clip(new ClipContext(camPos, camPos.add(clientLookVec.scale(4.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));

    }
    private Vec3 lookVec() {
        float f = (float) Math.PI / 180;
        float pi = (float) Math.PI;
        float f1 = (float) Math.cos(-mc.player.getYRot() * f - pi);
        float f2 = (float) Math.sin(-mc.player.getYRot() * f - pi);
        float f3 = -(float) Math.cos(-mc.player.getXRot() * f);
        float f4 = (float) Math.sin(-mc.player.getXRot() * f);
        return new Vec3(f2 * f3, f4, f1 * f3).normalize();
    }

    private boolean canPlaceCrystalServer(BlockPos block) {
        BlockState blockState = mc.level.getBlockState(block);
        if (!blockState.is(Blocks.OBSIDIAN) && !blockState.is(Blocks.BEDROCK))
            return false;
        BlockPos blockPos2 = block.above();
        if (!mc.level.isEmptyBlock(blockPos2))
            return false;
        double d = blockPos2.getX();
        double e = blockPos2.getY();
        double f = blockPos2.getZ();
        List<Entity> list = mc.level.getEntities((Entity)null, new AABB(d, e, f, d + 1.0D, e + 2.0D, f + 1.0D));
        return list.isEmpty();
    }
}

