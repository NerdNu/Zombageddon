package nu.nerd.zombageddon;

import org.bukkit.plugin.java.JavaPlugin;


public class Zombageddon extends JavaPlugin {


    public static Zombageddon instance;
    public Configuration CONFIG;


    public void onEnable() {
        Zombageddon.instance = this;
        CONFIG = new Configuration(this);
        CONFIG.reload();
        new ZombieListener(this);
        new ZombieMotivator(this);
        new CommandHandler(this);
    }


}
