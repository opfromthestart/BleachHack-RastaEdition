/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.module.Category;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventInteract;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.operation.Operation;
import bleach.hack.util.operation.OperationList;
import bleach.hack.util.operation.blueprint.OperationBlueprint;
import bleach.hack.util.operation.blueprint.PlaceDirOperationBlueprint;
import bleach.hack.util.operation.blueprint.PlaceOperationBlueprint;
import bleach.hack.util.operation.blueprint.RemoveOperationBlueprint;
import bleach.hack.util.render.RenderUtils;
import bleach.hack.util.render.color.QuadColor;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class AutoBuild extends Module {

	private static List<List<OperationBlueprint>> BLUEPRINTS = Arrays.asList(
			Arrays.asList( // Wither
					new PlaceOperationBlueprint(0, 0, 0, Items.SOUL_SAND),
					new PlaceOperationBlueprint(0, 1, 0, Items.SOUL_SAND),
					new PlaceOperationBlueprint(0, 1, -1, Items.SOUL_SAND),
					new PlaceOperationBlueprint(0, 1, 1, Items.SOUL_SAND),
					new PlaceOperationBlueprint(0, 2, -1, Items.WITHER_SKELETON_SKULL),
					new PlaceOperationBlueprint(0, 2, 0, Items.WITHER_SKELETON_SKULL),
					new PlaceOperationBlueprint(0, 2, 1, Items.WITHER_SKELETON_SKULL)),
			Arrays.asList( // WitherH
					new PlaceOperationBlueprint(0, 0, 0, Items.SOUL_SAND),
					new PlaceOperationBlueprint(1, 0, 0, Items.SOUL_SAND),
					new PlaceOperationBlueprint(1, 0, -1, Items.SOUL_SAND),
					new PlaceOperationBlueprint(1, 0, 1, Items.SOUL_SAND),
					new PlaceOperationBlueprint(2, 0, -1, Items.WITHER_SKELETON_SKULL),
					new PlaceOperationBlueprint(2, 0, 0, Items.WITHER_SKELETON_SKULL),
					new PlaceOperationBlueprint(2, 0, 1, Items.WITHER_SKELETON_SKULL)),
			Arrays.asList( // Iron Golem
					new PlaceOperationBlueprint(0, 0, 0, Items.IRON_BLOCK),
					new PlaceOperationBlueprint(0, 1, 0, Items.IRON_BLOCK),
					new PlaceOperationBlueprint(0, 1, -1, Items.IRON_BLOCK),
					new PlaceOperationBlueprint(0, 1, 1, Items.IRON_BLOCK),
					new PlaceOperationBlueprint(0, 2, 0, Items.CARVED_PUMPKIN)),
			Arrays.asList( // Snow Golem
					new PlaceOperationBlueprint(0, 0, 0, Items.SNOW_BLOCK),
					new PlaceOperationBlueprint(0, 1, 0, Items.SNOW_BLOCK),
					new PlaceOperationBlueprint(0, 2, 0, Items.CARVED_PUMPKIN)),
			Arrays.asList( // Nomad Hut
					new PlaceOperationBlueprint(-2, 0, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 0, -2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(0, 0, -2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 0, -2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 0, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 0, 0, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 0, 1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 0, 2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(0, 0, 2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 0, 2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-2, 0, 1, Items.OBSIDIAN),

					new PlaceOperationBlueprint(-2, 1, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 1, -2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 1, -2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 1, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 1, 1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 1, 2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 1, 2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-2, 1, 1, Items.OBSIDIAN),

					new PlaceOperationBlueprint(-2, 2, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 2, -2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(0, 2, -2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 2, -2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 2, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 2, 0, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 2, 1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 2, 2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(0, 2, 2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 2, 2, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-2, 2, 1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-2, 2, 0, Items.OBSIDIAN),

					new PlaceOperationBlueprint(-2, 3, 0, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 3, 0, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 3, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(-1, 3, 1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(0, 3, 0, Items.OBSIDIAN),
					new PlaceOperationBlueprint(0, 3, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(0, 3, 1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 3, 0, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 3, -1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(1, 3, 1, Items.OBSIDIAN),
					new PlaceOperationBlueprint(2, 3, 0, Items.OBSIDIAN)),
			Arrays.asList( // Bomber Mid
					new PlaceOperationBlueprint(0, 0, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(0, -1, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(1, -1, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(1, 0, 0, Items.DETECTOR_RAIL),
					new PlaceOperationBlueprint(1, -1, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(2, -1, 1, Items.SLIME_BLOCK),
					new RemoveOperationBlueprint(1, -1, 1),
					new PlaceOperationBlueprint(2, -2, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(3, -2, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(3, -2, 0, Items.TUBE_CORAL_FAN),
					new PlaceOperationBlueprint(2, -2, 0, Items.TNT),
					new PlaceOperationBlueprint(3, -1, 0, Items.SLIME_BLOCK),
					new PlaceDirOperationBlueprint(3, 0, 0, Items.OBSERVER, Direction.WEST),
					new PlaceDirOperationBlueprint(4, 0, 0, Items.PISTON, Direction.WEST)),
			Arrays.asList( // Bomber End
					new PlaceOperationBlueprint(0, 0, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(0, -1, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(1, -1, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(1, 0, 0, Items.DETECTOR_RAIL),
					new PlaceOperationBlueprint(1, -1, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(2, -1, 1, Items.SLIME_BLOCK),
					new RemoveOperationBlueprint(1, -1, 1),
					new PlaceOperationBlueprint(2, -2, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(3, -2, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(3, -2, 0, Items.TUBE_CORAL_FAN),
					new PlaceOperationBlueprint(2, -2, 0, Items.TNT),
					new PlaceOperationBlueprint(3, -1, 0, Items.SANDSTONE_WALL)));

	private OperationList current = null;
	private BlockHitResult ray = null;
	private boolean active = false;

	public AutoBuild() {
		super("AutoBuild", KEY_UNBOUND, Category.WORLD, "Auto builds stuff",
				new SettingMode("Build", "Wither", "WitherH", "IronGolem", "SnowGolem", "NomadHut", "Bomber-Mid", "Bomber-End").withDesc("What to build"),
				new SettingToggle("Repeat", false).withDesc("Lets you build multiple things without having to re-enable the module"));
	}

	@Override
	public void onDisable() {
		current = null;
		ray = null;
		active = false;

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (!active) {
			ray = (BlockHitResult) mc.player.raycast(40, mc.getTickDelta(), false);
			Direction dir = ray.getSide();

			if (dir.getAxis() == Axis.Y) {
				dir = Math.abs(ray.getBlockPos().getX() - mc.player.getBlockPos().getX()) > Math.abs(ray.getBlockPos().getZ() - mc.player.getBlockPos().getZ())
						? ray.getBlockPos().getX() - mc.player.getBlockPos().getX() > 0 ? Direction.EAST : Direction.WEST
						: ray.getBlockPos().getZ() - mc.player.getBlockPos().getZ() > 0 ? Direction.SOUTH : Direction.NORTH;
			}

			current = OperationList.create(BLUEPRINTS.get(getSetting(0).asMode().mode), ray.getBlockPos().offset(ray.getSide()), dir);

			if (mc.mouse.wasLeftButtonClicked() || mc.mouse.wasRightButtonClicked()) {
				active = true;
			}
		} else {
			if (current.executeNext() && current.isDone()) {
				setEnabled(false);

				if (getSetting(1).asToggle().state) {
					setEnabled(true);
				}
			}
		}
	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		if (current != null) {
			//RenderUtils.drawOutlineBox(current.getBox(), 1f, 1f, 0f, 0.5f);

			for (Operation o: current.getRemainingOps()) {
				o.render();
			}

			RenderUtils.drawBoxOutline(new Box(current.getNext().pos).contract(0.01), QuadColor.single(1f, 1f, 0f, 0.5f), 3f);
		}

		if (ray != null && !active) {
			BlockPos pos = ray.getBlockPos();

			RenderUtils.drawBoxFill(pos, QuadColor.single(1f, 1f, 0f, 0.3f), ArrayUtils.remove(Direction.values(), ray.getSide().ordinal()));
		}
	}

	@Subscribe
	public void onInteract(EventInteract.InteractBlock event) {
		if (ray != null && !active) {
			event.setCancelled(true);
		}
	}
}