/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * no tnt mode by https://github.com/LXYan2333
 */
package bleach.hack.module.mods;

import bleach.hack.event.events.EventInteract;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.render.RenderUtils;
import bleach.hack.util.render.color.QuadColor;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoBedrockBreak extends Module {

    private List<TargetBlock> blockList;
    private List<BlockPos> posList;
    private boolean isTicking;

    public AutoBedrockBreak() {
        super("AutoBedrockBreak", KEY_UNBOUND, Category.EXPLOITS, "Automatically does the bedrock break exploit.",
                new SettingToggle("Use TNT", true).withDesc("Use the original tnt method."));

        blockList = new ArrayList<>();
        posList = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        reset();

        super.onDisable();
    }

    @Subscribe
    public void onTick(EventTick event) {
        isTicking = true;
        for (TargetBlock tb : blockList)
        {
            tb.tick();
            if (tb.isDone())
            {
                posList.remove(tb.getBlockPos());
                blockList.remove(tb);
            }
        }
        isTicking = false;
    }

    @Subscribe
    public void onWorldRender(EventWorldRender.Post event) {
        if (mc.crosshairTarget instanceof BlockHitResult
                && !mc.world.isAir(((BlockHitResult) mc.crosshairTarget).getBlockPos())
                && mc.world.getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos()).getBlock()==Blocks.BEDROCK) {
            RenderUtils.drawBoxOutline(((BlockHitResult) mc.crosshairTarget).getBlockPos(), QuadColor.single(0xffc040c0), 2f);
        }
        for (BlockPos bp : posList) {
            RenderUtils.drawBoxOutline(bp, QuadColor.single(0x2020ffc0), 2f);
        }
    }

    @Subscribe
    public void onInteract(EventInteract.InteractBlock event) {
        if (!mc.world.isAir(event.getHitResult().getBlockPos()) && mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock()==Blocks.BEDROCK) {
            if (posList.isEmpty()) {
                blockList.add(new TargetBlock(event.getHitResult().getBlockPos(), mc, getSetting(0).asToggle().state));
                posList.add(event.getHitResult().getBlockPos());
                event.setCancelled(true);
            }
        }
    }

    private void reset() {
        blockList = new ArrayList<>();
        posList = new ArrayList<>();
    }
}

class TargetBlock {
    private BlockPos blockPos;
    private BlockPos redstoneTorchBlockPos;
    private BlockPos pistonBlockPos;
    private MinecraftClient mc;
    private ClientWorld world;
    private Status status;
    private int step;
    private BlockPos slimeBlockPos;
    private int tickTimes;
    private boolean hasTried;
    private boolean useTnt;
    private int stuckTicksCounter;

