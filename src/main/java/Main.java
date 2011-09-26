import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import model.Achievement;
import model.Game;
import model.GameOptions;
import model.GameOptionsLoader;
import model.PlayerColor;
import webservice.webservice;
class Main {

public static void main(String args[]) throws Exception {
		try {
			GameOptions options = GameOptionsLoader.load();
						
	        Game game = new Game(options.getAchievements());
	        
	        for (PlayerColor pc : options.getPlayerColors())
	        	game.addPlayer(pc);
	        
	        ui.Controller con = new ui.Controller(game, options);
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
