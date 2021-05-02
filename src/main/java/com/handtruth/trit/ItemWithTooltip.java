package com.handtruth.trit;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.BaseText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class ItemWithTooltip extends Item {

    private String tooltip;
    private int color;

    public ItemWithTooltip(Settings settings, String tooltip) {
        this(settings, tooltip, 0xa0a0a0);
    }

    public ItemWithTooltip(Settings settings, String tooltip, int color) {
        super(settings);
        this.tooltip = tooltip;
        this.color = color;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        BaseText text = new TranslatableText(this.tooltip);
        tooltip.add(text.setStyle(Style.EMPTY.withItalic(true).withColor(TextColor.fromRgb(color))));
    }
        
}