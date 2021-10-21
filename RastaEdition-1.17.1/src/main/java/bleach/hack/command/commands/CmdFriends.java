/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import java.util.Locale;

import bleach.hack.util.file.BleachFileHelper;
import org.apache.commons.lang3.StringUtils;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

public class CmdFriends extends Command {

	public CmdFriends() {
		super("friends", "Manage friends.", "friends add <user> | friends remove <user> | friends list | friends clear", CommandCategory.MISC,
				"friend");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0 || args.length > 2) {
			throw new CmdSyntaxException();
		}

		if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 2) {
				throw new CmdSyntaxException("No username selected");
			}

			BleachHack.friendMang.add(args[1]);
			BleachLogger.infoMessage("Added \"" + args[1] + "\" to the friend list");
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length < 2) {
				throw new CmdSyntaxException("No username selected");
			}

			BleachHack.friendMang.remove(args[1].toLowerCase(Locale.ENGLISH));
			BleachLogger.infoMessage("Removed \"" + args[1] + "\" from the friend list");
		} else if (args[0].equalsIgnoreCase("list")) {
			if (BleachHack.friendMang.getFriends().isEmpty()) {
				BleachLogger.infoMessage("You don't have any friends :(");
			} else {
				int len = BleachHack.friendMang.getFriends().stream()
						.sorted((f1, f2) -> f2.length() - f1.length())
						.findFirst()
						.get().length() + 3;

				MutableText text = new LiteralText("Friends:");

				for (String f : BleachHack.friendMang.getFriends()) {
					String spaces = StringUtils.repeat(' ', len - f.length());

					text
					.append("\n\u00a7b> " + f + spaces)
					.append(new LiteralText("\u00a7c[Del]")
							.styled(style -> style
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Remove " + f + " from your friendlist")))
									.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PREFIX + "friends remove " + f))))
					.append("   ")
					.append(new LiteralText("\u00a73[NameMC]")
							.styled(style -> style
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Open NameMC page of " + f)))
									.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://namemc.com/profile/" + f))));
				}

				BleachLogger.infoMessage(text);
			}
		} else if (args[0].equalsIgnoreCase("clear")) {
			BleachHack.friendMang.getFriends().clear();

			BleachLogger.infoMessage("Cleared Friend list");
		} else {
			throw new CmdSyntaxException();
		}

		BleachFileHelper.SCHEDULE_SAVE_FRIENDS = true;
	}

}
