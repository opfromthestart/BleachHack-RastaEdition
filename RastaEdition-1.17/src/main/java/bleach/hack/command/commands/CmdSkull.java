package bleach.hack.command.commands;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public class CmdSkull extends Command {

	public CmdSkull() {
		super("skull", "Gives you a player skull.", "skull <player> | skull img <image url>", CommandCategory.CREATIVE,
				"playerhead", "head");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		ItemStack item = new ItemStack(Items.PLAYER_HEAD, 64);

		Random random = new Random();
		String id = "[I;" + random.nextInt() + "," + random.nextInt() + "," + random.nextInt() + "," + random.nextInt() + "]";

		if (args.length < 2) {
			try {
				JsonObject json = new JsonParser().parse(
						Resources.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]), StandardCharsets.UTF_8))
						.getAsJsonObject();

				JsonObject json2 = new JsonParser().parse(
						Resources.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + json.get("id").getAsString()), StandardCharsets.UTF_8))
						.getAsJsonObject();

				item.setTag(StringNbtReader.parse("{SkullOwner:{Id:" + id + ",Properties:{textures:[{Value:\""
						+ json2.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString()
						+ "\"}]}}}"));
			} catch (Exception e) {
				e.printStackTrace();
				BleachLogger.errorMessage("Error getting head! (" + e.getClass().getSimpleName() + ")");
			}
		} else if (args[0].equalsIgnoreCase("img")) {
			NbtCompound tag = StringNbtReader.parse(
					"{SkullOwner:{Id:" + id + ",Properties:{textures:[{Value:\"" + encodeUrl(args[1]) + "\"}]}}}");
			item.setTag(tag);
			BleachLogger.logger.info(tag);
		}

		mc.player.getInventory().addPickBlock(item);
	}

	private String encodeUrl(String url) {
		return Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes());
	}

}