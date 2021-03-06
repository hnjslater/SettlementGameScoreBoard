package ui.setupscreen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import model.Achievement;
import model.Game;
import model.GameOptions;
import model.PlayerColor;
import model.RulesBrokenException;
import ui.Controller;
import webservice.Webservice;


public class SetupScreen {


	private Controller controller;
	private Game game;
	private final Webservice webservice;

	private Object mainLock;
	private boolean running;
	private Map<PlayerColor, JCheckBox> playerPlaying;
	private Map<PlayerColor, JTextField> playerNames;
	private Map<Achievement, JCheckBox> achievementPlaying;
	private JTextField winningVP;
	private boolean firstRun;
	public SetupScreen(Controller controller, Webservice webservice, Game game, GameOptions options) {
		this.controller = controller;
		this.game = game;
		this.webservice = webservice;
		
		
		this.mainLock = new Object();
		this.playerPlaying = new TreeMap<PlayerColor, JCheckBox>();		
		this.playerNames = new TreeMap<PlayerColor, JTextField>();
		for (PlayerColor pc : options.getPlayerColors()) {
			playerPlaying.put(pc, null);
			playerNames.put(pc, null);
		}
		this.achievementPlaying = new TreeMap<Achievement, JCheckBox>();
		for (Achievement a : options.getAchievements() ) {
			achievementPlaying.put(a, null);
		}
		this.firstRun = true;
	}

