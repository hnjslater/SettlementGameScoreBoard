
package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


 
public class Game implements PlayerListener, GameConstraints {
    private List<GameListener> gameListeners;
    private Player winner;
    private Map<PlayerColor,Player> playersByColor;
    private Map<Achievement,Player> playersByAchievement;
    private AtomicInteger sharedCount;
    private int maxVP = 10;
    private PlayerFactory playerFactory;
    private final Set<PlayerColor> colors;
    private final AchievementCollection achievements;

    public Game(Collection<PlayerColor> colors, Collection<Achievement> achievements) {
    	this.colors = Collections.unmodifiableSet(new HashSet<PlayerColor>(colors));
    	this.achievements = new AchievementCollection(achievements);
    	
    	
        this.playersByColor = new HashMap<PlayerColor,Player>();
        this.playersByAchievement = new HashMap<Achievement,Player>();
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
        for (Achievement a : player.getAchievements())
            playersByAchievement.remove(a);
        raisePlayerRemovedEvent(new GameEvent(this, player));
    }
    public List<Player> getLeaderBoard() {
        List<Player> players = new ArrayList<Player>(this.playersByColor.values());

        Collections.sort(players);
        return players;
    }
    public Player getPlayer(Achievement a) {
        return playersByColor.get(playersByAchievement.get(a));
    }
    public Player getPlayer(PlayerColor c) {
        return playersByColor.get(c);
    }
    public int getNumberOfPlayers() {
        return playersByColor.size();
    }
    public Set<PlayerColor> getColors() {
    	return this.colors;
    }
    public AchievementCollection getAchievements() {
    	return this.achievements;
    }
    public PlayerColor getPlayerColor(String color) {
    	for (PlayerColor pc : getColors()) {
    		if (pc.getName().equalsIgnoreCase(color)) {
    			return pc;
    		}
    	}
		return null;
    }

    public void gainAchievement(Player player, Achievement achievement) throws RulesBrokenException {
            if (playersByAchievement.containsKey(achievement)) {
                playersByAchievement.get(achievement).remove(achievement);
            }
            playersByAchievement.put(achievement,player);
    }
    public Player getAchievement(Achievement achievement) {
        if (playersByAchievement.containsKey(achievement))
            return playersByAchievement.get(achievement);
        else
            return null;
    }
    public void looseAchievement(Player player, Achievement achievement) {
        playersByAchievement.remove(achievement);
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

