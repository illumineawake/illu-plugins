package net.runelite.client.plugins.rooftopagility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;

import net.runelite.client.plugins.botutils.BotUtils;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class RooftopAgilityPanel extends PluginPanel
{
	public boolean startAgility;
	public boolean markPickup;

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
		agilityPanel.setLayout(new DynamicGridLayout(2, 1));

		JLabel markLabel = new JLabel("Pickup Mark of Grace");
		JCheckBox markCheck = new JCheckBox();
		markCheck.setSelected(true);
		markPickup = true;
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

		markLabel.setFont(smallFont);


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
					utils.sendGameMessage("picking up marks of grace"); //this is bad
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

		agilityPanel.add(markLabel);
		agilityPanel.add(markCheck);
		startPanel.add(startBot);

		add(agilityPanel, BorderLayout.NORTH);
		add(startPanel, BorderLayout.SOUTH);
	}
}
