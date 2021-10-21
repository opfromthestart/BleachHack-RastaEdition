/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.setting.base;

import org.lwjgl.glfw.GLFW;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class SettingBind extends SettingBase {

	private Module mod;

	public SettingBind(Module mod) {
		this.mod = mod;
	}

	public String getName() {
		return "Bind";
	}

	@Override
	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrices, x + 1, y, x + len, y + 12, 0x70303070);
		}
		
		if (window.keyDown >= 0 && window.keyDown != GLFW.GLFW_KEY_ESCAPE && window.mouseOver(x, y, x + len, y + 12)) {
			mod.setKey(window.keyDown == GLFW.GLFW_KEY_DELETE ? Module.KEY_UNBOUND : window.keyDown);
			MinecraftClient.getInstance().getSoundManager().play(
					PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
		}

		String name = mod.getKey() < 0 ? "NONE" : InputUtil.fromKeyCode(mod.getKey(), -1).getLocalizedText().getString();
		if (name == null)
			name = "KEY" + mod.getKey();
		else if (name.isEmpty())
			name = "NONE";

		MinecraftClient.getInstance().textRenderer.drawWithShadow(
				matrices, "Bind: " + name + (window.mouseOver(x, y, x + len, y + 12) ? "..." : ""), x + 3, y + 2, 0xcfe0cf);
	}

	public SettingBind withDesc(String desc) {
		description = desc;
		return this;
	}

	@Override
	public int getHeight(int len) {
		return 12;
	}

	@Override
	public void readSettings(JsonElement settings) {

	}

	@Override
	public JsonElement saveSettings() {
		return new JsonPrimitive(mod.getKey());
	}

	@Override
	public boolean isDefault() {
		return mod.getKey() == mod.getDefaultKey() || mod.getDefaultKey() >= 0;
	}

}
