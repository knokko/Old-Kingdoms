package nl.knokko.golems;

import nl.knokko.command.CommandGolems;

import org.bukkit.Location;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;

public class GolemAction {
	
	private LivingEntity target;
	
	private Location goal;

	public GolemAction(LivingEntity target) {
		this.target = target;
	}
	
	public GolemAction(Location goal){
		this.goal = goal;
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof GolemAction){
			GolemAction action = (GolemAction) other;
			return action.target == target && action.goal.equals(goal);
		}
		return false;
	}
	
	public Location getGoal(){
		return target != null ? target.getLocation() : goal;
	}
	
	public LivingEntity getTarget(){
		return target;
	}
	
	public void start(Golem golem){
		if(target != null)
			golem.setTarget(target);
		else if(goal != null)
			CommandGolems.setGolemPath(golem, goal);
	}
	
	public double getDistance(Golem golem){
		if(golem.getWorld() == getGoal().getWorld())
			return golem.getLocation().distance(getGoal());
		else
			return Double.POSITIVE_INFINITY;
	}
}
