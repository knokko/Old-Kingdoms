package nl.knokko.golems;

import static nl.knokko.golems.GolemPriority.*;

import java.util.ArrayList;
import java.util.HashMap;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.main.IEntityAttackListener;
import nl.knokko.main.IKingdomSwitchListener;
import nl.knokko.util.EntityUtils;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class GolemTasks implements IEntityAttackListener, IKingdomSwitchListener {
	
	private ArrayList<GolemTask> lowestTasks = new ArrayList<GolemTask>();
	private ArrayList<GolemTask> lowTasks = new ArrayList<GolemTask>();
	private ArrayList<GolemTask> mediumTasks = new ArrayList<GolemTask>();
	private ArrayList<GolemTask> highTasks = new ArrayList<GolemTask>();
	
	private ArrayList<GolemAction> lowestActions = new ArrayList<GolemAction>();
	private ArrayList<GolemAction> lowActions = new ArrayList<GolemAction>();
	private ArrayList<GolemAction> mediumActions = new ArrayList<GolemAction>();
	private ArrayList<GolemAction> highActions = new ArrayList<GolemAction>();
	
	private HashMap<GolemPriority, ArrayList<GolemTask>> tasks = new HashMap<GolemPriority, ArrayList<GolemTask>>();
	private HashMap<GolemPriority, ArrayList<GolemAction>> actions = new HashMap<GolemPriority, ArrayList<GolemAction>>();
	
	private GolemAction currentAction;
	
	private String golemID;
	
	private boolean isDirty;

	public GolemTasks(Golem golem, GolemTask... golemTasks) {
		this(golem.getUniqueId().toString(), golemTasks);
	}
	
	private GolemTasks(String golemUUID, GolemTask... golemTasks){
		tasks.put(NONE, lowestTasks);
		tasks.put(LOW, lowestTasks);
		tasks.put(MEDIUM, mediumTasks);
		tasks.put(HIGH, highTasks);
		actions.put(NONE, lowestActions);
		actions.put(LOW, lowestActions);
		actions.put(MEDIUM, mediumActions);
		actions.put(HIGH, highActions);
		golemID = golemUUID;
		addTasks(golemTasks);
	}
	
	public void addTask(GolemTask task){
		if(!tasks.get(task.getPriority()).contains(task))
			tasks.get(task.getPriority()).add(task);
	}
	
	public void addTasks(GolemTask... tasks){
		int i = 0;
		while(i < tasks.length){
			addTask(tasks[i]);
			++i;
		}
	}
	
	public void clearTasks(GolemPriority maxPriority){
		lowestTasks.clear();
		lowestActions.clear();
		if(!LOW.isMoreImportant(maxPriority)){
			lowTasks.clear();
			lowActions.clear();
		}
		if(!MEDIUM.isMoreImportant(maxPriority)){
			mediumTasks.clear();
			mediumTasks.clear();
		}
		if(!HIGH.isMoreImportant(maxPriority)){
			highTasks.clear();
			highActions.clear();
		}
	}
	
	public void clearActions(GolemPriority maxPriority){
		lowestActions.clear();
		if(!LOW.isMoreImportant(maxPriority))
			lowActions.clear();
		if(!MEDIUM.isMoreImportant(maxPriority))
			mediumTasks.clear();
		if(!HIGH.isMoreImportant(maxPriority))
			highTasks.clear();
	}
	
	public void addAction(GolemAction action, GolemPriority priority){
		if(!actions.get(priority).contains(action))
			actions.get(priority).add(action);
		refreshCurrentAction();
	}
	
	private void addSafeAction(GolemAction action, GolemPriority priority){
		if(action != null && !actions.get(priority).contains(action)){
			actions.get(priority).add(action);
			isDirty = true;
		}
	}
	
	private void addSafeTask(GolemTask task, GolemPriority priority){
		if(task != null)
			tasks.get(priority).add(task);
	}
	
	public void refreshCurrentAction(){
		GolemAction prev = currentAction;
		currentAction = findBestAction();
		Golem gol = getGolem();
		if(prev != currentAction && gol != null)
			currentAction.start(gol);
	}
	
	public GolemAction getCurrentAction(){
		return KingdomData.canGolemsHelp() ? currentAction : null;
	}
	
	public Golem getGolem(){
		return EntityUtils.getGolemById(golemID);
	}
	
	public GolemAction findBestAction(){
		if(!KingdomData.canGolemsHelp())
			return null;
		Golem golem = getGolem();
		if(golem == null)
			return null;
		GolemAction action = findBestAction(HIGH, golem);
		if(action != null)
			return action;
		action = findBestAction(MEDIUM, golem);
		if(action != null)
			return action;
		action = findBestAction(LOW, golem);
		if(action != null)
			return action;
		action = findBestAction(NONE, golem);
		return action;
	}
	
	private GolemAction findBestAction(GolemPriority priority, Golem golem){
		ArrayList<GolemTask> tas = tasks.get(priority);
		ArrayList<GolemAction> acs = actions.get(priority);
		int i = 0;
		while(i < tas.size()){
			GolemAction newAction = tas.get(i).getNextAction(golem);
			if(newAction != null && !acs.contains(newAction))
				acs.add(newAction);
			++i;
		}
		GolemAction returnAction = null;
		double minDistance = Double.POSITIVE_INFINITY;
		i = 0;
		while(i < acs.size()){
			GolemAction action = acs.get(i);
			double distance = action.getDistance(golem);
			if(distance < minDistance){
				minDistance = distance;
				returnAction = action;
			}
			++i;
		}
		return returnAction;
	}
	
	public void onPlayerKingdomSwitch(Player player, Kingdom from, Kingdom to){
		Golem golem = getGolem();
		if(golem == null || golem.getWorld() != player.getWorld() || !KingdomData.canGolemsHelp())
			return;
		int i = 0;
		while(i < highTasks.size()){
			addSafeAction(highTasks.get(i).onKingdomSwitch(player, from, to), GolemPriority.HIGH);
			++i;
		}
		i = 0;
		while(i < mediumTasks.size()){
			addSafeAction(mediumTasks.get(i).onKingdomSwitch(player, from, to), GolemPriority.MEDIUM);
			++i;
		}
		i = 0;
		while(i < lowTasks.size()){
			addSafeAction(lowTasks.get(i).onKingdomSwitch(player, from, to), GolemPriority.LOW);
			++i;
		}
		i = 0;
		while(i < lowestTasks.size()){
			addSafeAction(lowestTasks.get(i).onKingdomSwitch(player, from, to), GolemPriority.NONE);
			++i;
		}
		if(isDirty)
			refreshCurrentAction();
		isDirty = false;
	}

	public void onEntityAttack(EntityDamageByEntityEvent event) {
		Golem golem = getGolem();
		if(golem == null || !KingdomData.canGolemsHelp())
			return;
		Entity attacker = event.getDamager();
		if(attacker instanceof Projectile){
			ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
			if(source instanceof Entity)
				attacker = (Entity) source;
		}
		int i = 0;
		while(i < highTasks.size()){
			addSafeAction(highTasks.get(i).onEntityAttack(attacker, event.getEntity(), golem), GolemPriority.HIGH);
			++i;
		}
		i = 0;
		while(i < mediumTasks.size()){
			addSafeAction(mediumTasks.get(i).onEntityAttack(attacker, event.getEntity(), golem), GolemPriority.MEDIUM);
			++i;
		}
		i = 0;
		while(i < lowTasks.size()){
			addSafeAction(lowTasks.get(i).onEntityAttack(attacker, event.getEntity(), golem), GolemPriority.LOW);
			++i;
		}
		i = 0;
		while(i < lowestTasks.size()){
			addSafeAction(lowestTasks.get(i).onEntityAttack(attacker, event.getEntity(), golem), GolemPriority.NONE);
			++i;
		}
		if(isDirty)
			refreshCurrentAction();
		isDirty = false;
	}
	
	public void verifyMovement(Block newBlock){
		if(!KingdomData.canGolemsHelp())
			return;
		Golem golem = getGolem();
		if(golem == null)
			return;
		refreshCurrentAction();
	}
	
	public void save(ConfigurationSection section){
		int i = 0;
		while(i < lowestTasks.size()){
			section.set("lowest task " + i, lowestTasks.get(i).getSaveString());
			++i;
		}
		i = 0;
		while(i < lowTasks.size()){
			section.set("low task " + i, lowTasks.get(i).getSaveString());
			++i;
		}
		i = 0;
		while(i < mediumTasks.size()){
			section.set("medium task " + i, mediumTasks.get(i).getSaveString());
			++i;
		}
		i = 0;
		while(i < highTasks.size()){
			section.set("high task " + i, highTasks.get(i).getSaveString());
			++i;
		}
	}
	
	public static GolemTasks load(ConfigurationSection section){
		GolemTasks tasks = new GolemTasks(section.getName());
		int i = 0;
		while(section.contains("lowest task " + i)){
			tasks.addSafeTask(GolemTask.fromString(section.getString("lowest task " + i)), GolemPriority.NONE);
			++i;
		}
		i = 0;
		while(section.contains("low task " + i)){
			tasks.addSafeTask(GolemTask.fromString(section.getString("low task " + i)), GolemPriority.LOW);
			++i;
		}
		i = 0;
		while(section.contains("medium task " + i)){
			tasks.addSafeTask(GolemTask.fromString(section.getString("medium task " + i)), GolemPriority.MEDIUM);
			++i;
		}
		i = 0;
		while(section.contains("high task " + i)){
			tasks.addSafeTask(GolemTask.fromString(section.getString("high task " + i)), GolemPriority.HIGH);
			++i;
		}
		return tasks;
	}

	public String getGolemID() {
		return golemID;
	}
}
