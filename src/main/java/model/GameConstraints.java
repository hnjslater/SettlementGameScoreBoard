package model;
public interface GameConstraints {
    public void updateVP(Player p, int vp_delta) throws RulesBrokenException;
    public void gainAchievement(Player p, Achievement a) throws RulesBrokenException;
    public void looseAchievement(Player p, Achievement a) throws RulesBrokenException;
}
