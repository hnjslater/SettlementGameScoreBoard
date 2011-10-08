import model.Game;
import model.GameOptions;
import model.GameOptionsLoader;
import model.PlayerColor;
import webservice.Webservice;
class Main {

public static void main(String args[]) throws Exception {
		try {
			GameOptions options = GameOptionsLoader.load();
						
	        Game game = new Game(options.getAchievements());
	        
	        for (PlayerColor pc : options.getPlayerColors())
	        	game.addPlayer(pc);

	        Webservice ws = new Webservice(game);
	        ui.Controller con = new ui.Controller(game, ws, options);
        
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
