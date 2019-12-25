package dev.ckitty.mc.soup.main;

import dev.ckitty.mc.soup.ident.Credential;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;

import static dev.ckitty.mc.soup.main.SOUP.IDSYSTEM;

public class SoupEvents implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onRespawn(PlayerRespawnEvent e) {
        if(!e.isBedSpawn()) { // lolololol
            Location spawn = SOUP.SOUPCMD.getSpawn();
            if(spawn != null) e.setRespawnLocation(spawn);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        // hold login message // IF DOESNT WORK TRY PRIORITY HIGHEST //
        // e.setJoinMessage(null);
        // goto login
        Credential cred = IDSYSTEM.initiatePlayer(e.getPlayer());
        
        // if hold-msgs then avoid msgs. hold the message in tempInfo
        if(IDSYSTEM.hold_msgs) {
            cred.setMessage(e.getJoinMessage());
            e.setJoinMessage(null);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        // goto logout
        if(IDSYSTEM.terminatePlayer(e.getPlayer()) && IDSYSTEM.hold_msgs) {
            e.setQuitMessage(""); // hold msgs
        }
        // remove leave msg if not identified //
//        if(!SOUP.IDSYSTEM.identified(e.getPlayer()))
//            e.setQuitMessage(null);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        // return is it's a command // if doesnt work...
        // cancell if player isn't identified
        if(!IDSYSTEM.identified(e.getPlayer()))
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(EntityPickupItemEvent e) {
        // [IDSYS] test if it's a player and it's identified
        if(e.getEntity() instanceof Player && !IDSYSTEM.identified((Player) e.getEntity())) {
            // cancell event if it's not identified
            e.setCancelled(true);
        }
        // [IDSYS] end
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent e) {
        // [IDSYS] avoid damage if not identified
        if(e.getEntity() instanceof Player && !IDSYSTEM.identified((Player) e.getEntity())) {
            // cancell event if it's not identified
            e.setCancelled(true);
        }
        // [IDSYS] end
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        // [IDSYS] avoid damage if not identified
        if(!IDSYSTEM.identified(e.getPlayer())) {
            // cancell event if it's not whitelisted!!!
            if(!IDSYSTEM.cmdCanGo(e.getMessage())) {
                e.getPlayer().sendMessage(SOUP.lang("cmds.msgs.login-please"));
                e.setCancelled(true);
            }
        }
        // [IDSYS] end
    }
    
}
