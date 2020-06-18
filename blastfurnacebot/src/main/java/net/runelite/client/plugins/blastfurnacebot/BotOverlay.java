package net.runelite.client.plugins.blastfurnacebot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import static net.runelite.api.MenuOpcode.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.api.Varbits.BLAST_FURNACE_COFFER;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.ui.overlay.infobox.Timer;
import net.runelite.client.util.QuantityFormatter;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;
import org.w3c.dom.css.RGBColor;

@Singleton
class BotOverlay extends OverlayPanel
{
	@Inject
	ItemManager itemManager;
	private static final float COST_PER_HOUR = 72000.0f;

	private final Client client;
	private final BlastFurnaceBotPlugin plugin;
	private final BlastFurnaceBotConfig config;

	private int previousAmount = 0;
	private int barsAmount = 0;

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
		TableComponent headerComponent = new TableComponent();
		headerComponent.setColumnAlignments(TableAlignment.CENTER);

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		Duration duration = Duration.between(plugin.botTimer, Instant.now());
		int amount = client.getVar(BarsOres.RUNITE_BAR.getVarbit());
		if (amount != previousAmount)
		{
			barsAmount += amount;
			previousAmount = amount;
		}
		headerComponent.addRow("Illumine Blast Furnace");
		headerComponent.addRow("");
		tableComponent.addRow("Time running:", formatDuration(duration.toMillis(),"HH:mm:ss"));
		if (barsAmount > 0)
		{
			tableComponent.addRow("Bars made:", String.valueOf(barsAmount));
			tableComponent.addRow("Bars p/h:", String.valueOf(barsAmount * (3600000 / duration.toMillis())));
		}

			/*if (config.showCofferTime())
			{
				final long millis = (long) (coffer / COST_PER_HOUR * 60 * 60 * 1000);
				//tableComponent.addRow("Time:", formatDuration(millis, "H'h' m'm' s's'", true));
			}*/

		if (!tableComponent.isEmpty())
		{
			panelComponent.getChildren().add(headerComponent);
			panelComponent.getChildren().add(tableComponent);
			panelComponent.setPreferredSize(new Dimension(150,150));
			panelComponent.setBorder(new Rectangle(5,5,5,5));
			panelComponent.setBackgroundColor(ColorScheme.SCROLL_TRACK_COLOR);
		}

		return super.render(graphics);
	}

}
