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
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GameTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GameTest.class );
    }

    /**
     * 3 players, one player gains 8 points, check winner
     */
    public void test1Winner10VP() throws RulesBrokenException
    {
	
	// Could maybe do this with an Answer?
        PlayerFactory pf = new PlayerFactory() {
	    @Override
	    public Player createPlayer(PlayerColor playerColor, AtomicInteger integer, GameConstraints constraints) {
		Player p =  createNiceMock(Player.class);
		if (playerColor.equals(PlayerColor.Blue)) {
		    expect(p.getVP()).andStubReturn(10);
		}
		else {
		    expect(p.getVP()).andStubReturn(2);
		}
		expect(p.getPlayerColor()).andStubReturn(playerColor);
		expect(p.getAchievements()).andStubReturn(new HashSet<Achievement>());
		replay(p);
		return p;
	    }
	};
	
       
        Game g = new Game();
        g.setPlayerFactory(pf);
        g.addPlayer(PlayerColor.Blue);
        g.addPlayer(PlayerColor.Red);
        g.addPlayer(PlayerColor.Green);
        
        // Little hack to make sure the setWinner logic in game is actually run.
        g.playerVPChanged(new PlayerEvent(g.getPlayer(PlayerColor.Blue)));

        // So we should have a winner (as blue always reports it's VP as 10
        Player p = g.getPlayer(PlayerColor.Blue);
        assertSame( p, g.getWinner() );
        
        // so now if we get rid of the winning player, it should default to null;
        g.removePlayer(p);
        assertNull ( g.getWinner() );
    }
    
    public void testGameEvents() throws RulesBrokenException {
	
	final Player bluePlayer = createNiceMock(Player.class);
	expect(bluePlayer.getPlayerColor()).andStubReturn(PlayerColor.Blue);
	expect(bluePlayer.getAchievements()).andStubReturn(new HashSet<Achievement>());
	replay(bluePlayer);
	
        PlayerFactory pf = new PlayerFactory() {
	    @Override
	    public Player createPlayer(PlayerColor playerColor, AtomicInteger integer, GameConstraints constraints) { 
		return bluePlayer;
	    }
        };

	Game g = new Game();
	g.setPlayerFactory(pf);
        
        GameListener mockListener = createMock(GameListener.class);
        mockListener.playerAdded(new GameEvent(g, bluePlayer));
        mockListener.playerRemoved(new GameEvent(g, bluePlayer));
        replay(mockListener);

	g.addGameListener(mockListener);
	g.addPlayer(PlayerColor.Blue);
	g.removePlayer(g.getPlayer(PlayerColor.Blue));
	
	//verify(mockListener);
	
    }
    
    

}
