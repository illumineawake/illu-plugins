package net.runelite.client.plugins.rooftopagility;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import static net.runelite.api.MenuOpcode.RUNELITE_OVERLAY_CONFIG;
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
class RooftopAgilityOverlay extends OverlayPanel
{
	private final Client client;
	private final RooftopAgilityPlugin plugin;
	private final RooftopAgilityConfig config;

	String timeFormat;
	private String infoStatus = "Starting...";

	@Inject
	private RooftopAgilityOverlay(final Client client, final RooftopAgilityPlugin plugin, final RooftopAgilityConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.BOTTOM_LEFT);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Rooftop Agility overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.botTimer == null || !plugin.startAgility || !config.enableUI())
		{
			return null;
		}
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		Duration duration = Duration.between(plugin.botTimer, Instant.now());
		timeFormat = (duration.toHours() < 1) ? "mm:ss" : "HH:mm:ss";
		tableComponent.addRow("Time running:", formatDuration(duration.toMillis(),timeFormat));
		if (plugin.state != null)
		{
			if (!plugin.state.name().equals("TIMEOUT"))
				infoStatus = plugin.state.name();
		}
		tableComponent.addRow("Status:", infoStatus);

		TableComponent tableMarksComponent = new TableComponent();
		tableMarksComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		tableMarksComponent.addRow("Collected:", String.valueOf(plugin.mogCollectCount));
		tableMarksComponent.addRow("Per hour:", String.valueOf(plugin.marksPerHour));

		TableComponent tableDelayComponent = new TableComponent();
		tableDelayComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		tableDelayComponent.addRow("Sleep delay:", plugin.sleepLength + "ms");
		tableDelayComponent.addRow("Tick delay:", String.valueOf(plugin.timeout));

		if (!tableComponent.isEmpty())
		{
			panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212")); //Material Dark default
			panelComponent.setPreferredSize(new Dimension(200,200));
			panelComponent.setBorder(new Rectangle(5,5,5,5));
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Illumine Rooftop Agility")
				.color(ColorUtil.fromHex("#40C4FF"))
				.build());
			panelComponent.getChildren().add(tableComponent);
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Marks of grace")
				.color(ColorUtil.fromHex("#FFA000"))
				.build());
			panelComponent.getChildren().add(tableMarksComponent);
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Delays")
				.color(ColorUtil.fromHex("#F8BBD0"))
				.build());
			panelComponent.getChildren().add(tableDelayComponent);
		}
		return super.render(graphics);
	}
}
