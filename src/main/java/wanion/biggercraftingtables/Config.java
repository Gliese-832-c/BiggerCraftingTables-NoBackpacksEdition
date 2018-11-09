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

	private Config()
	{
		final Configuration config = new Configuration(new File("." + separatorChar + "config" + separatorChar + Reference.MOD_NAME.replace(" ", Strings.EMPTY) + ".cfg"), Reference.MOD_VERSION);

		createTableRecipes = config.getBoolean("createTableRecipes", Configuration.CATEGORY_GENERAL, true, "should be created recipes for the Tables?");
		powerMultiplier = config.getInt("powerMultiplier", Configuration.CATEGORY_GENERAL, 10, 1, Short.MAX_VALUE, "Formula: powerConsumption = craftingSlotAmount * powerMultiplier");
		capacityMultiplier = config.getInt("capacityMultiplier", Configuration.CATEGORY_GENERAL, 100, 1, Short.MAX_VALUE, "Formula: capacity = powerConsumption * capacityMultiplier");
		config.setCategoryPropertyOrder(Configuration.CATEGORY_GENERAL, Arrays.asList("createTableRecipes", "powerMultiplier", "capacityMultiplier"));

		if (config.hasChanged())
			config.save();
	}
}