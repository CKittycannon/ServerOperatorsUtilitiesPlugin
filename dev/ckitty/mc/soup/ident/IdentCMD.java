package dev.ckitty.mc.soup.ident;

import dev.ckitty.mc.soup.main.SOUP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IdentCMD implements CommandExecutor {
    
    /*
# permissions identcmd
> email      - soup.user.cmd.email
> password   - soup.user.cmd.password
> unregister - soup.user.cmd.unregister
> register   - soup.user.cmd.register
> logout     - soup.user.cmd.logout
> login      - soup.user.cmd.login
    * */
    
    private IdentSystem idsys;
    
    public IdentCMD(IdentSystem idsys) {
        this.idsys = idsys;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Tell console she's not a player
            sender.sendMessage(SOUP.lang("misc.not-a-player"));
            return true;
        }
        
        Player player = (Player) sender;
        Credential cred = idsys.getCredential(player);
        switch (label.toLowerCase()) {
            case "login":
                if(!player.hasPermission("soup.user.cmd.login")) {
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                login(player, cred, args);
                break;
            case "logout":
                if(!player.hasPermission("soup.user.cmd.logout")) {
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                logout(player, cred, args);
                break;
            case "password":
                if(!player.hasPermission("soup.user.cmd.password")) {
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                password(player, cred, args);
                break;
            case "email":
                if(!player.hasPermission("soup.user.cmd.email")) {
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                email(player, cred, args);
                break;
            case "register":
                if(!player.hasPermission("soup.user.cmd.register")) {
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                register(player, cred, args);
                break;
            case "unregister":
                if(!player.hasPermission("soup.user.cmd.unregister")) {
                    player.sendMessage(SOUP.lang("misc.no-perm-cmd"));
                    return true;
                }
                unregister(player, cred, args);
                break;
            //setLoginSpawn(player, cred, args);
            // Unknown command: do nothing
        }
        
        return true;
    }
    
    private void email(Player player, Credential cred, String[] args) {
        // "/email set <email>"
        // "/email send"
        // "/email"
        
        if (args.length == 0) {
            // is the player registered? //
            if (!cred.registered) {
                player.sendMessage(SOUP.lang("cmds.msgs.register-please"));
                return;
            }
            
            // is the player NOT logged in? //
            if (!cred.identified) {
                player.sendMessage(SOUP.lang("cmds.msgs.login-please"));
                return;
            }
            
            // TELL CURRENT EMAIL
            if (cred.email == null)
                player.sendMessage(SOUP.lang("cmds.msgs.email-no-current"));
            else
                player.sendMessage(SOUP.lang("cmds.msgs.email-current", "{email}", idsys.getEmail(cred)));
            return;
        }
        
        switch (args[0]) {
            case "set":
                // is the player registered? //
                if (!cred.registered) {
                    player.sendMessage(SOUP.lang("cmds.msgs.register-please"));
                    return;
                }
                
                // is the player NOT logged in? //
                if (!cred.identified) {
                    player.sendMessage(SOUP.lang("cmds.msgs.login-please"));
                    return;
                }
                
                if (args.length != 2) {
                    player.sendMessage(SOUP.lang("cmds.usage.email-set"));
                    return;
                }
                
                // TODO SET EMAIL
                player.sendMessage(SOUP.lang("cmds.msgs.email-update", "{email}", args[1]));
                idsys.setEmail(cred, args[1]);
                break;
            case "send":
                if (args.length != 1) {
                    player.sendMessage(SOUP.lang("cmds.usage.email-send"));
                    return;
                }
                
                // TODO SEND EMAIL
                if (cred.email == null)
                    player.sendMessage(SOUP.lang("cmds.msgs.email-sorry"));
                else {
                    String email = idsys.getEmail(cred); // setup sender
                    idsys.sender.setTarget(email);
                    if (idsys.sender.sendMail(idsys.getPassword(cred))) // check if the message got through
                        player.sendMessage(SOUP.lang("cmds.msgs.email-send"));
                    else
                        player.sendMessage(SOUP.lang("cmds.msgs.email-error"));
                }
                break;
            default:
                player.sendMessage(SOUP.lang("cmds.usage.email"));
                break;
        }
    }
    
    private void password(Player player, Credential cred, String[] args) {
        // "/password <vieja contra.> <nueva contra.> <conf. nueva contra.>"
        if (args.length == 0) {
            player.sendMessage(SOUP.lang("cmds.msgs.password-see", "{password}", cred.password));
        } else if (args.length == 3) {
            if (!idsys.passwordMatches(cred, args[0])) {
                player.sendMessage(SOUP.lang("cmds.msgs.password-incorect"));
                return;
            }
            
            // las contraseñas no son iguales
            if (!args[1].equals(args[2])) {
                player.sendMessage(SOUP.lang("cmds.msgs.password-doesnt-match"));
                return;
            }
            
            // password size
            if (args[1].length() < idsys.min_password) {
                player.sendMessage(SOUP.lang("cmds.msgs.password-min-length", "{n}", idsys.min_password));
                return;
            }
            
            if (args[1].length() > idsys.max_password) {
                player.sendMessage(SOUP.lang("cmds.msgs.password-max-length", "{n}", idsys.max_password));
                return;
            }
            
            // bad password
            if (idsys.badpwords.contains(args[1])) {
                player.sendMessage(SOUP.lang("cmds.msgs.password-unsafe"));
                return;
            }
            
            idsys.setPassword(cred, args[1]);
            player.sendMessage(SOUP.lang("cmds.msgs.password-changed"));
        } else {
            player.sendMessage(SOUP.lang("cmds.usage.password"));
        }
    }
    
    private void unregister(Player player, Credential cred, String[] args) {
        // "/unregister <contra> <contra>" // 2 args obligatory //
        
        // is the player registered? //
        if (!cred.registered) {
            player.sendMessage(SOUP.lang("cmds.msgs.register-please"));
            return;
        }
        
        if (args.length == 2) {
            // do they match?
            if (!args[0].equals(args[1])) {
                player.sendMessage(SOUP.lang("cmds.msgs.password-doesnt-match"));
                return;
            }
            
            if (!idsys.passwordMatches(cred, args[0])) {
                player.sendMessage(SOUP.lang("cmds.msgs.password-incorrect"));
                return;
            }
            
            // UNREGISTER
            idsys.deleteRegister(player);
        } else {
            // show warning, the user clearly doesn't know what she is doing
            List<String> msgs = SOUP.langs("cmds.usage.unregister-warning");
            for (String s : msgs) player.sendMessage(s);
        }
        
    }
    
    private void register(Player player, Credential cred, String[] args) {
        // "/register <contra> <contra>" // 2 args obligatory //
        
        if (args.length != 2) {
            player.sendMessage(SOUP.lang("cmds.usage.register"));
            return;
        }
        
        // is player registered? //
        if (cred.registered) {
            player.sendMessage(SOUP.lang("cmds.msgs.register-already-here"));
            return;
        }
        
        // las contraseñas no son iguales
        if (!args[0].equals(args[1])) {
            player.sendMessage(SOUP.lang("cmds.msgs.password-doesnt-match"));
            return;
        }
        
        // password size
        if (args[0].length() < idsys.min_password) {
            player.sendMessage(SOUP.lang("cmds.msgs.password-min-length", "{n}", idsys.min_password));
            return;
        }
        
        if (args[0].length() > idsys.max_password) {
            player.sendMessage(SOUP.lang("cmds.msgs.password-max-length"));
            return;
        }
        
        // bad password
        if (idsys.badpwords.contains(args[0])) {
            player.sendMessage(SOUP.lang("cmds.msgs.password-unsafe"));
            return;
        }
        
        cred.registered = true;
        cred.identified = true;
        this.idsys.setPassword(cred, args[0]);
        List<String> msgs = SOUP.langs("cmds.msgs.register-welcome");
        for (String s : msgs) player.sendMessage(s);
    }
    
    private void logout(Player player, Credential cred, String[] args) {
        // "/logout" // 0 arg obligatory //
        
        if (args.length != 0) {
            player.sendMessage(SOUP.lang("cmds.usage.logout"));
            return;
        }
        
        // is the player registered? //
        if (!cred.registered) {
            player.sendMessage(SOUP.lang("cmds.msgs.register-please"));
            return;
        }
        
        // is the player NOT logged in? //
        if (!cred.identified) {
            player.sendMessage(SOUP.lang("cmds.msgs.login-please"));
            return;
        }
        
        // do it
        cred.identified = false;
        // TODO: FIRE UNLOGGING FROM HERE!
        this.idsys.initiatePlayer(player); // OK
    }
    
    private void login(Player player, Credential cred, String[] args) {
        // "/login <password>" // 1 arg obligatory //
        if (args.length != 1) {
            player.sendMessage(SOUP.lang("cmds.usage.login"));
            return;
        }
        
        // is the player registered? //
        if (!cred.registered) {
            player.sendMessage(SOUP.lang("cmds.msgs.register-please"));
            return;
        }
        
        // is the player already logged in? //
        if (cred.identified) {
            player.sendMessage(SOUP.lang("cmds.msgs.login-already-here"));
            return;
        }
        
        // check if the password is correct //
        if (idsys.passwordMatches(cred, args[0])) {
            // YES
            cred.attempts = 0;
            cred.identified = true;
            player.sendMessage(SOUP.lang("cmds.msgs.login-welcome"));
            
            if (player.getAddress() != null)
                cred.addressIP = player.getAddress().getAddress().getHostAddress();
        } else {
            // NO
            cred.attempts++;
            
            if (cred.attempts > idsys.max_attemps) {
                //player.teleport(cred.tempInfo.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                //cred.loadInv(player);
                player.kickPlayer(SOUP.lang("cmds.msgs.login-kick"));
            } else
                player.sendMessage(SOUP.lang("cmds.msgs.password-incorrect"));
        }
    }
    
}