package nu.nerd.zombageddon;

import org.bukkit.plugin.java.JavaPlugin;


public class Zombageddon extends JavaPlugin {


    public static Zombageddon instance;


    public void onEnable() {
        Zombageddon.instance = this;
    }


}
