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
import org.bukkit.util.BlockIterator;

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
            tryWallBreaking(zombie, tryBridging(zombie, tryPillaring(zombie)));
            destroyTorches(zombie);
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
     * Determine whether to break blocks in the way
     * @param zombie the entity
     * @param didBridge whether the prior bridging attempt was successful
     * @return true if the attempt was successful
     */
    private boolean tryWallBreaking(Zombie zombie, boolean didBridge) {

        if (!zombieMeta.containsKey(zombie.getUniqueId()) || zombieMeta.get(zombie.getUniqueId()).getFrustLoc() == null) {
            return false;
        }

        ZombieMeta meta = zombieMeta.get(zombie.getUniqueId());

        if (plugin.CONFIG.BREAKABLE_MATERIALS.size() < 1) return false;
        if (!meta.isFrustrated()) return false;
        if (didBridge) return false;

        return doWallBreak(zombie);

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

        if (!placementLoc.getBlock().getType().equals(Material.AIR)) {
            return false; //prevent destruction of existing platforms
        }

        placementLoc.getBlock().setType(material);
        zombie.getWorld().playEffect(placementLoc, Effect.STEP_SOUND, material.getId());
        meta.setLastBlockMillis(System.currentTimeMillis());
        exZombie.walkTo(placementLoc.add(0, 1, 0), 1.0f);
        meta.setFrustLoc(placementLoc);

        return true;

    }


    /**
     * Make the zombie break blocks impeding its path to the player
     */
    private boolean doWallBreak(Zombie zombie) {

        ZombieMeta meta = zombieMeta.get(zombie.getUniqueId());
        TargetBreakable tb = meta.getWallTarget();

        //increment timer and break the block when ready
        if (tb != null && plugin.CONFIG.BREAKABLE_MATERIALS.containsKey(tb.getBlock().getType())) {
            tb.incrementTicks();
            tb.updateLastTouched();
            if (tb.getSeconds() >= plugin.CONFIG.BREAKABLE_MATERIALS.get(tb.getBlock().getType())) {
                plugin.getLogger().info("Breaking block!" + tb.getBlock().toString());
                zombie.getWorld().playEffect(zombie.getLocation(), Effect.STEP_SOUND, Material.TORCH.getId());
                tb.getBlock().breakNaturally();
                meta.setWallTarget(null);
                return true;
            }
        }

        //determine a new target block
        if (tb == null || (System.currentTimeMillis() - tb.getLastTouched()) > 120000) {
            Block eye = firstObstructingBlock(zombie.getEyeLocation());
            Block foot = firstObstructingBlock(zombie.getLocation());
            if (eye != null && !eye.getType().equals(Material.AIR) && plugin.CONFIG.BREAKABLE_MATERIALS.containsKey(eye.getType())) {
                meta.setWallTarget(new TargetBreakable(eye));
                plugin.getLogger().info("Selected eye block " + eye.getLocation().toString());
            }
            else if (foot != null && !foot.getType().equals(Material.AIR) && plugin.CONFIG.BREAKABLE_MATERIALS.containsKey(foot.getType())) {
                meta.setWallTarget(new TargetBreakable(foot));
                plugin.getLogger().info("Selected foot block " + foot.getLocation().toString());
            }
            else {
                meta.setWallTarget(null);
            }
        }

        return false;

    }


    /**
     * Zombies will break torches in brightly lit areas
     */
    private void destroyTorches(Zombie zombie) {

        if (!plugin.CONFIG.TORCH_DESTRUCTION) return;
        if (zombie.getLocation().getBlock().getLightLevel() < 11) return;
        ZombieMeta meta = zombieMeta.get(zombie.getUniqueId());

        //Destroy nearby torches
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = zombie.getLocation().getBlock().getRelative(x, y, z);
                    if (!block.getType().equals(Material.TORCH)) continue;
                    zombie.getWorld().playEffect(zombie.getLocation(), Effect.STEP_SOUND, Material.TORCH.getId());
                    block.breakNaturally();
                    meta.setLastBlockMillis(System.currentTimeMillis());
                }
            }
        }

        //Seek nearby torches
        if (plugin.CONFIG.TORCH_SEEKING && (System.currentTimeMillis() - meta.getLastBlockMillis() > 10000)) {
            for (int x = -10; x <= 10; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -10; z <= 10; z++) {
                        Block block = zombie.getLocation().getBlock().getRelative(x, y, z);
                        double blockDist = MathUtil.distance2DSquared(zombie.getLocation(), block.getLocation());
                        double targetDist = MathUtil.distance2DSquared(zombie.getLocation(), zombie.getTarget().getLocation());
                        if (block.getType().equals(Material.TORCH) && blockDist < targetDist) {
                            ExtendedEntity xZombie = new ExtendedEntity(zombie);
                            xZombie.walkTo(block.getLocation(), 1.0f);
                            break;
                        }
                    }
                }
            }
        }

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


    private Block firstObstructingBlock(Location loc) {
        BlockIterator it = new BlockIterator(loc, 0.0d, 4);
        while (it.hasNext()) {
            Block block = it.next();
            if (!block.getType().equals(Material.AIR)) {
                return block;
            }
        }
        return null;
    }


}
