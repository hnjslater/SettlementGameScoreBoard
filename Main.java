class Main {

public static void main(String args[])  throws Exception {

        Game game = new Game();
        game.addPlayer(PlayerColor.Blue);
        game.addPlayer(PlayerColor.Orange);
        game.addPlayer(PlayerColor.Green);
        game.addPlayer(PlayerColor.Red);

        ScoreBoard scoreBoard = new ScoreBoard(game);

        ScoreBoardKeyListener scoreBoardController = new ScoreBoardKeyListener(game,scoreBoard);

        scoreBoard.startRenderLoop();
    } 

}
