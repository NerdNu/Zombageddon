package nu.nerd.zombageddon;


import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;


public class Configuration {


    private Zombageddon plugin;

    public Set<String> WORLDS;
    public int AGGRO_RADIUS;
    public boolean CONSTRUCTION;


    public Configuration(Zombageddon plugin) {
        this.plugin = plugin;
    }


    public void reload() {
        plugin.reloadConfig();
        WORLDS = new HashSet<String>(plugin.getConfig().getStringList("worlds"));
        AGGRO_RADIUS = plugin.getConfig().getInt("aggro_radius", 160);
        CONSTRUCTION = plugin.getConfig().getBoolean("construction", true);
    }


    public void save() {
        plugin.getConfig().set("worlds", WORLDS);
        plugin.getConfig().set("aggro_raius", AGGRO_RADIUS);
        plugin.getConfig().set("construction", CONSTRUCTION);
        plugin.saveConfig();
    }


    public boolean worldEnabled(World world) {
        return WORLDS.contains(world.getName());
    }


}
