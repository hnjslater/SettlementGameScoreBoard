import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import model.Achievement;
import model.Game;
import model.GameConstraints;
import model.GameEvent;
import model.GameListener;
import model.Player;
import model.PlayerColor;
import model.PlayerEvent;
import model.PlayerFactory;
import model.RulesBrokenException;
/**
 * Unit test for simple App.
 */
public class GameTest 
extends TestCase
{

	private final PlayerFactory mockPlayerFactory;
	private final List<PlayerColor> colors;
	
	private final PlayerColor blue;
	private final PlayerColor red;
	private final PlayerColor green;
	
	private final Player bluePlayer;
	private final Player redPlayer;
	private final Player greenPlayer;
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public GameTest( String testName )
	{
		super( testName );

		colors = new ArrayList<PlayerColor>();
				
		blue = new PlayerColor("Blue", Color.BLUE, 'b');
		red = new PlayerColor("Red", Color.RED, 'r');
		green = new PlayerColor("Green", Color.GREEN, 'g');

		colors.add(blue);
		colors.add(red);
		colors.add(green);

		bluePlayer = createNiceMock(Player.class);
		redPlayer = createNiceMock(Player.class);
		greenPlayer = createNiceMock(Player.class);

		mockPlayerFactory = new PlayerFactory() {
			@Override
			public Player createPlayer(PlayerColor playerColor, AtomicInteger integer, GameConstraints constraints) {
				if (playerColor.equals(blue))
					return bluePlayer;
				else if (playerColor.equals(red))
					return redPlayer;
				else if (playerColor.equals(green))
					return greenPlayer;
				else
					throw new RuntimeException("");
			}		
		};
	}


	private void beginReplay() {
		replay(bluePlayer);
		replay(redPlayer);
		replay(greenPlayer);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( GameTest.class );
	}

	@Override
	protected void setUp() throws Exception {
		Player[] players = {bluePlayer, redPlayer, greenPlayer};
		for (Player p : players ) {
			reset(p);
			expect(p.getAchievements()).andStubReturn(new HashSet<Achievement>());
		}

		expect(bluePlayer.getPlayerColor()).andStubReturn(blue);
		expect(redPlayer.getPlayerColor()).andStubReturn(red);
		expect(greenPlayer.getPlayerColor()).andStubReturn(green);

	}

	public void testAddingRemovingPlayers() throws RulesBrokenException
	{
		beginReplay();

		Game game = new Game();
		assertEquals(game.getNumberOfPlayers(), 0);
		assertNull(game.getPlayer(blue));
		assertNull(game.getPlayer(green));

		game.addPlayer(blue);
		assertEquals(game.getNumberOfPlayers(), 1);
		assertNotNull(game.getPlayer(blue));
		assertNull(game.getPlayer(green));

		game.addPlayer(green);
		assertEquals(game.getNumberOfPlayers(), 2);
		assertNotNull(game.getPlayer(blue));
		assertNotNull(game.getPlayer(green));

		game.removePlayer(game.getPlayer(blue));
		assertEquals(game.getNumberOfPlayers(), 1);
		assertNull(game.getPlayer(blue));
		assertNotNull(game.getPlayer(green));

		game.removePlayer(game.getPlayer(green));
		assertEquals(game.getNumberOfPlayers(), 0);
		assertNull(game.getPlayer(blue));
		assertNull(game.getPlayer(green));
	}
	public void testPlayerUniqueness() throws RulesBrokenException
	{
		beginReplay();
		Game game = new Game();
		game.addPlayer(blue);
		try {
			game.addPlayer(blue);
			fail("Expected Rules Broken Exception");
		}
		catch (RulesBrokenException ex) {

		}
	}

	/**
	 * 3 players, one player gains 8 points, check winner
	 */
	public void test1Winner10VP() throws RulesBrokenException
	{

		expect(bluePlayer.getVP()).andStubReturn(10);
		expect(redPlayer.getVP()).andStubReturn(2);
		expect(greenPlayer.getVP()).andStubReturn(2);

		beginReplay();


		Game g = new Game();
		g.setPlayerFactory(mockPlayerFactory);
		g.addPlayer(blue);
		g.addPlayer(red);
		g.addPlayer(green);

		// Little hack to make sure the setWinner logic in game is actually run.
		g.playerVPChanged(new PlayerEvent(g.getPlayer(blue)));

		// So we should have a winner (as blue always reports its VP as 10
		Player p = g.getPlayer(blue);
		assertSame( p, g.getWinner() );

		// so now if we get rid of the winning player, it should default to null;
		g.removePlayer(p);
		assertNull ( g.getWinner() );
	}

	public void testGameEvents() throws RulesBrokenException {

		beginReplay();	

		Game g = new Game();
		g.setPlayerFactory(mockPlayerFactory);

		GameListener mockListener = createMock(GameListener.class);
		mockListener.playerAdded(new GameEvent(g, bluePlayer));
		mockListener.playerRemoved(new GameEvent(g, bluePlayer));
		replay(mockListener);

		g.addGameListener(mockListener);
		g.addPlayer(blue);
		g.removePlayer(g.getPlayer(blue));

		verify(mockListener);

	}





}
