package net.runelite.client.plugins.blastfurnacebot;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import static net.runelite.api.MenuOpcode.RUNELITE_OVERLAY_CONFIG;
import net.runelite.client.game.ItemManager;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;

@Singleton
class ProfitOverlay extends OverlayPanel
{
	@Inject
	ItemManager itemManager;

	private final Client client;
	private final BlastFurnaceBotPlugin plugin;
	private final BlastFurnaceBotConfig config;

	@Inject
	private ProfitOverlay(final Client client, final BlastFurnaceBotPlugin plugin, final BlastFurnaceBotConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.BOTTOM_LEFT);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Profit overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getConveyorBelt() == null)
		{
			return null;
		}

		panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212")); //Material Dark default
		panelComponent.setPreferredSize(new Dimension(75, 100));
		panelComponent.setBorder(new Rectangle(5, 5, 5, 5));

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Per Hour")
			.color(ColorUtil.fromHex("#40c4ff"))
			.build());
		if (plugin.barsAmount > 0)
		{
			panelComponent.getChildren().add(new ImageComponent(getImage(plugin.bar.getItemID(), (int) plugin.barsPerHour)));
			panelComponent.getChildren().add(new ImageComponent(getImage(ItemID.COINS_995, (int) plugin.profit)));
		}
		else
		{
			panelComponent.getChildren().add(new ImageComponent(getImage(plugin.bar.getItemID(), 0)));
			panelComponent.getChildren().add(new ImageComponent(getImage(ItemID.COINS_995, 0)));
		}
		return super.render(graphics);
	}

	private BufferedImage getImage(int itemID, int amount)
	{
		return itemManager.getImage(itemID, amount, true);
	}
}
