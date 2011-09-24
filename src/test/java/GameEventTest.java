import java.awt.Color;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import model.*;
import static org.easymock.EasyMock.*;
/**
 * Unit test for simple App.
 */
public class GameEventTest 
extends TestCase
{
	private Game game1;
	private Player player1;
	private Game game2;
	private Player player2;
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public GameEventTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( GameEventTest.class );
	}

	@Override
	protected void setUp() throws Exception {
		game1 = createMock(Game.class);
		game2 = createMock(Game.class);

		player1 = createMock(Player.class);
		player2 = createMock(Player.class);

		expect(player1.getPlayerColor()).andStubReturn(new PlayerColor("Blue", Color.blue, 'b'));
		expect(player2.getPlayerColor()).andStubReturn(new PlayerColor("Green", Color.green, 'g'));

		replay(player1);
		replay(player2);
	}

	public void testSameObject() {
		GameEvent e = new GameEvent(game1, player1);
		assertEquals(e,e);
	}

	public void testDiffObjectSameValues() {
		GameEvent e1 = new GameEvent(game1, player1);
		GameEvent e2 = new GameEvent(game1, player1);
		assertEquals(e1,e2);
		assertEquals(e2,e1);
	}

	public void testSameGameNullPlayer() {
		GameEvent e1 = new GameEvent(game1, null);
		GameEvent e2 = new GameEvent(game1, null);
		assertEquals(e1,e2);
		assertEquals(e2,e1);
	}    

	public void testSameGame1NullPlayer() {
		GameEvent e1 = new GameEvent(game1, player1);
		GameEvent e2 = new GameEvent(game1, null);
		assertFalse(e1.equals(e2));
		assertFalse(e2.equals(e1));
	}   

	public void testSameGameDiffPlayers() {
		GameEvent e1 = new GameEvent(game1, player1);
		GameEvent e2 = new GameEvent(game1, player2);
		assertFalse(e1.equals(e2));
		assertFalse(e2.equals(e1));
	}

	public void testDiffGamesSamePlayer() {
		GameEvent e1 = new GameEvent(game1, player1);
		GameEvent e2 = new GameEvent(game2, player1);
		assertFalse(e1.equals(e2));
		assertFalse(e2.equals(e1));
	}

	public void testDiffGamesDiffPlayers() {
		GameEvent e1 = new GameEvent(game1, player1);
		GameEvent e2 = new GameEvent(game2, player2);
		assertFalse(e1.equals(e2));
		assertFalse(e2.equals(e1));
	}    
}
