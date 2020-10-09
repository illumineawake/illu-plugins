package net.runelite.client.plugins.ibotutils;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.callback.ClientThread;
import static net.runelite.client.plugins.ibotutils.iBotUtils.sleep;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Singleton
public class MouseUtils
{

	@Inject
	private Client client;

	@Inject
	private iBotUtilsConfig config;

	@Inject
	private CalculationUtils calc;

	@Inject
	private ExecutorService executorService;

	private void mouseEvent(int id, @NotNull Point point)
	{
		MouseEvent e = new MouseEvent(
			client.getCanvas(), id,
			System.currentTimeMillis(),
			0, point.getX(), point.getY(),
			1, false, 1
		);

		client.getCanvas().dispatchEvent(e);
	}

	/**
	 * This method must be called on a new
	 * thread, if you try to call it on
	 * {@link ClientThread}
	 * it will result in a crash/desynced thread.
	 */
	public void click(Rectangle rectangle)
	{
		assert !client.isClientThread();

		Point point = getClickPoint(rectangle);
		click(point);
	}

	public void click(Point p)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
			mouseEvent(501, point);
			mouseEvent(502, point);
			mouseEvent(500, point);
			return;
		}
		mouseEvent(501, p);
		mouseEvent(502, p);
		mouseEvent(500, p);
	}

	public void moveClick(Rectangle rectangle)
	{
		assert !client.isClientThread();

		Point point = getClickPoint(rectangle);
		moveClick(point);
	}

	public void moveClick(Point p)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
			mouseEvent(504, point);
			mouseEvent(505, point);
			mouseEvent(503, point);
			mouseEvent(501, point);
			mouseEvent(502, point);
			mouseEvent(500, point);
			return;
		}
		mouseEvent(504, p);
		mouseEvent(505, p);
		mouseEvent(503, p);
		mouseEvent(501, p);
		mouseEvent(502, p);
		mouseEvent(500, p);
	}

	public Point getClickPoint(@NotNull Rectangle rect)
	{
		final int x = (int) (rect.getX() + calc.getRandomIntBetweenRange((int) rect.getWidth() / 6 * -1, (int) rect.getWidth() / 6) + rect.getWidth() / 2);
		final int y = (int) (rect.getY() + calc.getRandomIntBetweenRange((int) rect.getHeight() / 6 * -1, (int) rect.getHeight() / 6) + rect.getHeight() / 2);

		return new Point(x, y);
	}

	public void moveMouseEvent(Rectangle rectangle)
	{
		assert !client.isClientThread();

		Point point = getClickPoint(rectangle);
		moveClick(point);
	}

	public void clickRandomPoint(int min, int max)
	{
		assert !client.isClientThread();

		Point point = new Point(calc.getRandomIntBetweenRange(min, max), calc.getRandomIntBetweenRange(min, max));
		handleMouseClick(point);
	}

	public void clickRandomPointCenter(int min, int max)
	{
		assert !client.isClientThread();

		Point point = new Point(client.getCenterX() + calc.getRandomIntBetweenRange(min, max), client.getCenterY() + calc.getRandomIntBetweenRange(min, max));
		handleMouseClick(point);
	}

	public void delayClickRandomPointCenter(int min, int max, long delay)
	{
		executorService.submit(() ->
		{
			try
			{
				sleep(delay);
				clickRandomPointCenter(min, max);
			}
			catch (RuntimeException e)
			{
				e.printStackTrace();
			}
		});
	}

	/*
	 *
	 * if given Point is in the viewport, click on the Point otherwise click a random point in the centre of the screen
	 *
	 * */
	public void handleMouseClick(Point point)
	{
		assert !client.isClientThread();
		final int viewportHeight = client.getViewportHeight();
		final int viewportWidth = client.getViewportWidth();
		log.debug("Performing mouse click: {}", config.getMouse());

		switch(config.getMouse())
		{
			case ZERO_MOUSE:
				click(new Point(0, 0));
				return;
			case MOVE:
				if (point.getX() > viewportWidth || point.getY() > viewportHeight || point.getX() < 0 || point.getY() < 0)
				{
					clickRandomPointCenter(-100, 100);
					return;
				}
				moveClick(point);
				return;
			case NO_MOVE:
				if (point.getX() > viewportWidth || point.getY() > viewportHeight || point.getX() < 0 || point.getY() < 0)
				{
					Point rectPoint = new Point(client.getCenterX() + calc.getRandomIntBetweenRange(-100, 100), client.getCenterY() + calc.getRandomIntBetweenRange(-100, 100));
					click(rectPoint);
					return;
				}
				click(point);
				return;
			case RECTANGLE:
				Point rectPoint = new Point(client.getCenterX() + calc.getRandomIntBetweenRange(-100, 100), client.getCenterY() + calc.getRandomIntBetweenRange(-100, 100));
				click(rectPoint);
		}
	}

	public void handleMouseClick(Rectangle rectangle)
	{
		assert !client.isClientThread();

		Point point = getClickPoint(rectangle);
		moveClick(point);
	}

	public void delayMouseClick(Point point, long delay)
	{
		executorService.submit(() ->
		{
			try
			{
				sleep(delay);
				handleMouseClick(point);
			}
			catch (RuntimeException e)
			{
				e.printStackTrace();
			}
		});
	}

	public void delayMouseClick(Rectangle rectangle, long delay)
	{
		Point point = getClickPoint(rectangle);
		delayMouseClick(point, delay);
	}

}
