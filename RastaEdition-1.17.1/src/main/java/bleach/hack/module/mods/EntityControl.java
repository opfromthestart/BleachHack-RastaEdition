/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.event.events.EventEntityControl;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.world.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityControl extends Module {

	public EntityControl() {
		super("EntityControl", KEY_UNBOUND, Category.MOVEMENT, "Manipulate Entities.",
				new SettingToggle("EntitySpeed", true).withDesc("Lets you control the speed of riding entities").withChildren(
						new SettingSlider("Speed", 0, 5, 1.2, 2).withDesc("Entity speed")),
				new SettingToggle("EntityFly", false).withDesc("Lets you fly with entities").withChildren(
						new SettingSlider("Ascend", 0, 2, 0.3, 2).withDesc("Ascend speed"),
						new SettingSlider("Descend", 0, 2, 0.5, 2).withDesc("Descend speed"),
						new SettingToggle("EcmeBypass", false).withDesc("Prevents you from getting kicked off when flying on ec.me")),
				new SettingToggle("HorseJump", true).withDesc("Always makes your horse do the highest jump it can"),
				new SettingToggle("GroundSnap", false).withDesc("Snaps the entity to the ground when going down blocks"),
				new SettingToggle("AntiStuck", false).withDesc("Tries to prevent rubberbanding when going up blocks"),
				new SettingToggle("NoAI", true).withDesc("Disables the entities AI"),
				new SettingToggle("RotationLock", false).withDesc("Locks the rotation of the vehicle to a certain angle serverside").withChildren(
						new SettingSlider("Yaw", -180, 180, 0, 0).withDesc("Yaw of the vehicle"),
						new SettingSlider("Pitch", -90, 90, 0, 0).withDesc("Pitch of the vehicle"),
						new SettingToggle("Player", true).withDesc("Also locks roation for player packets")));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.getVehicle() == null)
			return;

		Entity e = mc.player.getVehicle();
		double speed = getSetting(0).asToggle().getChild(0).asSlider().getValue();

		double forward = mc.player.forwardSpeed;
		double strafe = mc.player.sidewaysSpeed;
		float yaw = mc.player.getYaw();

		e.setYaw(yaw);
		if (e instanceof LlamaEntity) {
			((LlamaEntity) e).headYaw = mc.player.headYaw;
		}

		if (getSetting(5).asToggle().state && forward == 0 && strafe == 0) {
			e.setVelocity(new Vec3d(0, e.getVelocity().y, 0));
		}

		if (getSetting(0).asToggle().state) {
			if (forward != 0.0D) {
				if (strafe > 0.0D) {
					yaw += (forward > 0.0D ? -45 : 45);
				} else if (strafe < 0.0D) {
					yaw += (forward > 0.0D ? 45 : -45);
				}

				if (forward > 0.0D) {
					forward = 1.0D;
				} else if (forward < 0.0D) {
					forward = -1.0D;
				}

				strafe = 0.0D;
			}

			e.setVelocity(forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)),
					e.getVelocity().y,
					forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
		}

		if (getSetting(1).asToggle().state) {
			if (mc.options.keyJump.isPressed()) {
				e.setVelocity(e.getVelocity().x, getSetting(1).asToggle().getChild(0).asSlider().getValue(), e.getVelocity().z);
			} else {
				e.setVelocity(e.getVelocity().x, -getSetting(1).asToggle().getChild(1).asSlider().getValue(), e.getVelocity().z);
			}
		}

		if (getSetting(3).asToggle().state) {
			BlockPos p = new BlockPos(e.getPos());
			if (!mc.world.getBlockState(p.down()).getMaterial().isReplaceable() && e.fallDistance > 0.01) {
				e.setVelocity(e.getVelocity().x, -1, e.getVelocity().z);
			}
		}

		if (getSetting(4).asToggle().state) {
			Vec3d vel = e.getVelocity().multiply(2);
			if (WorldUtils.doesBoxCollide(e.getBoundingBox().offset(vel.x, 0, vel.z))) {
				for (int i = 2; i < 10; i++) {
					if (!WorldUtils.doesBoxCollide(e.getBoundingBox().offset(vel.x / i, 0, vel.z / i))) {
						e.setVelocity(vel.x / i / 2, vel.y, vel.z / i / 2);
						break;
					}
				}
			}
		}
	}

	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (getSetting(6).asToggle().state) {
			if (event.getPacket() instanceof VehicleMoveC2SPacket) {
				FabricReflect.writeField(event.getPacket(), getSetting(6).asToggle().getChild(0).asSlider().getValueFloat(), "field_12898", "yaw");
				FabricReflect.writeField(event.getPacket(), getSetting(6).asToggle().getChild(1).asSlider().getValueFloat(), "field_12896", "pitch");
			} else if (event.getPacket() instanceof PlayerMoveC2SPacket
					&& mc.player.hasVehicle()
					&& getSetting(6).asToggle().getChild(2).asToggle().state) {
				FabricReflect.writeField(event.getPacket(), getSetting(6).asToggle().getChild(0).asSlider().getValueFloat(), "field_12887", "yaw");
				FabricReflect.writeField(event.getPacket(), getSetting(6).asToggle().getChild(1).asSlider().getValueFloat(), "field_12885", "pitch");
			}
		}

		if (getSetting(1).asToggle().state && getSetting(1).asToggle().getChild(2).asToggle().state
				&& mc.player != null && mc.player.getVehicle() != null && event.getPacket() instanceof VehicleMoveC2SPacket) {
			mc.interactionManager.interactEntity(mc.player, mc.player.getVehicle(), Hand.MAIN_HAND);
		}
	}

	@Subscribe
	public void onReadPacket(EventReadPacket event) {
		if (getSetting(1).asToggle().state && getSetting(1).asToggle().getChild(2).asToggle().state
				&& mc.player != null && mc.player.hasVehicle()) {
			if (event.getPacket() instanceof PlayerPositionLookS2CPacket
					|| event.getPacket() instanceof EntityPassengersSetS2CPacket)
				event.setCancelled(true);
		}
	}

	@Subscribe
	public void onEntityControl(EventEntityControl event) {
		if (mc.player.getVehicle() instanceof ItemSteerable && mc.player.forwardSpeed == 0 && mc.player.sidewaysSpeed == 0) {
			return;
		}

		event.setControllable(true);
	}

	// HorseJump handled in MixinClientPlayerEntity.method_3151
}
