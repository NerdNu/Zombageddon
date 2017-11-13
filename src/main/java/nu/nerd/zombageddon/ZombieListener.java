package nu.nerd.zombageddon;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.world.ChunkLoadEvent;


public class ZombieListener implements Listener {


    private Zombageddon plugin;


    public ZombieListener(Zombageddon plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!plugin.CONFIG.worldEnabled(event.getEntity().getWorld())) return;
        if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) return;
        zombieSetUp((Zombie) event.getEntity());
    }


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!plugin.CONFIG.worldEnabled(event.getWorld())) return;
        for (Entity ent : event.getChunk().getEntities()) {
            if (ent.getType().equals(EntityType.ZOMBIE)) {
                zombieSetUp((Zombie) ent);
            }
        }
    }


    /**
     * Zombies should always prioritize player targets
     */
    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (!plugin.CONFIG.worldEnabled(event.getEntity().getWorld())) return;
        if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) return;
        if (event.getTarget() == null || !event.getTarget().getType().equals(EntityType.PLAYER)) {
            Player player = MathUtil.nearestPlayer(event.getEntity(), plugin.CONFIG.AGGRO_RADIUS);
            if (!(player == null)) {
                ((Zombie) event.getEntity()).setTarget(player);
            }
        }
    }


    /**
     * Apply custom attributes and otherwise set up zombies to be scarier
     */
    private void zombieSetUp(Zombie zombie) {
        // aggro radius
        zombie.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(plugin.CONFIG.AGGRO_RADIUS);
    }


}
