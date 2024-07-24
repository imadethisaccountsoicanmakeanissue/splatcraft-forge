package net.splatcraft.forge.registries;

import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.splatcraft.forge.items.ColoredBlockItem;
import net.splatcraft.forge.util.ColorUtils;
import net.splatcraft.forge.util.InkColor;

import static net.splatcraft.forge.registries.SplatcraftItems.inkwell;
import static net.splatcraft.forge.registries.SplatcraftItems.sardiniumBlock;
import static net.splatcraft.forge.registries.SplatcraftItems.splattershot;

public class SplatcraftItemGroups
{
    public static final CreativeModeTab GROUP_GENERAL = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.splatcraft_general"))
            .icon(() -> new ItemStack(sardiniumBlock.get()))
            .build();

    public static final CreativeModeTab GROUP_WEAPONS = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.splatcraft_weapons"))
            .icon(() -> ColorUtils.setInkColor(new ItemStack(splattershot.get()), ColorUtils.ORANGE))
            .build();

    public static final ArrayList<Item> colorTabItems = new ArrayList<>();

    public static final CreativeModeTab GROUP_COLORS = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.splatcraft_colors"))
            .withSearchBar()
            .icon(() -> ColorUtils.setInkColor(new ItemStack(inkwell.get()), ColorUtils.ORANGE))
            .displayItems((pParameters, pOutput) -> {
                for(Item item : colorTabItems)
                {
                    for(InkColor color : SplatcraftInkColors.REGISTRY.get().getValues().stream().sorted().toList())
                        pOutput.accept(ColorUtils.setColorLocked(ColorUtils.setInkColor(new ItemStack(item), color.getColor()), true));
                    if(!(item instanceof ColoredBlockItem coloredBlockItem) || coloredBlockItem.matchesColor())
                        pOutput.accept(ColorUtils.setInverted(new ItemStack(item), true));
                }
            })
            .build();
}