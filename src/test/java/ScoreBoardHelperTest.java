import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ui.scoreboard.ScoreBoardHelper;

public class ScoreBoardHelperTest 
    extends TestCase
{
    
 
    public ScoreBoardHelperTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( ScoreBoardHelperTest.class );
    }

    private ScoreBoardHelper helper;
    
    @Override
    protected void setUp() throws Exception {
    	helper = new ScoreBoardHelper();
    }
 
    public void testBestIndexOf() {
    	assertEquals(helper.bestIndexOf("largest army", 'l'), 0);
    	assertEquals(helper.bestIndexOf("largest army", 'a'), 8);
    	assertEquals(helper.bestIndexOf("largest army", 'r'), 2);
    	

    	assertEquals(helper.bestIndexOf("LARGEST ARMY", 'l'), 0);
    	assertEquals(helper.bestIndexOf("LARGEST ARMY", 'a'), 8);
    	assertEquals(helper.bestIndexOf("LARGEST ARMY", 'r'), 2);
    	
    	assertEquals(helper.bestIndexOf("Largest Army", 'l'), 0);
    	assertEquals(helper.bestIndexOf("Largest Army", 'a'), 8);
    	assertEquals(helper.bestIndexOf("Largest Army", 'r'), 2);
    	
    	assertEquals(helper.bestIndexOf(null, 'z'), -1);
    	assertEquals(helper.bestIndexOf("", 'z'), -1);
    	assertEquals(helper.bestIndexOf("largest army", 'z'), -1);
    }
    
}
    