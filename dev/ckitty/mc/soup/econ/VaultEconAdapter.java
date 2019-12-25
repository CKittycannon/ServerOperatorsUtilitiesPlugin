package dev.ckitty.mc.soup.econ;

import dev.ckitty.mc.soup.main.SOUP;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

public class VaultEconAdapter implements Economy {
    
    EconomyManager manager;
    
    public VaultEconAdapter(EconomyManager manager) {
        this.manager = manager;
    }
    
    EconomyResponse response(Boolean success, double amt, double money, String msg) {
        EconomyResponse.ResponseType type;
        if(success == null)
            type = EconomyResponse.ResponseType.NOT_IMPLEMENTED;
        else if(success)
            type = EconomyResponse.ResponseType.SUCCESS;
        else
            type = EconomyResponse.ResponseType.FAILURE;
        
        // jeez this is quite a mouthful!!!
        return new EconomyResponse(amt, money, type, msg == null ? null : SOUP.lang(msg));
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public String getName() {
        return "SOUP Economy";
    }
    
    @Override
    public boolean hasBankSupport() {
        return true;
    }
    
    @Override // arbitrary value to represent IEEE754 decimals
    public int fractionalDigits() {
        return 32;
    }
    
    @Override
    public String format(double money) {
        return manager.format(money);
    }
    
    @Override
    public String currencyNamePlural() {
        return manager.coin_plur;
    }
    
    @Override
    public String currencyNameSingular() {
        return manager.coin_sing;
    }
    
    @Override
    @Deprecated
    public boolean hasAccount(String name) {
        return false;
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return true; // we always have an account for players
    }
    
    @Override
    @Deprecated
    public boolean hasAccount(String s, String s1) {
        return false;
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return true; // we do not differenciate worlds
    }
    
    @Override
    @Deprecated
    public double getBalance(String s) {
        return 0;
    }
    
    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return manager.getMoney(offlinePlayer.getUniqueId());
    }
    
    @Override
    @Deprecated
    public double getBalance(String s, String s1) {
        return 0;
    }
    
    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return manager.getMoney(offlinePlayer.getUniqueId()); // worlds are the same!
    }
    
    @Override
    @Deprecated
    public boolean has(String s, double v) {
        return false;
    }
    
    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amt) {
        return manager.getMoney(offlinePlayer.getUniqueId()) >= amt;
    }
    
    @Override
    @Deprecated
    public boolean has(String s, String s1, double v) {
        return false;
    }
    
    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double amt) {
        return manager.getMoney(offlinePlayer.getUniqueId()) >= amt; // worlds are the same xD
    }
    
    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String s, double v) {
        return null;
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amt) {
        // user money
        double money = manager.getMoney(player.getUniqueId());
        if(money < amt) {
            // player cannot pay!
            return response(false, amt, money, "eco.not-enough-money");
        }
        
        // PAY DAY!!!
        money -= amt;
        manager.setMoney(player.getUniqueId(), money);
        return response(true, amt, money, null);
    }
    
    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return null;
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return withdrawPlayer(offlinePlayer, v); // send to a regular implementation
    }
    
    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String s, double v) {
        return null;
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amt) {
        // user money
        double money = manager.getMoney(player.getUniqueId());
        
        if(money + amt > manager.account_max_amt) {
            // filty capitalist pig user has too many monies
            return response(false, amt, money, "eco.maxed-out-deposit");
        }
        
        money += amt;
        manager.setMoney(player.getUniqueId(), money);
        return response(true, amt, money, null);
    }
    
    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return null;
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return depositPlayer(offlinePlayer, v); // already implemented
    }
    
    @Override
    @Deprecated
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }
    
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer owner) {
        EconomyManager.Bank bank = manager.createBank(name, owner.getUniqueId());
        return response(true, 0, bank.money, null);
    }
    
    @Override
    public EconomyResponse deleteBank(String name) {
        EconomyManager.Bank bank = manager.banks.remove(name);
        return response(bank != null, 0, 0, null);
    }
    
    @Override
    public EconomyResponse bankBalance(String s) {
        EconomyManager.Bank bank = manager.banks.get(s);
        if(bank == null)
            return response(false, 0, 0, null);
        return response(true, 0, bank.money, null);
    }
    
    @Override
    public EconomyResponse bankHas(String name, double amt) {
        EconomyManager.Bank bank = manager.banks.get(name);
        if(bank == null)
            return response(false, 0, 0, null);
        return response(bank.money >= amt, 0, bank.money, null);
    }
    
    @Override
    public EconomyResponse bankWithdraw(String name, double amt) {
        EconomyManager.Bank bank = manager.banks.get(name);
        if(bank == null || bank.money < amt)
            return response(false, 0, 0, null);
        
        // remove money
        return response(true, 0, bank.money -= amt, null);
    }
    
    @Override
    public EconomyResponse bankDeposit(String name, double amt) {
        EconomyManager.Bank bank = manager.banks.get(name);
        if(bank == null || bank.money + amt > manager.bank_max_amt)
            return response(false, 0, 0, null);
    
        // remove money
        return response(true, 0, bank.money += amt, null);
    }
    
    @Override
    @Deprecated
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer owner) {
        EconomyManager.Bank bank = manager.banks.get(name);
        if(bank == null)
            return response(false, 0, 0, null);
        return response(bank.owner.equals(owner.getUniqueId()), 0, bank.money, null);
    }
    
    @Override
    @Deprecated
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }
    
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer member) {
        EconomyManager.Bank bank = manager.banks.get(name);
        if(bank == null)
            return response(false, 0, 0, null);
        return response(bank.members.contains(member.getUniqueId()), 0, bank.money, null);
    }
    
    @Override
    public List<String> getBanks() {
        List<String> list = new ArrayList<>();
        for(EconomyManager.Bank bank : manager.banks.values())
            list.add(bank.name);
        return list;
    }
    
    @Override
    @Deprecated
    public boolean createPlayerAccount(String s) {
        return false;
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return true; // player accounts are created automatically
    }
    
    @Override
    @Deprecated
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false; // doesnt support multiple world-accounts
    }
}
