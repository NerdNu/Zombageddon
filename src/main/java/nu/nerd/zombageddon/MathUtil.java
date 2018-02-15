package nu.nerd.zombageddon;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class MathUtil {


    /**
     * Get the squared 2D distance (x,z) between two Location objects.
     * The Bukkit Location.distance() checks all three dimensions, and
     * also uses a costly square root function.
     */
    public static double distance2DSquared(Location loc1, Location loc2) {
        return square(loc1.getX() - loc2.getX()) + square(loc1.getZ() - loc2.getZ());
    }


    /**
     * Square a double
     */
    public static double square(double n) {
        return n * n;
    }


    /**
     * Find the nearest player in a radius from the specified entity
     */
    public static Player nearestPlayer(Entity entity, double radius) {
        Player player = null;
        double shortest = (radius * radius) + 1;
        double distSq;
        for (Player p : entity.getWorld().getPlayers()) {
            if (p.isDead() || !p.getGameMode().equals(GameMode.SURVIVAL)) continue;
            if (!entity.getWorld().equals(p.getWorld())) continue;
            distSq = entity.getLocation().distanceSquared(p.getLocation()); //3D distance
            if (distSq < shortest) {
                player = p;
                shortest = distSq;
            }
        }
        return player;
    }


}