	public void run() {
		controller.setFullScreen(false);
		this.running = true;

		JPanel main = new JPanel(new BorderLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.NORTH;

		GridBagConstraints panelConstraints = (GridBagConstraints) gbc.clone();
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.weighty = 1;


		JButton doneButton;
		if (firstRun)
			doneButton = new JButton("Begin Playing");
		else
			doneButton = new JButton("Resume Playing");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				done();
			}
		});
		
		JPanel left = new JPanel(new GridBagLayout());
		JPanel right = new JPanel(new GridBagLayout());
		JPanel bottom = new JPanel();
		

		left.add(makeGamePanel(), panelConstraints);
		left.add(makeAchievementsPanel(), panelConstraints);
		left.add(makePlayerPanel(), panelConstraints);
		right.add(new WebServicePanel(webservice), panelConstraints);
		
		bottom.add(doneButton);
		
		main.add(bottom, BorderLayout.SOUTH);
		main.add(left, BorderLayout.WEST);
		main.add(right, BorderLayout.EAST);
		

		controller.getJFrame().setContentPane(main);
		controller.getJFrame().pack();

		while (running) {
			try {
				synchronized(mainLock) {
					mainLock.wait();
				}
			}
			catch (InterruptedException ex) {
				// Don't mind being interrupted.
			}
		}
		controller.getJFrame().getContentPane().removeAll();
		controller.setFullScreen(true);
		firstRun = false;
	}

	private JPanel makePlayerPanel() {
		JPanel players = new JPanel(new GridBagLayout());
		players.setBorder(BorderFactory.createTitledBorder("Players"));

		List<PlayerColor> playerColors = new ArrayList<PlayerColor>(this.playerNames.keySet());

		Collections.sort(playerColors, new Comparator<PlayerColor>() {
			@Override
			public int compare(PlayerColor o1, PlayerColor o2) {
				// TODO Auto-generated method stub
				return o1.toString().compareTo(o2.toString());
			}
		});

		for (PlayerColor pc : playerColors) {
			final JCheckBox checkbox = new JCheckBox(pc.toString());
			final JTextField textfield = new JTextField(pc.toString(), 30);

			if (game.getPlayer(pc) != null) {
				checkbox.setSelected(true);
				textfield.setText(game.getPlayer(pc).getName());
				textfield.setEnabled(true);
			}
			else {
				checkbox.setSelected(false);
				textfield.setEnabled(false);
			}

			checkbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					textfield.setEnabled(checkbox.isSelected());
				}
			});
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.WEST;
				gbc.weightx = 40;
				players.add(checkbox, gbc);
			}
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridwidth = GridBagConstraints.REMAINDER;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 60;
				players.add(textfield, gbc);
			}

			playerPlaying.put(pc, checkbox);
			playerNames.put(pc, textfield);

		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weighty = 1;
			players.add(new JPanel(), gbc);
		}
		return players;
	}

	private JPanel makeGamePanel() {	
		JPanel gamePanel = new JPanel(new GridBagLayout());
		
		gamePanel.setBorder(BorderFactory.createTitledBorder("Game Properties"));

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 40;
			gbc.anchor = GridBagConstraints.WEST;
			gamePanel.add(new JLabel("Winning Victory Points"), gbc);
		}

		final JTextField winningVP = new JTextField(3);
		final JSlider slider = new JSlider(8,24);

		this.winningVP = winningVP;
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(0, 10, 0, 10);
			gbc.weightx = 10;
			gamePanel.add(winningVP, gbc);	
		}


		final ChangeListener sliderListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				winningVP.setText(Integer.toString(slider.getValue()));
			}
		};

		// Change the color of the background of the VP textbox if a non-number is entered
		winningVP.getDocument().addDocumentListener(new DocumentListener() {
			private Color originalColor = winningVP.getBackground();
			public void textChanged() {
				try {
					int max = Integer.parseInt(winningVP.getText());
					winningVP.setBackground(originalColor);
					slider.removeChangeListener(sliderListener);
					slider.setValue(max);
					slider.addChangeListener(sliderListener);

				}
				catch (Exception ex) {
					winningVP.setBackground(Color.PINK);
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				textChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				textChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				textChanged();	
			}
		});




		slider.addChangeListener(sliderListener);

		slider.setValue(game.getWinningVP());
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);
		slider.setSnapToTicks(true);

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.weightx = 50;

			gamePanel.add(slider, gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weighty = 1;
			gamePanel.add(new JPanel(), gbc);
		}

		return gamePanel;
	}

	private JPanel makeAchievementsPanel() {	
		JPanel achievementsPanel = new JPanel(new GridBagLayout());
		achievementsPanel.setBorder(BorderFactory.createTitledBorder("Achievements"));

		for (Achievement a : achievementPlaying.keySet())
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			JCheckBox checkbox = new JCheckBox(a.getName());
			if (game.getAchievements().contains(a))
				checkbox.setSelected(true);
			achievementsPanel.add(checkbox, gbc);
			achievementPlaying.put(a, checkbox);
		}
		
		return achievementsPanel;
	}
	
		
	public void done() {
		for (PlayerColor pc : this.playerPlaying.keySet()) {
			// is the player in the game right now?
			boolean playerCurrent = (game.getPlayer(pc) != null);

			// should they be?
			boolean playerShould = playerPlaying.get(pc).isSelected();

			// if the player isn't in the game, get them in there.
			if (!playerCurrent && playerShould) {
				try {
					game.addPlayer(pc);
				}
				catch (RulesBrokenException ex) {
					throw new RuntimeException(ex);
				}
			}
			// if they're playing and they shouldn't be, pull them out
			else if (playerCurrent && !playerShould) {
				game.removePlayer(game.getPlayer(pc));
			}
			// now that's sorted, best set their name.
			if (playerShould) {
				game.getPlayer(pc).setName(playerNames.get(pc).getText());
			}
		}
		try {
			game.setWinningVP(Integer.parseInt(winningVP.getText()));
			stop();
		}
		catch (NumberFormatException ex) {
		}
		for (Achievement a : this.achievementPlaying.keySet()) {
			boolean achievementCurrent = (game.getAchievements().contains(a));
			boolean achievementShould = achievementPlaying.get(a).isSelected();
			if (!achievementCurrent && achievementShould) {
				game.getAchievements().add(a);
			}
			else if (achievementCurrent && !achievementShould) {
				game.getAchievements().remove(a);
			}
		}
	}

	public void stop() {
		synchronized(mainLock) {
			running = false;
			mainLock.notifyAll();
		}
	}


}
