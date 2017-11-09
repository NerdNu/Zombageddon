package nu.nerd.zombageddon;

import org.bukkit.Location;


public class MathUtil {


    /**
     * Get the squared 2D distance (x,z) between two Location objects.
     * The Bukkit Location.distance() checks all three dimensions, and
     * also uses a costly square root function.
     */
    public static double distance2DSquared(Location loc1, Location loc2) {
        return square(loc1.getX() - loc2.getX()) - square(loc1.getZ() - loc2.getZ());
    }


    /**
     * Square a double
     */
    public static double square(double n) {
        return n * n;
    }


}
