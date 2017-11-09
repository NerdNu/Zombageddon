package nu.nerd.zombageddon;

import org.bukkit.Location;


public class MathUtil {


    /**
     * Get the squared 2D distance (x,z) between two Location objects.
     * The Bukkit Location.distance() checks all three dimensions, and
     * also uses a costly square root function.
     */
    public static double distance2DSquared(Location loc1, Location loc2) {
        return Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2);
    }


}
