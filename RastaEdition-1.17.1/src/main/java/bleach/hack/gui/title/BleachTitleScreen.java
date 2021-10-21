/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.title;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import bleach.hack.gui.window.widget.WindowButtonWidget;
import bleach.hack.util.VersionCompare;
import bleach.hack.util.file.CupGithubReader;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.util.BleachLogger;
import bleach.hack.BleachHack;
import bleach.hack.gui.title.particle.ParticleManager;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.module.mods.UI;
import bleach.hack.util.file.BleachFileHelper;
import bleach.hack.util.file.BleachFileMang;
import bleach.hack.util.file.BleachGithubReader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class BleachTitleScreen extends WindowScreen {

	private ParticleManager particleMang = new ParticleManager();
	public static boolean customTitleScreen = true;

	public static String splash = "";
	public static JsonObject version = null;

	private static String updaterText = "";

	public BleachTitleScreen() {
		super(new TranslatableText("narrator.screen.title"));
	}

	public void init() {
		super.init();

		clearWindows();
		addWindow(new Window(width / 8,
				height / 8,
				width / 8 + (width - width / 4),
				height / 8 + (height - height / 4), "BleachHack RastaEdition", new ItemStack(Items.MUSIC_DISC_CAT)));

		int w = getWindow(0).x2 - getWindow(0).x1;
		int h = getWindow(0).y2 - getWindow(0).y1;
		int maxY = MathHelper.clamp(h / 4 + 119, 0, h - 22);

		getWindow(0).addWidget(
				new WindowButtonWidget(w / 2 - 100, h / 4 + 38, w / 2 + 100, h / 4 + 58, I18n.translate("menu.singleplayer"), () -> {
					client.setScreen(new SelectWorldScreen(client.currentScreen));
				}));
		getWindow(0).addWidget(
				new WindowButtonWidget(w / 2 - 100, h / 4 + 62, w / 2 + 100, h / 4 + 82, I18n.translate("menu.multiplayer"), () -> {
					client.setScreen(new MultiplayerScreen(client.currentScreen));
				}));

		getWindow(0).addWidget(
				new WindowButtonWidget(w / 2 - 100, h / 4 + 86, w / 2 + 100, h / 4 + 106, I18n.translate("menu.online"), () -> {
					client.setScreen(new RealmsMainScreen(this));
				}));
		getWindow(0).addWidget(
				new WindowButtonWidget(w / 2 - 124, h / 4 + 86, w / 2 - 104, h / 4 + 106, "MC", () -> {
					customTitleScreen = !customTitleScreen;
					BleachFileHelper.saveMiscSetting("customTitleScreen", new JsonPrimitive(false));
					client.setScreen(new TitleScreen(false));
				}));
		getWindow(0).addWidget(
				new WindowButtonWidget(w / 2 - 100, maxY, w / 2 - 2, maxY + 20, I18n.translate("menu.options"), () -> {
					client.setScreen(new OptionsScreen(client.currentScreen, client.options));
				}));
		getWindow(0).addWidget(
				new WindowButtonWidget(w / 2 + 2, maxY, w / 2 + 100, maxY + 20, I18n.translate("menu.quit"), () -> {
					client.scheduleStop();
				}));


		addWindow(new Window(width / 2 - 100,
				height / 2 - 70,
				width / 2 + 100,
				height / 2 + 70, "Update", new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA), true) {

			protected void drawBar(MatrixStack matrix, int mouseX, int mouseY, TextRenderer textRend) {
				super.drawBar(matrix, mouseX, mouseY, textRend);

				Window.verticalGradient(matrix, x1 + 1, y1 + 12, x2 - 1, y2 - 1, 0xff606090, 0x00606090);
			}
		});

		getWindow(1).addWidget(
				new WindowButtonWidget(5, 115, 95, 135, "Update", () -> {
					try {
						if (!SystemUtils.IS_OS_WINDOWS) {
							updaterText = "Updater only supports Windows";
							return;
						}

						File modpath = new File(((ModContainer) FabricLoader.getInstance().getModContainer("bleachhack-cupedition").get()).getOriginUrl().toURI());

						if (!modpath.isFile()) {
							updaterText = "Invalid mod path";
							return;
						}

						if (version == null || !version.has("installer")) {
							updaterText = "Invalid metadata json";
							return;
						}

						String link = version.get("installer").getAsJsonObject().get("link").getAsString();
						String name = link.replaceFirst("^.*\\/", "");

						Path installerPath = BleachFileMang.stringsToPath("temp", name);

						BleachLogger.infoMessage(
								"\n> Installer path: " + installerPath
										+ "\n> Installer URL: " + link
										+ "\n> Installer file name: " + name
										+ "\n> Regular File: " + Files.isRegularFile(installerPath)
										+ "\n> File Length: " + installerPath.toFile().length());

						if (!Files.isRegularFile(installerPath) || installerPath.toFile().length() <= 1024L) {
							BleachFileMang.createEmptyFile("temp", name);
							FileUtils.copyURLToFile(new URL(link), installerPath.toFile());
						}

						Runtime.getRuntime().exec("\"" + installerPath + "\" \"" + modpath + "\" \"" + version.get("installer").getAsJsonObject().get("url").getAsString() + "\"");

						client.scheduleStop();
					} catch (Exception e) {
						updaterText = "Unknown error";
						e.printStackTrace();
					}
				}));

		getWindow(1).addWidget(
				new WindowButtonWidget(105, 115, 195, 135, "Github", () -> {
					Util.getOperatingSystem().open(URI.create("https://github.com/CUPZYY/BleachHack-CupEdition/releases"));
				}));

		if (version == null) {
			version = CupGithubReader.readJson("autoupdate", SharedConstants.getGameVersion().getName().replace(' ', '_') + ".json");
			if (!(version == null)) {
				if (VersionCompare.compareVersions(version.get("version").getAsString(), BleachHack.VERSION.replaceAll("-DEV", "")) == 1) {
					getWindow(1).closed = false;
					selectWindow(1);
				}

				if (splash.isEmpty()) {
					List<String> sp = BleachGithubReader.readFileLines("splashes.txt");
					splash = !sp.isEmpty() ? sp.get(new Random().nextInt(sp.size())) : "";
				}
			}
		}
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrix);

		int copyWidth = this.textRenderer.getWidth("Not copyright Mojang AB. Do distribute!") + 2;
		//System.out.println(versions);
		textRenderer.drawWithShadow(matrix, "Not copyright Mojang AB. Do distribute!", width - copyWidth, height - 10, -1);
		textRenderer.drawWithShadow(matrix, "BH RastaEdition: " + BleachHack.VERSION, 4, height - 40, -1);
		textRenderer.drawWithShadow(matrix, "Fabric: " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString(),
				4, height - 30, -1);
		textRenderer.drawWithShadow(matrix, "Minecraft: " + SharedConstants.getGameVersion().getName(), 4, height - 20, -1);
		textRenderer.drawWithShadow(matrix, "Logged in as: \u00a7a" + client.getSession().getUsername(), 4, height - 10, -1);

		super.render(matrix, mouseX, mouseY, delta);

		particleMang.addParticle(mouseX, mouseY);
		particleMang.renderParticles(matrix);

	}

	public void onRenderWindow(MatrixStack matrix, int window, int mouseX, int mouseY) {
		super.onRenderWindow(matrix, window, mouseX, mouseY);

		if (window == 0) {
			int x = getWindow(0).x1;
			int y = getWindow(0).y1 - 10;
			int w = width - width / 4;
			int h = height - height / 4;

			/* Main Text */
			matrix.push();
			matrix.scale(3f, 3f, 0f);

			// drawString(this.font, "BleachHack", (x + w/2 - 81)/3, (y + h/4 - 15)/3,
			// 0xffc0e0);

			int[] intarray = { 7, 13, 16, 22, 28, 34, 40, 46, 52, 58 };
			String[] bruh = { "B", "l", "e", "a", "c", "h", "H", "a", "c", "k" };
			for (int i = 0; i < bruh.length; i++) {
				drawStringWithShadow(matrix, this.textRenderer, bruh[i], (x + w / 2 - 81) / 3 + intarray[i] - 8, (y + h / 4 - 10) / 3, UI.getRainbowFromSettings(i * 270));
			}
			int[] intarrayRasta = {2, 8, 14, 20, 24, 32, 38, 44, 46, 50, 52, 58};
			String[] bruhRasta = {"R", "a", "s", "t", "a", "E", "d", "i", "t", "i", "o", "n"};
			for (int i = 0; i < bruhRasta.length; i++) {
				drawStringWithShadow(matrix, this.textRenderer, bruhRasta[i], (x + w / 2 - 72) / 3 + intarrayRasta[i] - 8, (y + h / 4 + 20) / 3, UI.getRainbowFromSettings(i * 270));
			}

			matrix.scale(1f / 3f, 1f / 3f, 0f);

			if (version != null && version.has("version") && VersionCompare.compareVersions(version.get("version").getAsString(), BleachHack.VERSION.replaceAll("-DEV", "")) == 1) {
				drawStringWithShadow(matrix, textRenderer, "\u00a76[ \u00a7nUpdate\u00a76 ]", getWindow(0).x1 + 3, getWindow(0).y2 - 12, -1);
			}
		} else if (window == 1) {
			int x = getWindow(1).x1;
			int y = getWindow(1).y1;

			drawCenteredText(matrix, this.textRenderer, "\u00a7cOutdated BH-RastaEdition version!", x + 100, y + 15, -1);
			drawCenteredText(matrix, this.textRenderer, "\u00a7eClick update to auto-update", x + 100, y + 28, -1);
			drawCenteredText(matrix, this.textRenderer, "\u00a7eOr Github to manually update", x + 100, y + 38, -1);
			drawCenteredText(matrix, this.textRenderer, "\u00a7c\u00a7o" + updaterText, x + 100, y + 58, -1);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (version != null && version.has("version") && VersionCompare.compareVersions(version.get("version").getAsString(), BleachHack.VERSION.replaceAll("-DEV", "")) == 1) {
			int x = getWindow(0).x1;
			int y = getWindow(0).y2;
			if (mouseX >= x + 1 && mouseX <= x + 70 && mouseY >= y - 12 && mouseY <= y) {
				getWindow(1).closed = false;
				selectWindow(1);
				//Util.getOperatingSystem().open(URI.create("https://github.com/BleachDrinker420/BleachHack/releases"));
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}
}
