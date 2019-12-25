package dev.ckitty.mc.soup.misc;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import static dev.ckitty.mc.soup.main.SOUP.WARPMAN;
import static dev.ckitty.mc.soup.main.SOUP.lang;

public class MiscCMD implements CommandExecutor {
    
    /*
     * /spawn      - soup.user.cmd.spawn
     *
     * /gmc        - soup.admin.cmd.creative
     * /gms        - soup.admin.cmd.survival
     * /gma        - soup.admin.cmd.adventure
     * /gmz        - soup.admin.cmd.spectator
     * /fly        - soup.admin.cmd.fly
     * /enderchest - soup.admin.cmd.enderchest
     * /suicide    - soup.admin.cmd.suicide
     * /thunder    - soup.admin.cmd.thunder
     * /invsee     - soup.admin.cmd.invsee
     * /feed       - soup.admin.cmd.feed
     * /heal       - soup.admin.cmd.heal
     *
     * */
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "feed": // check perm
                if (!sender.hasPermission("soup.admin.cmd.feed")) {
                    sender.sendMessage(lang("misc.no-perm-cmd"));
                    return true;
                }
                
                if (args.length == 0) { // 0 args, feed self
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(lang("misc.not-a-player"));
                        return true; // not a player
                    }
                    
                    // feed this player
                    Player player = (Player) sender;
                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                    
                    // tell info
                    player.sendMessage(lang("cmds.msgs.feeded-self"));
                } else if (args.length == 1) { // 1 arg, feed other
                    Player target = Bukkit.getPlayer(args[0]);
                    
                    if (target == null) {
                        sender.sendMessage(lang("misc.player-isnt-online", "{player}", args[0]));
                        return true;
                    }
                    
                    // feed other player
                    target.setFoodLevel(20);
                    target.setSaturation(20f);
                    
                    // tell info
                    sender.sendMessage(lang("cmds.msgs.feeded-other", "{target}", target.getName()));
                }
                break;
            case "heal":
                if (!sender.hasPermission("soup.admin.cmd.heal")) {
                    sender.sendMessage(lang("misc.no-perm-cmd"));
                    return true;
                }
                
                if (args.length == 0) { // 0 args, heal self
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(lang("misc.not-a-player"));
                        return true; // not a player
                    }
                    
                    // heal this player
                    Player player = (Player) sender;
                    player.setHealth(20f);
                    
                    // tell info
                    player.sendMessage(lang("cmds.msgs.healed-self"));
                } else if (args.length == 1) { // 1 arg, heal other
                    Player target = Bukkit.getPlayer(args[0]);
                    
                    if (target == null) {
                        sender.sendMessage(lang("misc.player-isnt-online", "{player}", args[0]));
                        return true;
                    }
                    
                    // heal other player
                    target.setHealth(20f);
                    
                    // tell info
                    sender.sendMessage(lang("cmds.msgs.healed-other", "{target}", target.getName()));
                }
                break;
            case "spawn":
                if (!sender.hasPermission("soup.user.cmd.spawn")) {
                    sender.sendMessage(lang("misc.no-perm-cmd"));
                    return true;
                }
                
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    WARPMAN.taskSpawn(p); // go!
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "gmc":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.creative")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    p.setGameMode(GameMode.CREATIVE);
                    sender.sendMessage(lang("cmds.msgs.gamemode-creative"));
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "gms":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.survival")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    p.setGameMode(GameMode.SURVIVAL);
                    sender.sendMessage(lang("cmds.msgs.gamemode-survival"));
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "gma":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.adventure")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    p.setGameMode(GameMode.ADVENTURE);
                    sender.sendMessage(lang("cmds.msgs.gamemode-adventure"));
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "gmz":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.spectator")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    p.setGameMode(GameMode.SPECTATOR);
                    sender.sendMessage(lang("cmds.msgs.gamemode-spectator"));
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "fly":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.fly")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    // check if server allows flight
                    if (Bukkit.getServer().getAllowFlight()) {
                        p.setFlying(!p.isFlying());
                        sender.sendMessage(lang("cmds.msgs.fly-changed", "{state}", p.isFlying()));
                    } else // flight-not-allowed
                        sender.sendMessage(lang("cmds.msgs.flight-not-allowed"));
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "invsee":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.invsee")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    if (args.length != 1) {
                        sender.sendMessage(lang("cmds.usage.invsee"));
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        p.sendMessage(lang("misc.player-isnt-online", "{player}", args[0]));
                        return true;
                    }
                    
                    copyInventory(target, p); // send!
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "enderchest":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.enderchest")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    p.openInventory(p.getEnderChest()); // go!
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "suicide":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.suicide")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    // sorry in advance!
                    ((Player) sender).setHealth(0d);
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
            case "thunder":
                if (sender instanceof Player) {
                    if (!sender.hasPermission("soup.admin.cmd.thunder")) {
                        sender.sendMessage(lang("misc.no-perm-cmd"));
                        return true;
                    }
                    
                    // this should be cool
                    Block b = ((Player) sender).getTargetBlock(null, 100);
                    b.getWorld().strikeLightningEffect(b.getLocation());
                } else
                    sender.sendMessage(lang("misc.not-a-player"));
                break;
        }
        return true;
    }
    
    private void copyInventory(Player target, Player player) {
        // create a new inventory //
        Inventory inv = Bukkit.createInventory(null, 45, lang("cmds.msgs.invsee", "{player}", target.getName()));
        PlayerInventory pinv = target.getInventory();
        
        ItemStack[] conts = pinv.getContents();
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, conts[i]);
        }
        
        ItemStack[] armor = pinv.getArmorContents();
        for (int i = 0; i < 4; i++) {
            inv.setItem(i + 36, armor[i]);
        }
        
        ItemStack[] extra = pinv.getExtraContents();
        for (int i = 0; i < 1; i++) {
            inv.setItem(i + 40, extra[i]);
        }
        
        ItemStack xp = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = xp.getItemMeta();
        meta.setDisplayName(Integer.toString(target.getLevel()));
        xp.setItemMeta(meta);
        inv.setItem(44, xp);
        
        // open inv to player
        player.openInventory(inv);
    }
    
}
