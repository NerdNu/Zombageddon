package nu.nerd.zombageddon;

import org.bukkit.Location;

import java.util.UUID;


public class ZombieMeta {


    private UUID id;
    private Location frustLoc;
    private int frustTicks;
    private long lastBlockMillis;
    private TargetBreakable wallTarget;


    public ZombieMeta(UUID id) {
        this.id = id;
        this.frustLoc = null;
        this.frustTicks = 0;
        this.lastBlockMillis = 0;
        this.wallTarget = null;
    }


    public boolean isFrustrated() {
        return frustTicks > 100;
    }


    public UUID getId() {
        return id;
    }


    public Location getFrustLoc() {
        return frustLoc;
    }


    public void setFrustLoc(Location frustLoc) {
        this.frustLoc = frustLoc;
    }


    public int getFrustTicks() {
        return frustTicks;
    }


    public void setFrustTicks(int frustTicks) {
        this.frustTicks = frustTicks;
    }


    public void incrementFrustTicks() {
        this.frustTicks++;
    }


    public long getLastBlockMillis() {
        return lastBlockMillis;
    }


    public void setLastBlockMillis(long lastBlockMillis) {
        this.lastBlockMillis = lastBlockMillis;
    }


    public TargetBreakable getWallTarget() {
        return wallTarget;
    }


    public void setWallTarget(TargetBreakable wallTarget) {
        this.wallTarget = wallTarget;
    }


}
