package ui.scoreboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
	private List<HotZone> hotZones;
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
			guiObjects.add(factory.getPainter(player, game, this));
		}
		game.addGameListener(this);
		helper.getHelpMessage(ScoreBoardState.DEFAULT);
		this.keyController = new ScoreBoardKeyListener(game, this);

		this.hotZones = Collections.synchronizedList(new LinkedList<HotZone>()); 
		this.mouseController = new ScoreBoardMouseListener(this,game,hotZones);
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
		List<HotZone> newHotZones = new ArrayList<HotZone>();
		for (GUIObject p : guiObjects) {
			isAnimating |= p.paint(graphics, now, frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), newHotZones );
		}

		final Player player = editing;
		final ScoreBoard board = this;
		if (this.state == ScoreBoardState.EDIT_PLAYER && player != null) {
			newHotZones.clear();
			graphics.setFont(bigFont);
			int lineHeight = graphics.getFontMetrics().getHeight();
			int h = lineHeight * (game.getAchievements().size()+1);
			int w = 0;
			for (Achievement a: game.getAchievements()) {
				int newWidth = graphics.getFontMetrics().stringWidth("+ " + a.getName());
				if (newWidth > w)
					w = newWidth;
			}
			int nameWidth = graphics.getFontMetrics().stringWidth(player.getName());
			if (nameWidth > w)
				w = nameWidth;
			w += lineHeight; // for the minus button
			int x = (frame.getContentPane().getWidth() - w) / 2;
			int y = (frame.getContentPane().getHeight() - h) / 2;
			int ascent = graphics.getFontMetrics().getAscent(); 
			int descent = graphics.getFontMetrics().getDescent();
			if (player.getPlayerColor().getColor().equals(Color.BLACK))
				graphics.setColor(Color.WHITE);
			else
				graphics.setColor(Color.BLACK);	
			graphics.fillRect(x, y, w, h);
			graphics.setColor(player.getPlayerColor().getColor());
				
			graphics.drawRect(x, y, w, h);
			graphics.drawLine(x, y + lineHeight, x + w, y + lineHeight);
			
			int cursor = y + ascent;
			graphics.drawString(player.getName(), x + (w - nameWidth)/2, cursor);
			cursor += lineHeight;
			
			for (final Achievement a: game.getAchievements()) {
				String mainText = "+ " + a.getName();
				if (player.getAchievements().contains(a) && (a.getMaxInGame() == 1 || a.getMaxPerPlayer() == 1)) {
					mainText = "\u2212 " + a.getName();
				}
				AttributedString name = new AttributedString(mainText);
				name.addAttribute(TextAttribute.FONT, bigFont);
				int underlineIndex = helper.bestIndexOf(mainText, a.getCharacter());
				if (underlineIndex > -1)
					name.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, underlineIndex, underlineIndex+1);
				
				graphics.drawString(name.getIterator(), x, cursor);
				graphics.drawLine(x, cursor - ascent, x + w, cursor - ascent);
				
				Rectangle minusButton = null;
				Rectangle plusButton = new Rectangle(x,cursor - ascent,w-lineHeight, lineHeight);
				if (a.getMaxInGame() != 1 && a.getMaxPerPlayer() != 1 && player.getAchievementCount(a) > 0) {
					graphics.drawLine(x + w - lineHeight, cursor - ascent, x+w-lineHeight, cursor + descent);
					String minusSymbol ="\u2212"; 
					graphics.drawString(minusSymbol, x+w-lineHeight + (lineHeight - graphics.getFontMetrics().stringWidth(minusSymbol))/2, cursor);
					minusButton = new Rectangle(x+w-lineHeight,cursor - ascent,lineHeight, lineHeight);
				}
				else if (player.getAchievements().contains(a) && (a.getMaxInGame() == 1 || a.getMaxPerPlayer() == 1)) {
					minusButton = plusButton;
					plusButton = null;
				}
				if (plusButton != null) {
					newHotZones.add(new HotZone(plusButton)
					{
						@Override
						protected void clicked() {
							// TODO Auto-generated method stub
							try {
								player.add(a);
								board.setState(ScoreBoardState.DEFAULT);

							} catch (RulesBrokenException e) {
							}
						}	
					});
				}
				
				if (minusButton != null) {
					newHotZones.add(new HotZone(minusButton)
					{
						@Override
						protected void clicked() {
							// TODO Auto-generated method stub
							try {
								player.remove(a);
								board.setState(ScoreBoardState.DEFAULT);
							} catch (RulesBrokenException e) {
							}
						}	
					});
				}

				cursor += lineHeight;
			}
			newHotZones.add(new HotZone(new Rectangle(0,0,frame.getContentPane().getWidth(), frame.getContentPane().getHeight())) {
				
				@Override
				protected void clicked() {
					board.setState(ScoreBoardState.DEFAULT);
				}
			});
		}
		synchronized(hotZones) { // otherwise sometimes a click will happen when hotZones is empty
			hotZones.clear();
			hotZones.addAll(newHotZones);
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
		guiObjects.add(factory.getPainter(e.getPlayer(), game, this));
		
		for (GUIObject o : guiObjects) {
			o.invalidate();
		}
		
	}

	public void achievementSelected(Achievement achievement) {
		try {
			editing.add(achievement);
			this.setState(ScoreBoardState.DEFAULT);
		} catch (RulesBrokenException e) {
		}
		
		
	}

	public void achievementUnselected(Achievement achievement) {
		try {
			editing.remove(achievement);
			this.setState(ScoreBoardState.DEFAULT);
		} catch (RulesBrokenException e) {
		}
		
	}

}
