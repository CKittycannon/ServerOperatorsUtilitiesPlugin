package dev.ckitty.mc.soup.misc;

import dev.ckitty.mc.soup.main.SOUP;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.ckitty.mc.soup.main.SOUP.lang;

public class SOUPCMD implements CommandExecutor {
    
    /*
     * /soup setlogin   - soup.admin.cmd.setlogin
     * /soup setspawn   - soup.admin.cmd.setspawn
     * /soup reload     - soup.admin.cmd.reload
     * /soup exportlang - soup.admin.cmd.exportlang
     * */
    
    private Location SPAWN;
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(lang("cmds.usage.soup"));
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "setspawn": {
                // no player
                if (!(sender instanceof Player)) {
                    sender.sendMessage(lang("misc.not-a-player"));
                    return true;
                }
                
                // player
                Player player = (Player) sender;
                
                // no perm
                if (!player.hasPermission("soup.admin.cmd.setspawn")) {
                    player.sendMessage(lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // setspawn & tell info
                SPAWN = player.getLocation().clone();
                player.sendMessage(lang("cmds.msgs.spawn-updated"));
            }
            break;
            case "setlogin": {
                // no player
                if (!(sender instanceof Player)) {
                    sender.sendMessage(lang("misc.not-a-player"));
                    return true;
                }
                
                // player
                Player player = (Player) sender;
                
                // no perm
                if (!player.hasPermission("soup.admin.cmd.setlogin")) {
                    player.sendMessage(lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // setlogin & tell info
                SOUP.IDSYSTEM.setSpawn(player.getLocation().clone());
                player.sendMessage(lang("cmds.msgs.login-updated"));
            }
            break;
            case "reload": // cmds.msgs.lang-reloaded
                if (sender instanceof Player && !sender.hasPermission("soup.admin.cmd.reload")) {
                    sender.sendMessage(lang("misc.no-perm-cmd"));
                    return true;
                }
                
                SOUP.LANG.reload();
                sender.sendMessage(lang("cmds.msgs.lang-reloaded"));
                break;
            case "exportlang": // soup.admin.exportlang
                if (sender instanceof Player && !sender.hasPermission("soup.admin.cmd.exportlang")) {
                    sender.sendMessage(lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // export
                SOUP.INSTANCE.saveResource("lang.yml", true);
                SOUP.LANG.reload();
                sender.sendMessage(lang("cmds.msgs.lang-exported"));
                break;
            default:
                sender.sendMessage(lang("cmds.usage.soup"));
                break;
        }
        return true;
    }
    
    public Location getSpawn() {
        return SPAWN;
    }
    
    public void setSpawn(Location spawn) {
        this.SPAWN = spawn;
    }
    
}
