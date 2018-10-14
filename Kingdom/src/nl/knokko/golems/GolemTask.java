package nl.knokko.golems;

import nl.knokko.kingdom.Kingdom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Player;

public abstract class GolemTask {
	
	protected GolemPriority priority;
	protected GolemState state;

	public GolemTask(GolemPriority priority, GolemState state) {
		this.priority = priority;
		this.state =  state;
	}
	
	public static GolemTask fromString(String saveString){
		if(saveString.equals("free"))
			return new GolemFreeTask();
		try {
			int index1 = saveString.indexOf(" ");
			int index2 = saveString.indexOf(" ", index1 + 1);
			int index3 = saveString.indexOf(" ", index2 + 1);
			GolemPriority priority = GolemPriority.valueOf(saveString.substring(0, index1));
			GolemState state = GolemState.valueOf(saveString.substring(index1 + 1, index2));
			String type = saveString.substring(index2 + 1, index3);
			String args = saveString.substring(index3 + 1);
			if(type.equals("area"))
				return new GolemAreaTask(priority, state, args);
			if(type.equals("follow"))
				return new GolemFollowTask(state, priority, args);
			if(type.equals("kingdom"))
				return new GolemKingdomTask(priority, state, args);
			throw new IllegalArgumentException("saveString = " + saveString + " and class type = " + type);
		} catch(Exception ex){
			Bukkit.getLogger().warning("Failed to load a golem task for Kingdom Wars:");
			Bukkit.getLogger().throwing("nl.knokko.golems.GolemTask", "fromString(String)", ex);
			return new GolemFreeTask();
		}
	}
	
	@Override
	public abstract boolean equals(Object other);
	
	public abstract Location getTaskLocation(Golem golem);
	
	public abstract GolemAction getNextAction(Golem golem);
	
	public abstract String getSaveString();
	
	public GolemPriority getPriority(){
		return priority;
	}
	
	public GolemState getState(){
		return state;
	}
	
	public GolemAction onEntityAttack(Entity attacker, Entity victim, Golem golem){
		return null;
	}
	
	public GolemAction onKingdomSwitch(Player player, Kingdom from, Kingdom to){
		return null;
	}
}
