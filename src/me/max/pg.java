package me.max;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;











import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class pg extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public File shops = null;
    public FileConfiguration shopsc = null;
    

    

    public void onEnable() {
    	PluginDescriptionFile pdffile = this.getDescription();
		this.logger.info(pdffile.getName() + " Has Been Enabled!");
		this.getServer().getPluginManager().registerEvents(new pgl(this), this);
		reloadCustomConfig();
		getShops().options().copyDefaults(true);
		saveShops();
		idleTimeEvent();
		File config = new File(this.getDataFolder(), "config.yml");
		if(!config.exists()){
			this.saveDefaultConfig();
			System.out.println("[PetGolem] No config.yml detected, config.yml created");

		}
		
		hookIntoWorldGuard();
		

	}
		
	public void onDisable() {
		PluginDescriptionFile pdffile = this.getDescription();
		this.logger.info(pdffile.getName() + " Has Been Disabled!");
		savePets();
	}
	public void saveShops() {
	    if (shopsc == null || shops == null) {
	    return;
	    }
	    try {
	        getShops().save(shops);
	    } catch (IOException ex) {
	        this.getLogger().log(Level.SEVERE, "Could not save config to " + shops, ex);
	    }
	}
	public void savePets(){
		for(Player p : pgl.hasGolem.keySet()){
			getShops().set(p.getName(), pgl.hasGolem.get(p).getUniqueId().toString());
		}
		saveShops();
	}
	public FileConfiguration getShops() {
	    if (shopsc == null) {
	        this.reloadCustomConfig();
	    }
	    return shopsc;
	}
	public void reloadCustomConfig() {
	    if (shops == null) {
	    	shops = new File(getDataFolder(), "golems.yml");
	    }
	    shopsc = YamlConfiguration.loadConfiguration(shops);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = this.getResource("golems.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        shopsc.setDefaults(defConfig);
	    }
	    
	}
	public void idleTimeEvent(){
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			public void run() {
				
					for(Player p : getServer().getOnlinePlayers()){
						if(pgl.hasGolem.containsKey(p)){
							msg(p,"idle");
						}
					}
				
					
			    
				
			}
		}, 0L, getConfig().getLong("idleTime") * 20);
	}
	public void msg(Player p, String s){
		String msg = null;
		List<String> msgs = getConfig().getStringList("messages." + s);
		int amount = msgs.size();
		Random r = new Random();
		msg = msgs.get(r.nextInt(amount)); 
		p.sendMessage(ChatColor.DARK_AQUA + "Golem: " + msg);
	}
	public boolean onCommand(CommandSender Sender, Command cmd, String commandLabel, String[] args){
		
		if(commandLabel.equalsIgnoreCase("petgolem")){
			if(Sender instanceof Player){
				Player p = (Player)Sender;
				if(!p.isOp()){
					return false;
				}
				p.sendMessage(ChatColor.GREEN + "Reloading PetGolem Config");
				reloadConfig();
				saveConfig();
				p.sendMessage(ChatColor.GREEN + "Reloaded PetGolem Config");
			}else{
				System.out.println(ChatColor.GREEN + "Reloading PetGolem Config");
				reloadConfig();
				saveConfig();
				System.out.println(ChatColor.GREEN +  "Reloaded PetGolem Config");
			}
			
			
			
		}
		return false;
	}
	public static void hookIntoWorldGuard() {
        WorldGuardPlugin wg = (WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        // Reflect the IPVP flags into WorldGuard(as there is no API for this)
        GolemFlag.reflectIntoFlags();
        // Reload WorldGuard (the same as the "reload" command)
        wg.getGlobalStateManager().unload();
        wg.getGlobalRegionManager().unload();
        wg.getGlobalStateManager().load();
        wg.getGlobalRegionManager().preload();
	}
	
}