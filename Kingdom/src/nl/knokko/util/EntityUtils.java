package nl.knokko.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public final class EntityUtils implements Listener {
	
	private static final HashMap<String,IronGolem> ironGolemIds = new HashMap<String,IronGolem>();
	private static final HashMap<String, Snowman> snowGolemIds = new HashMap<String,Snowman>();
	private static final HashMap<String, Golem> golemIds = new HashMap<String, Golem>();
	
	public static final Entity getEntityByUUID(World world, UUID id){
		List<Entity> entities = world.getEntities();
		int i = 0;
		while(i < entities.size()){
			Entity entity = entities.get(i);
			if(entity.getUniqueId().equals(id))
				return entity;
			++i;
		}
		return null;
	}
	
	public static final Entity getEntityByUUID(World world, String uuid){
		List<Entity> entities = world.getEntities();
		int i = 0;
		while(i < entities.size()){
			Entity entity = entities.get(i);
			if(entity.getUniqueId().toString().equals(uuid))
				return entity;
			++i;
		}
		return null;
	}
	
	public static final Entity getEntityInSight(LivingEntity looker){
		List<Block> blocks = looker.getLineOfSight(new HashSet<Material>(), 200);
		int i = 0;
		while(i < blocks.size()){
			Block b = blocks.get(i);
			Collection<Entity> entities = looker.getWorld().getNearbyEntities(b.getLocation(), 1, 1, 1);
			if(!entities.isEmpty())
				return (Entity) entities.toArray()[0];
			++i;
		}
		return null;
	}
	
	public static final ArrayList<Golem> getAvailableGolems(Player player, double range){
		Kingdom kd = KingdomData.getKingdom(player);
		if(kd != null){
			if(kd.canStartAttack(player)){
				ArrayList<Golem> returnList = new ArrayList<Golem>();
				ArrayList<Golem> golems = kd.getGolems(player.getWorld());
				int i = 0;
				while(i < golems.size()){
					Golem golem = golems.get(i);
					if(golem.getLocation().distance(player.getLocation()) <= range)
						returnList.add(golem);
					++i;
				}
				return returnList;
			}
			player.sendMessage(ChatColor.RED + "You need at least the rank of lutenant in order to command golems.");
		}
		player.sendMessage(ChatColor.RED + "You need a kingdom to control golems.");
		return null;
	}
	
	public static final IronGolem getIronGolemById(UUID uuid){
		return getIronGolemById(uuid.toString());
	}
	
	public static final IronGolem getIronGolemById(String uuid){
		return ironGolemIds.get(uuid);
	}
	
	public static final Snowman getSnowGolemById(UUID uuid){
		return getSnowGolemById(uuid.toString());
	}
	
	public static final Snowman getSnowGolemById(String uuid){
		return snowGolemIds.get(uuid);
	}
	
	public static final Golem getGolemById(UUID uuid){
		return getGolemById(uuid.toString());
	}
	
	public static final Golem getGolemById(String uuid){
		return golemIds.get(uuid);
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event){
		if(event.getEntity() instanceof IronGolem)
			ironGolemIds.put(event.getEntity().getUniqueId().toString(), (IronGolem) event.getEntity());
		if(event.getEntity() instanceof Snowman)
			snowGolemIds.put(event.getEntity().getUniqueId().toString(), (Snowman) event.getEntity());
		if(event.getEntity() instanceof Golem)
			golemIds.put(event.getEntity().getUniqueId().toString(), (Golem) event.getEntity());
	}
}
