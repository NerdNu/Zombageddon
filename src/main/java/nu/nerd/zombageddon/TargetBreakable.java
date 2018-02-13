package nu.nerd.zombageddon;

import org.bukkit.block.Block;


public class TargetBreakable {


    private Block block;
    private int ticks;
    private long lastTouched;


    public TargetBreakable(Block block) {
        this.block = block;
        this.ticks = 0;
    }


    public Block getBlock() {
        return block;
    }


    public int getTicks() {
        return ticks;
    }


    public void incrementTicks() {
        ticks++;
    }


    public int getSeconds() {
        return ticks / 20;
    }


    public void updateLastTouched() {
        lastTouched = System.currentTimeMillis();
    }


    public long getLastTouched() {
        return lastTouched;
    }


}
