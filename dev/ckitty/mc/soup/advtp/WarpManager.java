package dev.ckitty.mc.soup.advtp;

import dev.ckitty.mc.soup.main.ConfigPair;
import dev.ckitty.mc.soup.main.SOUP;
import dev.ckitty.mc.soup.misc.SOUPCMD;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class WarpManager {
    
    Set<Warp> warps = new HashSet<>();
    private List<String> worldblacklist;
    private ConfigPair config;
    int count, timeout;
    
    public WarpManager() {
        // export warps.yml if not found!
        SOUP.INSTANCE.exportIfMissing("warps.yml");
    
        // prepare config!
        config = new ConfigPair().setFile(SOUP.INSTANCE.getDataFolder(), "warps.yml").pack();
        loadWarps();
    }
    
    public boolean worldInBlacklist(String name) {
        return worldblacklist.contains(name);
    }
    
    public void reloadConfig() {
        this.config.reload();
    }
    
    private void loadWarps() {
        // whipe our data
        warps.clear();
        
        // reload
        config.reload();
        
        // add the count
        count = config.data().getInt("config.wait-tp");
        timeout = config.data().getInt("config.tpx-timeout");
        worldblacklist = config.data().getStringList("config.worlds-blacklist");
        
        // gets spawn
        ConfigurationSection spawn = config.data().getConfigurationSection("config.spawn");
        if(spawn != null) {
            World w = Bukkit.getWorld(spawn.getString("world"));
            Vector vec = spawn.getVector("loc");
            double yaw = spawn.getDouble("yaw");
            double pit = spawn.getDouble("pit");
            Location loc = new Location(w, vec.getX(), vec.getY(), vec.getZ(), (float) yaw, (float) pit);
            SOUP.SOUPCMD.setSpawn(loc); // ok
        }
        
        // load and add warps
        ConfigurationSection section = config.data().getConfigurationSection("warps");
        if (section == null) return;
        
        for (String s : section.getKeys(false)) {
            // s is the name of the address!
            
            // position
            double x = section.getDouble(s + ".x");
            double y = section.getDouble(s + ".y");
            double z = section.getDouble(s + ".z");
            
            // direction
            float pit = (float) section.getDouble(s + ".pit");
            float yaw = (float) section.getDouble(s + ".yaw");
            
            // world
            World world = Bukkit.getWorld(section.getString(s + ".world"));
            
            // pack everything
            addWarp(s, new Location(world, x, y, z, yaw, pit));
        }
        
    }
    
    public void saveWarps() {
        // wipe file data
        config.data().set("warps", null);
        
        // save spawn
        Location spawn = SOUP.SOUPCMD.getSpawn();
        if(spawn != null) {
            config.data().set("config.spawn.world", spawn.getWorld().getName());
            config.data().set("config.spawn.loc", spawn.toVector());
            config.data().set("config.spawn.yaw", spawn.getYaw());
            config.data().set("config.spawn.pit", spawn.getPitch());
        }
        
        // save our data
        for (Warp w : warps) {
            // path
            String path = "warps." + w.name;
            
            config.data().set(path + ".x", w.location.getX());
            config.data().set(path + ".y", w.location.getY());
            config.data().set(path + ".z", w.location.getZ());
            
            config.data().set(path + ".yaw", w.location.getYaw());
            config.data().set(path + ".pit", w.location.getPitch());
            
            config.data().set(path + ".world", w.location.getWorld().getName());
        }
        
        config.save();
    }
    
    void taskWarp(Player player, Warp warp) {
        // blacklisted world?
        if(worldInBlacklist(player.getWorld().getName())) {
            player.sendMessage(SOUP.lang("cmds.msgs.warp-blacklisted-world"));
            return;
        }
        
        // wait disabled?
        if (count == 0) {
            player.sendMessage(SOUP.lang("cmds.msgs.warp-teleport", "{warpname}", warp.name));
            player.teleport(warp.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
            // create watcher object
            WarpWatcher watcher = new WarpWatcher(player, warp, count * 20);
            watcher.updateLocation(); // tuturu~
            player.sendMessage(SOUP.lang("cmds.msgs.warp-waiting", "{warpname}", warp.name, "{secs}", count));
            watcher.runTaskTimerAsynchronously(SOUP.INSTANCE, 0, 1); // run every tick
        }
    }
    
    public void taskSpawn(Player player) {
        // blacklisted world?
        if(worldInBlacklist(player.getWorld().getName())) {
            player.sendMessage(SOUP.lang("cmds.msgs.warp-blacklisted-world"));
            return;
        }
    
        // wait disabled?
        if (count == 0) {
            player.sendMessage(SOUP.lang("cmds.msgs.spawn-teleporting"));
            player.teleport(SOUP.SOUPCMD.getSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
            // create watcher object
            GenericTeleportWatcher watcher = new GenericTeleportWatcher() {
                
                @Override
                public void onTick() {
                    // do nothing
                }
    
                @Override
                public void countIsDone() {
                    player.sendMessage(SOUP.lang("cmds.msgs.spawn-teleporting"));
                    Bukkit.getScheduler().runTask(SOUP.INSTANCE,
                            () -> player.teleport(SOUP.SOUPCMD.getSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN));
                }
    
                @Override
                public void forceAbort() {
                    player.sendMessage(SOUP.lang("cmds.msgs.spawn-abort"));
                }
                
            };
            watcher.player = player;
            watcher.count = count * 20;
            watcher.updateLocation(); // tuturu~
            player.sendMessage(SOUP.lang("cmds.msgs.spawn-waiting", "{secs}", count));
            watcher.runTaskTimerAsynchronously(SOUP.INSTANCE, 0, 1); // run every tick
        }
    }
    
    Warp getWarp(String name) {
        for (Warp w : warps) {
            if (w.name.equalsIgnoreCase(name)) return w;
        }
        return null;
    }
    
    void addWarp(String name, Location loc) {
        warps.add(new Warp(name, loc));
    }
    
    void delWarp(String name) {
        Iterator<Warp> it = warps.iterator();
        while(it.hasNext()) {
            if(it.next().name.equalsIgnoreCase(name)) {
                it.remove();
                return;
            }
        }
    }
    
    private void taskTP(Player player, Warp warp) {
        // for async purposes!
        Bukkit.getScheduler().runTaskLater(SOUP.INSTANCE,
                () -> player.teleport(warp.location, PlayerTeleportEvent.TeleportCause.PLUGIN),
                0);
    }
    
    public static class Warp {
        
        String name;
        Location location;
        
        Warp(String name, Location location) {
            this.name = name;
            this.location = location;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Warp warp = (Warp) o;
            return name.equalsIgnoreCase(warp.name); // mk it simpler
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
    
    // watcher class for warps
    class WarpWatcher extends GenericTeleportWatcher {
        
        Warp warp;
        
        WarpWatcher(Player player, Warp warp, int count) {
            this.player = player;
            this.count = count;
            this.warp = warp;
        }
        
        @Override
        public void onTick() {
            // do nothing lol
        }
        
        @Override
        public void countIsDone() {
            // execute teleport
            player.sendMessage(SOUP.lang("cmds.msgs.warp-teleport", "{warpname}", warp.name));
            taskTP(player, warp);
        }
        
        @Override
        public void forceAbort() {
            player.sendMessage(SOUP.lang("cmds.msgs.warp-abort", "{warpname}", warp.name));
        }
        
    }
    
}
