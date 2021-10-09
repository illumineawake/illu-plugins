package net.runelite.client.plugins.iworldwalker;

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
import java.time.Duration;
import java.time.Instant;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Slf4j
@Singleton
class iWorldWalkerOverlay extends OverlayPanel {
    private final Client client;
    private final iWorldWalkerPlugin plugin;
    private final iWorldWalkerConfig config;

    String timeFormat;
    String farmLocation;
    private String infoStatus = "Starting...";

    @Inject
    private iWorldWalkerOverlay(final Client client, final iWorldWalkerPlugin plugin, final iWorldWalkerConfig config) {
        super(plugin);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "World Walker overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.botTimer == null || !config.enableUI()) {
            return null;
        }
        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        Duration duration = Duration.between(plugin.botTimer, Instant.now());
        timeFormat = (duration.toHours() < 1) ? "mm:ss" : "HH:mm:ss";
        tableComponent.addRow("Time running:", formatDuration(duration.toMillis(), timeFormat));

        if (plugin.mapPoint != null) {
            tableComponent.addRow("Walking to Map Point:", +plugin.mapPoint.getX() + ", " +
                    plugin.mapPoint.getY() + ", " + plugin.mapPoint.getPlane());
        } else if (config.category().equals(Category.BANKS)) {
            tableComponent.addRow("Walking to:", config.catBanks().getName());
        } else if (config.category().equals(Category.BARCRAWL)) {
            tableComponent.addRow("Walking to:", config.catBarcrawl().getName());
        } else if (config.category().equals(Category.CITIES)) {
            tableComponent.addRow("Walking to:", config.catCities().getName());
        } else if (config.category().equals(Category.FARMING)) {
            tableComponent.addRow("Walking to:", getFarmName());
        } else if (config.category().equals(Category.GUILDS)) {
            tableComponent.addRow("Walking to:", config.catGuilds().getName());
        } else if (config.category().equals(Category.SKILLING)) {
            tableComponent.addRow("Walking to:", config.catSkilling().getName());
        } else if (config.category().equals(Category.SLAYER)) {
            tableComponent.addRow("Walking to:", config.catSlayer().getName());
        } else {
            tableComponent.addRow("Walking to:", config.catMisc().getName());
        }

        TableComponent tableDelayComponent = new TableComponent();
        tableDelayComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        tableDelayComponent.addRow("Sleep delay:", plugin.sleepLength + "ms");
        tableDelayComponent.addRow("Tick delay:", String.valueOf(plugin.timeout));

        if (!tableComponent.isEmpty()) {
            panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212")); //Material Dark default
            panelComponent.setPreferredSize(new Dimension(200, 200));
            panelComponent.setBorder(new Rectangle(5, 5, 5, 5));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Illumine World Walker")
                    .color(ColorUtil.fromHex("#40C4FF"))
                    .build());
            panelComponent.getChildren().add(tableComponent);
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Delays")
                    .color(ColorUtil.fromHex("#F8BBD0"))
                    .build());
            panelComponent.getChildren().add(tableDelayComponent);
        }
        return super.render(graphics);
    }

    private String getFarmName() {
        if (config.category().equals(Category.FARMING) && !config.catFarming().equals(Farming.NONE)) {
            switch (config.catFarming()) {
                case ALLOTMENTS:
                    return farmLocation = config.catFarmAllotments().getName();
                case BUSHES:
                    return farmLocation = config.catFarmBushes().getName();
                case FRUIT_TREES:
                    return farmLocation = config.catFarmFruitTrees().getName();
                case HERBS:
                    return farmLocation = config.catFarmHerbs().getName();
                case HOPS:
                    return farmLocation = config.catFarmHops().getName();
                case TREES:
                    return farmLocation = config.catFarmTrees().getName();
            }
        }
        return null;
    }
}
