/*
 * Copyright (c) 2018, John Pettenger
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.essencehighlighter;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.SkillIconManager;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.client.ui.overlay.components.ImageComponent;


import java.awt.*;

import static net.runelite.api.ItemID.*;


@Slf4j
public class DenseEssenceHighlighter extends Overlay
{

	private static final int Z_OFFSET = 200;


	// green color /

	private static final Color CLICKBOX_BORDER_COLOR = new Color(0, 0, 0, 30);
	private static final Color CLICKBOX_FILL_COLOR = new Color(0, 255, 0, 80);
	private static final Color CLICKBOX_BORDER_HOVER_COLOR = new Color(0, 0, 0, 100);


	// red color //

	private static final Color CLICKBOX_BORDER_COLORd = new Color(0, 0, 0, 77);
	private static final Color CLICKBOX_FILL_COLORd = new Color(255, 0, 0, 50);
	private static final Color CLICKBOX_BORDER_HOVER_COLORd = new Color(0, 0, 0, 100);


	private final Client client;
	private final DenseEssencePlugin plugin;
	private final EssenceConfig config;
	private final SkillIconManager skillIconManager;


	@Inject
	private DenseEssenceHighlighter(
		Client client, DenseEssencePlugin plugin, EssenceConfig config, SkillIconManager skillIconManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.skillIconManager = skillIconManager;


		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}


	private boolean hasdenseess()
	{
		ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
		if (container == null)
		{
			return false;
		}
		for (Item item : container.getItems())
		{
			if (item.getId() == DENSE_ESSENCE_BLOCK)
			{
				return true;
			}
		}
		return false;
	}


	private boolean hasdarkess()
	{
		ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
		if (container == null)
		{
			return false;
		}
		for (Item item : container.getItems())
		{
			if (item.getId() == DARK_ESSENCE_BLOCK)
			{
				return true;
			}
		}
		return false;
	}


	private boolean hasfragments()
	{
		ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
		if (container == null)
		{
			return false;
		}
		for (Item item : container.getItems())
		{
			if (item.getId() == DARK_ESSENCE_FRAGMENTS)
			{
				return true;
			}
		}
		return false;
	}


	@Override
	public Dimension render(Graphics2D graphics)
	{


		boolean northStoneMineable = plugin.isDenseRunestoneNorthMineable();
		boolean southStoneMineable = plugin.isDenseRunestoneSouthMineable();


		GameObject northStone = plugin.getDenseRunestoneNorth();
		GameObject southStone = plugin.getDenseRunestoneSouth();
		GameObject BLOOD_ALTAr = plugin.getBLOOD_ALTAr();
		GameObject DARK_ALTAR = plugin.getDark_ALTAR();


		if (BLOOD_ALTAr != null)
		{


			if (config.highlightDenseBloodALTAR())
			{

				if (!hasfragments() & !hasdarkess())
				{

					renderbloodaltardone(graphics, BLOOD_ALTAr);
				}

				if (hasdarkess() & !hasfragments())
				{
					renderbloodaltar(graphics, BLOOD_ALTAr);
				}

				if (hasfragments() & !hasdarkess())
				{


					renderbloodaltar(graphics, BLOOD_ALTAr);
				}

				if (hasfragments() && hasdarkess())
				{

					renderbloodaltar(graphics, BLOOD_ALTAr);

				}


			}


		}
		if (DARK_ALTAR != null)
		{

			if (hasdenseess())
			{
				renderbloodaltar(graphics, DARK_ALTAR);
			}

			if (hasdarkess() && hasfragments())
			{

				renderbloodaltardone(graphics, DARK_ALTAR);
				// System.out.println("none");
			}

			if (hasdarkess() & !hasfragments())
			{
				renderbloodaltar(graphics, DARK_ALTAR);
			}
			if (hasfragments() & !hasdarkess())
			{
				renderbloodaltar(graphics, DARK_ALTAR);
			}

		}


		if (northStoneMineable && northStone != null)
		{

			//     System.out.println(ESS_COUNT);
			renderStone(graphics, northStone);
		}
		else
		{
			renderStoned(graphics, northStone);

		}


		if (southStoneMineable && southStone != null)
		{
			renderStone(graphics, southStone);
		}
		else
		{
			renderStoned(graphics, southStone);
		}

		return null;
	}

	private void renderStone(Graphics2D graphics, GameObject gameObject)
	{
		if (config.showDenseRunestoneClickbox())
		{
			Shape clickbox = gameObject.getClickbox();
			Point mousePosition = client.getMouseCanvasPosition();
			OverlayUtil.renderHoverableArea(
				graphics, clickbox, mousePosition,
				CLICKBOX_FILL_COLOR, CLICKBOX_BORDER_COLOR, CLICKBOX_BORDER_HOVER_COLOR);
		}


		if (config.showDenseRunestoneClickbox())
		{
			Shape clickbox = gameObject.getClickbox();
			Point mousePosition = client.getMouseCanvasPosition();
			OverlayUtil.renderHoverableArea(
				graphics, clickbox, mousePosition,
				CLICKBOX_FILL_COLOR, CLICKBOX_BORDER_COLOR, CLICKBOX_BORDER_HOVER_COLOR);
		}


		if (config.showDenseRunestoneIndicator())
		{
			LocalPoint gameObjectLocation = gameObject.getLocalLocation();
			OverlayUtil.renderImageLocation(
				client, graphics, gameObjectLocation,
				skillIconManager.getSkillImage(Skill.MINING, false), Z_OFFSET);

		}
	}


	private void renderbloodaltardone(Graphics2D graphics, GameObject gameObject)
	{
		if (config.highlightDenseBloodALTAR())
		{
			Shape clickbox = gameObject.getClickbox();
			Point mousePosition = client.getMouseCanvasPosition();
			OverlayUtil.renderHoverableArea(
				graphics, clickbox, mousePosition,
				CLICKBOX_FILL_COLOR, CLICKBOX_BORDER_COLOR, CLICKBOX_BORDER_HOVER_COLOR);
		}


	}


	private void renderbloodaltar(Graphics2D graphics, GameObject gameObject)
	{


		if (config.highlightDenseBloodALTAR())
		{

			Shape clickbox = gameObject.getClickbox();
			Point mousePosition = client.getMouseCanvasPosition();
			OverlayUtil.renderHoverableArea(
				graphics, clickbox, mousePosition,
				CLICKBOX_FILL_COLORd, CLICKBOX_BORDER_COLORd, CLICKBOX_BORDER_HOVER_COLORd);
		}


	}


	private void renderStoned(Graphics2D graphics, GameObject gameObject)
	{
		if (config.showDenseRunestoneClickbox())
		{
			Shape clickbox = gameObject.getClickbox();
			Point mousePosition = client.getMouseCanvasPosition();
			OverlayUtil.renderHoverableArea(
				graphics, clickbox, mousePosition,
				CLICKBOX_FILL_COLORd, CLICKBOX_BORDER_COLORd, CLICKBOX_BORDER_HOVER_COLORd);
		}


		if (config.showDenseRunestoneIndicator())
		{
			LocalPoint gameObjectLocation = gameObject.getLocalLocation();
			OverlayUtil.renderImageLocation(
				client, graphics, gameObjectLocation,
				skillIconManager.getSkillImage(Skill.MINING, false), Z_OFFSET);
		}
	}
}





