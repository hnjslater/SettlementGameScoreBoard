import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.concurrent.atomic.AtomicInteger;
import model.*;
import static org.easymock.EasyMock.*;
/**
 * Unit test for simple App.
 */
public class PlayerTest 
    extends TestCase
{
    /**
     * Create the test case		
     *
     * @param testName name of the test case
     */
    public PlayerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PlayerTest.class );
    }

    /**
     * Test renaming of Players including checking events are raised
     */
    public void testPlayerNaming() throws RulesBrokenException
    {
        GameConstraints mockConstraints = createMock(GameConstraints.class);
        PlayerListener mockListener = createMock(PlayerListener.class);
        Player p = new Player(PlayerColor.Blue, new AtomicInteger(), mockConstraints);

        mockListener.playerRenamed(new PlayerEvent(p));
        mockListener.playerRenamed(new PlayerEvent(p));
        replay(mockListener);

        p.addPlayerListener(mockListener);

        assertEquals (p.getName(), PlayerColor.Blue.toString());

        p.setName("BLAH");

        assertEquals (p.getName(), "BLAH");

        p.resetName();

        assertEquals (p.getName(), PlayerColor.Blue.toString());

        verify(mockListener);
    }
    
    public void testPlayerVP() throws RulesBrokenException {
        GameConstraints mockConstraints = createNiceMock(GameConstraints.class);
        AtomicInteger changeCount = new AtomicInteger();
        PlayerListener mockListener = createMock(PlayerListener.class);
        
        Player p = new Player(PlayerColor.Blue, changeCount, mockConstraints);
        
        mockListener.playerVPChanged(new PlayerEvent(p));
        expectLastCall().times(4);
                
        replay(mockListener);
        
        p.addPlayerListener(mockListener);
        
        p.updateVP(+1);
        p.updateVP(-1);
        p.add(Achievement.LargestArmy);
        p.remove(Achievement.LargestArmy);
        
        verify(mockListener);
    }
    
    public void testPlayerComparing() throws RulesBrokenException {

        GameConstraints mockConstraints = createNiceMock(GameConstraints.class);
        AtomicInteger changeCount = new AtomicInteger();
        
        Player pB = new Player(PlayerColor.Blue, changeCount, mockConstraints);
        Player pG = new Player(PlayerColor.Green, changeCount, mockConstraints);
        
        // sanity check
        assertTrue (pB.compareTo(pB) == 0);
        assertTrue (pG.compareTo(pG) == 0);
        assertTrue (pB.compareTo(pG) != 0);
        assertTrue (pG.compareTo(pB) != 0);
        
        // Give Blue some points
        pB.updateVP(+1);
        
        // Now Green should be less than Blue
        assertTrue (pB.compareTo(pG) < 0);
        assertTrue (pG.compareTo(pB) > 0);
        
        // Put Green ahead;
        pG.updateVP(+2);
        
        // Now Green should be more than Blue
        assertTrue (pB.compareTo(pG) > 0);
        assertTrue (pG.compareTo(pB) < 0);
        
        // Catch Blue up:
        pB.updateVP(+1);
        
        // Green should still be ahead (as they got there first);
        assertTrue (pB.compareTo(pG) > 0);
        assertTrue (pG.compareTo(pB) < 0);        
	
    }
}

