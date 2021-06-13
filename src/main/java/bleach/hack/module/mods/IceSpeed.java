package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.util.FabricReflect;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Arrays;

public class IceSpeed extends Module {
    public IceSpeed() {
        super("IceSpeed", KEY_UNBOUND, Category.MOVEMENT, "Move faster on ice",
        new SettingSlider("Speed", 0, 9, 4, 2).withDesc("The opacity of the fill"));
    }

    @Subscribe
    public void onTick(EventTick event) {
        setSlipperiness(((1) - (getSetting(0).asSlider().getValueFloat() / 10)));
    }

    @Override
    public void onDisable() {
        setSlipperiness(0.98f);
        for(Block block: Arrays.asList(Blocks.ICE, Blocks.PACKED_ICE, Blocks.FROSTED_ICE, Blocks.BLUE_ICE)) {
            FabricReflect.writeField(block, 0.989f, "slipperiness", "field_23163");
        }
        super.onDisable();
    }

    private void setSlipperiness(float speed) {
        for(Block block: Arrays.asList(Blocks.ICE, Blocks.PACKED_ICE, Blocks.FROSTED_ICE, Blocks.BLUE_ICE)){
            FabricReflect.writeField(block, speed, "slipperiness", "field_23163");
        }
    }
}
