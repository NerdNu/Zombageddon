package nu.nerd.zombageddon;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;


/**
 * Responsible for ensuring that players are constantly swarmed with zombie hordes.
 */
public class HordeSpawner extends BukkitRunnable {


    private Zombageddon plugin;
    private Random rand;


    public HordeSpawner(Zombageddon plugin) {
        this.plugin = plugin;
        this.rand = new Random();
        this.runTaskTimer(plugin, plugin.CONFIG.SPAWNING_FREQUENCY, plugin.CONFIG.SPAWNING_FREQUENCY);
    }


    public void run() {
        if (!plugin.CONFIG.SPAWNING) return;
        int max = plugin.CONFIG.SPAWNING_RADIUS_MAX;
        for (World world : plugin.getServer().getWorlds()) {
            if (!plugin.CONFIG.worldEnabled(world)) continue;
            if (world.getLivingEntities().size() >= plugin.CONFIG.SPAWNING_CAP) continue;
            for (Player player : world.getPlayers()) {
                if (!player.getGameMode().equals(GameMode.SURVIVAL)) continue;
                if (zombiesNearbyExceedCap(player)) continue;
                int nearbyPlayers = getNearbyPlayerCount(player, max) + 1;
                int adjustedAttempts = Math.round(plugin.CONFIG.SPAWNING_ATTEMPTS / nearbyPlayers);
                if (adjustedAttempts < 1) adjustedAttempts = 1;
                for (int i = 0; i < adjustedAttempts; i++) {
                    attemptSpawn(player);
                }
            }

        }
    }


    /**
     * Attempt a random zombie spawn in a range surrounding a player
     */
    public void attemptSpawn(Player player) {

        int min = plugin.CONFIG.SPAWNING_RADIUS_MIN;
        int max = plugin.CONFIG.SPAWNING_RADIUS_MAX;

        // choose random x,z coordinates within the allowed radius range
        int distance = rand.nextInt((max - min) + 1) + min;
        double angle = 2 * Math.PI * rand.nextDouble();
        double deltaX = distance * Math.cos(angle);
        double deltaZ = distance * Math.sin(angle);
        Location loc = player.getLocation().clone();
        loc.setX(player.getLocation().getX() + deltaX);
        loc.setZ(player.getLocation().getZ() + deltaZ);

        // pick a new y coordinate if the same level is not possible
        if (!isSpawnable(loc)) {
            int high = (loc.getBlockY() + 10 >= 255) ? 254 : loc.getBlockY() + 10;
            int low = (loc.getBlockY() - 10 <= 0) ? 1 : loc.getBlockY() - 10;
            for (int i = high; i >= low; i--) {
                loc.setY(i);
                if (!isSpawnable(loc)) return;
            }
        }

        // do the spawn
        player.getWorld().spawnEntity(loc, EntityType.ZOMBIE);

    }


    /**
     * Returns true if a given block location is suitable to spawn on
     */
    public boolean isSpawnable(Location loc) {
        Block block = loc.getBlock();
        Block under = loc.getBlock().getRelative(BlockFace.DOWN);
        Block above = loc.getBlock().getRelative(BlockFace.UP);
        if (block.getType().equals(Material.AIR) && !under.getType().equals(Material.AIR) && above.getType().equals(Material.AIR)) {
            if (block.getLightLevel() <= plugin.CONFIG.SPAWNING_LIGHT) {
                return true;
            }
        }
        return false;
    }


    public int getNearbyPlayerCount(Player player, int dist) {
        int count = 0;
        for (Entity ent : player.getNearbyEntities(dist, dist, dist)) {
            if (ent.getType().equals(EntityType.PLAYER)) count++;
        }
        return count;
    }


    /**
     * Returns true if the number of zombies in the outer spawning radius from a player exceed the local cap
     * defined in spawning_nearby_cap.
     */
    public boolean zombiesNearbyExceedCap(Player player) {
        int zombies = 0;
        double dist = plugin.CONFIG.SPAWNING_RADIUS_MAX / 2;
        for (Entity ent : player.getNearbyEntities(dist, dist, dist)) {
            if (ent.getType().equals(EntityType.ZOMBIE)) zombies++;
        }
        return zombies >= plugin.CONFIG.SPAWNING_NEARBY_CAP;
    }


}
