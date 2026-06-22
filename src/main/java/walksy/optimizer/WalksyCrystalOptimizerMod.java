package walksy.optimizer;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import walksy.optimizer.command.EnableOptimizerCommand;

import java.util.List;
import java.util.Random;


public class WalksyCrystalOptimizerMod implements ClientModInitializer {
    public static Minecraft mc;
    public static final Random random = new Random();


    /**
     * just because his mod works, doesn't mean it should be banned - Walksy's Mother
     */

    @Override
    public void onInitializeClient() {
        mc = Minecraft.getInstance();
        EnableOptimizerCommand command = new EnableOptimizerCommand();
        command.initializeToggleCommands();
    }

    public static int hitCount;
    public static int breakingBlockTick;
    public static void useOwnTicks() {
        if (!EnableOptimizerCommand.fastCrystal ||
                random.nextDouble() * 100 > EnableOptimizerCommand.fastCrystalChance) {
            return;
        }
        ItemStack mainHandStack = mc.player.getMainHandItem();

        if (mc.options.keyAttack.isDown()) {
            breakingBlockTick++;
        } else breakingBlockTick = 0;

        if (breakingBlockTick > 2)
            return;

        if (!mc.options.keyUse.isDown()) {
            hitCount = 0;
        }
        if (hitCount == limitPackets())
            return;
        if (lookingAtSaidEntity()) {
            if (mc.options.keyAttack.isDown()) {
                if (hitCount >= 1) {
                    Entity entity = removeSaidEntity();
                    if (entity != null) {
                        entity.setRemoved(Entity.RemovalReason.KILLED);
                    }
                }
                hitCount++;
            }
        }
        if (!mainHandStack.is(Items.END_CRYSTAL)) {
            return;
        }
        if (mc.options.keyUse.isDown()
                && (isLookingAt(Blocks.OBSIDIAN, generalLookPos().getBlockPos())
                || isLookingAt(Blocks.BEDROCK, generalLookPos().getBlockPos())))
        {
            sendInteractBlockPacket(generalLookPos().getBlockPos(), generalLookPos().getDirection());
            if (canPlaceCrystalServer(generalLookPos().getBlockPos())) {
                mc.player.swing(mc.player.getUsedItemHand());
            }
        }
    }


    private static BlockState getBlockState(BlockPos pos) {
        return mc.level.getBlockState(pos);
    }
    private static boolean isLookingAt(Block block, BlockPos pos) {
        return getBlockState(pos).getBlock() == block;
    }


    private static BlockHitResult generalLookPos() {
        Vec3 camPos = mc.player.getEyePosition();
        Vec3 clientLookVec = lookVec();
        return mc.level.clip(new ClipContext(camPos, camPos.add(clientLookVec.scale(4.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));

    }

    private static Entity removeSaidEntity() {
        Entity entity = null;
        if (mc.hitResult instanceof EntityHitResult hit) {
            if (hit.getEntity() instanceof EndCrystal crystalEntity) {
                entity = crystalEntity;
            } else if (hit.getEntity() instanceof Slime slimeEntity) {
                entity = slimeEntity;
            } else if (hit.getEntity() instanceof MagmaCube magmaCubeEntity) {
                entity = magmaCubeEntity;
            }
        }
        return entity;
    }

    private static boolean lookingAtSaidEntity() {
        return
                mc.hitResult instanceof EntityHitResult entity && (entity.getEntity() instanceof EndCrystal
                        || entity.getEntity() instanceof MagmaCube
                        || entity.getEntity() instanceof Slime);
    }

    private static Vec3 lookVec() {
        float f = (float) Math.PI / 180;
        float pi = (float) Math.PI;
        float f1 = (float) Math.cos(-mc.player.getYRot() * f - pi);
        float f2 = (float) Math.sin(-mc.player.getYRot() * f - pi);
        float f3 = -(float) Math.cos(-mc.player.getXRot() * f);
        float f4 = (float) Math.sin(-mc.player.getXRot() * f);
        return new Vec3(f2 * f3, f4, f1 * f3).normalize();
    }

    private static InteractionResult sendInteractBlockPacket(BlockPos pos, Direction dir) {
        Vec3 vec = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        return setPacket(vec,dir);
    }

    private static InteractionResult setPacket(Vec3 vec3d, Direction dir) {
        Vec3i vec3i = new Vec3i((int) vec3d.x, (int) vec3d.y, (int) vec3d.z);
        BlockPos pos = new BlockPos(vec3i);
        BlockHitResult result = new BlockHitResult(vec3d, dir,pos,false);
        return mc.gameMode.useItemOn(mc.player,mc.player.getUsedItemHand(),result);
    }

    public static int limitPackets() {
        int stop = 2;
        if (getPing() > 50) stop = 2;
        if (getPing() < 50) stop = 1;
        return stop;
    }

    private static int getPing() {
        if (mc.getConnection() == null) return 0;

        PlayerInfo playerListEntry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }

    private static boolean canPlaceCrystalServer(BlockPos block) {
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
