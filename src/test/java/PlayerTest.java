import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 3 players, one player gains 8 points, check winner
     */
    public void testPlayerNaming() throws RulesBrokenException
    {
        GameConstraints nothing = new GameConstraints() {
            public void updateVP(Player p, int vp_delta) {}
        };
        // using atomic integer as it can be made final and thus passed into an annonymous class
        // FIXME use a mock object
        final AtomicInteger renamedEvents =  new AtomicInteger(0);
        PlayerListener testListener = new PlayerListener() {
            public void playerRenamed(PlayerEvent e) { renamedEvents.incrementAndGet(); };
            public void playerVPChanged(PlayerEvent e) { };
            public void playerRankChanged(PlayerEvent e) { };
        };

        Player p = new Player(PlayerColor.Blue, new AtomicInteger(), nothing);
        p.addPlayerListener(testListener);

        assertTrue (p.getName().equals(PlayerColor.Blue.toString()));

        p.setName("BLAH");

        assertTrue (p.getName().equals("BLAH"));
        assertTrue (renamedEvents.get() == 1);


        p.resetName();

        assertTrue (p.getName().equals(PlayerColor.Blue.toString()));
        assertTrue (renamedEvents.get() == 2);
    }
}

