/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.module.Module;
import bleach.hack.util.PlayerInteractEntityC2SUtils;
import bleach.hack.util.PlayerInteractEntityC2SUtils.InteractType;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * @author sl, Bleach
 */
public class Criticals extends Module {

	public Criticals() {
		super("Criticals", KEY_UNBOUND, Category.COMBAT, "Attempts to force Critical hits on entities you hit.",
				new SettingMode("Mode", "MiniJump", "FullJump").withDesc("Criticals mode, MiniJump does the smallest posible jump, FullJump simulates a full jump"));
	}

	@Subscribe
	public void sendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
			PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket) event.getPacket();
			if (PlayerInteractEntityC2SUtils.getInteractType(packet) == InteractType.ATTACK
					&& PlayerInteractEntityC2SUtils.getEntity(packet) instanceof LivingEntity) {
				sendCritPackets();
			}
		}
	}

	private void sendCritPackets() {
		if (mc.player.isClimbing() || mc.player.isTouchingWater()
				|| mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || mc.player.hasVehicle()) {
			return;
		}

		boolean sprinting = mc.player.isSprinting();
		if (sprinting) {
			mc.player.setSprinting(false);
			mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
		}

		if (mc.player.isOnGround()) {
			double x = mc.player.getX();
			double y = mc.player.getY();
			double z = mc.player.getZ();
			if (getSetting(0).asMode().mode == 0) {
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0633, z, false));
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
			} else {
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.42, z, false));
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.65, z, false));
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.72, z, false));
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.53, z, false));
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.32, z, false));
			}
		}

		if (sprinting) {
			mc.player.setSprinting(true);
			mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
		}
	}
}
