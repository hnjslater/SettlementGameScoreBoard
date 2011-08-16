import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import model.*;
import static org.easymock.EasyMock.*;
/**
 * Unit test for simple App.
 */
public class GameTest 
    extends TestCase
{
    
    private final PlayerFactory mockPlayerFactory;
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

        bluePlayer = createNiceMock(Player.class);
        redPlayer = createNiceMock(Player.class);
        greenPlayer = createNiceMock(Player.class);
        
        mockPlayerFactory = new PlayerFactory() {
	    @Override
	    public Player createPlayer(PlayerColor playerColor, AtomicInteger integer, GameConstraints constraints) {
		switch (playerColor) {
		case Blue:
		    return bluePlayer;
		case Red:
		    return redPlayer;
		case Green:
		    return greenPlayer;
		default:
		    throw new RuntimeException("Not set up for that colour");
		}		
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
        
        expect(bluePlayer.getPlayerColor()).andStubReturn(PlayerColor.Blue);
        expect(redPlayer.getPlayerColor()).andStubReturn(PlayerColor.Red);
        expect(greenPlayer.getPlayerColor()).andStubReturn(PlayerColor.Green);
        
    }
    
    public void testAddingRemovingPlayers() throws RulesBrokenException
    {
	beginReplay();
	
	Game game = new Game();
	assertEquals(game.getNumberOfPlayers(), 0);
	assertNull(game.getPlayer(PlayerColor.Blue));
	assertNull(game.getPlayer(PlayerColor.Green));
	
	game.addPlayer(PlayerColor.Blue);
	assertEquals(game.getNumberOfPlayers(), 1);
	assertNotNull(game.getPlayer(PlayerColor.Blue));
	assertNull(game.getPlayer(PlayerColor.Green));
	
	game.addPlayer(PlayerColor.Green);
	assertEquals(game.getNumberOfPlayers(), 2);
	assertNotNull(game.getPlayer(PlayerColor.Blue));
	assertNotNull(game.getPlayer(PlayerColor.Green));
	
	game.removePlayer(game.getPlayer(PlayerColor.Blue));
	assertEquals(game.getNumberOfPlayers(), 1);
	assertNull(game.getPlayer(PlayerColor.Blue));
	assertNotNull(game.getPlayer(PlayerColor.Green));
	
	game.removePlayer(game.getPlayer(PlayerColor.Green));
	assertEquals(game.getNumberOfPlayers(), 0);
	assertNull(game.getPlayer(PlayerColor.Blue));
	assertNull(game.getPlayer(PlayerColor.Green));
    }
    public void testPlayerUniqueness() throws RulesBrokenException
    {
	beginReplay();
	Game game = new Game();
	game.addPlayer(PlayerColor.Blue);
	try {
	    game.addPlayer(PlayerColor.Blue);
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
        g.addPlayer(PlayerColor.Blue);
        g.addPlayer(PlayerColor.Red);
        g.addPlayer(PlayerColor.Green);
        
        // Little hack to make sure the setWinner logic in game is actually run.
        g.playerVPChanged(new PlayerEvent(g.getPlayer(PlayerColor.Blue)));

        // So we should have a winner (as blue always reports its VP as 10
        Player p = g.getPlayer(PlayerColor.Blue);
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
	g.addPlayer(PlayerColor.Blue);
	g.removePlayer(g.getPlayer(PlayerColor.Blue));
	
	verify(mockListener);
	
    }
    

    
    

}
