/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingLists;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Surround extends Module {

	private static final Set<Block> SURROUND_BLOCKS = Sets.newHashSet(
			Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.ENCHANTING_TABLE,
			Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL,
			Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK, Blocks.ANCIENT_DEBRIS);

	public Surround() {
		super("Surround", KEY_UNBOUND, Category.COMBAT, "Surrounds yourself with obsidian",
				new SettingMode("Mode", "1x1", "Fit").withDesc("Mode, 1x1 places 4 blocks around you, fit fits the blocks around you so it doesn't place inside of you"),
				new SettingMode("Support", "Place", "AirPlace", "Skip").withDesc("What to do when theres no blocks to place surround blocks on"),
				new SettingToggle("Autocenter", false).withDesc("Autocenters you to the nearest block"),
				new SettingToggle("Keep on", true).withDesc("Keeps the module on after placing the obsidian"),
				new SettingToggle("Jump disable", true).withDesc("Disables the module if you jump"),
				new SettingSlider("BPT", 1, 8, 2, 0).withDesc("Blocks per tick, how many blocks to place per tick"),
				new SettingRotate(false).withDesc("Rotates when placing"),
				SettingLists.newBlockList("Blocks", "Surround Blocks", SURROUND_BLOCKS::contains, Blocks.OBSIDIAN).withDesc("What blocks to surround with"));
	}

	public void onEnable() {
		super.onEnable();

		if (getSetting(2).asToggle().state) {
			Vec3d centerPos = Vec3d.of(mc.player.getBlockPos()).add(0.5, 0, 0.5);
			mc.player.updatePosition(centerPos.x, centerPos.y, centerPos.z);
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(centerPos.x, centerPos.y, centerPos.z, mc.player.isOnGround()));
		}

		place();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (getSetting(4).asToggle().state && mc.options.keyJump.isPressed()) {
			setEnabled(false);
			return;
		}

		place();
	}

	private void place() {
		int slot = InventoryUtils.getSlot(true,
				i -> getSetting(7).asList(Block.class).contains(Block.getBlockFromItem(mc.player.getInventory().getStack(i).getItem())));

		if (slot == -1) {
			BleachLogger.errorMessage("No blocks to surround with!");
			setEnabled(false);
			return;
		}

		int cap = 0;

		Box box = mc.player.getBoundingBox();
		Set<BlockPos> placePoses = getSetting(0).asMode().mode == 0
				? Sets.newHashSet(
				mc.player.getBlockPos().north(), mc.player.getBlockPos().east(),
				mc.player.getBlockPos().south(), mc.player.getBlockPos().west())
				: Sets.newHashSet(
				new BlockPos(box.minX - 1, box.minY, box.minZ), new BlockPos(box.minX, box.minY, box.minZ - 1),
				new BlockPos(box.maxX + 1, box.minY, box.minZ), new BlockPos(box.maxX, box.minY, box.minZ - 1),
				new BlockPos(box.minX - 1, box.minY, box.maxZ), new BlockPos(box.minX, box.minY, box.maxZ + 1),
				new BlockPos(box.maxX + 1, box.minY, box.maxZ), new BlockPos(box.maxX, box.minY, box.maxZ + 1));
		placePoses.removeIf(pos -> !mc.world.getBlockState(pos).getMaterial().isReplaceable());

		if (placePoses.isEmpty()) {
			return;
		}

		if (InventoryUtils.selectSlot(slot) == Hand.OFF_HAND) {
			ItemStack itemStack = mc.player.getStackInHand(Hand.OFF_HAND);
			mc.player.setStackInHand(Hand.OFF_HAND, mc.player.getStackInHand(Hand.MAIN_HAND));
			mc.player.setStackInHand(Hand.MAIN_HAND, itemStack);
			mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.UP));
		}

		int supportMode = getSetting(1).asMode().mode;
		for (BlockPos pos : placePoses) {
			if (cap >= getSetting(5).asSlider().getValueInt()) {
				return;
			}

			if (WorldUtils.placeBlock(pos, -1, getSetting(6).asRotate(), false, supportMode == 1, true)) {
				cap++;
			} else {
				if (supportMode == 2) {
					continue;
				}

				if (WorldUtils.placeBlock(pos.down(), -1, getSetting(6).asRotate(), false, supportMode == 1, true)) {
					cap++;

					if (cap >= getSetting(5).asSlider().getValueInt()) {
						return;
					}

					if (WorldUtils.placeBlock(pos, -1, getSetting(6).asRotate(), false, supportMode == 1, true)) {
						cap++;
					}
				}
			}
		}

		if (!getSetting(3).asToggle().state) {
			setEnabled(false);
		}
	}

}