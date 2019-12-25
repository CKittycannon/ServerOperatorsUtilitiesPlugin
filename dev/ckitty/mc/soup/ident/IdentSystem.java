package dev.ckitty.mc.soup.ident;

import dev.ckitty.mc.soup.main.ConfigPair;
import dev.ckitty.mc.soup.main.SOUP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class IdentSystem {
    
    private Map<UUID, Credential> users = new HashMap<>();
    List<String> badpwords = new ArrayList<>();
    private List<String> wlcmds;
    private LWCrypter crypter;
    private Location spawn;
    private ConfigPair config;
    MailSender sender;
    public boolean hold_msgs;
    
    int reminder, max_password, min_password, kick_timer, max_attemps;
    
    public IdentSystem() {
        // SOUP.INSTANCE.export("credentials.yml"); //
        config = new ConfigPair().setFile(SOUP.INSTANCE.getDataFolder(), "credentials.yml").pack();
        loadCredentials();
        
        // whitelist cmds!!!
        wlcmds = config.data().getStringList("config.cmd-whitelist");
        
        // mail sender
        SOUP.INSTANCE.exportIfMissing("email.txt");
        sender = new MailSender();
        sender.setHost(config.data().getString("config.email.host"));
        sender.setUser(config.data().getString("config.email.user"), config.data().getString("config.email.password"));
        sender.setMessage(config.data().getString("config.email.title"), SOUP.INSTANCE.loadText("email.txt"));
    }
    
    // reload config
    public void reloadConfig() {
        config.reload();
    }
    
    // cmd whitelist
    public boolean cmdCanGo(String msg) {
        if(msg.startsWith("/"))
            msg = msg.substring(1, msg.length() - 1);
        
        for(String s : wlcmds) {
            if(msg.toLowerCase().startsWith(s.toLowerCase()))
                return true;
        }
        return false;
    }
    
    // quick method for some things
    public boolean identified(Player player) {
        return getCredential(player).identified;
    }
    
    // =================================================================== // SAVE //
    public void saveCredentials() {
        config.data().set("users", null); // wipe file data
        for (UUID id : users.keySet()) {
            String path = "users." + id.toString();
            Credential cred = users.get(id);
            
            config.data().set(path + ".registered", cred.registered);
            config.data().set(path + ".password", cred.password);
            config.data().set(path + ".email", cred.email);
        }
        
        // bad passwords
        config.data().set("config.password.unsafe", badpwords);
        
        // cryptokey //
        config.data().set("config.cryptokey", crypter.getKey());
        
        // loginspawn //
        if (spawn != null) {
            config.data().set("config.loginspawn.pos", spawn.toVector());
            config.data().set("config.loginspawn.pit", spawn.getPitch());
            config.data().set("config.loginspawn.yaw", spawn.getYaw());
            config.data().set("config.loginspawn.world", spawn.getWorld().getName());
        }
        
        config.data().set("config.reminder-timer", reminder);
        config.data().set("config.password.min-length", min_password);
        config.data().set("config.password.max-length", max_password);
        config.data().set("config.kick-timer", kick_timer);
        config.data().set("config.max-attempts", max_attemps);
        
        config.save();
    }
    
    // =================================================================== // LOAD //
    private void loadCredentials() {
        //* configs *//
        if (!config.data().contains("config.cryptokey")) {
            // file does not exist or it's invalid. make a new one.
            SOUP.INSTANCE.export("credentials.yml");
            config.reload();
            
            // add random cryptokey
            config.data().set("config.cryptokey",
                    new Random((long) (Math.random() * System.nanoTime())).nextInt(201) - 100);
            config.save();
        }
        
        // ¿?
        config.reload();
        
        // unsafe passwords & add vars
        badpwords.addAll(config.data().getStringList("config.password.unsafe"));
        hold_msgs = config.data().getBoolean("config.hold-msgs");
        
        // cryptokey //
        crypter = new LWCrypter(config.data().getInt("config.cryptokey"));
        // loginspawn //
        if (config.data().contains("config.loginspawn")) {
            Vector vec = config.data().getVector("config.loginspawn.pos");
            float pit = (float) config.data().getDouble("config.loginspawn.pit");
            float yaw = (float) config.data().getDouble("config.loginspawn.yaw");
            World world = Bukkit.getWorld(config.data().getString("config.loginspawn.world"));
            
            this.spawn = new Location(world, vec.getX(), vec.getY(), vec.getZ(), yaw, pit);
        }
        
        reminder = config.data().getInt("config.reminder-timer");
        min_password = config.data().getInt("config.password.min-length");
        max_password = config.data().getInt("config.password.max-length");
        kick_timer = config.data().getInt("config.kick-timer");
        max_attemps = config.data().getInt("config.max-attempts");
        
        //* Everything else *//
        ConfigurationSection section = config.data().getConfigurationSection("users");
        if (section == null) return;
        
        users.clear(); // wipe OUR data
        for (String idstr : section.getKeys(false)) {
            // System.out.println("SOUP >> " + idstr);
            UUID uuid = UUID.fromString(idstr);
            boolean regist = section.getBoolean(idstr + ".registered");
            String pword = section.getString(idstr + ".password");
            String email = section.getString(idstr + ".email");
            
            Credential cred = new Credential();
            cred.registered = regist;
            cred.password = pword;
            cred.identified = false;
            cred.attempts = 0;
            cred.lastSeen = -1;
            cred.email = email;
            cred.addressIP = "00.00.00.000:00000";
            
            users.put(uuid, cred); // puttings!
        }
        
    }
    
    public Location getSpawn() {
        return spawn;
    }
    
    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }
    
    public Credential initiatePlayer(Player player) {
        // - Fire a runnable that bothers the player to log in! - //
        //Player player = e.getPlayer();
        
        //**  Save the inventory and location of the player  **/
        Credential cred = getCredential(player);
        cred.createInfo();
        cred.saveInv(player);
        
        // moved code to a proper class
        new Botherer(player, cred).runTaskTimerAsynchronously(SOUP.INSTANCE, 0, 1);
        
        // return credential, very useful and optimizing
        return cred;
    }
    
    private void taskAddBlind(Player player) {
        Bukkit.getScheduler().runTaskLater(SOUP.INSTANCE,
                () -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false), true),
                0);
    }
    
    void taskDelBlind(Player player) {
        Bukkit.getScheduler().runTaskLater(SOUP.INSTANCE,
                () -> player.removePotionEffect(PotionEffectType.BLINDNESS),
                0);
    }
    
    private void taskTp(Player p, Location loc) {
        Bukkit.getScheduler().runTaskLater(SOUP.INSTANCE,
                () -> p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN),
                0);
    }
    
    public boolean terminatePlayer(Player player) {
        return playerLoggedOut(player, getCredential(player));
    }
    
    private void playerLoggedIn(Player player, Credential cred) {
        // if hold-msgs then broadcast msg
        if(hold_msgs) {
            String msg = cred.tempInfo.message;
            Bukkit.getScheduler().runTaskAsynchronously(SOUP.INSTANCE,
                    () -> Bukkit.broadcastMessage(msg));
        }
        
        // at this point, destroy tempInfo
        cred.deleteInfo();
    }
    
    // return if it should hold msgs
    private boolean playerLoggedOut(Player player, Credential cred) {
        cred.identified = false;
        cred.lastSeen = System.nanoTime();
        
        // may get null!
        if (player.getAddress() != null)
            cred.addressIP = player.getAddress().getAddress().getHostAddress();
    
        // load inv if not identified!!!
        // note: if the player doesnt login, it never is identified and
        // tempInfo is not null!
        // HEYYY: WE JUST SET IDENTIFIED TO FALSE, NO NEED TO CHECK IT AGHHH
        if(cred.tempInfo != null) { // this should get npe
            player.teleport(cred.tempInfo.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            cred.loadInv(player);
            
            // hold messages if hold-msgs is true
            return true;
        }
        
        // we can pretty much delete tempInfo now
        cred.deleteInfo();
        
        return false;
    }
    
    void setPassword(Credential cred, String raw) {
        cred.password = crypter.encrypt(raw);
    }
    
    String getPassword(Credential cred) {
        return crypter.decrypt(cred.password);
    }
    
    void setEmail(Credential cred, String raw) {
        cred.email = crypter.encrypt(raw);
    }
    
    String getEmail(Credential cred) {
        return crypter.decrypt(cred.email);
    }
    
    boolean passwordMatches(Credential cred, String password) {
        String crypted = crypter.encrypt(password);
        return crypted.equals(cred.password);
    }
    
    void deleteRegister(Player player) {
        users.remove(player.getUniqueId());
        player.kickPlayer(SOUP.lang("cmds.msgs.unregister-goodbye"));
    }
    
    Credential getCredential(Player player) {
        Credential cred = users.get(player.getUniqueId());
        if (cred != null) return cred; // quick return if she has connected before
        
        cred = new Credential();
        cred.registered = false;
        cred.identified = false;
        cred.attempts = 0;
        cred.lastSeen = System.nanoTime();
        cred.password = null;
        cred.email = null;
        
        // may get null!
        if (player.getAddress() != null)
            cred.addressIP = player.getAddress().getAddress().getHostAddress();
        
        // add this credential to the user
        users.put(player.getUniqueId(), cred);
        return cred;
    }
    
    
    // BUKKIT RUNNABLE ==================================================== BUKKIT RUNNABLE //
    class Botherer extends BukkitRunnable {
    
        // Runnable variables
        int kick = kick_timer * 20;
        int bother = reminder; // starts at reminder so in the first it starts
        Player player;
        Credential cred;
        
        Botherer(Player player, Credential cred) {
            this.player = player;
            this.cred = cred;
        }
    
        @Override
        public void run() {
            // cancel if player is offline!!
            if(!player.isOnline()) {
                this.cancel();
                return;
            }
            
            bother++;
        
            // Each 20 ticks (1 second), send a message
            if (bother >= reminder) {
                bother = 0;
                if (cred.registered)
                    player.sendMessage(SOUP.lang("cmds.msgs.login-bother-please"));
                else
                    player.sendMessage(SOUP.lang("cmds.msgs.register-bother-please"));
            }
        
            // teleport to "spawn"
            if (spawn != null)
                taskTp(player, spawn);
        
            // apply blindness
            IdentSystem.this.taskAddBlind(player);
        
            // check if credential is not null and is identified
            if (cred.registered && cred.identified) {
                this.cancel();
                taskTp(player, cred.tempInfo.location); // throws npe!!!
                cred.loadInv(player);
                playerLoggedIn(player, cred);
            }
        
            kick--;
            if (kick == 0) {
                //taskTp(player, cred.tempInfo.location);
                //cred.loadInv(player);
                Bukkit.getScheduler().runTaskLater(SOUP.INSTANCE,
                        () -> player.kickPlayer(SOUP.lang("cmds.msgs.login-timeout")),
                        0);
                this.cancel();
            }
        }
    
    
    }
    
}
