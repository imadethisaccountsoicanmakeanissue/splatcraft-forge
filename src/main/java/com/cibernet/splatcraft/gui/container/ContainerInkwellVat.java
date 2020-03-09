package com.cibernet.splatcraft.gui.container;

import com.cibernet.splatcraft.items.ItemWeaponBase;
import com.cibernet.splatcraft.recipes.RecipesInkwellVat;
import com.cibernet.splatcraft.registries.SplatCraftBlocks;
import com.cibernet.splatcraft.registries.SplatCraftItems;
import com.cibernet.splatcraft.tileentities.TileEntityInkwellVat;
import com.cibernet.splatcraft.utils.InkColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ContainerInkwellVat extends Container
{
	public TileEntityInkwellVat te;

	public ContainerInkwellVat(InventoryPlayer player, TileEntityInkwellVat te)
	{
		super();
		this.te = te;

		addSlotToContainer(new SlotInput(new ItemStack(Items.DYE, 1, 0), te, 0, 26, 70));
		addSlotToContainer(new SlotInput(new ItemStack(SplatCraftItems.powerEgg), te, 1, 46, 70));
		addSlotToContainer(new SlotInput(new ItemStack(SplatCraftBlocks.emptyInkwell), te, 2, 92, 82));
		addSlotToContainer(new Slot(te, 3, 36, 89));
		addSlotToContainer(new SlotOutput(te, 4, 112, 82));

		for(int xx = 0; xx < 9; xx++)
			for(int yy = 0; yy < 3; yy++)
				addSlotToContainer(new Slot(player, xx+yy*9 + 9, 8 + xx*18, 126 + yy*18));

		for(int xx = 0; xx < 9; xx++)
			addSlotToContainer(new Slot(player, xx, 8 + xx*18, 184));
	}

	@Override
	public void updateProgressBar(int id, int data) {
		super.updateProgressBar(id, data);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn)
	{
		return playerIn.getDistanceSq((double)te.getPos().getX() + 0.5D, (double)te.getPos().getY() + 0.5D, (double)te.getPos().getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn)
	{
		super.onContainerClosed(playerIn);

		/*
		if (!te.getWorld().isRemote)
		{
			this.clearContainer(playerIn, te.getWorld(), te);
		}
		*/

	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s). TODO
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 5)
			{
				if (!this.mergeItemStack(itemstack1, 5, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(itemstack1, 0, 5, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	/**
	 * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in
	 * is null for the initial slot that was double-clicked.
	 */
	public boolean canMergeSlot(ItemStack stack, Slot slotIn)
	{
		return slotIn.slotNumber != 4 && super.canMergeSlot(stack, slotIn);
	}

	class SlotInput extends Slot
	{
		ItemStack validItem;
		public SlotInput(ItemStack validItem, IInventory inventoryIn, int index, int xPosition, int yPosition)
		{
			super(inventoryIn, index, xPosition, yPosition);
			this.validItem = validItem;
		}

		@Override
		public boolean isItemValid(ItemStack stack)
		{
			return stack.isItemEqual(validItem);
		}
	}
	class SlotOutput extends Slot
	{
		public SlotOutput(IInventory inventoryIn, int index, int xPosition, int yPosition)
		{
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack)
		{
			return false;
		}
	}
}
