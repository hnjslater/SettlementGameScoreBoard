import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import model.Achievement;
import model.Game;
import model.PlayerColor;
import webservice.webservice;
class Main {

public static void main(String args[]) throws Exception {
		try {
			
			Set<PlayerColor> colors = new HashSet<PlayerColor>();
			colors.add(new PlayerColor("Blue", Color.BLUE, 'b'));
			colors.add(new PlayerColor("Green", Color.GREEN, 'g'));
			colors.add(new PlayerColor("Red", Color.RED, 'r'));
			colors.add(new PlayerColor("Orange", Color.ORANGE, 'o'));
			colors.add(new PlayerColor("Black", Color.BLACK, 'k'));
			colors.add(new PlayerColor("White", Color.WHITE, 'w'));
			colors.add(new PlayerColor("Yellow", Color.YELLOW, 'y'));
			
			Set<Achievement> achievements = new HashSet<Achievement>();
			achievements.add(new Achievement("Longest Road", 2, "LR", 'l'));
			achievements.add(new Achievement("Largest Army", 2, "LA", 'a'));
			achievements.add(new Achievement("Harbourmaster", 2, "HM", 'h'));
			
			
	        Game game = new Game(colors, achievements);
	        
	        for (PlayerColor pc : colors)
	        	game.addPlayer(pc);
	        
	        ui.Controller con = new ui.Controller(game);
	        webservice ws = new webservice(game);
	        ws.start();
        
            con.run();
        }
        catch (Exception ex) {
            System.err.println(ex.getClass());
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    } 

}
