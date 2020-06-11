/*
 * Copyright (c) 2018, Seth <Sethtroll3@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.blastfurnacebot;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import static net.runelite.api.MenuOpcode.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.api.Varbits.BLAST_FURNACE_COFFER;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.QuantityFormatter;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Singleton
class BlastFurnaceCofferOverlay extends OverlayPanel
{
	private static final float COST_PER_HOUR = 72000.0f;

	private final Client client;
	private final BlastFurnaceBotPlugin plugin;
	private final BlastFurnaceBotConfig config;

	@Inject
	private BlastFurnaceCofferOverlay(final Client client, final BlastFurnaceBotPlugin plugin, final BlastFurnaceBotConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Coffer overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getConveyorBelt() == null)
		{
			return null;
		}

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		Widget sack = client.getWidget(WidgetInfo.BLAST_FURNACE_COFFER);

		if (sack != null)
		{
			final int coffer = client.getVar(BLAST_FURNACE_COFFER);

			sack.setHidden(true);

			tableComponent.addRow("Coffer:", QuantityFormatter.quantityToStackSize(coffer) + " gp");

			if (config.showCofferTime())
			{
				final long millis = (long) (coffer / COST_PER_HOUR * 60 * 60 * 1000);

				tableComponent.addRow("Time:", formatDuration(millis, "H'h' m'm' s's'", true));
			}
		}

		if (!tableComponent.isEmpty())
		{
			panelComponent.getChildren().add(tableComponent);
		}

		return super.render(graphics);
	}
}