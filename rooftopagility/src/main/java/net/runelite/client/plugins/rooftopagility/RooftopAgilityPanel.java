package net.runelite.client.plugins.rooftopagility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class RooftopAgilityPanel extends PluginPanel
{
	public boolean startAgility;
	public boolean markPickup = true;

	@Inject
	BotUtils utils;

	void init()
	{
		setLayout(new BorderLayout(0, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		final Font smallFont = FontManager.getRunescapeSmallFont();

		JPanel agilityPanel = new JPanel();
		agilityPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		agilityPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		agilityPanel.setLayout(new DynamicGridLayout(3, 1));

		JTextArea supportedCoursesText = new JTextArea();
		supportedCoursesText.setLineWrap(true);
		supportedCoursesText.setEditable(false);
		supportedCoursesText.setFont(smallFont);
		supportedCoursesText.setText("Supported Courses:\r\nTree Gnome\r\nDraynor\r\nVarrock\r\nCanifis\r\nFalador\r\nSeers Village\r\nRellekka\r\nPollniveach" +
			"\r\n\r\nInstructions: Stand near the course and click start!\r\n");

		JCheckBox markCheck = new JCheckBox();
		markCheck.setFont(smallFont);
		markCheck.setSelected(true);
		markCheck.setText("Pickup Mark of Grace");

		markCheck.addActionListener(ev ->
		{
			if (markCheck.isSelected())
			{
				utils.sendGameMessage("picking up marks of grace");
				markPickup = true;
			}
			else
			{
				utils.sendGameMessage("NOT picking up marks of grace");
				markPickup = false;
			}
		});

		JPanel startPanel = new JPanel();
		startPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		startPanel.setLayout(new DynamicGridLayout(1, 1));

		JButton startBot = new JButton("Run!");
		startBot.setPreferredSize(new Dimension(30, 30));
		startBot.setFont(smallFont);
		startBot.setToolTipText("Start agility bot");

		startBot.addActionListener(ev ->
		{
			if (!startAgility)
			{
				if (markCheck.isSelected())
				{
					utils.sendGameMessage("Picking up marks of grace"); //this is bad
					markPickup = true;
				}
				startAgility = true;
				startBot.setText("Stop");
				startBot.setBackground(ColorScheme.BRAND_BLUE_TRANSPARENT);
			}
			else
			{
				utils.sendGameMessage("NOT picking up marks of grace");
				markPickup = false;
				startAgility = false;
				startBot.setText("Run!");
				startBot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});

		agilityPanel.add(supportedCoursesText);
		agilityPanel.add(markCheck);
		startPanel.add(startBot);

		add(agilityPanel, BorderLayout.NORTH);
		add(startPanel, BorderLayout.SOUTH);
	}
}
