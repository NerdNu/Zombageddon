package nu.nerd.zombageddon;


import nu.nerd.mirrormirror.ExtendedEntity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class ZombieMotivator extends BukkitRunnable {


    private Zombageddon plugin;
    private Map<UUID, ZombieMeta> zombieMeta;


    public ZombieMotivator(Zombageddon plugin) {
        this.plugin = plugin;
        this.zombieMeta = new HashMap<UUID, ZombieMeta>();
        this.runTaskTimer(plugin, 1L, 1L);
    }


    public void run() {
        for (Zombie zombie : getEligibleZombies()) {
            incrementFrustrationTime(zombie);
            tryBridging(zombie, tryPillaring(zombie));
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
                    if (!zombieMeta.containsKey(entity.getUniqueId())) {
                        zombieMeta.put(entity.getUniqueId(), new ZombieMeta(entity.getUniqueId()));
                    }
                } else {
                    zombieMeta.remove(entity.getUniqueId());
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
        ZombieMeta meta = zombieMeta.get(zombie.getUniqueId());
        if (meta.getFrustLoc() == null) {
            meta.setFrustLoc(zombie.getLocation());
            meta.setFrustTicks(0);
        } else {
            if (MathUtil.distance2DSquared(meta.getFrustLoc(), zombie.getLocation()) <= MathUtil.square(2.0)) {
                meta.incrementFrustTicks();
            } else {
                meta.setFrustTicks(0);
                meta.setFrustLoc(null);
            }
        }
    }


    /**
     * Determine whether to pillar or not
     * @param zombie the entity
     * @return true if the attempt was successful
     */
    private boolean tryPillaring(Zombie zombie) {

        if (!zombieMeta.containsKey(zombie.getUniqueId()) || zombieMeta.get(zombie.getUniqueId()).getFrustLoc() == null) {
            return false;
        }

        ZombieMeta meta = zombieMeta.get(zombie.getUniqueId());

        if (!plugin.CONFIG.CONSTRUCTION) return false;
        if (!meta.isFrustrated()) return false; //under frustration limit
        if (zombie.getLocation().getBlockY() >= zombie.getTarget().getLocation().getBlockY()) return false; //on or higher than the target's Y
        if ((System.currentTimeMillis() - meta.getLastBlockMillis()) < 1000) return false; //block place cooldown

        return doPillar(zombie);

    }


    /**
     * Determine whether to bridge horizontally or not
     * @param zombie the entity
     * @param didPillar whether the previous pillar attempt was successful
     * @return true if the attempt was successful
     */
    private boolean tryBridging(Zombie zombie, boolean didPillar) {

        if (!zombieMeta.containsKey(zombie.getUniqueId()) || zombieMeta.get(zombie.getUniqueId()).getFrustLoc() == null) {
            return false;
        }

        ZombieMeta meta = zombieMeta.get(zombie.getUniqueId());

        if (!plugin.CONFIG.CONSTRUCTION) return false;
        if (!meta.isFrustrated()) return false; //under frustration limit
        if ((System.currentTimeMillis() - meta.getLastBlockMillis()) < 1000) return false; //block place cooldown
        if (didPillar) return false; //don't bridge on this iteration if a pillar attempt was successful
        if (isAirBelow(zombie.getLocation().getBlock())) return false;
        if (MathUtil.distance2DSquared(zombie.getLocation(), zombie.getTarget().getLocation()) < MathUtil.square(2)) return false;

        return doBridge(zombie);

    }


    /**
     * Make the zombie jump up and place a block
     */
    private boolean doPillar(Zombie zombie) {

        Location loc = zombie.getLocation();
        Block block = loc.getBlock();
        Block blockBelow = loc.subtract(0, 1, 0).getBlock();
        Block blockAbove = loc.add(0, 1, 0).getBlock();
        Material material = Material.LEAVES;
        ZombieMeta meta = zombieMeta.get(zombie.getUniqueId());

        //Don't place a block if the zombie is falling/otherwise in midair
        if (blockBelow == null || blockBelow.getType().equals(Material.AIR)) {
            return false;
        }

        //Don't pillar if there's an obstruction
        if (blockAbove != null && !blockAbove.getType().equals(Material.AIR)) {
            return false;
        }

        //Use sponge if the zombie is standing in water
        if (blockBelow.getType().equals(Material.WATER) || blockBelow.getType().equals(Material.STATIONARY_WATER)) {
            material = Material.SPONGE;
        }

        zombie.teleport(loc.add(0, 1.3, 0));
        block.setType(material);
        zombie.getWorld().playEffect(loc, Effect.STEP_SOUND, material.getId());

        meta.setLastBlockMillis(System.currentTimeMillis());

        return true;

    }


    /**
     * Make the zombie place a horizontal bridge block
     */
    private boolean doBridge(Zombie zombie) {

        ZombieMeta meta = zombieMeta.get(zombie.getUniqueId());
        Location loc = zombie.getLocation();
        Location targetLoc = zombie.getTarget().getLocation();
        Material material = Material.LEAVES;
        ExtendedEntity exZombie = new ExtendedEntity(zombie);

        int xDist = Math.abs(loc.getBlockX() - targetLoc.getBlockX());
        int zDist = Math.abs(loc.getBlockZ() - targetLoc.getBlockZ());
        Location placementLoc = loc.clone();

        if (xDist > zDist) {
            placementLoc.add(1, -1, 0);
        } else {
            placementLoc.add(0, -1, 1);
        }

        if (!isAirBelow(placementLoc.getBlock())) {
            return false;
        }

        placementLoc.getBlock().setType(material);
        zombie.getWorld().playEffect(placementLoc, Effect.STEP_SOUND, material.getId());
        meta.setLastBlockMillis(System.currentTimeMillis());
        exZombie.walkTo(placementLoc.add(0, 1, 0), 1.0f);
        meta.setFrustLoc(placementLoc);

        return true;

    }


    private boolean isAirBelow(Block block) {
        Block under = block.getRelative(BlockFace.DOWN);
        Material[] air = {
                Material.AIR,
                Material.LONG_GRASS,
                Material.RED_ROSE,
                Material.YELLOW_FLOWER
        };
        return Arrays.asList(air).contains(under.getType());
    }


}
