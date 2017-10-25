package nu.nerd.zombageddon;


import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class ZombieMotivator extends BukkitRunnable {


    private Zombageddon plugin;
    private Map<UUID, Integer> timeMap;
    private Map<UUID, Location> locMap;
    private Map<UUID, Long> lastBlockMap;


    public ZombieMotivator(Zombageddon plugin) {
        this.plugin = plugin;
        this.timeMap = new HashMap<UUID, Integer>();
        this.locMap = new HashMap<UUID, Location>();
        this.lastBlockMap = new HashMap<UUID, Long>();
        this.runTaskTimer(plugin, 5L, 5L);
    }


    public void run() {
        for (Zombie zombie : getEligibleZombies()) {
            incrementFrustrationTime(zombie);
            tryPillaring(zombie);
        }
    }


    /**
     * Load a Set of eligible zombies that are currently targeting a Player
     */
    private Set<Zombie> getEligibleZombies() {
        Set<Zombie> eligibleZombies = new HashSet<Zombie>();
        for (World world : plugin.getServer().getWorlds()) {
            if (!plugin.CONFIG.worldEnabled(world)) continue;
            for (LivingEntity entity : world.getLivingEntities()) {
                if (isZombieChasingPlayer(entity)) {
                    eligibleZombies.add((Zombie) entity);
                } else {
                    timeMap.remove(entity.getUniqueId());
                    locMap.remove(entity.getUniqueId());
                    lastBlockMap.remove(entity.getUniqueId());
                }
            }
        }
        return eligibleZombies;
    }


    /**
     * Check if a given entity is a zombie targeting a player
     */
    private boolean isZombieChasingPlayer(LivingEntity entity) {
        if (entity.getType().equals(EntityType.ZOMBIE)) {
            Zombie zombie = (Zombie) entity;
            if (zombie.getTarget() != null && zombie.getTarget().getType().equals(EntityType.PLAYER)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Add ticks to timeMap if the zombie is still in the same place as the last time around
     */
    private void incrementFrustrationTime(Zombie zombie) {
        Location loc;
        if (!timeMap.containsKey(zombie.getUniqueId())) {
            timeMap.put(zombie.getUniqueId(), 0);
            locMap.put(zombie.getUniqueId(), zombie.getLocation());
        } else {
            loc = locMap.get(zombie.getUniqueId());
            if (loc.distance(zombie.getLocation()) <= 1.0) {
                timeMap.put(zombie.getUniqueId(), timeMap.get(zombie.getUniqueId()) + 5);
                locMap.put(zombie.getUniqueId(), zombie.getLocation());
            }
        }
    }


    private void tryPillaring(Zombie zombie) {

        int time = timeMap.get(zombie.getUniqueId());
        long lastPlaced = (lastBlockMap.containsKey(zombie.getUniqueId())) ? lastBlockMap.get(zombie.getUniqueId()) : 0;

        if (time < 200) return; //under 200 ticks of frustration
        if (zombie.getLocation().getBlockY() >= zombie.getTarget().getLocation().getBlockY()) return; //on or higher than the target's Y
        if ((System.currentTimeMillis() - lastPlaced) < 1000) return; //block place cooldown

        doPillar(zombie);

    }


    /**
     * Make the zombie jump up and place a block
     */
    private void doPillar(Zombie zombie) {

        Location loc = zombie.getLocation();
        Block block = loc.getBlock();
        Block blockBelow = loc.subtract(0, 1, 0).getBlock();
        Block blockAbove = loc.add(0, 1, 0).getBlock();
        Material material = Material.LEAVES;

        //Don't place a block if the zombie is falling/otherwise in midair
        if (blockBelow == null || blockBelow.getType().equals(Material.AIR)) {
            return;
        }

        //Don't pillar if there's an obstruction
        if (blockAbove != null && !blockAbove.getType().equals(Material.AIR)) {
            return;
        }

        //Use sponge if the zombie is standing in water
        if (blockBelow.getType().equals(Material.WATER) || blockBelow.getType().equals(Material.STATIONARY_WATER)) {
            material = Material.SPONGE;
        }

        zombie.teleport(loc.add(0, 1.3, 0));
        block.setType(material);
        zombie.getWorld().playEffect(loc, Effect.STEP_SOUND, material.getId());

        this.lastBlockMap.put(zombie.getUniqueId(), System.currentTimeMillis());

    }


}
