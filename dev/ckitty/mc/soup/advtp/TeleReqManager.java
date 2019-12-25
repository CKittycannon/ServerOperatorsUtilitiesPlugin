package dev.ckitty.mc.soup.advtp;

import dev.ckitty.mc.soup.main.SOUP;
import dev.ckitty.mc.ticktime.TickTime;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

public class TeleReqManager {
    
    private Map<UUID, TeleRequest> requests = new HashMap<>();
    
    void cancelRequest(Player sender) {
        TeleRequest req = requests.get(sender.getUniqueId());
        if(req == null) { // no requests
            sender.sendMessage(SOUP.lang("cmds.msgs.tpx-no-requests-sent"));
        } else { // cancel
            // tell info
            req.source.sendMessage(SOUP.lang("cmds.msgs.tpx-cancel-source", "{target}", req.target.getName()));
            req.target.sendMessage(SOUP.lang("cmds.msgs.tpx-cancel-target", "{source}", req.source.getName()));
            
            // remove
            requests.remove(req);
        }
    }
    
    private void executeResponse(TeleRequest req, boolean response) {
        // REMOVE REQUEST NOW!!!
        requests.remove(req.source.getUniqueId()); // ok
        
        // tell info
        if(response) {
            // tell yes
            req.source.sendMessage(SOUP.lang("cmds.msgs.tpx-accepted-source", "{target}", req.target.getName()));
            req.target.sendMessage(SOUP.lang("cmds.msgs.tpx-accepted-target", "{source}", req.source.getName()));
        } else {
            // tell no
            req.source.sendMessage(SOUP.lang("cmds.msgs.tpx-rejected-source", "{target}", req.target.getName()));
            req.target.sendMessage(SOUP.lang("cmds.msgs.tpx-rejected-target", "{source}", req.source.getName()));
            return; // stop here
        }
        
        
        /*
         * - true: source -> target (/tpa)
         * - false: target -> source (/tph)
         * */
        Player dest, move; // set the moved and destination player
        if(req.direction) {
            move = req.source;
            dest = req.target;
        } else {
            move = req.target;
            dest = req.source;
        }
        
        // count, is it zero?
        int count = SOUP.WARPMAN.count;
        
        // tell info
        if(count == 0) {
            // ups, forgot to check if players are in a blacklisted world... FIXED
            if (invalidWorld(move)) { // source is in a blacklisted world
                move.sendMessage(SOUP.lang("cmds.msgs.tpx-source-bw"));
                dest.sendMessage(SOUP.lang("cmds.msgs.tpx-target-bw", "{target}", move.getName()));
                return;
            }
    
            if (invalidWorld(dest)) { // target is in a blacklisted world
                move.sendMessage(SOUP.lang("cmds.msgs.tpx-target-bw", "{target}", dest.getName()));
                dest.sendMessage(SOUP.lang("cmds.msgs.tpx-source-bw"));
                return;
            }
            
            // tell info
            move.sendMessage(SOUP.lang("cmds.msgs.tpx-now-tp", "{dest}", dest.getName()));
            dest.sendMessage(SOUP.lang("cmds.msgs.tpx-arrived-tp", "{move}", move.getName()));
            return;
        }
        
        // regular msg
        move.sendMessage(SOUP.lang("cmds.msgs.tpx-wait-tp", "{n}", count));
        
        // runnable
        TeleReqWatcher watcher = new TeleReqWatcher(dest, move, count * 20);
        // CURRENTLY TESTING IF WATCHER CAN BE ASYNC!!!
        watcher.updateLocation(); // tuturu~
        watcher.runTaskTimerAsynchronously(SOUP.INSTANCE, 0, 1); // SYNCHRONOUS BECAUSE IT'S EASIER TO HANDLE ADDS AND DELETES!!!
    }
    
    void respondRequest(Player target, String caller, boolean response) {
        // get requests by the target
        Set<Map.Entry<UUID, TeleRequest>> set = requestsByTarget(target);
        
        // is there 0 requests?
        if(set.isEmpty()) {
            // no requests
            target.sendMessage(SOUP.lang("cmds.msgs.tpx-no-requests"));
            return;
        }
        
        // does the caller is specified?
        if(caller == null) {
            // are there 2 or more?
            if(set.size() >= 2) {
                target.sendMessage(SOUP.lang("cmds.msgs.tpx-multiple-requests"));
                return;
            }
            
            // accept the one request.
            // TODO ACCEPT REQUEST
            executeResponse(set.iterator().next().getValue(), response);
        } else {
            // does the target has a caller with that name?
            TeleRequest request;
            for(Map.Entry<UUID, TeleRequest> e : set) {
                request = e.getValue();
                
                if(request.target.getName().equalsIgnoreCase(caller)) {
                    // TODO ACCEPT REQUEST
                    executeResponse(request, response);
                    return;
                }
            }
            
            // Tell target there is no such caller
            target.sendMessage(SOUP.lang("cmds.msgs.tpx-no-such-caller", "{target}", caller));
        }
    }
    
    private Set<Map.Entry<UUID, TeleRequest>> requestsByTarget(Player target) {
        Set<Map.Entry<UUID, TeleRequest>> set = requests.entrySet();
        Set<Map.Entry<UUID, TeleRequest>> res = new HashSet<>();
        TeleRequest req;
    
        for (Map.Entry<UUID, TeleRequest> e : set) {
            req = e.getValue();
            if (req.target.equals(target)) // if the target match, add
                res.add(e);
        }
        
        // return results
        return res;
    }
    
