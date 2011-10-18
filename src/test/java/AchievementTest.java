import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import model.Achievement;



public class AchievementTest extends TestCase {
    public AchievementTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( AchievementTest.class );
    }
    
    public void testGetShortName() {
    	assertEquals(new Achievement("Settlement", 2, 's', "BLAH", 0, 0).getShortName(), "S");
    	assertEquals(new Achievement("Longest Road", 2, 's', "BLAH", 2, 0).getShortName(), "LR");
    	assertEquals(new Achievement("Longest Blah Road", 2, 's', "BLAH", 0, 0).getShortName(), "LBR");
    	
    }
}
