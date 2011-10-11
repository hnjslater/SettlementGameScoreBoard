package ui.scoreboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.awt.image.BufferStrategy;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

import model.Achievement;
import model.Game;
import model.GameEvent;
import model.GameListener;
import model.Player;
import model.PlayerColor;
import model.RulesBrokenException;

public class ScoreBoard implements GameListener {

	public enum ScoreBoardState {
		DEFAULT,
		SELECT_PLAYER_TO_EDIT,
		SELECT_PLAYER_TO_DELETE,
		SELECT_PLAYER_TO_ADD,
		EDIT_PLAYER
	}
	private ScoreBoardState state = ScoreBoardState.DEFAULT;
	private JFrame frame;
	private ui.Controller controller;
	private Game game;
	private ScoreBoardHelper helper = new ScoreBoardHelper();
	private KeyListener keyController;
	private MouseInputListener mouseController;
	private boolean fullScreen = false;
	private boolean isAnimating = true;
	private Player winner;
	private final Object winnerLock = new Object();
	private List<GUIObject> guiObjects;
	//private List<HotZone> hotZones;
	private PlayerPainterFactory factory;
	private Player editing;

	private volatile boolean running;
	private Thread graphicsThread;
	private Thread tickThread;





	// might as well cache these;
	private Font bigFont = new Font( "Helvectica", 0, 64 );

	public ScoreBoard(ui.Controller controller, Game game) {
		this.game = game;
		this.controller = controller;

		guiObjects = new CopyOnWriteArrayList<GUIObject>();
		this.factory = new PlayerPainterFactory();


		for (Player player : game.getLeaderBoard()) {
			guiObjects.add(factory.getPainter(player, game));
		}
		game.addGameListener(this);
		helper.getHelpMessage(ScoreBoardState.DEFAULT);
		this.keyController = new ScoreBoardKeyListener(game, this);

		//this.hotZones = Collections.synchronizedList(new LinkedList<HotZone>()); 
		//this.mouseController = new ScoreBoardMouseListener(this,game,hotZones);
		this.running = false;
	}

	public ScoreBoardState getState() {
		return this.state;
	}
	public void setState(ScoreBoardState state) {
		this.state = state;
	}

	public void setStateEditing(Player player) {
		this.editing = player;
		this.setState(ScoreBoardState.EDIT_PLAYER);
	}

	public void renderLoop() {
		while (running) {
			paint();
			// limit framerate if there's no animation currently happening
			try {
				if (!isAnimating) {
					Thread.sleep(100);
				}
				else {
					Thread.yield();
				}
			}
			catch (Exception ex) {
				// Don't really care if I get woken.
			}
		}
	}
	public void tickLoop() {
		int width;
		int height;
		while (running) {
			synchronized(controller.getFrameLock()) { 
				width = frame.getContentPane().getWidth();
				height = frame.getContentPane().getHeight();
			}


			long time = (new Date()).getTime();

			List<GUIObject> toremove = new ArrayList<GUIObject>();
			for (GUIObject p : guiObjects) {
				if ((p.getY(time) > height || p.getX(time) < 0 || p.getX(time) > width) && p instanceof Particle) {
					toremove.add(p);
				}
			}
			guiObjects.removeAll(toremove);

			synchronized(winnerLock) {
				if (guiObjects.size() < 20  && winner != null) {
					int startx = (int)(Math.floor(Math.random() * width));
					int starty = (int)(Math.floor(Math.random() * height));
					for (int i = 0; i < 30; i++) {
						guiObjects.add(new Particle(startx,starty,helper.getGraphicsColor(winner.getPlayerColor()),time));
					}
				}
			}

			try {
				Thread.sleep(10);
			}
			catch (InterruptedException ex) {
				// If we've been interrupted, do the update;
			}
		}
	}

	public void run() throws InterruptedException {
		running = true;


		graphicsThread = new Thread("UIGraphicsThread") {
			public void run() {
				renderLoop();
			}
		};

		tickThread = new Thread("UIWorkerThread") {
			public void run() {
				tickLoop();
			}
		};
		fullScreen = controller.setFullScreen(true);
		frame = controller.getJFrame();
		frame.addKeyListener(keyController);
		frame.addMouseListener(mouseController);
		frame.addMouseMotionListener(mouseController);
		graphicsThread.start();
		tickThread.start();
		graphicsThread.join();
		tickThread.join();
		frame.removeKeyListener(keyController);
		frame.removeMouseListener(mouseController);
		frame.removeMouseMotionListener(mouseController);

	}

	public void stop() {
		synchronized(controller.getFrameLock()) {
			frame.removeMouseListener(mouseController);
			frame.removeMouseMotionListener(mouseController);
			frame.removeKeyListener(keyController);
			running = false;
		}
	}
	/*
    public synchronized void toggleFullScreen() {
        synchronized(controller.getFrameLock()) {
            controller.setFullScreen(!fullScreen);
            this.fullScreen = !fullScreen;
            frame = controller.getJFrame();
            frame.addKeyListener(keyController);
            frame.addMouseListener(mouseController);
            frame.addMouseMotionListener(mouseController);
        }
    }
	 */

