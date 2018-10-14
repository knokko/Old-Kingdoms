package nl.knokko.golems;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.main.KingdomEventHandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;

public class GolemAreaTask extends GolemTask {
	
	private Location centre;
	
	private String backUpArea;
	
	private double radius;

	public GolemAreaTask(GolemPriority priority, GolemState state, Location centre, double radius) {
		super(priority, state);
		this.centre = centre;
		this.radius = radius;
	}
	
	public GolemAreaTask(GolemPriority priority, GolemState state, String arguments){
		super(priority, state);
		backUpArea = arguments;
		getCentre();
	}

	@Override
	public Location getTaskLocation(Golem golem) {
		getCentre();
		return centre;
	}
	
	public double getRadius(){
		getCentre();
		return radius;
	}

	@Override
	public GolemAction getNextAction(Golem golem) {
		if(!getCentre())
			return null;
		if(golem.getWorld() == centre.getWorld()){
			double distance = golem.getLocation().distance(centre);
			if(distance > radius)
				return new GolemAction(centre);
			else {
				if(getState() == GolemState.ATTACK_MONSTERS){
					LivingEntity target = findNearestMonster(golem);
					if(target != null)
						return new GolemAction(target);
				}
				if(getState() == GolemState.ATTACK_PLAYERS){
					Player target = findNearestEnemyPlayer(golem);
					if(target != null)
						return new GolemAction(target);
				}
				if(getState() == GolemState.ATTACK_MONSTERS_AND_PLAYERS){
					LivingEntity target = findNearestEnemyPlayer(golem);
					if(target != null)
						return new GolemAction(target);
					target = findNearestMonster(golem);
					if(target != null)
						return new GolemAction(target);
				}
				if(getState() == GolemState.ATTACKING){
					LivingEntity target = findNearestLivingEntity(golem);
					if(target != null)
						return new GolemAction(target);
				}
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof GolemAreaTask && getCentre()){
			GolemAreaTask task = (GolemAreaTask) other;
			return task.getCentre() && task.state == state && task.radius == radius && task.centre.equals(centre);
		}
		return false;
	}
	
	public LivingEntity findNearestMonster(Golem golem){
		if(!getCentre() || golem.getWorld() != centre.getWorld())
			return null;
		LivingEntity nearest = null;
		double minDistance = radius;
		Iterator<Entity> entities = golem.getWorld().getNearbyEntities(centre, radius, radius, radius).iterator();
		while(entities.hasNext()){
			Entity entity = entities.next();
			double distance = golem.getLocation().distance(entity.getLocation());
			if((entity instanceof Monster || entity instanceof Flying) && entity.getLocation().distance(centre) <= radius && (distance < minDistance || nearest == null)){
				minDistance = distance;
				nearest = (LivingEntity) entity;
			}
		}
		return nearest;
	}
	
	public Player findNearestEnemyPlayer(Golem golem){
		if(!getCentre() || golem.getWorld() != centre.getWorld())
			return null;
		Player nearest = null;
		double minDistance = radius;
		List<Player> players = golem.getWorld().getPlayers();
		Kingdom kd = KingdomData.getEntityKingdom(golem);
		if(kd == null)
			return null;
		int i = 0;
		while(i < players.size()){
			Player player = players.get(i);
			Kingdom claimer = KingdomData.getClaim(player.getLocation().getChunk());
			double distanceCentre = player.getLocation().distance(centre);
			double distance = player.getLocation().distance(golem.getLocation());
			if(KingdomData.getKingdom(player) != kd && KingdomEventHandler.canKingdomEdit(kd, claimer) && distanceCentre <= radius && (distance < minDistance || nearest == null)){
				minDistance = distance;
				nearest = player;
			}
				
			++i;
		}
		return nearest;
	}
	
	public LivingEntity findNearestLivingEntity(Golem golem){
		if(!getCentre())
			return null;
		Kingdom kd = KingdomData.getEntityKingdom(golem);
		if(golem.getWorld() == centre.getWorld() && kd != null){
			LivingEntity nearest = null;
			double minDistance = radius;
			Iterator<Entity> entities = golem.getWorld().getNearbyEntities(centre, radius, radius, radius).iterator();
			while(entities.hasNext()){
				Entity entity = entities.next();
				Kingdom claimer = KingdomData.getClaim(entity.getLocation().getChunk());
				double distance = entity.getLocation().distance(golem.getLocation());
				if(KingdomData.getEntityKingdom(entity) != kd && KingdomEventHandler.canKingdomEdit(kd, claimer) && entity instanceof LivingEntity && distance < minDistance){
					minDistance = distance;
					nearest = (LivingEntity) entity;
				}
			}
			return nearest;
		}
		return null;
	}

	@Override
	public GolemAction onEntityAttack(Entity attacker, Entity victim, Golem golem) {
		if(!getCentre())
			return null;
		Location loc = attacker.getLocation();
		Location loc2 = victim.getLocation();
		Location loc3 = golem.getLocation();
		if(loc2.getWorld() != centre.getWorld() || centre.distance(loc2) > radius)
			return null;
		Kingdom kdAtt = KingdomData.getEntityKingdom(attacker);
		Kingdom kdGol = KingdomData.getGolemOwner(golem);
		Kingdom kdDef = KingdomData.getEntityKingdom(victim);
		if(getState() == GolemState.ASSISTING){
			if(kdAtt == kdGol && victim instanceof LivingEntity && kdDef != kdGol)
				return new GolemAction((LivingEntity) victim);
			else if(kdDef == kdGol && kdAtt != kdGol && attacker instanceof LivingEntity && loc.getWorld() == loc3.getWorld() && loc.getWorld() == centre.getWorld() && loc.distance(centre) <= 2 * radius)
				return new GolemAction((LivingEntity) attacker);
		}
		if(getState() == GolemState.PROTECTING){
			if(kdAtt != kdGol && attacker.getWorld() == centre.getWorld() && attacker.getWorld() == golem.getWorld() && centre.distance(loc) <= 2 * radius && attacker instanceof LivingEntity)
				return new GolemAction((LivingEntity) attacker);
		}
		return null;
	}

	@Override
	public String getSaveString() {
		getCentre();
		if(centre != null)
			return getPriority().name() + " " + getState().name() + " area " + centre.getX() + " " + centre.getY() + " " + centre.getZ() + " " + centre.getWorld().getUID() + " " + radius;
		else
			return getPriority().name() + " " + getState().name() + " area " + backUpArea;
	}
	
	private boolean getCentre(){
		if(centre == null){
				try {
				int index1 = backUpArea.indexOf(" ");
				int index2 = backUpArea.indexOf(" ", index1 + 1);
				int index3 = backUpArea.indexOf(" ", index2 + 1);
				int index4 = backUpArea.indexOf(" ", index3 + 1);
				double x = Double.valueOf(backUpArea.substring(0, index1));
				double y = Double.valueOf(backUpArea.substring(index1 + 1, index2));
				double z = Double.valueOf(backUpArea.substring(index2 + 1, index3));
				World world = Bukkit.getWorld(UUID.fromString(backUpArea.substring(index3 + 1, index4)));
				radius = Double.valueOf(backUpArea.substring(index4 + 1));
				centre = new Location(world, x, y, z);
				return world != null;
			} catch(Exception ex){
				Bukkit.getLogger().warning("Failed to load the centre of a golem task:");
				Bukkit.getLogger().throwing("nl.knokko.golems.GolemAreaTask", "getCentre()", ex);
				return false;
			}
		}
		else
			return true;
	}
}
