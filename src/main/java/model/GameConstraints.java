package model;
public interface GameConstraints {
    public void updateVP(Player p, int vp_delta) throws RulesBrokenException;
}
