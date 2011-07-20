import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

class Player implements Comparable<Player> {

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
    
    public Player(PlayerColor color, AtomicInteger sharedCount) {
        this.achievements = new HashSet<Achievement>();
        this.vp_times = new int[14];
        this.color = color;
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
    public void updateVP(int vp_delta) {
        synchronized(vp_lock) {
            this.vp += vp_delta;
            if (getVP() < 2) {
                this.vp = 2;
            }
            else if (getVP() > 10) {
                this.vp = 10 - getAchievementsVP();
            }
            else {
                raisePlayerVPChangedEvent();
                // if the player points has changed, best update vp_times
                if (vp_delta != -1) {
                    this.vp_times[getVP()] = sharedCount.getAndIncrement();
                }
            }
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
        return achievements.size()*2;
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

    public void add(Achievement a) {
        achievements.add(a);
        vp_times[getVP()] = sharedCount.getAndIncrement();
        raisePlayerVPChangedEvent();
    }
    public void remove(Achievement a) {
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
        return ((this == o) || ((o instanceof Player) && (((Player)o).color == this.color)));
    }

    public int hashCode() {
        return color.hashCode();
    }

    public void addPlayerListener(PlayerListener p) {
        this.playerListeners.add(p);
    }

    public void removeGameListener(PlayerListener p) {
        this.playerListeners.remove(p);
    }


    private void raisePlayerVPChangedEvent() {
            synchronized(playerListeners) {
                for (PlayerListener p: playerListeners) {
                    p.playerVPChanged(new PlayerEvent(this));
                }
            }
    }
    private void raisePlayerRenamedEvent() {
            synchronized(playerListeners) {
                for (PlayerListener p: playerListeners) {
                    p.playerRenamed(new PlayerEvent(this));
                }
            }
    }
    public void raisePlayerRankChangedEvent() {
            synchronized(playerListeners) {
                for (PlayerListener p: playerListeners) {
                    p.playerRankChanged(new PlayerEvent(this));
                }
            }
    }
}