	private synchronized void paint () {
		BufferStrategy bf = frame.getBufferStrategy();
		Graphics graphics = bf.getDrawGraphics();

		// background
		if (game.getWinner() != null && game.getWinner().getPlayerColor().getColor().equals(Color.black)) {
			graphics.setColor( java.awt.Color.WHITE );
		}
		else {
			graphics.setColor( java.awt.Color.BLACK );
		}
		graphics.fillRect( 0, 0, frame.getWidth(),frame.getHeight() );

		// FIXME a bit iffy if not fullscreen
		if (!fullScreen) {
			graphics.translate(0, frame.getHeight() - frame.getContentPane().getHeight());
		}

		// antialiasing
		((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		graphics.setFont(bigFont);

		long now = (new Date()).getTime();

		 
		isAnimating = false;
		//synchronized(hotZones) { // otherwise sometimes a click will happen when hotZones is empty
		//	hotZones.clear();
			for (GUIObject p : guiObjects) {
				List<HotZone> hotZones = new ArrayList<HotZone>();
				isAnimating |= p.paint(graphics, now, frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), hotZones );
			}
		//}
		Player player = editing;
		if (this.state == ScoreBoardState.EDIT_PLAYER && player != null) {

			graphics.setFont(bigFont);
			int height = graphics.getFontMetrics().getHeight() * (game.getAchievements().size()+1);
			int width = 0;
			for (Achievement a: game.getAchievements()) {
				int newWidth = graphics.getFontMetrics().stringWidth(a.getName());
				if (newWidth > width)
					width = newWidth;
			}
			int nameWidth = graphics.getFontMetrics().stringWidth(player.getName());
			if (nameWidth > width)
				width = nameWidth;
			int x = (frame.getContentPane().getWidth() - width) / 2;
			int y = (frame.getContentPane().getHeight() - height) / 2;

			if (player.getPlayerColor().getColor().equals(Color.BLACK))
				graphics.setColor(Color.WHITE);
			else
				graphics.setColor(Color.BLACK);	
			graphics.fillRect(x, y, width, height);
			graphics.setColor(player.getPlayerColor().getColor());
				
			graphics.drawRect(x, y, width, height);
			graphics.drawLine(x, y + graphics.getFontMetrics().getHeight(), x + width, y + graphics.getFontMetrics().getHeight());
			int cursor = y + graphics.getFontMetrics().getAscent();
			graphics.drawString(player.getName(), x + (width - nameWidth)/2, cursor);
			cursor += graphics.getFontMetrics().getHeight();
			
			for (Achievement a: game.getAchievements()) {
				AttributedString name = new AttributedString(a.getName());
				name.addAttribute(TextAttribute.FONT, bigFont);
				int underlineIndex = helper.bestIndexOf(a.getName(), a.getCharacter());
				if (underlineIndex > -1)
					name.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, underlineIndex, underlineIndex+1);
				
				graphics.drawString(name.getIterator(), x, cursor);
				
				cursor += graphics.getFontMetrics().getHeight();
			}
		}
		
		graphics.dispose();
		bf.show();
	}

	public void winnerChanged(GameEvent wce) {
		synchronized (winnerLock) {
			boolean bgColorChanged = false;
			if ((winner != null && winner.getPlayerColor().getColor().equals(Color.black)) || (wce.getPlayer() != null && wce.getPlayer().getPlayerColor().getColor().equals(Color.black)))
				bgColorChanged = true;
			winner = wce.getPlayer();
			if (bgColorChanged) {
				synchronized(guiObjects) {
					for (GUIObject o : guiObjects) {
						if (o instanceof PlayerPainter && ((PlayerPainter)o).player.getPlayerColor().getColor().equals(Color.white)) {
							o.invalidate();
						}
					}

				}

			}
		}

	}

	public void playerRemoved(GameEvent e) {
		PlayerPainter toRemove = null;
		for (GUIObject o : guiObjects) {
			if (o instanceof PlayerPainter && ((PlayerPainter)o).player.equals(e.getPlayer())) {
				toRemove = (PlayerPainter)o;
			}
		}
		
		if (toRemove != null) {
			guiObjects.remove(toRemove);
		}
		for (GUIObject o : guiObjects) {
			o.invalidate();
		}
		
	}

	public void playerAdded(GameEvent e) {
		guiObjects.add(factory.getPainter(e.getPlayer(), game));
		
		for (GUIObject o : guiObjects) {
			o.invalidate();
		}
		
	}

	public void achievementSelected(Achievement achievement) {
		try {
			editing.add(achievement);
			this.setState(ScoreBoardState.DEFAULT);
		} catch (RulesBrokenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void updateVP(int i) {
		try {
			editing.setVP(editing.getSettlementVP() + i);
		} catch (RulesBrokenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setState(ScoreBoardState.DEFAULT);
		
	}

	public void achievementUnselected(Achievement achievement) {
		try {
			editing.remove(achievement);
			this.setState(ScoreBoardState.DEFAULT);
		} catch (RulesBrokenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
