package me.max;



import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.server.EntityCreature;
import net.minecraft.server.Navigation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class pgl implements Listener{
	public static Map<Player, IronGolem> hasGolem = new HashMap<Player, IronGolem>();
	public static Map<IronGolem, LivingEntity> target = new HashMap<IronGolem, LivingEntity>();

	public pg plugin;
	public pgl(pg p){
		plugin = p;
	}
	public WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		final Player p = e.getPlayer();
		if(hasGolem.containsKey(p)){
			final IronGolem golem = hasGolem.get(p);
			Location l = p.getLocation();
			RegionManager man = wg.getRegionManager(l.getWorld());
		    ApplicableRegionSet regions = man.getApplicableRegions(l);
		    if(regions.getFlag(GolemFlag.flag) == State.ALLOW){
		    	return;
		    }
			if(golem.getLocation().distance(p.getLocation()) > 50){
				golem.teleport(p);
				return;
			}
			if(golem.getTarget() != null){
				if(golem.getTarget() == target.get(golem)){
					return;
				}
			}
			EntityCreature entity = ((CraftCreature)golem).getHandle();
			Navigation n = entity.getNavigation();
			n.a(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), .5F);
			if(golem.getLocation().getBlock().getType() == Material.WATER){
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						golem.teleport(p);
						
							
							
					}
				}, 60L);
			}
				Location loc = golem.getLocation();
				breakNear(Material.LEAVES, loc.getBlock());
				breakNear(Material.LEAVES, golem.getEyeLocation().getBlock());
				breakNear(Material.VINE, loc.getBlock());
				breakNear(Material.VINE, golem.getEyeLocation().getBlock());
		}
	}
	@EventHandler
	public void playerCLick(PlayerInteractEntityEvent e){
		if(e.getRightClicked() instanceof IronGolem){
			Player p = e.getPlayer();
			IronGolem golem = (IronGolem)e.getRightClicked();
			
			
				ItemStack i = p.getItemInHand();
				if(i.getType()==Material.RED_ROSE){
					
					if(!hasGolem.containsValue(golem)){
						boolean trade = false;
						if(hasGolem.containsKey(p)){
							hasGolem.remove(p);
							trade = true;
						}
						hasGolem.put(p, golem);
						if(trade){msg(p, "claimed");}else{msg(p,"claimed");}
						p.getInventory().remove(new ItemStack(i.getType(), 1));
					}else{
						if(hasGolem.get(p) == golem){
							msg(p,"yourGolem");
						}else {msg(p,"alreadyOwned");}
					}
					
				}else if(i.getType()==Material.YELLOW_FLOWER){
					if(hasGolem.get(p) != golem){
						msg(p, "notYourGolem");
						return;
					}
					hasGolem.remove(p);
					msg(p,"abandoned");
					p.getInventory().remove(new ItemStack(i.getType(), 1));
				}else if(i.getType() == Material.IRON_INGOT){
					if(golem.getHealth() != golem.getMaxHealth()){
						msg(p,"feed");
						if(plugin.getConfig().getInt("ingotHeal") + golem.getHealth() > golem.getMaxHealth()){
							golem.setHealth(100);
							p.getInventory().remove(new ItemStack(i.getType(), 1));
							return;
						}
						golem.setHealth(golem.getHealth() + plugin.getConfig().getInt("ingotHeal"));
						p.getInventory().remove(new ItemStack(i.getType(), 1));
					}else{msg(p,"fullHealth");}
				}
			
			
			
		}
	}
	@EventHandler
	public void onDeath(EntityDeathEvent e){
		Entity entity = e.getEntity();
		if(entity instanceof IronGolem){
			IronGolem g = (IronGolem)entity;
			if(hasGolem.containsValue(g)){
				Player owner = null;
				for(Player p : hasGolem.keySet()){
					if(hasGolem.get(p)==g){
						owner = p;
					}
				}
				if(owner!=null){
					msg(owner, "died");
					hasGolem.remove(owner);
				}
			}
		}else if(entity instanceof Player){
			if(hasGolem.containsKey((Player)entity)){
				msg((Player)entity, "abandoned");
				hasGolem.remove((Player)entity);
			}
		}
		if(entity instanceof LivingEntity){
			LivingEntity le = (LivingEntity)entity;
			if(target.containsValue(le)){
				for(IronGolem golem : target.keySet()){
					if(target.get(golem) == le){
						target.remove(golem);
					}
				}
			}
		}
				
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		if(plugin.getShops().getKeys(true).contains(p.getName())){
			for(Entity en : p.getWorld().getEntities()){
				if(en.getUniqueId().toString().equals(plugin.getShops().getString(p.getName()))){
					
					msg(p,"remembered");
					if(en instanceof IronGolem){
						
						IronGolem ir = (IronGolem)en;
						hasGolem.put(p,ir);
						
					}
					
				}
			}
		}
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent e){
		Entity en = e.getEntity();
		Entity d = e.getDamager();
		if(d instanceof Player){
			Player p = (Player)d;
			if(en instanceof IronGolem){
				IronGolem ir = (IronGolem)en;
				
				if(hasGolem.containsKey(p)){
					if(hasGolem.get(p)==ir){
						e.setCancelled(true);
						
						
					}
				}
				
			}
			if(hasGolem.containsKey(p)){
				IronGolem golem = hasGolem.get(p);
				if(en instanceof LivingEntity){
					LivingEntity le = (LivingEntity)en;
					target.put(golem, le);
				}
				
			}
			
		}
		if(en instanceof Player){
			Player p = (Player)en;
			if(hasGolem.containsKey(p)){
				IronGolem ir = hasGolem.get(p);
				if(d instanceof LivingEntity){
					LivingEntity t = (LivingEntity)d;
					target.put(ir, t);
					Random r = new Random();
					if(r.nextInt(10)==0){
						msg(p,"defend");
					}
					
				}
				
			}
		}
	}
	public void msg(Player p, String s){
		String msg = null;
		List<String> msgs = plugin.getConfig().getStringList("messages." + s);
		int amount = msgs.size();
		Random r = new Random();
		msg = msgs.get(r.nextInt(amount)); 
		p.sendMessage(ChatColor.DARK_AQUA + "Golem: " + msg);
	}
	public void breakNear(Material m, Block b){
		Block bl = b.getRelative(0, 0, 1);
		if(bl.getType() == m){bl.breakNaturally();}
		bl = b.getRelative(0,0,-1);
		if(bl.getType() == m){bl.breakNaturally();}
		//--
		bl = b.getRelative(1,0,0);
		if(bl.getType() == m){bl.breakNaturally();}
		bl = b.getRelative(-1,0,0);
		if(bl.getType() == m){bl.breakNaturally();}
		//--
		bl = b.getRelative(1,0,1);
		if(bl.getType() == m){bl.breakNaturally();}
		bl = b.getRelative(-1,0,-1);
		if(bl.getType() == m){bl.breakNaturally();}
		//--

		
	}

}
