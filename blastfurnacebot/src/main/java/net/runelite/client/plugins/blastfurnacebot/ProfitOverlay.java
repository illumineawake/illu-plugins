package net.runelite.client.plugins.blastfurnacebot;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import static net.runelite.api.MenuOpcode.RUNELITE_OVERLAY_CONFIG;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.botutils.BotUtils;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.ColorUtil;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Singleton
class ProfitOverlay extends OverlayPanel
{
	@Inject
	ItemManager itemManager;

	@Inject
	BotUtils utils;

	private static final float COST_PER_HOUR = 72000.0f;

	private final Client client;
	private final BlastFurnaceBotPlugin plugin;
	private final BlastFurnaceBotConfig config;

	private int previousAmount = 0;
	private int barsAmount = 0;
	private long barsPerHour = 0;
	String timeFormat;

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

	private long profitPerHour()
	{
		switch (plugin.bar.name())
		{
			case "IRON_BAR":
			case "SILVER_BAR":
			case "GOLD_BAR":
				return (barsPerHour * plugin.barPrice) - ((barsPerHour * plugin.orePrice) + (9 * plugin.staminaPotPrice) + 72000);
			case "STEEL_BAR":
				return (barsPerHour * plugin.barPrice) - ((barsPerHour * plugin.orePrice) + (barsPerHour * plugin.coalPrice) + (9 * plugin.staminaPotPrice) + 72000);
			case "MITHRIL_BAR":
				return (barsPerHour * plugin.barPrice) - ((barsPerHour * plugin.orePrice) + ((barsPerHour * 2) * plugin.coalPrice) + (9 * plugin.staminaPotPrice) + 72000);
			case "ADAMANTITE_BAR":
				return (barsPerHour * plugin.barPrice) - ((barsPerHour * plugin.orePrice) + ((barsPerHour * 3) * plugin.coalPrice) + (9 * plugin.staminaPotPrice) + 72000);
			case "RUNITE_BAR":
				return (barsPerHour * plugin.barPrice) - ((barsPerHour * plugin.orePrice) + ((barsPerHour * 4) * plugin.coalPrice) + (9 * plugin.staminaPotPrice) + 72000);
		}
		return 0;
	}

	private long getBarsPH()
	{
		int amount = client.getVar(Bars.RUNITE_BAR.getVarbit());
		if (amount != previousAmount)
		{
			barsAmount += amount;
			previousAmount = amount;
		}

		Duration duration = Duration.between(plugin.botTimer, Instant.now());
		return barsAmount * (3600000 / duration.toMillis());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		/*if (plugin.getConveyorBelt() == null)
		{
			return null;
		}*/

		barsPerHour = getBarsPH();

		panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212")); //Material Dark default
		panelComponent.setPreferredSize(new Dimension(75, 100));
		panelComponent.setBorder(new Rectangle(5, 5, 5, 5));

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Per Hour")
			.color(ColorUtil.fromHex("#40c4ff"))
			.build());
		if (barsAmount > 0)
		{
			panelComponent.getChildren().add(new ImageComponent(getImage(plugin.bar.getItemID(), (int) barsPerHour)));
			panelComponent.getChildren().add(new ImageComponent(getImage(ItemID.COINS_995, (int) profitPerHour())));
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
