package nu.nerd.zombageddon;


import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class Configuration {


    private Zombageddon plugin;

    public Set<String> WORLDS;


    public Configuration(Zombageddon plugin) {
        this.plugin = plugin;
    }


    public void reload() {
        plugin.reloadConfig();
        WORLDS = new HashSet<String>(plugin.getConfig().getStringList("worlds"));
    }


    public void save() {
        plugin.getConfig().set("worlds", WORLDS);
        plugin.saveConfig();
    }


    public boolean worldEnabled(World world) {
        return WORLDS.contains(world.getName());
    }


}
