package dev.ckitty.mc.soup.ident;

import dev.ckitty.mc.soup.main.SOUP;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Credential {
    
    public String password, email, addressIP;
    
    public long lastSeen;
    public boolean registered, identified;
    public int attempts;
    
    // Temporary information when people enter without loging in
    public TempInfo tempInfo;
    
    public void createInfo() {
        tempInfo = new TempInfo();
    }
    
    public void deleteInfo() { // upsy daisy
        tempInfo = null;
    }
    
    public String getMessage() {
        return tempInfo.message;
    }
    
    public void setMessage(String msg) {
        tempInfo.message = msg;
    }
    
    public void saveInv(Player player) {
        // tempInfo = new TempInfo();
        PlayerInventory inv = player.getInventory();
        
        tempInfo.conts = inv.getContents();
        tempInfo.armor = inv.getArmorContents();
        tempInfo.extra = inv.getExtraContents();
        
        tempInfo.location = player.getLocation().clone();
        tempInfo.level = player.getLevel();
        tempInfo.xp = player.getExp();
        
        inv.clear();
        player.setLevel(0);
        player.setExp(0f);
    }
    
    public void loadInv(Player player) {
        // check if npe
        if(this.tempInfo == null) return;
        
        SOUP.IDSYSTEM.taskDelBlind(player);
        PlayerInventory inv = player.getInventory();
        
        // print conts
        
        inv.setContents(tempInfo.conts);
        inv.setArmorContents(tempInfo.armor); // throws npe!!!
        inv.setExtraContents(tempInfo.extra);
        
        // cant tp while async!
        //player.teleport(tempInfo.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setExp(tempInfo.xp);
        player.setLevel(tempInfo.level);
        
        // tempInfo = null;
    }
    
    class TempInfo {
        
        public int level;
        public float xp;
        public Location location;
        
        public ItemStack[] conts;
        public ItemStack[] armor;
        public ItemStack[] extra;
        
        String message;
        
    }
    
}
