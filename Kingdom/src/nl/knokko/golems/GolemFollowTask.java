package nl.knokko.golems;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.main.KingdomEventHandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class GolemFollowTask extends GolemTask {
	
	private OfflinePlayer commander;

	public GolemFollowTask(GolemState state, GolemPriority priority, OfflinePlayer commander) {
		super(priority, state);
		this.commander = commander;
	}
	
	public GolemFollowTask(GolemState state, GolemPriority priority, String arguments){
		this(state, priority, Bukkit.getOfflinePlayer(UUID.fromString(arguments)));
	}
	
	public OfflinePlayer getCommander(){
		return commander;
	}

	@Override
	public Location getTaskLocation(Golem golem) {
		return commander instanceof Player ? ((Player) commander).getLocation() : golem.getLocation();
	}

	@Override
	public GolemAction getNextAction(Golem golem) {
		if(!(commander instanceof Player))
			return null;
		Player commander = (Player) this.commander;
		if(golem.getWorld() == commander.getWorld()){
			double distance = golem.getLocation().distance(commander.getLocation());
			if(distance > 30)
				return new GolemAction(commander.getLocation());
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
		if(other instanceof GolemFollowTask){
			GolemFollowTask task = (GolemFollowTask) other;
			return task.commander == commander && task.state == state;
		}
		return false;
	}
	
	public LivingEntity findNearestMonster(Golem golem){
		if(!(commander instanceof Player))
			return null;
		Player commander = (Player) this.commander;
		if(golem.getWorld() != commander.getWorld())
			return null;
		LivingEntity nearest = null;
		double minDistance = 30;
		Iterator<Entity> entities = golem.getWorld().getNearbyEntities(commander.getLocation(), 30, 30, 30).iterator();
		while(entities.hasNext()){
			Entity entity = entities.next();
			double distance = golem.getLocation().distance(entity.getLocation());
			if((entity instanceof Monster || entity instanceof Flying) && entity.getLocation().distance(commander.getLocation()) <= 30 && (distance < minDistance || nearest == null)){
				minDistance = distance;
				nearest = (LivingEntity) entity;
			}
		}
		return nearest;
	}
	
	public Player findNearestEnemyPlayer(Golem golem){
		if(!(commander instanceof Player))
			return null;
		Player commander = (Player) this.commander;
		if(golem.getWorld() != commander.getWorld())
			return null;
		Player nearest = null;
		double minDistance = 30;
		List<Player> players = golem.getWorld().getPlayers();
		Kingdom kd = KingdomData.getEntityKingdom(golem);
		if(kd == null)
			return null;
		int i = 0;
		while(i < players.size()){
			Player player = players.get(i);
			Kingdom claimer = KingdomData.getClaim(player.getLocation().getChunk());
			double distanceCentre = player.getLocation().distance(commander.getLocation());
			double distance = player.getLocation().distance(golem.getLocation());
			if(KingdomData.getKingdom(player) != kd && KingdomEventHandler.canKingdomEdit(kd, claimer) && distanceCentre <= 30 && (distance < minDistance || nearest == null)){
				minDistance = distance;
				nearest = player;
			}
			++i;
		}
		return nearest;
	}
	
	public LivingEntity findNearestLivingEntity(Golem golem){
		if(!(commander instanceof Player))
			return null;
		Player commander = (Player) this.commander;
		Kingdom kd = KingdomData.getEntityKingdom(golem);
		if(golem.getWorld() == commander.getWorld() && kd != null){
			LivingEntity nearest = null;
			double minDistance = 30;
			Iterator<Entity> entities = golem.getWorld().getNearbyEntities(commander.getLocation(), 30, 30, 30).iterator();
			while(entities.hasNext()){
				Entity entity = entities.next();
				Kingdom claimer = KingdomData.getClaim(entity.getLocation().getChunk());
				double distance = entity.getLocation().distance(golem.getLocation());
				if(KingdomData.getEntityKingdom(entity) != kd && KingdomEventHandler.canKingdomEdit(kd, claimer)&& entity instanceof LivingEntity && distance < minDistance){
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
		if(!(commander instanceof Player))
			return null;
		Player commander = (Player) this.commander;
		Location loc = attacker.getLocation();
		Location loc2 = victim.getLocation();
		Location loc3 = golem.getLocation();
		if(loc2.getWorld() != commander.getWorld() || commander.getLocation().distance(loc2) > 30)
			return null;
		Kingdom kdAtt = KingdomData.getEntityKingdom(attacker);
		Kingdom kdGol = KingdomData.getGolemOwner(golem);
		Kingdom kdDef = KingdomData.getEntityKingdom(victim);
		if(getState() == GolemState.ASSISTING){
			if(attacker == commander && victim instanceof LivingEntity && kdDef != kdGol)
				return new GolemAction((LivingEntity) victim);
			else if(victim == commander && kdAtt != kdGol && attacker instanceof LivingEntity && loc.getWorld() == loc3.getWorld() && loc.getWorld() == commander.getWorld() && loc.distance(commander.getLocation()) <= 30)
				return new GolemAction((LivingEntity) attacker);
		}
		if(getState() == GolemState.PROTECTING){
			if(kdAtt != kdGol && attacker.getWorld() == golem.getWorld() && commander == victim && loc.getWorld() == commander.getWorld() && loc2.distance(loc) <= 30 && attacker instanceof LivingEntity)
				return new GolemAction((LivingEntity) attacker);
		}
		return null;
	}

	@Override
	public String getSaveString() {
		return getPriority().name() + " " + getState().name() + " follow " + commander.getUniqueId();
	}
}
