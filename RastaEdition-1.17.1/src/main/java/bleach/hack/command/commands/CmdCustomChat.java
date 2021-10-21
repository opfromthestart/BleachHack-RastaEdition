/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import bleach.hack.util.file.BleachFileHelper;
import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonPrimitive;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.CustomChat;
import bleach.hack.util.BleachLogger;

public class CmdCustomChat extends Command {

	public CmdCustomChat() {
		super("customchat", "Changes customchat prefix and suffix.", "customchat current | customchat reset | customchat prefix <prefix> | customchat suffix <suffix>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		CustomChat chat = (CustomChat) ModuleManager.getModule("CustomChat");

		if (args[0].equalsIgnoreCase("current")) {
			BleachLogger.infoMessage("Current prefix: \"" + chat.prefix + "\", suffix: \"" + chat.suffix + "\"");
		} else if (args[0].equalsIgnoreCase("reset")) {
			chat.prefix = "";
			chat.suffix = " \u25ba \u0432\u2113\u0454\u03b1c\u043d\u043d\u03b1c\u043a";

			BleachFileHelper.saveMiscSetting("customChatPrefix", new JsonPrimitive(chat.prefix));
			BleachFileHelper.saveMiscSetting("customChatSuffix", new JsonPrimitive(chat.suffix));
			BleachLogger.infoMessage("Reset the chat prefix and suffix");
		} else if (args[0].equalsIgnoreCase("prefix")) {
			chat.prefix = String.join(" ", ArrayUtils.subarray(args, 1, args.length)).trim() + " ";

			BleachFileHelper.saveMiscSetting("customChatPrefix", new JsonPrimitive(chat.prefix));
			BleachLogger.infoMessage("Set prefix to: \"" + chat.prefix + "\"");
		} else if (args[0].equalsIgnoreCase("suffix")) {
			chat.suffix = " " + String.join(" ", ArrayUtils.subarray(args, 1, args.length)).trim();

			BleachFileHelper.saveMiscSetting("customChatSuffix", new JsonPrimitive(chat.suffix));
			BleachLogger.infoMessage("Set suffix to: \"" + chat.suffix + "\"");
		} else {
			throw new CmdSyntaxException();
		}
	}

}