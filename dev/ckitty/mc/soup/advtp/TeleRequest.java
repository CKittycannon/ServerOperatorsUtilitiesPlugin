package dev.ckitty.mc.soup.advtp;

import dev.ckitty.mc.soup.main.SOUP;
import dev.ckitty.mc.ticktime.TickTime;
import org.bukkit.entity.Player;

public class TeleRequest {

    /*
    * Players:
    *  - source: the one who sends the request
    *  - target: the one who gets requested
    * Direction:
    *  - true: source -> target (/tpa)
    *  - false: target -> source (/tph)
    * */
    
    public Player source, target;
    public boolean direction;
    public long c_time;
    
    public boolean isValid() {
        if(!source.isOnline() || !target.isOnline())
            return false;
        // warp manager, are they on allowed worlds?
        WarpManager man = SOUP.WARPMAN;
        
        // check worlds
        if(!man.worldInBlacklist(source.getWorld().getName()))
            return false;
        if(!man.worldInBlacklist(target.getWorld().getName()))
            return false;
        
        // finalized checks
        return true;
    }

}
