import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import model.*;
import static org.easymock.EasyMock.*;
/**
 * Unit test for simple App.
 */
public class PlayerTest 
    extends TestCase
{
    
    private GameConstraints mockConstraints;
    private PlayerListener mockListener;
    private AtomicInteger changeNo;
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

    @Override
    protected void setUp() throws Exception {
        mockConstraints = createMock(GameConstraints.class);
        mockListener = createMock(PlayerListener.class);
        changeNo = new AtomicInteger();
    }
    
    /**
     * Test renaming of Players including checking events are raised
     */
    public void testPlayerNaming() throws RulesBrokenException
    {
    	PlayerColor blue = new PlayerColor("Blue", Color.blue, 'b');
        Player p = new Player(blue, changeNo, mockConstraints);

        mockListener.playerRenamed(new PlayerEvent(p));
        mockListener.playerRenamed(new PlayerEvent(p));
        replay(mockListener);

        p.addPlayerListener(mockListener);

        // Check the default name works
        assertEquals (p.getName(), blue.toString());

        // Can we change it (and does the right event fire?)
        p.setName("BLAH");

        assertEquals (p.getName(), "BLAH");

        // Back to where we started (along with another event)
        p.resetName();

        assertEquals (p.getName(), blue.toString());
        
        // Check the remove works (the next setName shouldn't send an event)
        p.removePlayerListener(mockListener);
        
        p.setName("MORE BLAHS");

        verify(mockListener);
    }
    
    public void testPlayerVP() throws RulesBrokenException {
    	PlayerColor blue = new PlayerColor("Blue", Color.blue, 'b');
        Player p = new Player(blue, changeNo, mockConstraints);
        Achievement largestArmy = new Achievement("Largest Army", 2, 'l', "LargestArmy", 1);
        
        mockListener.playerVPChanged(new PlayerEvent(p));
        expectLastCall().times(3);
                
        replay(mockListener);
        
        p.addPlayerListener(mockListener);
        
        p.setVP(10);
        p.add(largestArmy);
        p.remove(largestArmy);
        
        // check the remove works
        p.removePlayerListener(mockListener);
        
        // none of these should pass events to mockListener:
        p.setVP(12);
        p.add(largestArmy);
        p.remove(largestArmy);
        
        verify(mockListener);
    }
    
    public void testPlayerComparing() throws RulesBrokenException {
    	PlayerColor blue = new PlayerColor("Blue", Color.blue, 'b');
    	PlayerColor green = new PlayerColor("Green", Color.green, 'g');
    	
        Player pB = new Player(blue, changeNo, mockConstraints);
        Player pG = new Player(green, changeNo, mockConstraints);
        
        // sanity check
        assertTrue (pB.compareTo(pB) == 0);
        assertTrue (pG.compareTo(pG) == 0);
        assertTrue (pB.compareTo(pG) != 0);
        assertTrue (pG.compareTo(pB) != 0);
        
        // Give Blue some points
        pB.setVP(3);
        
        // Now Green should be less than Blue
        assertTrue (pB.compareTo(pG) < 0);
        assertTrue (pG.compareTo(pB) > 0);
        
        // Put Green ahead;
        pG.setVP(4);
        
        // Now Green should be more than Blue
        assertTrue (pB.compareTo(pG) > 0);
        assertTrue (pG.compareTo(pB) < 0);
        
        // Catch Blue up:
        pB.setVP(4);
        
        // Green should still be ahead (as they got there first);
        assertTrue (pB.compareTo(pG) > 0);
        assertTrue (pG.compareTo(pB) < 0);        
	
    }
    
    public void testPlayerEquals() {
    	PlayerColor blue = new PlayerColor("Blue", Color.blue, 'b');
    	PlayerColor green = new PlayerColor("Green", Color.green, 'g');
        Player pB = new Player(blue, changeNo, mockConstraints);
        Player pG = new Player(green, changeNo, mockConstraints);

        assertEquals(pB,pB);
        assertTrue(Player.equals(pB, pB));
        
        assertFalse(pB.equals(pG));
        assertFalse(Player.equals(pB, pG));
    }
}

