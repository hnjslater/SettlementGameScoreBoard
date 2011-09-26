package ui.scoreboard;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;

import model.Achievement;
import model.Game;
import model.Player;
import model.PlayerEvent;
import model.PlayerListener;



class PlayerPainter extends GUIObject implements PlayerListener {
	public Player player;
	private Game game;
	private ScoreBoardHelper helper = new ScoreBoardHelper();
	private BufferedImage playerName;
	Boolean playerNameDirty = true;
	Object playerNameDirtyLock = new Object();
	final int margin = 25;
	int letterWidth;
	int maxLetterWidth;
	private PlayerPainterFontHelper paintHelper;
	private boolean editing;
	private int frameWidth;
	private int frameHeight;
	private int numPlayers;

	private int startY = -1;
	private int endY = -1;
	private long startTime;

	public PlayerPainter(Player p, Game g, PlayerPainterFontHelper paintHelper) {
		super(0,0,0,0);
		this.player = p;
		this.game = g;
		this.playerName = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
		this.paintHelper = paintHelper;
		p.addPlayerListener(this);
	}

	// GUIObject methods ///////////////////////////////////////////
	public boolean paint(Graphics graphics, long time, int frameWidth, int frameHeight, List<HotZone> hotZones) {



		if (this.frameHeight != frameHeight || this.frameWidth != frameWidth || this.numPlayers != game.getNumberOfPlayers() || playerNameDirty) {

			this.frameHeight = frameHeight;
			this.frameWidth = frameWidth;
			this.numPlayers = game.getNumberOfPlayers();

			paintHelper.setProperties(graphics, frameWidth, frameHeight, getLineHeight());
			updateImage(graphics.getFont());
		}
		else {
			graphics.setFont(paintHelper.font);
		}
		int lineHeight = getLineHeight();
		Color bgColor = (game.getWinner() != null && game.getWinner().getPlayerColor().getColor().equals(Color.black)) ? Color.white : Color.black;

		if (startY == -1) {
			startY =(lineHeight * game.getLeaderBoard().lastIndexOf(player)); 
			endY = startY;
		}
		int cursor = getY(time);       

		graphics.drawImage(playerName,0,cursor + (lineHeight - playerName.getHeight()) /2,null);
		char[] colorChar = new char[] { helper.getColorChar(player.getPlayerColor())} ;

		if (helper.getGraphicsColor((player.getPlayerColor())) != bgColor) {
			graphics.setColor( helper.getGraphicsColor(player.getPlayerColor()) );            
			graphics.drawChars(colorChar , 0, 1, frameWidth-letterWidth-margin, cursor+paintHelper.cursorDrop );
		}
		else {
			graphics.setColor( (bgColor.equals(Color.WHITE) ? Color.BLACK : Color.WHITE) );
			drawOutlineText(paintHelper.font, new String(colorChar), (Graphics2D)graphics, frameWidth-letterWidth-margin, cursor+paintHelper.cursorDrop );
			graphics.setColor( helper.getGraphicsColor(player.getPlayerColor()) );
		}

		if (showMouseAffordances) {
			float unit = lineHeight / 23f;
			float buttonStart = frameWidth - maxLetterWidth - margin - unit * 16 * 4;
			float buttonWidth = unit * 16;

			if (game.getWinner() == null) {
				// Longest Road button

				float currentButtonX = buttonStart + buttonWidth;

				for (Achievement a : game.getAchievements()) {
					Ellipse2D currentButton = new Ellipse2D.Double(currentButtonX, cursor+unit*4, unit*15, unit*15);

					if (bgColor.equals(helper.getGraphicsColor(player.getPlayerColor()))) {                        
						graphics.setColor(Color.WHITE);
						graphics.drawOval((int)currentButton.getX(), (int)currentButton.getY(), (int)currentButton.getWidth(), (int)currentButton.getHeight());
						if (!player.getAchievements().contains(a)) {
							drawOutlineText(graphics.getFont(), a.getCharacter(), (Graphics2D)graphics, (int)(currentButtonX + (buttonWidth - unit - paintHelper.widthOfR)/2), cursor+paintHelper.cursorDrop );
						}                        
					}
					else {
						if (!player.getAchievements().contains(a)) {
							graphics.fillOval((int)currentButton.getX(), (int)currentButton.getY(), (int)currentButton.getWidth(), (int)currentButton.getHeight());                        
							graphics.setColor(Color.BLACK);
							graphics.drawChars( new char[] { a.getCharacter() }, 0, 1, (int)(currentButtonX + (buttonWidth - unit - paintHelper.widthOfR)/2), cursor+paintHelper.cursorDrop );
							graphics.setColor(player.getPlayerColor().getColor());
						}
						else {
							Stroke normal = ((Graphics2D)graphics).getStroke();
							Stroke wider = new BasicStroke(4);
							((Graphics2D)graphics).setStroke(wider);
							graphics.drawOval((int)currentButton.getX(), (int)currentButton.getY(), (int)currentButton.getWidth(), (int)currentButton.getHeight());
							((Graphics2D)graphics).setStroke(normal);                        
						}                	
					}
					if (player.getAchievements().contains(a)) {
						hotZones.add(new HotZone(null,HotZone.ops.ACH,currentButton,a));
					}
					else {
						hotZones.add(new HotZone(player,HotZone.ops.ACH,currentButton,a));
					}

					currentButtonX -= buttonWidth;
				}
				
				




				// Plus box:
				graphics.setColor(helper.getGraphicsColor(player.getPlayerColor()));
				Ellipse2D plusButton = new Ellipse2D.Double(buttonStart + buttonWidth * 2, cursor+unit*4, unit*15, unit*15);
				graphics.setColor(helper.getGraphicsColor(player.getPlayerColor()));
				Polygon pol = new Polygon();
				pol.addPoint((int)(unit*3), 0);
				pol.addPoint((int)(unit*4), 0);
				pol.addPoint((int)(unit*4), (int)(unit*3));
				pol.addPoint((int)(unit*7), (int)(unit*3));
				pol.addPoint((int)(unit*7), (int)(unit*4));
				pol.addPoint((int)(unit*4), (int)(unit*4));
				pol.addPoint((int)(unit*4), (int)(unit*7));
				pol.addPoint((int)(unit*3), (int)(unit*7));
				pol.addPoint((int)(unit*3), (int)(unit*4));
				pol.addPoint(0, (int)(unit*4));
				pol.addPoint(0, (int)(unit*3));
				pol.addPoint((int)(unit*3), (int)(unit*3));
				pol.translate((int)(plusButton.getX()+unit*4), (int)(plusButton.getY()+unit*4));

				if (bgColor.equals(helper.getGraphicsColor(player.getPlayerColor()))) {
					graphics.setColor(Color.WHITE);
					graphics.drawOval((int)plusButton.getX(), (int)plusButton.getY(), (int)plusButton.getWidth(), (int)plusButton.getHeight());
					graphics.drawPolygon(pol);
					hotZones.add(new HotZone(player,HotZone.ops.INC,plusButton,null));

				}
				else {
					graphics.fillOval((int)plusButton.getX(), (int)plusButton.getY(), (int)plusButton.getWidth(), (int)plusButton.getHeight());
					graphics.setColor(Color.BLACK);
					graphics.fillPolygon(pol);
				}
				hotZones.add(new HotZone(player,HotZone.ops.INC,plusButton, null));
			}
			// Minus box:
			if ((game.getWinner() == null && player.getSettlementVP() > 2) || game.getWinner() == player) {
				Ellipse2D box2 = new Ellipse2D.Double(buttonStart + buttonWidth*3, cursor+unit*4, unit*15, unit*15);
				if (bgColor.equals(helper.getGraphicsColor(player.getPlayerColor()))) {
					graphics.setColor(Color.WHITE);                	
					graphics.drawOval((int)box2.getX(), (int)box2.getY(), (int)box2.getWidth(), (int)box2.getHeight());                	
					graphics.drawRect((int)(box2.getX()+unit*4), (int)(box2.getY()+unit*7), (int)(unit*7), (int)(unit));
				}
				else {
					graphics.setColor(helper.getGraphicsColor(player.getPlayerColor()));                	
					graphics.fillOval((int)box2.getX(), (int)box2.getY(), (int)box2.getWidth(), (int)box2.getHeight());
					graphics.setColor(Color.BLACK);
					graphics.fillRect((int)(box2.getX()+unit*4), (int)(box2.getY()+unit*7), (int)(unit*7), (int)(unit));
				}
				hotZones.add(new HotZone(player,HotZone.ops.DEC,box2,null));
			}

		}
		// if they are equal, then we are not animating
		return startY == endY;
	}
	public int getX(long time) {
		return 0;
	}
	public int getY(long time) {
		//return endY;
		int currentY = 0;
		int direction = 0;
		if (endY > startY)
			direction = +1;
		else if (endY < startY)
			direction = -1;
		long duration = time - startTime;

		if (duration < 0)
			duration = 0;

		currentY =(int)(duration * direction)/2 + startY;

		if ((direction == -1) == (currentY < endY)) {
			startY = endY;
			currentY = startY;
		}

		return currentY;

	}
	public void invalidate() {
		synchronized(playerNameDirtyLock) {
			playerNameDirty = true;
			recalculateEndPosition();
		}
	}
	public void setEditing(boolean editing) {
		this.editing = editing;
		invalidate();
	}

