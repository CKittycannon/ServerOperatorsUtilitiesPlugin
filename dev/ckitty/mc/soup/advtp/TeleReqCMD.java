package dev.ckitty.mc.soup.advtp;

import dev.ckitty.mc.soup.main.SOUP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleReqCMD implements CommandExecutor {
    
    /*
    *
    * soup.user.cmd.tpask
    * soup.user.cmd.tphere
    * soup.user.cmd.tpno
    * soup.user.cmd.tpok
    * soup.user.cmd.tpcancel
    *
    * */
    
    TeleReqManager manager = SOUP.TPXMANAGER;
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // tpa <player>
        // tph <player>
        // tpok [player]
        // tpno [player]
        
        // sender has to be a player
        if(!(sender instanceof Player)) {
            sender.sendMessage(SOUP.lang("misc.not-a-player"));
            return true;
        }
        
        Player player = (Player) sender;
        switch (label.toLowerCase()) {
            case "tpask":
                if(!player.hasPermission("soup.user.cmd.tpask")) { // check for permission
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // 1 arg obligatory
                if(args.length != 1) {
                    player.sendMessage(SOUP.lang("cmds.usage.tpa"));
                    return true;
                }
                
                // check that player exists
                Player target = Bukkit.getPlayer(args[0]);
                if(target == null) {
                    player.sendMessage(SOUP.lang("misc.player-isnt-online", "{player}", args[0]));
                    return true;
                }
                
                // send request
                manager.fireRequest(player, true, target);
                break;
            case "tphere":
                if(!player.hasPermission("soup.user.cmd.tphere")) { // check for permission
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // 1 arg obligatory
                if(args.length != 1) {
                    player.sendMessage(SOUP.lang("cmds.usage.tph"));
                    return true;
                }
    
                // check that player exists
                target = Bukkit.getPlayer(args[0]);
                if(target == null) {
                    player.sendMessage(SOUP.lang("misc.player-isnt-online", "{player}", args[0]));
                    return true;
                }
    
                // send request
                manager.fireRequest(player, false, target);
                break;
            case "tpok":
                if(!player.hasPermission("soup.user.cmd.tpok")) { // check for permission
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // check if there are args.
                if(args.length == 0)
                    manager.respondRequest(player, null, true);
                else if(args.length == 1)
                    manager.respondRequest(player, args[0], true);
                else
                    player.sendMessage(SOUP.lang("cmds.usage.tpok"));
                break;
            case "tpno":
                if(!player.hasPermission("soup.user.cmd.tpno")) { // check for permission
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // check if there are args.
                if(args.length == 0)
                    manager.respondRequest(player, null, false);
                else if(args.length == 1)
                    manager.respondRequest(player, args[0], false);
                else
                    player.sendMessage(SOUP.lang("cmds.usage.tpno"));
                break;
            case "tpcancel":
                if(!player.hasPermission("soup.user.cmd.tpcancel")) { // check for permission
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // no args!
                if(args.length != 0) {
                    player.sendMessage(SOUP.lang("cmds.usage.tpcancel"));
                    return true;
                }
                
                // remove request from sender
                manager.cancelRequest(player);
                break;
        }
        
        return true;
    }
}
