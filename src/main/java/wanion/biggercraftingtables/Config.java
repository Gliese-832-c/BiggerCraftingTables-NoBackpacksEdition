package wanion.biggercraftingtables;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import joptsimple.internal.Strings;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Arrays;

import static java.io.File.separatorChar;

public final class Config
{
	public static final Config INSTANCE = new Config();

	public final boolean createTableRecipes;
	public final int powerMultiplier;
	public final int capacityMultiplier;

	public final boolean shouldKeepContents;
	public final int maxStackSize;

	private Config()
	{
		final Configuration config = new Configuration(new File("." + separatorChar + "config" + separatorChar + Reference.MOD_NAME.replace(" ", Strings.EMPTY) + ".cfg"), Reference.MOD_VERSION);

		createTableRecipes = config.getBoolean("createTableRecipes", Configuration.CATEGORY_GENERAL, false, "Should be created recipes for the Tables?");
		powerMultiplier = config.getInt("powerMultiplier", Configuration.CATEGORY_GENERAL, 10, 1, Short.MAX_VALUE, "Formula: powerConsumption = craftingSlotAmount * powerMultiplier");
		capacityMultiplier = config.getInt("capacityMultiplier", Configuration.CATEGORY_GENERAL, 100, 1, Short.MAX_VALUE, "Formula: capacity = powerConsumption * capacityMultiplier");

		shouldKeepContents = config.getBoolean("shouldKeepContents", Configuration.CATEGORY_GENERAL, false, "Should the tables keep their contents when Broken? Note: As of currently, items inside of tables get deleted when the table is broken. I was not bothered to spend time figuring out how to convert the NBT tag to an array of ItemStacks to spawn into the world as entities.");
		maxStackSize = config.getInt("maxStackSize", Configuration.CATEGORY_GENERAL, 1, 1, Integer.MAX_VALUE, "The maximum stack size of the slots in the tables.");

		config.setCategoryPropertyOrder(Configuration.CATEGORY_GENERAL, Arrays.asList("createTableRecipes", "powerMultiplier", "capacityMultiplier", "shouldKeepContents", "maxStackSize"));

		if (config.hasChanged())
			config.save();
	}
}