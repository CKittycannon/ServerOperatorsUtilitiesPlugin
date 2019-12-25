package dev.ckitty.mc.soup.main;

import dev.ckitty.mc.soup.advtp.TeleReqCMD;
import dev.ckitty.mc.soup.advtp.TeleReqManager;
import dev.ckitty.mc.soup.advtp.WarpCMD;
import dev.ckitty.mc.soup.advtp.WarpManager;
import dev.ckitty.mc.soup.econ.EconCMD;
import dev.ckitty.mc.soup.econ.EconomyManager;
import dev.ckitty.mc.soup.econ.VaultEconAdapter;
import dev.ckitty.mc.soup.ident.IdentCMD;
import dev.ckitty.mc.soup.ident.IdentSystem;
import dev.ckitty.mc.soup.misc.MiscCMD;
import dev.ckitty.mc.soup.misc.SOUPCMD;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class SOUP extends JavaPlugin {
    
    /*
    
    public static void main(String[] args) {
        new MailSender().sendMail();
    }
    
    /**/
    
    /*
     * TODO LIST
     *
     * + Add trade and checks
     * + /fixme, /heal and /feed
     * + /suicide
     *
     * + Add jails
     * + Sign protection
     * */
    
    public static SOUP INSTANCE;
    
    public static IdentSystem IDSYSTEM;
    public static WarpManager WARPMAN;
    public static TeleReqManager TPXMANAGER;
    public static EconomyManager EMANAGER;
    public static WarpCMD WARPCMD;
    public static SOUPCMD SOUPCMD;
    
    public static VaultEconAdapter VECON;
    public static ConfigPair LANG;
    
    @Override
    public void onEnable() {
        INSTANCE = this;
        SOUPCMD = new SOUPCMD();
        IDSYSTEM = new IdentSystem();
        WARPMAN = new WarpManager();
        TPXMANAGER = new TeleReqManager();
        EMANAGER = new EconomyManager();
        VECON = new VaultEconAdapter(EMANAGER);
        EMANAGER.loadEconomy();
        
        // lang.yml
        File lang = new File(getDataFolder(), "lang.yml");
        if (!lang.exists()) export("lang.yml");
        LANG = new ConfigPair().setFile(lang).pack();
        
        // setup stuffs
        TPXMANAGER.setupRequestTicker();
        
        // Register Events
        this.getServer().getPluginManager().registerEvents(new SoupEvents(), this);
        
        // register vault
        Vault vault = (Vault) Bukkit.getPluginManager().getPlugin("Vault");
        Bukkit.getServicesManager().register(Economy.class, VECON, vault, ServicePriority.Normal);
        
        // > Ident commands
        IdentCMD identCMD = new IdentCMD(IDSYSTEM);
        this.getCommand("login").setExecutor(identCMD);
        this.getCommand("logout").setExecutor(identCMD);
        this.getCommand("register").setExecutor(identCMD);
        this.getCommand("unregister").setExecutor(identCMD);
        this.getCommand("email").setExecutor(identCMD);
        this.getCommand("password").setExecutor(identCMD);
        
        // > Warp commands
        WARPCMD = new WarpCMD(WARPMAN);
        this.getCommand("warp").setExecutor(WARPCMD);
        
        // > Teleport Request commands
        TeleReqCMD tpcmd = new TeleReqCMD();
        this.getCommand("tpask").setExecutor(tpcmd);
        this.getCommand("tphere").setExecutor(tpcmd);
        this.getCommand("tpok").setExecutor(tpcmd);
        this.getCommand("tpno").setExecutor(tpcmd);
        this.getCommand("tpcancel").setExecutor(tpcmd);
        
        // > eco commands
        EconCMD ecoCMD = new EconCMD();
        this.getCommand("eco").setExecutor(ecoCMD);
        this.getCommand("pay").setExecutor(ecoCMD);
        this.getCommand("money").setExecutor(ecoCMD);
        
        // > soup cmds
        this.getCommand("soup").setExecutor(SOUPCMD);
        
        /*
         * /spawn - no perm
         * /gmc
         * /gms
         * /gma
         * /gmz - soup.misc.gamemode
         * /fly - soup.misc.fly
         * /enderchest - soup.misc.enderchest
         * /suicide - soup.misc.suicide
         * /thunder - soup.misc.thunder
         * /invsee - soup.misc.invsee
         *
         * */
        MiscCMD mcmd = new MiscCMD();
        this.getCommand("spawn").setExecutor(mcmd);
        this.getCommand("gmc").setExecutor(mcmd);
        this.getCommand("gms").setExecutor(mcmd);
        this.getCommand("gma").setExecutor(mcmd);
        this.getCommand("gmz").setExecutor(mcmd);
        this.getCommand("fly").setExecutor(mcmd);
        this.getCommand("enderchest").setExecutor(mcmd);
        this.getCommand("suicide").setExecutor(mcmd);
        this.getCommand("thunder").setExecutor(mcmd);
        this.getCommand("invsee").setExecutor(mcmd);
        this.getCommand("feed").setExecutor(mcmd);
        this.getCommand("heal").setExecutor(mcmd);
    }
    
    @Override
    public void onDisable() {
        IDSYSTEM.saveCredentials();
        WARPMAN.saveWarps();
        EMANAGER.saveEconomy();
        
        // Unregister events
        HandlerList.unregisterAll(this);
        
        // Unregister Commands? Nah
        // cancel runnables
        Bukkit.getScheduler().cancelTasks(this); // OK
        
        // remove instance
        SOUPCMD = null;
        INSTANCE = null;
    }
    
    public void exportIfMissing(String file) {
        if (!new File(getDataFolder(), file).exists())
            export(file);
    }
    
    public String loadText(String file) {
        File fileobj = new File(getDataFolder(), file);
        try {
            return new String(Files.readAllBytes(fileobj.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void export(String file) {
        this.saveResource(file, true);
    }
    
    public static String lang(String msg, Object... objs) {
        String target = LANG.data().getString(msg);
        
        if(target == null) {
            return "[SOUP] The message " + msg + " does not exist!";
        }
        
        for (int i = 0; i < objs.length; i += 2) {
            target = target.replace(objs[i].toString(), objs[i + 1].toString());
        }
        return target;
    }
    
    public static List<String> langs(String msg) {
        List<String> target = LANG.data().getStringList(msg);
        return target;
    }
    
}