    public TargetBlock(BlockPos pos, MinecraftClient mc, boolean useTnt) {
        this.hasTried = false;
        this.useTnt = useTnt;
        this.stuckTicksCounter = 0;
        this.status = Status.UNINITIALIZED;
        this.step = 0;
        this.blockPos = pos;
        this.mc = mc;
        this.world = mc.world;
        this.pistonBlockPos = pos.up();
        this.redstoneTorchBlockPos = findNearbyFlatBlockToPlaceRedstoneTorch();
        if (redstoneTorchBlockPos == null) {
            this.slimeBlockPos = findPossibleSlimeBlockPos();
            if (slimeBlockPos != null) {
                dirtyPlace(slimeBlockPos, InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.SLIME_BLOCK), Direction.DOWN);
                redstoneTorchBlockPos = slimeBlockPos.up();
            } else {
                this.status = Status.FAILED;
            }
        }
    }

    public Status tick() {
        this.tickTimes++;
        updateStatus();
        System.out.println(this.status);
        if (useTnt) {
            switch (step) {
                case 0:
                    if (!world.isSpaceEmpty(new Box(blockPos.up(), blockPos.add(1, 7, 0)))) {
                        this.status = Status.FAILED;
                        BleachLogger.infoMessage("Not enough empty space to break this block!");
                    } else if (InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.PISTON) == -1) {
                        this.status = Status.FAILED;
                        BleachLogger.infoMessage("Missing pistons!");
                    } else if (InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.REDSTONE_BLOCK) == -1) {
                        this.status = Status.FAILED;
                        BleachLogger.infoMessage("Missing a redstone block!");
                    } else if (InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.TNT) == -1) {
                        this.status = Status.FAILED;
                        BleachLogger.infoMessage("Missing TNT!");
                    } else if (InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.LEVER) == -1) {
                        this.status = Status.FAILED;
                        BleachLogger.infoMessage("Missing a lever!");
                    } else if (dirtyPlace(blockPos.up(3), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.REDSTONE_BLOCK), Direction.DOWN)) {
                        step++;
                    }

                    break;
                case 1:
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), 90, mc.player.isOnGround()));
                    // mc.player.setPitch(90) "its jank either way"
                    step++;

                    break;
                case 2:
                    if (dirtyPlace(blockPos.up(), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.PISTON), Direction.DOWN))
                        step++;

                    break;
                case 3:
                    if (dirtyPlace(blockPos.up(7), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.TNT), Direction.DOWN))
                        step++;

                    break;
                case 4:
                    if (dirtyPlace(blockPos.up(6), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.LEVER), Direction.UP))
                        step++;

                    break;
                case 5:
                    if (dirtyPlace(blockPos.up(5), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.TNT), Direction.DOWN))
                        step++;

                    break;
                case 6:
                    Vec3d leverCenter = Vec3d.ofCenter(blockPos.up(6));
                    if (mc.player.getEyePos().distanceTo(leverCenter) <= 4.75) {
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(leverCenter, Direction.DOWN, blockPos.up(6), false));
                        step++;
                    }

                    break;
                default:
                    if (mc.world.getBlockState(blockPos).isAir()
                            || mc.world.getBlockState(blockPos).getBlock() instanceof PistonBlock
                            || (mc.world.getBlockState(blockPos.up()).getBlock() instanceof PistonBlock
                            && mc.world.getBlockState(blockPos.up()).get(PistonBlock.FACING) != Direction.UP)) {
                        break;
                    }

                    if (step >= 82) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), -90, mc.player.isOnGround()));
                        // mc.player.setPitch(-90) "its jank either way"
                    }

                    if (step > 84) {
                        Hand hand = InventoryUtils.selectSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.PISTON);
                        if (hand != null) {
                            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
                                    new BlockHitResult(Vec3d.ofBottomCenter(blockPos.up()), Direction.DOWN, blockPos.up(), false)));
                        }
                    }

                    step++;
                    break;
            }
        } else {
            switch (this.status) {
                case UNINITIALIZED:
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), 90, mc.player.isOnGround()));

                    dirtyPlace(pistonBlockPos, InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.PISTON), Direction.UP);
                    dirtyPlace(redstoneTorchBlockPos, InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.REDSTONE_TORCH), Direction.UP);
                    break;
                case UNEXTENDED_WITH_POWER_SOURCE:
                    break;
                case EXTENDED:
                    //打掉红石火把
                    ArrayList<BlockPos> nearByRedstoneTorchPosList = findNearbyRedstoneTorch();
                    for (BlockPos pos : nearByRedstoneTorchPosList) {
                        breakBlock(pos);
                    }
                    //打掉活塞
                    breakBlock(this.pistonBlockPos);
                    //放置朝下的活塞
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), -90, mc.player.isOnGround()));
                    dirtyPlace(pistonBlockPos, InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.PISTON), Direction.DOWN);
                    this.hasTried = true;
                    break;
                case RETRACTED:
                    breakBlock(pistonBlockPos);
                    breakBlock(pistonBlockPos.up());
                    if (this.slimeBlockPos != null) {
                        breakBlock( slimeBlockPos);
                    }
                    return Status.RETRACTED;
                case RETRACTING:
                    return Status.RETRACTING;
                case UNEXTENDED_WITHOUT_POWER_SOURCE:
                    dirtyPlace(slimeBlockPos, InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.REDSTONE_TORCH), Direction.DOWN);
                    break;
                case FAILED:
                    breakBlock(pistonBlockPos);
                    breakBlock(pistonBlockPos.up());
                    return Status.FAILED;
                case STUCK:
                    breakBlock( pistonBlockPos);
                    breakBlock( pistonBlockPos.up());
                    break;
                case NEEDS_WAITING:
                    break;
            }
        }
        return null;
    }

    enum Status {
        FAILED,
        UNINITIALIZED,
        UNEXTENDED_WITH_POWER_SOURCE,
        UNEXTENDED_WITHOUT_POWER_SOURCE,
        EXTENDED,
        NEEDS_WAITING,
        RETRACTING,
        RETRACTED,
        STUCK;
    }

    public boolean isDone()
    {
        return world.getBlockState(blockPos).getBlock() == Blocks.AIR;
    }

    private boolean dirtyPlace(BlockPos pos, int slot, Direction dir) {
        Vec3d hitPos = Vec3d.ofCenter(pos).add(dir.getOffsetX() * 0.5, dir.getOffsetY() * 0.5, dir.getOffsetZ() * 0.5);
        if (mc.player.getEyePos().distanceTo(hitPos) >= 4.75 || !mc.world.getOtherEntities(null, new Box(pos)).isEmpty()) {
            return false;
        }

        Hand hand = InventoryUtils.selectSlot(slot);
        if (hand != null) {
            mc.interactionManager.interactBlock(mc.player, mc.world, hand, new BlockHitResult(hitPos, dir, pos, false));
            return true;
        }

        return false;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ClientWorld getWorld() {
        return world;
    }

    public Status getStatus() {
        return status;
    }

    private void updateStatus() {
        if (this.tickTimes > 40) {
            this.status = Status.FAILED;
            return;
        }
        this.redstoneTorchBlockPos = findNearbyFlatBlockToPlaceRedstoneTorch();
        if (this.redstoneTorchBlockPos == null) {
            this.slimeBlockPos = findPossibleSlimeBlockPos();
            if (slimeBlockPos != null) {
                dirtyPlace(slimeBlockPos, InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.SLIME_BLOCK), Direction.DOWN);
                redstoneTorchBlockPos = slimeBlockPos.up();
            } else {
                this.status = Status.FAILED;
                BleachLogger.infoMessage("\"Missing redstone torch!\"");
            }
        } else if (!this.world.getBlockState(this.blockPos).isOf(Blocks.BEDROCK) && this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON)) {
            this.status = Status.RETRACTED;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED)) {
            this.status = Status.EXTENDED;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.MOVING_PISTON)) {
            this.status = Status.RETRACTING;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && findNearbyRedstoneTorch().size() != 0 && this.world.getBlockState(this.blockPos).isOf(Blocks.BEDROCK)) {
            this.status = Status.UNEXTENDED_WITH_POWER_SOURCE;
        } else if (this.hasTried && this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.stuckTicksCounter < 15) {
            this.status = Status.NEEDS_WAITING;
            this.stuckTicksCounter++;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.DOWN && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && findNearbyRedstoneTorch().size() != 0 && this.world.getBlockState(this.blockPos).isOf(Blocks.BEDROCK)) {
            this.status = Status.STUCK;
            this.hasTried = false;
            this.stuckTicksCounter = 0;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.UP && findNearbyRedstoneTorch().size() == 0 && this.world.getBlockState(this.blockPos).isOf(Blocks.BEDROCK)) {
            this.status = Status.UNEXTENDED_WITHOUT_POWER_SOURCE;
        } else if (has2BlocksOfPlaceToPlacePiston()) {
            this.status = Status.UNINITIALIZED;
        } else if (!has2BlocksOfPlaceToPlacePiston()) {
            this.status = Status.FAILED;
            BleachLogger.infoMessage("Piston could not be placed.");
        } else {
            this.status = Status.FAILED;
        }
    }

    public BlockPos findNearbyFlatBlockToPlaceRedstoneTorch() {
        if ((Block.sideCoversSmallSquare(world, blockPos.east(), Direction.UP) && (world.getBlockState(blockPos.east().up()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.east().up()).isOf(Blocks.REDSTONE_TORCH))) {
            return blockPos.east();
        } else if ((Block.sideCoversSmallSquare(world, blockPos.west(), Direction.UP) && (world.getBlockState(blockPos.west().up()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.west().up()).isOf(Blocks.REDSTONE_TORCH))) {
            return blockPos.west();
        } else if ((Block.sideCoversSmallSquare(world, blockPos.north(), Direction.UP) && (world.getBlockState(blockPos.north().up()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.north().up()).isOf(Blocks.REDSTONE_TORCH))) {
            return blockPos.north();
        } else if ((Block.sideCoversSmallSquare(world, blockPos.south(), Direction.UP) && (world.getBlockState(blockPos.south().up()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.south().up()).isOf(Blocks.REDSTONE_TORCH))) {
            return blockPos.south();
        }
        return null;
    }

    public BlockPos findPossibleSlimeBlockPos() {
        if (world.getBlockState(blockPos.east()).getMaterial().isReplaceable() && (world.getBlockState(blockPos.east().up()).getMaterial().isReplaceable())) {
            return blockPos.east();
        } else if (world.getBlockState(blockPos.west()).getMaterial().isReplaceable() && (world.getBlockState(blockPos.west().up()).getMaterial().isReplaceable())) {
            return blockPos.west();
        } else if (world.getBlockState(blockPos.south()).getMaterial().isReplaceable() && (world.getBlockState(blockPos.south().up()).getMaterial().isReplaceable())) {
            return blockPos.south();
        } else if (world.getBlockState(blockPos.north()).getMaterial().isReplaceable() && (world.getBlockState(blockPos.north().up()).getMaterial().isReplaceable())) {
            return blockPos.north();
        }
        return null;
    }

    public boolean has2BlocksOfPlaceToPlacePiston() {
        if (world.getBlockState(blockPos.up()).getHardness(world, blockPos.up()) == 0) {
            breakBlock(blockPos.up());
        }
        return world.getBlockState(blockPos.up()).getMaterial().isReplaceable() && world.getBlockState(blockPos.up().up()).getMaterial().isReplaceable();
    }

    public ArrayList<BlockPos> findNearbyRedstoneTorch() {
        ArrayList<BlockPos> list = new ArrayList<>();
        if (world.getBlockState(pistonBlockPos.east()).isOf(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.east());
        }
        if (world.getBlockState(pistonBlockPos.west()).isOf(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.west());
        }
        if (world.getBlockState(pistonBlockPos.south()).isOf(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.south());
        }
        if (world.getBlockState(pistonBlockPos.north()).isOf(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.north());
        }
        return list;
    }

    public void breakBlock(BlockPos pos) {
        InventoryUtils.selectSlot(InventoryUtils.getSlot(true, i -> (mc.player.getInventory().getStack(i).getItem() == Items.DIAMOND_PICKAXE || mc.player.getInventory().getStack(i).getItem() == Items.NETHERITE_PICKAXE)));
        mc.interactionManager.attackBlock(pos, Direction.UP);
    }
}

