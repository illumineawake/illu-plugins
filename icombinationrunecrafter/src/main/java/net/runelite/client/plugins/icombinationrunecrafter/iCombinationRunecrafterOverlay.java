package net.runelite.client.plugins.icombinationrunecrafter;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Slf4j
@Singleton
class iCombinationRunecrafterOverlay extends OverlayPanel {
    private final Client client;
    private final iCombinationRunecrafterPlugin plugin;
    private final iCombinationRunecrafterConfig config;

    String timeFormat;
    private String infoStatus = "Starting...";

    @Inject
    private iCombinationRunecrafterOverlay(final Client client, final iCombinationRunecrafterPlugin plugin, final iCombinationRunecrafterConfig config) {
        super(plugin);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "iCombination Rune Crafter overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.botTimer == null) {
            log.debug("Overlay conditions not met, not starting overlay");
            return null;
        }
        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        Duration duration = Duration.between(plugin.botTimer, Instant.now());
        timeFormat = (duration.toHours() < 1) ? "mm:ss" : "HH:mm:ss";
        tableComponent.addRow("Time running:", formatDuration(duration.toMillis(), timeFormat));
        if (plugin.state != null) {
            if (!plugin.state.name().equals("TIMEOUT")) {
                infoStatus = plugin.state.name();
            }
        }
        tableComponent.addRow("Status:", infoStatus);

        TableComponent tableMarksComponent = new TableComponent();
        tableMarksComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
        tableMarksComponent.addRow("Runes:", NumberFormat.getNumberInstance(Locale.US).format(plugin.totalCraftedRunes)
                + " / " + NumberFormat.getNumberInstance(Locale.US).format(plugin.runesPH));
        tableMarksComponent.addRow("Profit (gp):", NumberFormat.getNumberInstance(Locale.US).format(plugin.totalProfit)
                + " / " + NumberFormat.getNumberInstance(Locale.US).format(plugin.profitPH));

        TableComponent tableDelayComponent = new TableComponent();
        tableDelayComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        tableDelayComponent.addRow("Sleep delay:", plugin.sleepLength + "ms");
        tableDelayComponent.addRow("Tick delay:", String.valueOf(plugin.timeout));

        if (!tableComponent.isEmpty()) {
            panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212")); //Material Dark default
            panelComponent.setPreferredSize(new Dimension(250, 250));
            panelComponent.setBorder(new Rectangle(5, 5, 5, 5));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Illumine Combination Runecrafting")
                    .color(ColorUtil.fromHex("#40C4FF"))
                    .build());
            panelComponent.getChildren().add(tableComponent);
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Stats (Total / PH)")
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
