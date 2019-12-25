package dev.ckitty.mc.soup.advtp;

import dev.ckitty.mc.soup.main.SOUP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class WarpCMD implements CommandExecutor {
    
    /*
    *
    * soup.user.cmd.warp
    * soup.admin.cmd.warp
    *
    * */
    
    private WarpManager manager;
    
    public WarpCMD(WarpManager manager) {
        this.manager = manager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // $ /warp
        // $ /warp <warp>
        // $ /warp <warp> set
        // $ /warp <warp> del
    
        if(!(sender instanceof Player)) { // not a player
            sender.sendMessage(SOUP.lang("misc.not-a-player"));
            return true;
        }
        
        Player player = (Player) sender;
        if(args.length == 0) {
            // show warps // perm: soup.warps.list //
            if(!player.hasPermission("soup.user.cmd.warp")) {
                player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                return true;
            }
            
            // put warps in a string array so we can list them
            String[] names = new String[manager.warps.size()];
            int index = 0;
            for(WarpManager.Warp w : manager.warps) {
                names[index++] = w.name;
            }
            
            // tell available warps!
            player.sendMessage(SOUP.lang("cmds.msgs.warp-list"));
            player.sendMessage(Arrays.toString(names)); // srry if they're not in order...
        } else if(args.length == 1) {
            // search warp // perm: soup.warps.use //
            if(!player.hasPermission("soup.user.cmd.warp")) {
                player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                return true;
            }
            
            // does warp exist?
            WarpManager.Warp w = manager.getWarp(args[0]);
            if(w == null) {
                player.sendMessage(SOUP.lang("cmds.msgs.warp-doesnt-exist", "{warpname}", args[0]));
                return true;
            }
            
            // warp exists!
            // let the manager do its stuff
            manager.taskWarp(player, w);
        } else if(args.length == 2) {
            // check permission to make or remove a warp!
            // perm: soup.warps.set // perm: soup.warps.del //
            if(args[1].equalsIgnoreCase("set")) {
                if(!player.hasPermission("soup.admin.cmd.warp")) { // check for permission
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                
                // does warp exist?
                WarpManager.Warp w = manager.getWarp(args[0]);
                if(w != null) { // if not null (it exists) we cancel!
                    player.sendMessage(SOUP.lang("cmds.msgs.warp-does-exist", "{warpname}", w.name));
                    return true;
                }
                
                // create new warp!
                manager.addWarp(args[0], player.getLocation());
                player.sendMessage(SOUP.lang("cmds.msgs.warp-added", "{warpname}", args[0]));
            } else if(args[1].equalsIgnoreCase("del")) {
                if(!player.hasPermission("soup.admin.cmd.warp")) { // check for permission
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
    
                // does warp exist?
                WarpManager.Warp w = manager.getWarp(args[0]);
                if(w == null) { // if null (it doesnt exist) we cancel!
                    player.sendMessage(SOUP.lang("cmds.msgs.warp-doesnt-exist", "{warpname}", w.name));
                    return true;
                }
    
                // delete warp!
                manager.delWarp(args[0]);
                player.sendMessage(SOUP.lang("cmds.msgs.warp-removed", "{warpname}", w.name));
            }
        } else
            player.sendMessage(SOUP.lang("cmds.usage.warp"));
        
        return true;
    }
    
}