    public void setupRequestTicker() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(SOUP.INSTANCE, this::tickreqs, 0, 1);
    }
    
    private void tickreqs() {
        Iterator<Map.Entry<UUID, TeleRequest>> it = requests.entrySet().iterator();
        Map.Entry<UUID, TeleRequest> entry;
        TeleRequest req;
        int timeout = SOUP.WARPMAN.timeout;
        
        while (it.hasNext()) {
            // tick request
            entry = it.next();
            req = entry.getValue();
            
            // check if hasn't timed out
            if (req.c_time + timeout * 20 < TickTime.currentTickTime()) {
                // REQUEST IS OLD TAN TAN TAN, tell players
                req.source.sendMessage(SOUP.lang("cmds.msgs.tpx-source-timeout", "{target}", req.target.getName()));
                req.target.sendMessage(SOUP.lang("cmds.msgs.tpx-target-timeout", "{source}", req.source.getName()));
                // should delete it via iterator
                it.remove();
                // continue;
            }
            
            // do nothing lols
        }
    }
    
    void fireRequest(Player source, boolean dir, Player target) {
        // check worlds for players
        if (invalidWorld(source)) { // source is in a blacklisted world
            source.sendMessage(SOUP.lang("cmds.msgs.tpx-source-bw"));
            return;
        }
        
        if (invalidWorld(target)) { // target is in a blacklisted world
            target.sendMessage(SOUP.lang("cmds.msgs.tpx-target-bw", "{target}", target.getName()));
            return;
        }
        
        // create request
        TeleRequest req = this.createRequest(source, dir, target);
        
        // check source prev. req.
        // source should not have more than one
        Player prev = this.sourceCanRequest(source);
        if (prev != null) {
            // source has a previous, unanswered request.
            source.sendMessage(SOUP.lang("cmds.msgs.tpx-source-has-request", "{previousreq}", prev.getName()));
            // here copy it: .sendMessage(SOUP.lang(""));
            return;
        }
        
        // All good I guess. Send the request to target
        if (dir) {
            // tpa
            source.sendMessage(SOUP.lang("cmds.msgs.tpa-send", "{target}", target.getName()));
            target.sendMessage(SOUP.lang("cmds.msgs.tpa-recieve", "{source}", source.getName()));
        } else {
            // tph
            source.sendMessage(SOUP.lang("cmds.msgs.tph-send", "{target}", target.getName()));
            target.sendMessage(SOUP.lang("cmds.msgs.tph-recieve", "{source}", source.getName()));
        }
        
        // add request to the list
        requests.put(source.getUniqueId(), req);
    }
    
    private boolean invalidWorld(Player p) {
        return SOUP.WARPMAN.worldInBlacklist(p.getWorld().getName());
    }
    
    private TeleRequest createRequest(Player source, boolean dir, Player target) {
        // setup request
        TeleRequest req = new TeleRequest();
        req.source = source;
        req.direction = dir;
        req.target = target;
        req.c_time = TickTime.currentTickTime(); // get time on ticks
        // send request
        return req;
    }
    
    private Player sourceCanRequest(Player source) {
        // test if source doesn't has a request already
        TeleRequest other = getReqBySource(source);
        if (other != null) return other.target;
        return null;
    }
    
    private TeleRequest getReqBySource(Player source) {
        return requests.get(source.getUniqueId());
    }
    
    private class TeleReqWatcher extends GenericTeleportWatcher {
    
        Player dest;
        
        TeleReqWatcher(Player dest, Player move, int count) {
            this.player = move;
            this.dest = dest;
            this.count = count;
        }
    
        @Override
        public void onTick() {
            if(!dest.isOnline()) {
                this.cancel();
                forceAbort();
            }
        }
    
        @Override
        public void countIsDone() {
            if (invalidWorld(player)) { // source is in a blacklisted world
                player.sendMessage(SOUP.lang("cmds.msgs.tpx-source-bw"));
                dest.sendMessage(SOUP.lang("cmds.msgs.tpx-target-bw", "{target}", player.getName()));
                return;
            }
    
            if (invalidWorld(dest)) { // target is in a blacklisted world
                player.sendMessage(SOUP.lang("cmds.msgs.tpx-target-bw", "{target}", dest.getName()));
                dest.sendMessage(SOUP.lang("cmds.msgs.tpx-source-bw"));
                return;
            }
            
            // send info
            player.sendMessage(SOUP.lang("cmds.msgs.tpx-now-tp", "{dest}", dest.getName()));
            dest.sendMessage(SOUP.lang("cmds.msgs.tpx-arrived-tp", "{move}", player.getName()));
            
            // tasktp
            Bukkit.getScheduler().runTaskLater(SOUP.INSTANCE,
                    () -> player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN),
                    0);
        }
    
        @Override
        public void forceAbort() {
            player.sendMessage(SOUP.lang("cmds.msgs.tpx-unexpected-tp"));
            dest.sendMessage(SOUP.lang("cmds.msgs.tpx-unexpected-tp"));
        }
    }
    
}
