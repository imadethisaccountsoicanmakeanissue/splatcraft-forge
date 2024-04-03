package net.splatcraft.forge.items;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.splatcraft.forge.registries.SplatcraftItemGroups;
import net.splatcraft.forge.registries.SplatcraftSounds;
import net.splatcraft.forge.util.BlockInkedResult;
import net.splatcraft.forge.util.ColorUtils;
import net.splatcraft.forge.util.InkBlockUtils;

public class InkWaxerItem extends Item
{
    public InkWaxerItem()
    {
        super(new Properties().durability(256).tab(SplatcraftItemGroups.GROUP_GENERAL));
    }

    public void onBlockStartBreak(ItemStack itemstack, BlockPos pos, Level level)
    {
        if(InkBlockUtils.isInked(level, pos))
        {
            ColorUtils.addInkDestroyParticle(level, pos, InkBlockUtils.getInk(level, pos).color());

            SoundType soundType = SplatcraftSounds.SOUND_TYPE_INK;
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), soundType.getBreakSound(), SoundSource.PLAYERS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

            InkBlockUtils.clearInk(level, pos, true);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (InkBlockUtils.permanentInk(context.getLevel(), context.getClickedPos()) == BlockInkedResult.SUCCESS)
        {
            context.getLevel().levelEvent(context.getPlayer(), 3003, context.getClickedPos(), 0);
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level levelIn, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.getItem().equals(Items.HONEYCOMB) || super.isValidRepairItem(toRepair, repair);
    }
}
