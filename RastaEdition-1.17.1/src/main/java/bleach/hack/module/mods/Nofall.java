package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.util.FabricReflect;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Nofall extends Module {

	private boolean altBool = false;

	public Nofall() {
		super("Nofall", KEY_UNBOUND, Category.PLAYER, "Prevents you from taking fall damage.",
				new SettingMode("Mode", "Simple", "Packet", "ec.me").withDesc("What nofall mode to use"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.fallDistance > 2.5f && getSetting(0).asMode().mode == 0) {
			if (mc.player.isFallFlying())
				return;
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
		}

		if (mc.player.fallDistance > 2.5f && getSetting(0).asMode().mode == 1 &&
				mc.world.getBlockState(mc.player.getBlockPos().add(
						0, -1.5 + (mc.player.getVelocity().y * 0.1), 0)).getBlock() != Blocks.AIR) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
					mc.player.getX(), mc.player.getY() - 420.69, mc.player.getZ(), true));
			mc.player.fallDistance = 0;
		}
	}

	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof PlayerMoveC2SPacket && getSetting(0).asMode().mode == 2) {
			if (mc.player.fallDistance > 2.5f && !altBool) {
				mc.player.setOnGround(true);
				FabricReflect.writeField(event.getPacket(), true, "field_29179", "onGround");
				altBool = true;
			} else {
				altBool = false;
			}
		}
	}

}