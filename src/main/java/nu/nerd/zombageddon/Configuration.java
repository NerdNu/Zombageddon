package nu.nerd.zombageddon;


import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;


public class Configuration {


    private Zombageddon plugin;

    public Set<String> WORLDS;
    public int AGGRO_RADIUS;
    public boolean CONSTRUCTION;
    public boolean TORCH_DESTRUCTION;
    public boolean TORCH_SEEKING;
    public boolean SPAWNING;
    public int SPAWNING_CAP;
    public int SPAWNING_FREQUENCY;
    public int SPAWNING_ATTEMPTS;
    public int SPAWNING_LIGHT;
    public int SPAWNING_RADIUS_MIN;
    public int SPAWNING_RADIUS_MAX;


    public Configuration(Zombageddon plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }


    public void reload() {
        plugin.reloadConfig();
        WORLDS = new HashSet<String>(plugin.getConfig().getStringList("worlds"));
        AGGRO_RADIUS = plugin.getConfig().getInt("aggro_radius", 160);
        CONSTRUCTION = plugin.getConfig().getBoolean("construction", true);
        TORCH_DESTRUCTION = plugin.getConfig().getBoolean("torch_destruction", true);
        TORCH_SEEKING = plugin.getConfig().getBoolean("torch_seeking", true);
        SPAWNING = plugin.getConfig().getBoolean("spawning", true);
        SPAWNING_CAP = plugin.getConfig().getInt("spawning_cap", 3000);
        SPAWNING_FREQUENCY = plugin.getConfig().getInt("spawning_frequency", 100);
        SPAWNING_ATTEMPTS = plugin.getConfig().getInt("spawning_attempts", 10);
        SPAWNING_LIGHT = plugin.getConfig().getInt("spawning_light", 7);
        SPAWNING_RADIUS_MIN = plugin.getConfig().getInt("spawning_radius_min", 24);
        SPAWNING_RADIUS_MAX = plugin.getConfig().getInt("spawning_radius_max", 80);
    }


    public void save() {
        plugin.getConfig().set("worlds", WORLDS);
        plugin.getConfig().set("aggro_raius", AGGRO_RADIUS);
        plugin.getConfig().set("construction", CONSTRUCTION);
        plugin.getConfig().set("torch_destruction", TORCH_DESTRUCTION);
        plugin.getConfig().set("torch_seeking", TORCH_SEEKING);
        plugin.getConfig().set("spawning", SPAWNING);
        plugin.getConfig().set("spawning_cap", SPAWNING_CAP);
        plugin.getConfig().set("spawning_frequency", SPAWNING_FREQUENCY);
        plugin.getConfig().set("spawning_attempts", SPAWNING_ATTEMPTS);
        plugin.getConfig().set("spawning_light", SPAWNING_LIGHT);
        plugin.getConfig().set("spawning_radius_min", SPAWNING_RADIUS_MIN);
        plugin.getConfig().set("spawning_radius_max", SPAWNING_RADIUS_MAX);
        plugin.saveConfig();
    }


    public boolean worldEnabled(World world) {
        return WORLDS.contains(world.getName());
    }


}
