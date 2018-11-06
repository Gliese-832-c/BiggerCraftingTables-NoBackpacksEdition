package wanion.biggercraftingtables.compat.jei.giant;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.entity.player.EntityPlayer;
import wanion.biggercraftingtables.BiggerCraftingTables;
import wanion.biggercraftingtables.block.giant.ContainerAutoGiantCraftingTable;
import wanion.biggercraftingtables.compat.jei.BiggerRecipeWrapper;
import wanion.biggercraftingtables.network.BiggerAutoCraftingJeiTransfer;
import wanion.lib.common.Util;
import wanion.lib.recipe.advanced.IAdvancedRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AutoGiantRecipeTransferHandler implements IRecipeTransferHandler<ContainerAutoGiantCraftingTable>
{
	public AutoGiantRecipeTransferHandler() {}

	@Nonnull
	@Override
	public Class<ContainerAutoGiantCraftingTable> getContainerClass()
	{
		return ContainerAutoGiantCraftingTable.class;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(@Nonnull final ContainerAutoGiantCraftingTable container, @Nonnull final IRecipeLayout recipeLayout, @Nonnull final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer)
	{
		if (!doTransfer)
			return null;
		final IRecipeWrapper recipeWrapper = Util.getField(RecipeLayout.class, "recipeWrapper", recipeLayout, IRecipeWrapper.class);
		if (!(recipeWrapper instanceof BiggerRecipeWrapper))
			return null;
		final IAdvancedRecipe advancedRecipe = ((BiggerRecipeWrapper) recipeWrapper).advancedRecipe;
		BiggerCraftingTables.networkWrapper.sendToServer(new BiggerAutoCraftingJeiTransfer(advancedRecipe.getRecipeKey(), advancedRecipe.getOutput()));
		return null;
	}
}