/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;


import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.FabricReflect;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class AutoReconnect extends Module {

	public ServerInfo server;

	public AutoReconnect() {
		super("AutoReconnect", KEY_UNBOUND, Category.MISC, "Shows reconnect options when disconnecting from a server",
				new SettingToggle("Auto", true).withDesc("Automatically reconnects").withChildren(
						new SettingSlider("Time", 0.2, 10, 5, 2).withDesc("How long to wait before reconnecting")));
	}

	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		if (event.getScreen() instanceof DisconnectedScreen
				&& !(event.getScreen() instanceof NewDisconnectScreen)) {
			mc.openScreen(new NewDisconnectScreen((DisconnectedScreen) event.getScreen()));
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void readPacket(EventReadPacket event) {
		if (event.getPacket() instanceof DisconnectS2CPacket) {
			try {
				server = mc.getCurrentServerEntry();
			} catch (Exception e) {
			}
		}
	}

	@Subscribe
	public void sendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof HandshakeC2SPacket) {
			server = new ServerInfo("Server",
					(String) FabricReflect.getFieldValue(event.getPacket(), "field_13159", "address") + ":"
							+ (int) FabricReflect.getFieldValue(event.getPacket(), "field_13157", "port"),
							false);
		}
	}

	public class NewDisconnectScreen extends DisconnectedScreen {

		public long reconnectTime = Long.MAX_VALUE - 1000000L;
		public int reasonH = 0;

		private ButtonWidget reconnectButton;

		public NewDisconnectScreen(DisconnectedScreen screen) {
			super((Screen) FabricReflect.getFieldValue(screen, "field_2456", "parent"), new LiteralText("Disconnect"),
					(Text) FabricReflect.getFieldValue(screen, "field_2457", "reason"));
			reasonH = (int) FabricReflect.getFieldValue(screen, "field_2454", "reasonHeight");
		}

		public void init() {
			super.init();
			reconnectTime = System.currentTimeMillis();
			addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + reasonH / 2 + 35, 200, 20, new LiteralText("Reconnect"), button -> {
				if (server != null)
					ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, ServerAddress.parse(server.address), server);
			}));
			reconnectButton = addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + reasonH / 2 + 57, 200, 20, LiteralText.EMPTY,
					button -> {
						getSetting(0).asToggle().state = !getSetting(0).asToggle().state;
						reconnectTime = System.currentTimeMillis();
					}));
		}

		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			super.render(matrices, mouseX, mouseY, delta);

			reconnectButton.setMessage(new LiteralText(getSetting(0).asToggle().state ? "\u00a7aAutoReconnect ["
					+ (reconnectTime + getSetting(0).asToggle().getChild(0).asSlider().getValue() * 1000 - System.currentTimeMillis())
					+ "]" : "\u00a7cAutoReconnect [" + getSetting(0).asToggle().getChild(0).asSlider().getValue() * 1000 + "]"));

			if (reconnectTime + getSetting(0).asToggle().getChild(0).asSlider().getValue() * 1000 < System.currentTimeMillis() && getSetting(0).asToggle().state) {
				if (server != null)
					ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, ServerAddress.parse(server.address), server);
				reconnectTime = System.currentTimeMillis();
			}
		}

	}

}
