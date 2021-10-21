/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import net.minecraft.block.Blocks;
import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {

	private boolean jumping;

	public Speed() {
		super("Speed", KEY_UNBOUND, Category.MOVEMENT, "Allows you to go faster, what did you expect?",
				new SettingMode("Mode", "StrafeHop", "Strafe", "OnGround", "MiniHop", "Bhop", "EcmeSpeed").withDesc("Speed mode"),
				new SettingSlider("Strafe", 0.15, 0.4, 0.27, 2).withDesc("Strafe speed"),
				new SettingSlider("OnGround", 0.1, 10, 2, 1).withDesc("OnGround speed"),
				new SettingSlider("MiniHop", 0.1, 10, 2, 1).withDesc("MiniHop speed"),
				new SettingSlider("Bhop", 0.1, 10, 2, 1).withDesc("Bhop speed"),
				new SettingSlider("EcmeSpeed", 0.15, 1, 0.27, 2).withDesc("Speed"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		//System.out.println(mc.player.forwardSpeed + " | " + mc.player.sidewaysSpeed);
		if (mc.options.keySneak.isPressed())
			return;

			/* Strafe */
		if (getSetting(0).asMode().mode <= 1) {
			if ((mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) /*&& mc.player.isOnGround()*/) {
				if (!mc.player.isSprinting()) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
				}

				mc.player.setVelocity(new Vec3d(0, mc.player.getVelocity().y, 0));
				mc.player.updateVelocity(getSetting(1).asSlider().getValueFloat(),
						new Vec3d(mc.player.sidewaysSpeed, 0, mc.player.forwardSpeed));
				
				double vel = Math.abs(mc.player.getVelocity().getX()) + Math.abs(mc.player.getVelocity().getZ());
				
				if (getSetting(0).asMode().mode == 0 && vel >= 0.12 && mc.player.isOnGround()) {
					mc.player.updateVelocity(vel >= 0.3 ? 0.0f : 0.15f, new Vec3d(mc.player.sidewaysSpeed, 0, mc.player.forwardSpeed));
					mc.player.jump();
				}
			}
			
			/* OnGround */
		} else if (getSetting(0).asMode().mode == 2) {
			if (mc.options.keyJump.isPressed() || mc.player.fallDistance > 0.25)
				return;
			
			double speeds = 0.85 + getSetting(2).asSlider().getValue() / 30;

			if (jumping && mc.player.getY() >= mc.player.prevY + 0.399994D) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.9, mc.player.getVelocity().z);
				mc.player.setPos(mc.player.getX(), mc.player.prevY, mc.player.getZ());
				jumping = false;
			}

			if (mc.player.forwardSpeed != 0.0F && !mc.player.horizontalCollision) {
				if (mc.player.verticalCollision) {
					mc.player.setVelocity(mc.player.getVelocity().x * speeds, mc.player.getVelocity().y, mc.player.getVelocity().z * speeds);
					jumping = true;
					mc.player.jump();
					// 1.0379
				}

				if (jumping && mc.player.getY() >= mc.player.prevY + 0.399994D) {
					mc.player.setVelocity(mc.player.getVelocity().x, -100, mc.player.getVelocity().z);
					jumping = false;
				}

			}

			/* MiniHop */
		} else if (getSetting(0).asMode().mode == 3) {
			if (mc.player.horizontalCollision || mc.options.keyJump.isPressed() || mc.player.forwardSpeed == 0)
				return;
			
			double speeds = 0.9 + getSetting(3).asSlider().getValue() / 30;
			
			if (mc.player.isOnGround()) {
				mc.player.jump();
			} else if (mc.player.getVelocity().y > 0) {
				mc.player.setVelocity(mc.player.getVelocity().x * speeds, -1, mc.player.getVelocity().z * speeds);
				mc.player.input.movementSideways += 1.5F;
			}

			/* Bhop */
		} else if (getSetting(0).asMode().mode == 4) {
			if (mc.player.forwardSpeed > 0 && mc.player.isOnGround()) {
				double speeds = 0.65 + getSetting(4).asSlider().getValue() / 30;
				
				mc.player.jump();
				mc.player.setVelocity(mc.player.getVelocity().x * speeds, 0.255556, mc.player.getVelocity().z * speeds);
				mc.player.sidewaysSpeed += 3.0F;
				mc.player.jump();
				mc.player.setSprinting(true);
			}
		}
		/* EcmeSpeed */
		else if (getSetting(0).asMode().mode == 5) {
			if ((mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0)) {
				if (!mc.player.isSprinting()) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
				}

				if((mc.world.getBlockState(mc.player.getBlockPos().up(2)).getBlock() != Blocks.AIR
						&& mc.world.getBlockState(mc.player.getBlockPos()).getBlock() != Blocks.LAVA
						&& mc.player.getHungerManager().getFoodLevel() > 7)){
					mc.player.setVelocity(new Vec3d(0, mc.player.getVelocity().y, 0));
					mc.player.updateVelocity(getSetting(5).asSlider().getValueFloat(),
							new Vec3d(mc.player.sidewaysSpeed, 0, mc.player.forwardSpeed));
				}
			}
		}
	}
}
