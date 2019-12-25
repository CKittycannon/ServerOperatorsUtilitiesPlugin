package dev.ckitty.mc.soup.econ;

import dev.ckitty.mc.soup.main.SOUP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.ckitty.mc.soup.main.SOUP.lang;

public class EconCMD implements CommandExecutor {
    
    /*
    * /eco set <player> <ammount>      - v
    * /eco deposit <player> <ammount>  - v
    * /eco withdraw <player> <ammount> - soup.admin.cmd.eco
    *
    * /money [player]        - soup.user.cmd.money
    *                        - soup.admin.cmd.money
    * /pay <other> <ammount> - soup.user.cmd.pay
    *
    * */
    
    private EconomyManager manager = SOUP.EMANAGER;
    private VaultEconAdapter adapter = SOUP.VECON;
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "eco":
                economy(sender, args);
                break;
            case "money":
                money(sender, args);
                break;
            case "pay":
                pay(sender, args);
                break;
        }
        return true;
    }
    
    private void economy(CommandSender sender, String[] args) {
        if(sender instanceof Player && !sender.hasPermission("soup.admin.cmd.eco")) {
            sender.sendMessage(lang("misc.no-perm-cmd"));
            return;
        }
        
        if(args.length != 3) {
            //sender.sendMessage(SOUP.lang("cmds.usage.pay"));
            sender.sendMessage(lang("cmds.usage.eco"));
            return;
        }
        
        Player player = Bukkit.getPlayer(args[1]);
        if(player == null) {
            sender.sendMessage(lang("misc.player-isnt-online", "{player}", args[1]));
            return;
        }
        
        double ammount;
        try {
            ammount = Double.parseDouble(args[2]);
        } catch (Exception e) {
            sender.sendMessage(lang("misc.not-a-number", "{nan}", args[2]));
            return;
        }
        
        if(args[0].equalsIgnoreCase("set")) {
            manager.setMoney(player.getUniqueId(), ammount);
            player.sendMessage(lang("cmds.msgs.eco-set",
                    "{player}", player.getName(),
                    "{ammount}", manager.format(ammount)
            ));
        } else if(args[0].equalsIgnoreCase("deposit")) {
            manager.addMoney(player.getUniqueId(), ammount);
            player.sendMessage(lang("cmds.msgs.eco-deposit",
                    "{player}", player.getName(),
                    "{ammount}", manager.format(ammount),
                    "{total}", manager.getMoney(player.getUniqueId())
            ));
        } else if(args[0].equalsIgnoreCase("withdraw")) {
            manager.addMoney(player.getUniqueId(), -ammount);
            player.sendMessage(lang("cmds.msgs.eco-withdraw",
                    "{player}", player.getName(),
                    "{ammount}", manager.format(ammount),
                    "{total}", manager.getMoney(player.getUniqueId())
            ));
        } else
            sender.sendMessage(lang("cmds.usage.eco"));
    }
    
    private void money(CommandSender sender, String[] args) {
        if(args.length == 0) {
            if(sender instanceof Player) {
                // perm check
                if(!sender.hasPermission("soup.user.cmd.money")) {
                    sender.sendMessage(lang("misc.no-perm-cmd"));
                    return;
                }
                
                // tell info
                double money = manager.getMoney(((Player) sender).getUniqueId());
                sender.sendMessage(
                        lang("cmds.msgs.money-self",
                                "{coins}",
                                manager.format(money),
                                "{coinname}",
                                manager.name(money)
                        )
                );
            } else
                sender.sendMessage(lang("misc.not-a-player"));
        } else if(args.length == 1) {
            // perm check
            if(sender instanceof Player && !sender.hasPermission("soup.admin.cmd.money")) {
                sender.sendMessage(lang("misc.no-perm-cmd"));
                return;
            }
            
            Player player = Bukkit.getPlayer(args[0]);
            if(player == null) {
                sender.sendMessage(lang("misc.player-isnt-online", "{player}", args[0]));
                return;
            }
    
            // tell info
            double money = manager.getMoney(player.getUniqueId());
            lang("cmds.msgs.money-other",
                    "{player}",
                    player.getName(),
                    "{coins}",
                    manager.format(money),
                    "{coinname}",
                    manager.name(money)
            );
        } else
            sender.sendMessage(lang("cmds.usage.money"));
    }
    
    private void pay(CommandSender sender, String[] args) {
        if(args.length != 2) {
            sender.sendMessage(lang("cmds.usage.pay"));
            return;
        }
        
        if(sender instanceof Player) {
            // perm check
            Player player = (Player) sender;
            if(!player.hasPermission("soup.user.cmd.pay")) {
                player.sendMessage(lang("misc.no-perm-cmd"));
                return;
            }
            
            // player other must exist
            Player other = Bukkit.getPlayer(args[0]);
            if(other == null) {
                player.sendMessage(lang("misc.player-isnt-online", "{player}", args[0]));
                return;
            }
    
            // ammount
            double ammount;
            try {
                ammount = Double.parseDouble(args[1]);
            } catch (Exception e) {
                sender.sendMessage(lang("misc.not-a-number", "{nan}", args[1]));
                return;
            }
            
            // can player pay?
            if(adapter.has(player, ammount)) {
                // PAY // also: check the other player can recieve the money
                if(adapter.depositPlayer(other, ammount).transactionSuccess()) {
                    adapter.withdrawPlayer(player, ammount); // shoulnt have problems with this
                    // tell info
                    String name = manager.name(ammount);
                    String format = manager.format(ammount);
    
                    player.sendMessage(lang("eco.transfer-source",
                            "{player}", other.getName(),
                            "{coins}", format,
                            "{coinname}", name));
                    other.sendMessage(lang("eco.transfer-target",
                            "{player}", player.getName(),
                            "{coins}", format,
                            "{coinname}", name));
                } else {
                    player.sendMessage(lang("eco.maxed-out-other"));
                    other.sendMessage(lang("eco.maxed-out-deposit"));
                }
            } else {
                player.sendMessage(lang("eco.not-enough-money"));
            }
        } else {
            // can give monies
            // IF YOU ARENT A PLAYER YOU CAN GO USE /ECO YOU BASTARD!
            sender.sendMessage(lang("misc.not-a-player"));
        }
        
    }
    
}
