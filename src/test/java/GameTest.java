import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
        Game g = new Game();
        g.addPlayer(PlayerColor.Blue);
        g.addPlayer(PlayerColor.Red);
        g.addPlayer(PlayerColor.Green);

        Player p = g.getPlayer(PlayerColor.Blue);
        p.updateVP(+8);
        assertTrue( Player.equals(p,g.getWinner()) );
    }

    public void test1Winner10VPthenRemoveWinner() throws RulesBrokenException
    {
        Game g = new Game();
        g.addPlayer(PlayerColor.Blue);
        g.addPlayer(PlayerColor.Red);
        g.addPlayer(PlayerColor.Green);

        Player p = g.getPlayer(PlayerColor.Blue);
        p.updateVP(+8);
        assertTrue( Player.equals(p,g.getWinner()) );
        g.removePlayer(PlayerColor.Blue);
        assertTrue( Player.equals(null,g.getWinner() ));
    }
}
