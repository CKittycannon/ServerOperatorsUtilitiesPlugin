package dev.ckitty.mc.soup.econ;

import dev.ckitty.mc.soup.main.ConfigPair;
import dev.ckitty.mc.soup.main.SOUP;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.*;

public class EconomyManager {
    
    private Map<UUID, Double> accounts = new HashMap<>();
    Map<String, Bank> banks = new HashMap<>();
    String coin_sing, coin_plur;
    private String format;
    ConfigPair config;
    private double account_start_amt, bank_start_amt;
    double account_max_amt, bank_max_amt;
    
    public void loadEconomy() {
        // wipe data
        accounts.clear();
        banks.clear();
        
        // setup config file
        File file = new File(SOUP.INSTANCE.getDataFolder(), "econ.yml");
        SOUP.INSTANCE.exportIfMissing("econ.yml");
        config = new ConfigPair().setFile(file).pack();
        
        // get values
        coin_sing = config.data().getString("config.coin-sing");
        coin_plur = config.data().getString("config.coin-plur");
        format = config.data().getString("config.format");
        account_start_amt = config.data().getDouble("config.account-start");
        bank_start_amt = config.data().getDouble("config.bank-start");
        account_max_amt = config.data().getDouble("config.account-max");
        bank_max_amt = config.data().getDouble("config.bank-max");
        
        // load accounts
        ConfigurationSection section = config.data().getConfigurationSection("accounts");
        if(section != null) {
            for(String s : section.getKeys(false)) {
                UUID uuid = UUID.fromString(s);
                double money = section.getDouble(s);
                
                // set money
                accounts.put(uuid, money);
            }
        }
        
        // load banks
        section = config.data().getConfigurationSection("banks");
        if(section != null) {
            for(String s : section.getKeys(false)) {
                Bank bank = new Bank();
                bank.name = s;
                bank.money = section.getDouble(s + ".money");
                bank.owner = UUID.fromString(section.getString(s + ".owner"));
                
                // members
                List<String> list = section.getStringList(s + ".members");
                bank.members = new HashSet<>();
                for(String m : list) {
                    bank.members.add(UUID.fromString(m));
                }
            
                // add to map
                banks.put(s, bank);
            }
        }
        
        // DONE
    }
    
    public void saveEconomy() {
        // wipe data
        config.data().set("accounts", null);
        config.data().set("banks", null);
        
        // save accounts
        for(Map.Entry<UUID, Double> e : accounts.entrySet()) {
            config.data().set("accounts." + e.getKey().toString(), e.getValue());
        }
        
        // save Banks
        for(Bank bank : banks.values()) {
            config.data().set("banks." + bank.name + ".owner", bank.owner.toString());
            config.data().set("banks." + bank.name + ".money", bank.money);
            
            // members
            List<String> members = new ArrayList<>();
            for(UUID uuid : bank.members)
                members.add(uuid.toString());
            config.data().set("banks." + bank.name + ".members", members);
        }
        
        // DONE
        config.save();
    }
    
    // misc
    
    public String format(double money) {
        return String.format(format, money);
    }
    
    public String name(double money) {
        if(money == 1) return coin_sing;
        return coin_plur;
    }
    
    // BANKS
    
    Bank createBank(String name, UUID owner) {
        Bank bank = new Bank();
        bank.owner = owner;
        bank.name = name;
        bank.money = bank_start_amt;
        bank.members = new HashSet<>();
        
        banks.put(name, bank);
        return bank;
    }
    
    // ACCOUNTS
    
    public void addMoney(UUID player, double money) {
        accounts.put(player, getMoney(player) + money);
    }
    
    public void setMoney(UUID player, double money) {
        accounts.put(player, money);
    }
    
    public double getMoney(UUID player) {
        Double money = accounts.get(player);
        
        if (money == null) { // if null, create monies
            accounts.put(player, account_start_amt);
            return account_start_amt;
        }
        
        return money;
    }
    
    class Bank {
        String name;
        UUID owner;
        Set<UUID> members;
        double money;
    }
    
}
