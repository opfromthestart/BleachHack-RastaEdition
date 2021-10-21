/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingLists;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.render.RenderUtils;
import bleach.hack.util.render.color.QuadColor;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Module;

public class LiquidFiller extends Module {

    public LiquidFiller() {
        super("LiquidFiller", KEY_UNBOUND, Category.WORLD, "Places blocks in liquids",
                new SettingMode("Liquid", "Lava", "Water", "Both").withDesc("What liquids to fill"),
                new SettingSlider("Range", 1, 6, 4.5, 1).withDesc("How far to fill liquids"),
                new SettingSlider("BPT", 1, 6, 1, 0).withDesc("How many blocks to place per tick"),
                new SettingToggle("AirPlace", true).withDesc("Places blocks in the air"),
                new SettingToggle("LegitPlace", false).withDesc("Only places on sides of blocks you can see"),
                new SettingToggle("Filter", false).withDesc("Filters certain blocks from being placed").withChildren(
                        new SettingMode("Mode", "Blacklist", "Whitelist").withDesc("How to handle the list"),
                        SettingLists.newItemList("Edit Blocks", "Edit Filtered Blocks", i -> i instanceof BlockItem).withDesc("Edit the filtered blocks")),
                new SettingRotate(false).withDesc("Rotates when placing blocks"),
                new SettingToggle("Highlight", true).withDesc("Highlights liquids to fill").withChildren(
                        new SettingSlider("Opacity", 0.01, 1, 0.3, 2),
                        new SettingColor("Water Color", 0f, 0.5f, 1f, false).withDesc("Color of water"),
                        new SettingColor("Lava Color", 1f, 0.75f, 0f, false).withDesc("Color of lava")));
    }

    private boolean shouldUseItem(Item item) {
        if (!(item instanceof BlockItem)) {
            return false;
        }

        if (getSetting(5).asToggle().state) {
            boolean contains = getSetting(5).asToggle().getChild(1).asList(Item.class).contains(item);

            return (getSetting(5).asToggle().getChild(0).asMode().mode == 0 && !contains)
                    || (getSetting(5).asToggle().getChild(0).asMode().mode == 1 && contains);
        }

        return true;
    }

    @Subscribe
    public void onTick(EventTick event) {
        int cap = 0;
        int ceilRange = (int) Math.ceil(getSetting(1).asSlider().getValue());
        for (BlockPos pos: BlockPos.iterateOutwards(mc.player.getBlockPos().up(), ceilRange, ceilRange, ceilRange)) {
            FluidState fluid = mc.world.getFluidState(pos);

            if (((fluid.getFluid() instanceof WaterFluid.Still && getSetting(0).asMode().mode != 0)
                    || (fluid.getFluid() instanceof LavaFluid.Still && getSetting(0).asMode().mode != 1))
                    && InventoryUtils.selectSlot(false, i -> shouldUseItem(mc.player.getInventory().getStack(i).getItem())) != null) {
                if (WorldUtils.placeBlock(
                        pos, -1,
                        getSetting(6).asRotate(),
                        getSetting(4).asToggle().state,
                        getSetting(3).asToggle().state,
                        true)) {
                    cap++;

                    if (cap >= getSetting(2).asSlider().getValueInt()) {
                        return;
                    }
                }
            }
        }
    }

    @Subscribe
    public void onWorldRender(EventWorldRender.Post event) {
        if (getSetting(7).asToggle().state) {
            int opacity = (int) (getSetting(7).asToggle().getChild(0).asSlider().getValue() * 255);
            QuadColor waterColor = QuadColor.single((opacity << 24) | getSetting(7).asToggle().getChild(1).asColor().getRGB());
            QuadColor lavaColor = QuadColor.single((opacity << 24) | getSetting(7).asToggle().getChild(2).asColor().getRGB());

            int ceilRange = (int) Math.ceil(getSetting(1).asSlider().getValue());
            for (BlockPos pos: BlockPos.iterateOutwards(mc.player.getBlockPos().up(), ceilRange, ceilRange, ceilRange)) {
                FluidState fluid = mc.world.getFluidState(pos);

                if (fluid.getFluid() instanceof WaterFluid.Still && getSetting(0).asMode().mode != 0) {
                    RenderUtils.drawBoxBoth(fluid.getShape(mc.world, pos).getBoundingBox().offset(pos), waterColor, 3f);
                } else if (fluid.getFluid() instanceof LavaFluid.Still && getSetting(0).asMode().mode != 1) {
                    RenderUtils.drawBoxBoth(fluid.getShape(mc.world, pos).getBoundingBox().offset(pos), lavaColor, 3f);
                }
            }
        }
    }
}