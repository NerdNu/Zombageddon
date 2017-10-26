package nu.nerd.zombageddon;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class CommandHandler implements CommandExecutor {


    private Zombageddon plugin;


    public CommandHandler(Zombageddon plugin) {
        this.plugin = plugin;
        plugin.getCommand("zombageddon").setExecutor(this);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("zombageddon")) {
            if (args.length < 1) {
                sender.sendMessage("You must specify a subcommand.");
            }
            else if (args[0].equalsIgnoreCase("elytra")) {
                // silly test lol
                Player p = (Player) sender;
                LivingEntity ent = (LivingEntity) p.getWorld().spawnEntity(p.getLocation(), EntityType.ZOMBIE);
                ent.getEquipment().setChestplate(new ItemStack(Material.ELYTRA));
                ent.setGliding(true);
            }
            return true;
        }
        return false;
    }


}
