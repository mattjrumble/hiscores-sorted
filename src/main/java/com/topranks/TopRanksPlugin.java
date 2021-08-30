package com.topranks;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Top Ranks"
)
public class TopRanksPlugin extends Plugin
{
	@Inject
	@Nullable
	private Client client;

	@Inject
	private Provider<MenuManager> menuManager;

	@Inject
	private ClientToolbar clientToolbar;

	private NavigationButton navButton;

	private TopRanksPanel topRanksPanel;

	@Override
	protected void startUp() throws Exception
	{
		topRanksPanel = injector.getInstance(TopRanksPanel.class);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
		navButton = NavigationButton.builder().tooltip("Top Ranks").icon(icon).priority(5).panel(topRanksPanel).build();
		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}
}
