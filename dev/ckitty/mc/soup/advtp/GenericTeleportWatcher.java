package dev.ckitty.mc.soup.advtp;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public abstract class GenericTeleportWatcher extends BukkitRunnable {

    protected Player player;
    int count;
    private Vector prevloc;
    
    @Override
    public void run() {
        // check if player has moved! (or isn't online!)
        // updateLocation(); DO NOT UPDATE LOCATION IT'S A ONE-TIME ONLY!!!
        if(hasMoved() || !player.isOnline()) {
            this.cancel(); // cancel and abort!
            this.forceAbort();
            return;
        }
        
        // execute tick actions, if needed
        this.onTick();
        
        // go down!
        count--;
        if(count <= 0) {
            this.cancel();
            this.countIsDone();
        }
    }
    
    public abstract void onTick();
    public abstract void countIsDone();
    public abstract void forceAbort();
    
    public boolean hasMoved() {
        return !prevloc.equals(player.getLocation().toVector());
    }
    
    public void updateLocation() {
        prevloc = player.getLocation().toVector();
    }

}
