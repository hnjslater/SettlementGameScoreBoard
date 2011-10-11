
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


 
public class Game implements PlayerListener, GameConstraints {
    private List<GameListener> gameListeners;
    private Player winner;
    private Map<PlayerColor,Player> playersByColor;
    private AtomicInteger sharedCount;
    private int maxVP = 10;
    private PlayerFactory playerFactory;
    private final AchievementCollection achievements;

    public Game() {
    	this.achievements = new AchievementCollection();
        this.playersByColor = new HashMap<PlayerColor,Player>();
        this.gameListeners = Collections.synchronizedList(new ArrayList<GameListener>());
        this.sharedCount = new AtomicInteger();
        this.playerFactory = new PlayerFactory() {
            public Player createPlayer(PlayerColor pc, AtomicInteger i, GameConstraints gc) {
                return new Player(pc, i , gc);
            }
        };
    }
    public void addPlayer(PlayerColor color) throws RulesBrokenException {
        if (this.playersByColor.containsKey(color))
            throw new RulesBrokenException("Already have a player of that color");
        Player player = playerFactory.createPlayer(color, sharedCount, this);
        player.addPlayerListener(this);
        playersByColor.put(color,player);
        raisePlayerAddedEvent(new GameEvent(this, player));
    }
    public void setPlayerFactory(PlayerFactory pf) {
	this.playerFactory = pf;
    }
    public void removePlayer(Player player) {
        if (Player.equals(getWinner(), player)) {
            winner = null;
            raiseWinnerChangedEvent(new GameEvent(this, null));
        }
        playersByColor.remove(player.getPlayerColor());
        raisePlayerRemovedEvent(new GameEvent(this, player));
    }
    public List<Player> getLeaderBoard() {
        List<Player> players = new ArrayList<Player>(this.playersByColor.values());

        Collections.sort(players);
        return players;
    }
    public Player getPlayer(PlayerColor c) {
        return playersByColor.get(c);
    }
    public int getNumberOfPlayers() {
        return playersByColor.size();
    }
    public Set<PlayerColor> getPlayerColors() {
    	return this.playersByColor.keySet();
    }
    public AchievementCollection getAchievements() {
    	return this.achievements;
    }
    public PlayerColor getPlayerColor(String color) {
    	for (PlayerColor pc : getPlayerColors()) {
    		if (pc.getName().equalsIgnoreCase(color)) {
    			return pc;
    		}
    	}
		return null;
    }

    public void gainAchievement(Player player, Achievement achievement) throws RulesBrokenException {
    	if (achievement.getMaxInGame() == 1) {
    		synchronized(playersByColor) {
    			for (Player p : playersByColor.values()) {
    				p.remove(achievement);
    			}
    		}
    	}
    }
    public List<Player> getPlayers(Achievement achievement) {
    	List<Player> matches = new ArrayList<Player>();
    	synchronized(playersByColor) {
			for (Player p : playersByColor.values()) {
				if (p.getAchievements().contains(achievement))
					matches.add(p);
			}
    	}
    	return matches;
    }
    public void looseAchievement(Player player, Achievement achievement) {
    }
    public void addGameListener(GameListener g) {
        this.gameListeners.add(g);
    }
    public void removeGameListener(GameListener g) {
        this.gameListeners.remove(g);
    }

    public Player getWinner() {
        return this.winner;
    }
    public void setWinningVP(int VP) {
        this.maxVP = VP;
    }
    public int getWinningVP() {
        return this.maxVP;
    }
    

    public void playerVPChanged(PlayerEvent pe) {
        Player p = pe.getPlayer();
        if (p.getVP() >= maxVP && this.winner == null) {
            this.winner = p;
            raiseWinnerChangedEvent(new GameEvent(this,p));

        }
        else if (p.getVP() < maxVP && p.equals(this.winner)) {
            this.winner = null;
            raiseWinnerChangedEvent(new GameEvent(this,null));

        }
        for (Player i : playersByColor.values()) {
            i.raisePlayerRankChangedEvent();
        }
    }
    public void playerRenamed(PlayerEvent pe) {
    }
    public void playerRankChanged(PlayerEvent pe) {
    }
    private void raiseWinnerChangedEvent(final GameEvent wce) {
       Thread t = new Thread(new Runnable() {
               public void run() {
                       synchronized(gameListeners) {
                               for (GameListener g : gameListeners) {
                                       g.winnerChanged(wce);
                               }
                       }
               }
       }
       );
       t.run();
    }
    private void raisePlayerAddedEvent(final GameEvent wce) {
       Thread t = new Thread(new Runnable() {
               public void run() {
                       synchronized(gameListeners) {
                               for (GameListener g : gameListeners) {
                                       g.playerAdded(wce);
                               }
                       }
               }
       });
       t.run();
    }
    private void raisePlayerRemovedEvent(final GameEvent wce) {
       Thread t = new Thread(new Runnable() {
               public void run() {
                       synchronized(gameListeners) {
                               for (GameListener g : gameListeners) {
                                       g.playerRemoved(wce);
                               }
                       }
               }
       });
       t.run();
     }


    public void updateVP(Player p, int new_vp) {
        if (getWinner() != null) {
            if (!getWinner().equals(p) || new_vp > p.getVP())
                throw new RuntimeException("The game has finished.");
        }
        if (new_vp < 2)
            throw new RuntimeException("Settlement VPs cannot be less than 2.");

    }
}

