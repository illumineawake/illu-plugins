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
import net.runelite.api.Client;
import static net.runelite.api.MenuOpcode.RUNELITE_OVERLAY_CONFIG;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.botutils.BotUtils;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.ColorUtil;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Singleton
class BotOverlay extends OverlayPanel
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
	public long barsPerHour = 0;
	String timeFormat;

	@Inject
	private BotOverlay(final Client client, final BlastFurnaceBotPlugin plugin, final BlastFurnaceBotConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.BOTTOM_LEFT);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Bot overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		/*if (plugin.getConveyorBelt() == null)
		{
			return null;
		}*/
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		Duration duration = Duration.between(plugin.botTimer, Instant.now());
		int amount = client.getVar(Bars.RUNITE_BAR.getVarbit());
		if (amount != previousAmount)
		{
			barsAmount += amount;
			previousAmount = amount;
		}
		timeFormat = (duration.toHours() < 1) ? "mm:ss" : "HH:mm:ss";
		tableComponent.addRow("Time running:", formatDuration(duration.toMillis(),timeFormat));
		if (barsAmount > 0)
		{
			barsPerHour = barsAmount * (3600000 / duration.toMillis());
			tableComponent.addRow("Bars made:", String.valueOf(barsAmount));
		}
		else
		{
			tableComponent.addRow("Bars made:", "0");
		}
		tableComponent.addRow();
		if (!tableComponent.isEmpty())
		{
			panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212")); //Material Dark default
			panelComponent.setPreferredSize(new Dimension(150,150));
			panelComponent.setBorder(new Rectangle(5,5,5,5));
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Illumine Blast Furnace")
				.color(ColorUtil.fromHex("#40c4ff"))
				.build());
			panelComponent.getChildren().add(tableComponent);
		}
		return super.render(graphics);
	}
	private BufferedImage getImage(int itemID, int amount)
	{
		return itemManager.getImage(itemID, amount, true);
	}
}