	/// Player Listener methods

	public void playerVPChanged(PlayerEvent pe) {
		synchronized(playerNameDirtyLock) {
			playerNameDirty = true;
		}
	}

	public void playerRenamed(PlayerEvent pe) {
		synchronized(playerNameDirtyLock) {
			playerNameDirty = true;
		}
	}

	public void playerRankChanged(PlayerEvent pe) {
		recalculateEndPosition();
	}

	public void recalculateEndPosition() {
		// if the score board hasn't been rendered yet, we don't know the frameHeight so
		//  we can't work out where the Player is suposed to be =yet.
		if (getLineHeight() != 0) {
			long now = (new Date()).getTime();
			startY = getY(now);
			endY = (getLineHeight() * game.getLeaderBoard().lastIndexOf(player)); 
			startTime = now;
		}
	}

	// private methods
	private void updateImage(Font bigFont) {

		// Do as much as we can outside of the synchronized to reduce the change of a deadlock
		String displayName = player.getName();
		//Hacky cursor
		if (editing)
			displayName += "|";

		// Achievements
		for (Achievement a : player.getAchievements())
			displayName += "(" + a.getShortName() + ")";		

		String playerVP =  new Integer( player.getVP()).toString();
		synchronized(playerNameDirtyLock) {
			Graphics2D graphics = playerName.createGraphics();
			graphics.setFont(bigFont);
			FontMetrics metrics = graphics.getFontMetrics();
			int maxWidth = metrics.stringWidth(displayName);
			letterWidth = metrics.charWidth(helper.getColorChar(player.getPlayerColor()));
			maxLetterWidth = metrics.charWidth('w');
			int maxDescent = metrics.getMaxAscent();
			int maxHeight = maxDescent + metrics.getMaxDescent();
			graphics.dispose();
			playerName = new BufferedImage(maxWidth + paintHelper.maxScoreWidth + margin, maxHeight, BufferedImage.TYPE_INT_ARGB);
			graphics = playerName.createGraphics();

			graphics.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
					java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
					java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
					java.awt.RenderingHints.VALUE_RENDER_QUALITY);

			Color bgcolor = (game.getWinner() != null && game.getWinner().getPlayerColor().getColor().equals(Color.black)) ? Color.white : Color.black;
			if (!helper.getGraphicsColor(player.getPlayerColor()).equals(bgcolor)) {
				graphics.setColor( helper.getGraphicsColor(player.getPlayerColor()) );
				graphics.setFont(bigFont);
				graphics.drawString(playerVP, margin, maxDescent);
				graphics.drawString(displayName, paintHelper.maxScoreWidth + margin, maxDescent);
			}
			else {
				if (bgcolor.equals(Color.white))
					graphics.setColor(Color.black);
				else
					graphics.setColor(Color.white);
				graphics.setFont(bigFont);
				drawOutlineText(bigFont, playerVP, graphics, margin, maxDescent);
				drawOutlineText(bigFont, displayName, graphics, paintHelper.maxScoreWidth + margin, maxDescent);


			}
			playerNameDirty = false;
		}
	}

	private void drawOutlineText(Font bigFont, char displayName,
			Graphics2D graphics, int x, int y) {
		drawOutlineText(bigFont, Character.toString(displayName), graphics, x, y);
	}
	private void drawOutlineText(Font bigFont, String displayName,
			Graphics2D graphics, int x, int y) {

		FontRenderContext frc = graphics.getFontRenderContext();
		TextLayout tl = new TextLayout(displayName, bigFont, frc);
		AffineTransform transform = new AffineTransform();
		transform.setToTranslation(x , y);
		Shape shape = tl.getOutline(transform);
		graphics.draw(shape);
	}
	private int getLineHeight() {
		if (game.getNumberOfPlayers() < 4)
			return frameHeight / 4;
		else
			return frameHeight / game.getNumberOfPlayers();
	}
}
