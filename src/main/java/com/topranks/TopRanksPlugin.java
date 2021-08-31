package com.topranks;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
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
	private static final String LOOKUP = "Ranks";

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
	protected void startUp() throws Exception {
		topRanksPanel = injector.getInstance(TopRanksPanel.class);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
		navButton = NavigationButton.builder().tooltip("Top Ranks").icon(icon).priority(5).panel(topRanksPanel).build();
		clientToolbar.addNavigation(navButton);
		if (client != null) {
			menuManager.get().addPlayerMenuItem(LOOKUP);
		}
	}

	@Override
	protected void shutDown() throws Exception {
		clientToolbar.removeNavigation(navButton);
		if (client != null) {
			menuManager.get().removePlayerMenuItem(LOOKUP);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if ((event.getMenuAction() == MenuAction.RUNELITE_PLAYER) && event.getMenuOption().equals(LOOKUP)) {
			// The player id is included in the event, so we can use that to get the player name,
			// which avoids having to parse out the combat level and any icons preceding the name.
			Player player = client.getCachedPlayers()[event.getId()];
			if (player == null) {
				return;
			}
			lookupPlayer(player.getName());
		}
	}

	private void lookupPlayer(String playerName) {
		SwingUtilities.invokeLater(() -> {
			if (!navButton.isSelected()) {
				navButton.getOnSelect().run();
			}
			topRanksPanel.lookup(playerName);
		});
	}
}
