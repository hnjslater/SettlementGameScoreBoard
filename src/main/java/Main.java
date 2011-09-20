import webservice.webservice;
import model.*;
class Main {

public static void main(String args[]) throws Exception {
		try {
	        Game game = new Game();
	        
	        game.addPlayer(PlayerColor.Blue);
	        game.addPlayer(PlayerColor.Orange);
	        game.addPlayer(PlayerColor.Green);
	        game.addPlayer(PlayerColor.Red);
	        game.addPlayer(PlayerColor.Brown);
	        game.addPlayer(PlayerColor.White);
	        
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
