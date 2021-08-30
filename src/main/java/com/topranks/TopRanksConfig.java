package com.topranks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hiscoressorted")
public interface TopRanksConfig extends Config
{
	@ConfigItem(
			position = 1,
			keyName = "playerOption",
			name = "Player option",
			description = "Add Ranks option to players"
	)
	default boolean playerOption()
	{
		return true;
	}
}
