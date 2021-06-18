package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventSwing;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

public class NoSwing extends Module {
    public NoSwing() {
        super("NoSwing", KEY_UNBOUND, Category.PLAYER, "Makes you not swing(for other people than you)");
    }

    @Subscribe
    public void onSwing(EventSwing event) {
        event.setCancelled(true);
    }

    @Subscribe
    public void onPacket(EventSendPacket event) {
        if (event.getPacket() instanceof HandSwingC2SPacket)
            event.setCancelled(true);
    }
}
