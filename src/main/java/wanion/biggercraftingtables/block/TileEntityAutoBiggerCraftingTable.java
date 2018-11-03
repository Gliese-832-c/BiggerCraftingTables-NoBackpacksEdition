package wanion.biggercraftingtables.block;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import wanion.lib.common.MetaItem;
import wanion.lib.recipe.advanced.AbstractRecipeRegistry;
import wanion.lib.recipe.advanced.IAdvancedRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityAutoBiggerCraftingTable<R extends IAdvancedRecipe> extends TileEntity implements ISidedInventory, ITickable
{
	private final int full = getSizeInventory() - 2;
	private final int half = full / 2;
	private final BiggerCraftingMatrix biggerCraftingMatrix = new BiggerCraftingMatrix((int) Math.sqrt(half));
	private final int[] slots;
	private NonNullList<ItemStack> itemStacks = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
	private R cachedRecipe = null;
	private TIntIntMap patternMap = null;
	private IItemHandler itemHandler = new ItemHandlerAutoBiggerCraftingTable(this);

	protected TileEntityAutoBiggerCraftingTable()
	{
		slots = new int[half + 1];
		for (int i = 0; i < half; i++)
			slots[i] = i;
		slots[half] = full;
	}

	@Override
	public final void update()
	{
		if (world == null || world.isRemote || world.isBlockPowered(pos))
			return;
		if (cachedRecipe == null) {
			if (patternMap != null)
				patternMap = null;
			return;
		}
		final ItemStack recipeStack = itemStacks.get(getSizeInventory() - 1);
		final ItemStack outputStack = itemStacks.get(getSizeInventory() - 2);
		if (recipeStack.isEmpty() || (!outputStack.isEmpty() && outputStack.getCount() == outputStack.getMaxStackSize()))
			return;
		if (patternMap == null)
			patternMap = MetaItem.getKeySizeMap(half, full, itemStacks);
		if (outputStack.isEmpty() && !matches(MetaItem.getSmartKeySizeMap(0, half, itemStacks), patternMap))
			return;
		else if (!outputStack.isEmpty() && outputStack.getCount() + recipeStack.getCount() > outputStack.getMaxStackSize() || !matches(MetaItem.getSmartKeySizeMap(0, half, itemStacks), patternMap))
			return;
		cleanInput();
		if (outputStack.isEmpty())
			itemStacks.set(getSizeInventory() - 2, recipeStack.copy());
		else
			outputStack.setCount(outputStack.getCount() + recipeStack.getCount());
		markDirty();
	}

	@Nonnull
	@Override
	public int[] getSlotsForFace(@Nonnull final EnumFacing side)
	{
		return slots;
	}

	@Override
	public boolean canInsertItem(final int index, @Nonnull final ItemStack itemStackIn, @Nonnull final EnumFacing direction)
	{
		return index < half;
	}

	@Override
	public boolean canExtractItem(final int index, @Nonnull final ItemStack stack, @Nonnull final EnumFacing direction)
	{
		return index == full || stack.getItem() == Items.BUCKET;
	}

	@Nonnull
	public abstract AbstractRecipeRegistry<R> getRecipeRegistry();

	private boolean matches(@Nonnull final TIntIntMap inputMap, @Nonnull final TIntIntMap patternMap)
	{
		if (inputMap.size() >= patternMap.size() && inputMap.keySet().containsAll(patternMap.keySet())) {
			for (final int key : patternMap.keys())
				if (inputMap.get(key) < patternMap.get(key))
					return false;
			return true;
		} else
			return false;
	}

	public final void recipeShapeChanged()
	{
		R matchedRecipe = getRecipeRegistry().findMatchingRecipe(biggerCraftingMatrix);
		itemStacks.set(getSizeInventory() - 1, (cachedRecipe = matchedRecipe) != null ? cachedRecipe.getOutput().copy() : ItemStack.EMPTY);
		patternMap = null;
	}

	private void cleanInput()
	{
		final TIntIntMap patternMap = new TIntIntHashMap(this.patternMap);
		for (int i = 0; i < half && !patternMap.isEmpty(); i++) {
			final ItemStack itemStack = itemStacks.get(i);
			final int key = MetaItem.get(itemStack);
			if (patternMap.containsKey(key)) {
				final int total = patternMap.get(key);
				final int dif = MathHelper.clamp(total, 1, itemStack.getCount());
				if (itemStack.getItem() == Items.WATER_BUCKET || itemStack.getItem() == Items.LAVA_BUCKET || itemStack.getItem() instanceof UniversalBucket)
					setInventorySlotContents(i, new ItemStack(Items.BUCKET));
				else if (!itemStack.getItem().hasContainerItem(itemStack))
					itemStack.setCount(itemStack.getCount() - dif);
				if (dif - total == 0)
					patternMap.remove(key);
				else
					patternMap.put(key, total - dif);
				if (itemStack.getCount() == 0)
					itemStacks.set(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public boolean isEmpty()
	{
		for (final ItemStack itemStack : itemStacks)
			if (!itemStack.isEmpty())
				return false;
		return true;
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(final int slot)
	{
		return itemStacks.get(slot);
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(final int slot, final int howMuch)
	{
		final ItemStack slotStack = itemStacks.get(slot);
		if (slotStack.isEmpty())
			return ItemStack.EMPTY;
		final ItemStack newStack = slotStack.copy();
		newStack.setCount(howMuch);
		slotStack.setCount(slotStack.getCount() - howMuch);
		if (slotStack.isEmpty())
			itemStacks.set(slot, ItemStack.EMPTY);
		return newStack;
	}

	@Override
	@Nonnull
	public ItemStack removeStackFromSlot(final int index)
	{
		final ItemStack itemStack = itemStacks.get(index);
		itemStacks.set(index, ItemStack.EMPTY);
		return itemStack;
	}

	@Override
	public void setInventorySlotContents(final int slot, @Nonnull final ItemStack itemStack)
	{
		itemStacks.set(slot, itemStack);
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull final EntityPlayer player)
	{
		return world.getTileEntity(getPos()) == this && player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(@Nonnull final EntityPlayer player) {}

	@Override
	public void closeInventory(@Nonnull final EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(final int slot, @Nonnull final ItemStack itemStack)
	{
		return true;
	}

	@Override
	public int getField(final int id)
	{
		return 0;
	}

	@Override
	public void setField(final int id, final int value) {}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear() {}

	@Override
	public final void readFromNBT(final NBTTagCompound nbtTagCompound)
	{
		super.readFromNBT(nbtTagCompound);
		readCustomNBT(nbtTagCompound);
		recipeShapeChanged();
	}

	@Override
	@Nonnull
	public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound nbtTagCompound)
	{
		super.writeToNBT(nbtTagCompound);
		writeCustomNBT(nbtTagCompound);
		return nbtTagCompound;
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (world != null) {
			final IBlockState blockState = getWorld().getBlockState(pos);
			blockState.getBlock().updateTick(getWorld(), getPos(), blockState, getWorld().rand);
			getWorld().notifyBlockUpdate(pos, blockState, blockState, 3);
		}
	}

	@Override
	public final SPacketUpdateTileEntity getUpdatePacket()
	{
		final NBTTagCompound nbttagcompound = new NBTTagCompound();
		writeToNBT(nbttagcompound);
		return new SPacketUpdateTileEntity(pos, 3, nbttagcompound);
	}

	@Override
	@Nonnull
	public NBTTagCompound getUpdateTag()
	{
		return writeToNBT(new NBTTagCompound());
	}

	@Nonnull
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation(getName());
	}

	@Override
	public final void onDataPacket(final NetworkManager networkManager, final SPacketUpdateTileEntity packet)
	{
		readFromNBT(packet.getNbtCompound());
	}

	@Override
	public boolean hasCapability(@Nonnull final Capability<?> capability, final EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) itemHandler : super.getCapability(capability, facing);
	}

	void readCustomNBT(final NBTTagCompound nbtTagCompound)
	{
		final NBTTagList nbtTagList = nbtTagCompound.getTagList("Contents", 10);
		final int max = getSizeInventory() - 1;
		for (int i = 0; i < max; i++)
			setInventorySlotContents(i, ItemStack.EMPTY);
		for (int i = 0; i < nbtTagList.tagCount(); i++) {
			final NBTTagCompound slotCompound = nbtTagList.getCompoundTagAt(i);
			final int slot = slotCompound.getShort("Slot");
			if (slot >= 0 && slot < getSizeInventory())
				setInventorySlotContents(slot, new ItemStack(slotCompound));
		}
	}

	NBTTagCompound writeCustomNBT(final NBTTagCompound nbtTagCompound)
	{
		final NBTTagList nbtTagList = new NBTTagList();
		final int max = getSizeInventory() - 1;
		for (int i = 0; i < max; i++) {
			final ItemStack itemStack = getStackInSlot(i);
			if (itemStack.isEmpty())
				continue;
			final NBTTagCompound slotCompound = new NBTTagCompound();
			slotCompound.setShort("Slot", (short) i);
			nbtTagList.appendTag(itemStack.writeToNBT(slotCompound));
		}
		nbtTagCompound.setTag("Contents", nbtTagList);
		return nbtTagCompound;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	private static class ItemHandlerAutoBiggerCraftingTable extends InvWrapper
	{
		private final TileEntityAutoBiggerCraftingTable tileEntityAutoBiggerCraftingTable;

		private ItemHandlerAutoBiggerCraftingTable(@Nonnull final TileEntityAutoBiggerCraftingTable tileEntityAutoBiggerCraftingTable)
		{
			super(tileEntityAutoBiggerCraftingTable);
			this.tileEntityAutoBiggerCraftingTable = tileEntityAutoBiggerCraftingTable;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate)
		{
			return slot >= tileEntityAutoBiggerCraftingTable.half ? stack : super.insertItem(slot, stack, simulate);
		}

		@Nonnull
		@Override
		public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
		{
			boolean full = slot == tileEntityAutoBiggerCraftingTable.full;
			final ItemStack slotStack = simulate && full ? getStackInSlot(slot).copy() : getStackInSlot(slot);
			if (full || slotStack.getItem() == Items.BUCKET) {
				if (slotStack.isEmpty())
					return ItemStack.EMPTY;
				final ItemStack newStack = slotStack.copy();
				newStack.setCount(amount);
				slotStack.setCount(slotStack.getCount() - amount);
				if (!simulate && slotStack.isEmpty())
					setStackInSlot(slot, ItemStack.EMPTY);
				return newStack;
			} else return ItemStack.EMPTY;
		}
	}

	private final class BiggerCraftingMatrix extends InventoryCrafting
	{
		final int square;

		private BiggerCraftingMatrix(final int squareRoot)
		{
			super(new Container()
			{
				@Override
				public boolean canInteractWith(@Nonnull final EntityPlayer entityPlayer)
				{
					return false;
				}
			}, squareRoot, squareRoot);
			this.square = squareRoot * squareRoot;
		}

		@Override
		@Nonnull
		public ItemStack getStackInSlot(final int slot)
		{
			return itemStacks.get(square + slot);
		}
	}
}