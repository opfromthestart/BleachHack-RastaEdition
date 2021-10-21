/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.FabricReflect;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

public class AutoSign extends Module {

	public String[] text = new String[] {};

	public AutoSign() {
		super("AutoSign", KEY_UNBOUND, Category.PLAYER, "Automatically writes on signs",
				new SettingToggle("Random", false).withDesc("Writes random unicode in the sign").withChildren(
						new SettingSlider("Length", 1, 1000, 500, 0).withDesc("How many characters to write per line")));
	}

	public void onDisable() {
		text = new String[] {};
		super.onDisable();
	}

	@Subscribe
	public void sendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof UpdateSignC2SPacket && text.length < 3) {
			text = ((UpdateSignC2SPacket) event.getPacket()).getText();
		}
	}

	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		if (text.length < 3)
			return;

		if (event.getScreen() instanceof SignEditScreen) {
			event.setCancelled(true);

			if (getSetting(0).asToggle().state) {
				text = new String[] {};
				while (text.length < 4) {
					IntStream chars = new Random().ints(0, 0x10FFFF);
					int amount = getSetting(0).asToggle().getChild(0).asSlider().getValueInt();
					text = chars.limit(amount * 5)
							.mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining())
							.split("(?<=\\G.{" + amount + "})");
				}
			}

			SignEditScreen screen = (SignEditScreen) event.getScreen();
			SignBlockEntity sign = (SignBlockEntity) FabricReflect.getFieldValue(screen, "field_3031", "sign");

			mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), text[0], text[1], text[2], text[3]));
		}
	}
}
