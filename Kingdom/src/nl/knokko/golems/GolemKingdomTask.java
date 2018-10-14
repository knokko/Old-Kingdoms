package nl.knokko.golems;

import java.util.List;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.main.KingdomEventHandler;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class GolemKingdomTask extends GolemTask {
	
	private Kingdom area;

	public GolemKingdomTask(GolemPriority priority, GolemState state, Kingdom area) {
		super(priority, state);
		this.area = area;
	}
	
	public GolemKingdomTask(GolemPriority priority, GolemState state, String arguments){
		this(priority, state, KingdomData.getKingdom(arguments));
	}

	@Override
	public Location getTaskLocation(Golem golem) {
		return area.getSpawn();
	}
	
	public Kingdom getKingdom(){
		return area;
	}
	
	public LivingEntity findNearestMonster(Golem golem){
		List<Entity> entities = golem.getNearbyEntities(100, 100, 100);
		double distance = 100;
		LivingEntity target = null;
		int i = 0;
		while(i < entities.size()){
			Entity entity = entities.get(i);
			if(entity instanceof Monster || entity instanceof Flying){
				double dis = golem.getLocation().distance(entity.getLocation());
				if(dis < distance && KingdomData.getClaim(entity.getLocation().getChunk()) == area){
					distance = dis;
					target = (LivingEntity) entity;
				}
			}
			++i;
		}
		return target;
	}
	
	public Player findNearestIntruder(Golem golem){
		List<Player> players = golem.getWorld().getPlayers();
		Player nearest = null;
		double minDistance = Double.POSITIVE_INFINITY;
		int i = 0;
		while(i < players.size()){
			Player player = players.get(i);
			Kingdom kd = KingdomData.getKingdom(player);
			Kingdom claimer = KingdomData.getClaim(player.getLocation().getChunk());
			if(claimer == area){
				if(KingdomData.canEditOtherKingdoms()){
					if(kd != area && player.getWorld() == golem.getWorld()){
						double distance = player.getLocation().distance(golem.getLocation());
						if(distance < minDistance || nearest == null){
							distance = minDistance;
							nearest = player;
						}
					}
				}
				else {
					if(area.isUnderAttack(kd)){
						double distance = player.getLocation().distance(golem.getLocation());
						if(distance < minDistance || nearest == null){
							distance = minDistance;
							nearest = player;
						}
					}
				}
			}
			++i;
		}
		return nearest;
	}
	
	public LivingEntity findNearestEntity(Golem golem){
		List<Entity> entities = golem.getNearbyEntities(100, 100, 100);
		double distance = 100;
		LivingEntity target = null;
		int i = 0;
		while(i < entities.size()){
			Entity entity = entities.get(i);
			Kingdom golemKD = KingdomData.getEntityKingdom(golem);
			Kingdom entityKD = KingdomData.getEntityKingdom(entity);
			if(entityKD != golemKD && golemKD != null && KingdomEventHandler.canKingdomEdit(golemKD, area)){
				double dis = golem.getLocation().distance(entity.getLocation());
				if(dis < distance && KingdomData.getClaim(entity.getLocation().getChunk()) == area){
					distance = dis;
					target = (LivingEntity) entity;
				}
			}
			++i;
		}
		return target;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof GolemKingdomTask){
			GolemKingdomTask task = (GolemKingdomTask) other;
			return task.state == state && task.area == area;
		}
		return false;
	}

	@Override
	public GolemAction getNextAction(Golem golem) {
		Location loc = golem.getLocation();
		Chunk chunk = loc.getChunk();
		if(KingdomData.getClaim(chunk) != area){
			GolemAction action = tryThisLocation(loc.clone().add(0, 0, 16));
			if(action != null)
				return action;
			action = tryThisLocation(loc.clone().add(0, 0, -16));
			if(action != null)
				return action;
			action = tryThisLocation(loc.clone().add(16, 0, 0));
			if(action != null)
				return action;
			action = tryThisLocation(loc.clone().add(-16, 0, 0));
			if(action != null)
				return action;
			return new GolemAction(area.getSpawn());
		}
		else {
			if(state == GolemState.ATTACK_MONSTERS){
				LivingEntity target = findNearestMonster(golem);
				if(target != null)
					return new GolemAction(target);
				else
					return null;
			}
			if(state == GolemState.ATTACK_PLAYERS){
				Player intruder = findNearestIntruder(golem);
				if(intruder != null)
					return new GolemAction(intruder);
				else
					return null;
			}
			if(state == GolemState.ATTACK_MONSTERS_AND_PLAYERS){
				Player intruder = findNearestIntruder(golem);
				if(intruder != null)
					return new GolemAction(intruder);
				LivingEntity target = findNearestMonster(golem);
				if(target != null)
					return new GolemAction(target);
				else
					return null;
			}
		}
		return null;
	}
	
	private GolemAction tryThisLocation(Location loc){
		if(KingdomData.getClaim(loc.getChunk()) == area){
			loc.setY(loc.getWorld().getHighestBlockYAt(loc));
			return new GolemAction(loc);
		}
		return null;
	}
	
	@Override
	public GolemAction onEntityAttack(Entity attacker, Entity victim, Golem golem) {
		Location loc = attacker.getLocation();
		Location loc2 = victim.getLocation();
		Location loc3 = golem.getLocation();
		Kingdom victimClaim = KingdomData.getClaim(loc2.getChunk());
		Kingdom kdAtt = KingdomData.getEntityKingdom(attacker);
		Kingdom kdGol = KingdomData.getGolemOwner(golem);
		Kingdom kdDef = KingdomData.getEntityKingdom(victim);
		if(victimClaim != area || attacker.getWorld() != golem.getWorld() || victim.getWorld() != golem.getWorld())
			return null;
		if(getState() == GolemState.ASSISTING){
			if(kdAtt == kdGol && victim instanceof LivingEntity && kdDef != kdGol)
				return new GolemAction((LivingEntity) victim);
			else if(kdDef == kdGol && kdAtt != kdGol && attacker instanceof LivingEntity && loc.getWorld() == loc3.getWorld())
				return new GolemAction((LivingEntity) attacker);
		}
		if(getState() == GolemState.PROTECTING){
			if(kdAtt != kdGol && attacker.getWorld() == golem.getWorld() && attacker instanceof LivingEntity)
				return new GolemAction((LivingEntity) attacker);
		}
		return null;
	}
	
	@Override
	public String getSaveString() {
		return getPriority().name() + " " + getState().name() + " kingdom " + area.getName();
	}
}
