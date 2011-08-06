package ui.setupscreen;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.Game;
import model.PlayerColor;
import ui.Controller;


public class SetupScreen {


    private Controller controller;
    private Game game;
    private Object mainLock;
    private boolean running;
    private int winningVP;
    private Map<PlayerColor, JCheckBox> playerPlaying;
    private Map<PlayerColor, JTextField> playerNames;
    public SetupScreen(Controller controller, Game game) {
        this.controller = controller;
        this.game = game;
        this.mainLock = new Object();
        this.winningVP = game.getWinningVP();
        this.playerPlaying = new HashMap<PlayerColor, JCheckBox>();
        this.playerNames = new HashMap<PlayerColor, JTextField>();
    }

    public void run() {
        this.running = true;
        controller.setFullScreen(false);

        
        final JLabel maxVP = new JLabel(Integer.toString(game.getWinningVP()), JLabel.CENTER);

        JButton plusButton = new JButton("+");
        plusButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                winningVP++;
                maxVP.setText(Integer.toString(winningVP));
            }
        });
        JButton minusButton = new JButton("-");
        minusButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (winningVP > 2) {
                    winningVP--;
                    maxVP.setText(Integer.toString(winningVP));
                }
            }
        });
        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                done();
            }
        });


        JPanel centre = new JPanel(new GridBagLayout());
        centre.add(new JLabel("Winning Victory Points"));
        centre.add(minusButton);
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.anchor = GridBagConstraints.CENTER;
            centre.add(maxVP,gbc);
        }

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            centre.add(plusButton, gbc);
        }
    



        for (PlayerColor pc : PlayerColor.values()) {
            if (!pc.equals(PlayerColor.None)) {
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
                    centre.add(checkbox, gbc);
                }
                {
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridwidth = GridBagConstraints.REMAINDER;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    centre.add(textfield, gbc);
                }

                playerPlaying.put(pc, checkbox);
                playerNames.put(pc, textfield);
            }
        }

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            centre.add(doneButton, gbc);
        }
        controller.getJFrame().setContentPane(centre);
        controller.getJFrame().pack();

        while (running) {
            try {
                synchronized(mainLock) {
                    mainLock.wait();
                }
            }
            catch (Exception ex) {
            }
        }
        controller.setFullScreen(true);
    }

    public void done() {
        for (PlayerColor pc : PlayerColor.values()) {
            if (!pc.equals(PlayerColor.None)) {
                // is the player in the game right now?
                boolean playerCurrent = (game.getPlayer(pc) != null);

                // should they be?
                boolean playerShould = playerPlaying.get(pc).isSelected();

                // if the player isn't in the game, get them in there.
                if (!playerCurrent && playerShould) {
                    game.addPlayer(pc);
                }
                // if they're playing and they shouldn't be, pull them out
                else if (playerCurrent && !playerShould) {
                    game.removePlayer(pc);
                }
                // now that's sorted, best set their name.
                if (playerShould) {
                    game.getPlayer(pc).setName(playerNames.get(pc).getText());
                }

                game.setWinningVP(winningVP);
            }
        }
        stop();
    }

    public void stop() {
        synchronized(mainLock) {
            running = false;
            mainLock.notifyAll();
        }
    }
}
