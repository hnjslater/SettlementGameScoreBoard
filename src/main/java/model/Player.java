package model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Player implements Comparable<Player> {

    /** this array records the game.getChangeNo() for each VP level.
        This allows the compareTo to sort within each VP level fairly.
        For example imagine the scoreboard looks like this:
            2VP Alice
            2VP Bob
        Alice gains a victory point so Alice.updateVP(1) is called. As a result, Alice.vp_times[3] will be set to game.getChangeNo() (0).
        Bob gains a victory point so Bob.updateVP(1) is called, As a result, Bob.vp_times[3] will be set to game.getChangeNo() (1).
        Sorting the players will result in a call to Alice.compareTo(Bob) which will first compare VPs (equal) and then compare vp_times[3].
        As Alice has a lower vp_times[3], she will be placed above Bob, which seems fair as she got to 3VP first. */
    private int[] vp_times;
    private Set<Achievement> achievements;
    private String name = "";
    private Integer vp;
    private Object vp_lock;
    private PlayerColor color;
    private List<PlayerListener> playerListeners;
    private AtomicInteger sharedCount;
    private GameConstraints constraints;
    
    public Player(PlayerColor color, AtomicInteger sharedCount, GameConstraints constraints) {
        this.achievements = new HashSet<Achievement>();
        this.vp_times = new int[200];
        this.color = color;
        this.constraints = constraints; 
        this.playerListeners = Collections.synchronizedList(new ArrayList<PlayerListener>());
        this.sharedCount = sharedCount;
        this.vp = 2;
        this.vp_lock = new Object();
        resetName();
    }

    public void setName(String name) {
        this.name = name;
        raisePlayerRenamedEvent();
    }
    public String getName() {
        return this.name;
    }
    public void resetName() {
        setName(color.toString());
    }

    /** Updates the this.vp and vp_times (unless VP == -1). */ 
    public void setVP(int newVP) throws RulesBrokenException {
        synchronized(vp_lock) {
            constraints.updateVP(this, newVP);
            this.vp = newVP;
            // if the player points has changed, best update vp_times
            if (newVP != -1) {
                this.vp_times[getVP()] = sharedCount.getAndIncrement();
            }
            
            raisePlayerVPChangedEvent();
           
        }
    }

    public int getVP() {
        synchronized(vp_lock) {
            return vp + getAchievementsVP();
        }
    }

    public int getSettlementVP() {
        return vp;
    }

    private int getAchievementsVP() {
    	int achievementsVP = 0;
    	synchronized(achievements) {
    		for (Achievement a : achievements) {
    			achievementsVP += a.getVictoryPoints();
    		}
    	}
    	return achievementsVP;
    }

    public int compareTo(Player p) {
        if (this.getVP() < p.getVP()) {
            return 1;
        }
        else if (this.getVP() > p.getVP()) {
            return -1;
        }
        else if (this.vp_times[getVP()] < p.vp_times[getVP()]) {
            return -1;
        }
        else if (this.vp_times[getVP()] > p.vp_times[getVP()]) {
            return 1;
        }
        else {
            return color.toString().compareTo(p.color.toString());
        }

    }

    
    public void add(Achievement a) throws RulesBrokenException {
    	this.constraints.gainAchievement(this, a);
        achievements.add(a);
        vp_times[getVP()] = sharedCount.getAndIncrement();
        
        raisePlayerVPChangedEvent();
    }
    public void remove(Achievement a) throws RulesBrokenException {
    	this.constraints.looseAchievement(this, a);
        achievements.remove(a);
        vp_times[getVP()] = -sharedCount.getAndIncrement();
        raisePlayerVPChangedEvent();
    }

    public PlayerColor getPlayerColor() {
        return this.color;
    }

    public Set<Achievement> getAchievements() {
        return Collections.unmodifiableSet(achievements);
    }

    public boolean equals(Object o) {
        return ((this == o) || ((o instanceof Player) && (((Player)o).getPlayerColor() == this.getPlayerColor())));
    }

    public static boolean equals(Player p1, Player p2) {
        return ((p1 == p2) || (p1 != null) && (p2 != null) && (p1.getPlayerColor() == p2.getPlayerColor()));
    }

    public int hashCode() {
        return color.hashCode();
    }

    public void addPlayerListener(PlayerListener p) {
        this.playerListeners.add(p);
    }

    public void removePlayerListener(PlayerListener p) {
        this.playerListeners.remove(p);
    }


    private void raisePlayerVPChangedEvent() {
    	final PlayerEvent pe = new PlayerEvent(this);

    	Thread t = new Thread(new Runnable() {
    		@Override    		
    		public void run() {
    			synchronized(playerListeners) {
    				for (PlayerListener p: playerListeners) {
    					p.playerVPChanged(pe);
    				}
    			}
    		}
    	});
    	t.run();
    }
    private void raisePlayerRenamedEvent() {
       final PlayerEvent pe = new PlayerEvent(this);
       Thread t = new Thread(new Runnable() {

    	   @Override    		
    	   public void run() {
    		   synchronized(playerListeners) {
    			   for (PlayerListener p: playerListeners) {
    				   p.playerRenamed(pe);
    			   }
    		   }
    	   }
       });
       t.run();
    }
    void raisePlayerRankChangedEvent() {
    	final PlayerEvent pe = new PlayerEvent(this);
    	Thread t = new Thread(new Runnable() {
    		public void run() {
    			synchronized(playerListeners) {
    				for (PlayerListener p: playerListeners) {
    					p.playerRankChanged(pe);
    				}
    			}
    		}
    	});
        t.run();
    }

    public String toString() {
    	return "Player " + "(" + color.toString() + ")";
    }
}

