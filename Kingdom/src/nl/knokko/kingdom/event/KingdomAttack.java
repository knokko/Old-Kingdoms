package nl.knokko.kingdom.event;

import java.util.Calendar;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;

public class KingdomAttack extends KingdomEvent {
	
	private Kingdom attacker;
	private Kingdom defender;

	public KingdomAttack(Calendar startTime, Kingdom attacker, Kingdom defender) {
		super(startTime, endAfterStart(startTime));
		this.attacker = attacker;
		this.defender = defender;
	}
	
	public KingdomAttack(Kingdom attacker, Kingdom defender){
		this(startAfterDeclare(Calendar.getInstance()), attacker, defender);
	}
	
	private KingdomAttack(Calendar startTime, Calendar endTime, Kingdom attacker, Kingdom defender){
		super(startTime, endTime);
		this.attacker = attacker;
		this.defender = defender;
	}
	
	public Kingdom getAttacker(){
		return attacker;
	}
	
	public Kingdom getDefender(){
		return defender;
	}
	
	/**
	 * Cuts the start time and the end time of this attack, so they are within this war.
	 * @param war The war this attack will be placed in.
	 * @return whether startTime or endTime changed, or not.
	 */
	public boolean cutTime(KingdomWar war){
		boolean changed = false;
		if(start.before(war.start)){
			start = war.start;
			changed = true;
		}
		if(end.after(war.end)){
			end = war.end;
			changed = true;
		}
		return changed;
	}
	
	/**
	 * If the startTime and endTime of this attack are (partially) outside the minAttackTime and maxAttackTime, this method will return true and cut the times, so they are within minAttackTime and maxAttackTime.
	 * @return whether startTime or endTime has changed, or not.
	 */
	public boolean cutTime(){
		boolean changed = false;
		int[] min = KingdomData.minAttackTime();
		if(start.get(Calendar.HOUR_OF_DAY) < min[0]){
			start.set(Calendar.HOUR_OF_DAY, min[0]);
			changed = true;
		}
		if(start.get(Calendar.HOUR_OF_DAY) == min[0]){
			if(start.get(Calendar.MINUTE) < min[1]){
				start.set(Calendar.MINUTE, min[1]);
				changed = true;
			}
		}
		int[] max = KingdomData.maxAttackTime();
		if(end.get(Calendar.HOUR_OF_DAY) > max[0]){
			end.set(Calendar.HOUR_OF_DAY, max[0]);
			changed = true;
		}
		if(end.get(Calendar.HOUR_OF_DAY) == max[0]){
			if(end.get(Calendar.MINUTE) > max[1]){
				end.set(Calendar.MINUTE, max[1]);
				changed = true;
			}
		}
		if((start.get(Calendar.DAY_OF_MONTH) != end.get(Calendar.DAY_OF_MONTH)) && ((end.get(Calendar.HOUR_OF_DAY) < min[0]) || (end.get(Calendar.HOUR_OF_DAY) == min[0] && end.get(Calendar.MINUTE) < min[1]))){
			end.set(Calendar.MINUTE, max[1]);
			end.set(Calendar.HOUR_OF_DAY, max[0]);
			end.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH));
			end.set(Calendar.MONTH, start.get(Calendar.MONTH));
			end.set(Calendar.YEAR, start.get(Calendar.YEAR));
			changed = true;
		}
		return changed;
	}
	
	public boolean contains(Kingdom kd){
		return kd == attacker || kd == defender;
	}
	
	public String register(){
		return KingdomData.registerAttack(this);
	}
	
	public void expire(){
		KingdomData.unregisterAttack(this, false);
	}
	
	public void cancel(){
		KingdomData.unregisterAttack(this, true);
	}
	
	@Override
	public String toString(){
		return attacker.getName() + " " + defender.getName() + " " + start.getTimeInMillis() + " " + end.getTimeInMillis();
	}
	
	private static Calendar endAfterStart(Calendar start){
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.HOUR_OF_DAY, 2);
		return end;
	}
	
	private static Calendar startAfterDeclare(Calendar declare){
		Calendar start = (Calendar) declare.clone();
		start.add(Calendar.HOUR_OF_DAY, 1);
		return start;
	}

	public static KingdomAttack fromString(String string) {
		try {
			int index1 = string.indexOf(" ");
			int index2 = string.indexOf(" ", index1 + 1);
			int index3 = string.indexOf(" ", index2 + 1);
			Kingdom kdAtt = KingdomData.getKingdom(string.substring(0, index1));
			Kingdom kdDef = KingdomData.getKingdom(string.substring(index1 + 1, index2));
			long startMillis = Long.parseLong(string.substring(index2 + 1, index3));
			long endMillis = Long.parseLong(string.substring(index3 + 1));
			Calendar start = new Calendar.Builder().setInstant(startMillis).build();
			Calendar end = new Calendar.Builder().setInstant(endMillis).build();
			return new KingdomAttack(start, end, kdAtt, kdDef);
		} catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
}
