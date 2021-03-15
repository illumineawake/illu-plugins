package net.runelite.client.plugins.iutils.bot;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.iutils.WalkUtils;
import net.runelite.client.plugins.iutils.scene.Locatable;
import net.runelite.client.plugins.iutils.scene.ObjectCategory;
import net.runelite.client.plugins.iutils.scene.Position;

public class iTile implements Locatable
{
	@Inject	private WalkUtils walk;

	private Client client;

    final Bot bot;
    final Position position;
//    List<GroundItem> items = new ArrayList<>();
    iObject regularObject;
	iObject wall;
	iObject wallDecoration;
	iObject floorDecoration;

    iTile(Bot bot, Client client, Position position) {
        this.bot = bot;
		this.client = client;
        this.position = position;
    }

//    @Override
    public Bot bot() {
        return bot;
    }

    @Override
	public Client client() { return client; }

    @Override
    public Position position() {
        return position;
    }

    public void walkTo() { // todo: when is run = 2?
//		bot.minimapFlag = position;
//		bot.mouseClicked();
//		bot.connection().walkViewport(position.x, position.y, game.ctrlRun ? 1 : 0);
		walk.sceneWalk(new WorldPoint(position.x, position.y, position.z), 0, 0);
    }

//    public List<GroundItem> items() {
//        return items;
//    }

    public List<iObject> objects() {
        ArrayList<iObject> objects = new ArrayList<>(4);
        if (regularObject != null) objects.add(regularObject);
        if (wall != null) objects.add(wall);
        if (wallDecoration != null) objects.add(wallDecoration);
        if (floorDecoration != null) objects.add(floorDecoration);
        return objects;
    }

	public iObject object(ObjectCategory category) { //Might need .equals?
		iObject result = null;
		switch (category) {
			case REGULAR:
				result = regularObject;
				break;
			case WALL:
				result = wall;
				break;
			case WALL_DECORATION:
				result = wallDecoration;
				break;
			case FLOOR_DECORATION:
				result = floorDecoration;
				break;
		};
		return result;
	}

    /*public iObject object(ObjectCategory category) {
    	iObject result;
        return switch (category) {
            case REGULAR -> regularObject;
            case WALL -> wall;
            case WALL_DECORATION -> wallDecoration;
            case FLOOR_DECORATION -> floorDecoration;
        };
    }*/
}
