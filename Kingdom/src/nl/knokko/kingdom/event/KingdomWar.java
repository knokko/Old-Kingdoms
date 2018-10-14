package nl.knokko.kingdom.event;

import java.util.ArrayList;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;

public class KingdomWar extends KingdomEvent {
	
	private Kingdom starter;
	private Kingdom other;
	
	private byte requestedCancel = 0;
	
	public KingdomWar(Kingdom declarer, Kingdom target){
		super(startAfterDeclare(), endAfterDeclare());
		starter = declarer;
		other = target;
	}
	
	public KingdomWar(Calendar startTime, Kingdom declarer, Kingdom target){
		super(startTime, endAfterStart(startTime));
		starter = declarer;
		other = target;
	}

	private KingdomWar(Calendar startTime, Calendar endTime, Kingdom declarer, Kingdom target) {
		super(startTime, endTime);
		starter = declarer;
		other = target;
	}
	
	@Override
	public String toString(){
		return getWarStarter().getName() + " " + getWarOpponent().getName() + " " + start.getTimeInMillis() + " " + requestedCancel;
	}
	
	public String register(){
		return KingdomData.registerWar(this);
	}
	
	/**
	 * This method returns true if the wars are too soon after each other and contain the same declarer and the same target.
	 * @param other The other war
	 * @return Whether the wars can both happen, or not.
	 */
	public boolean isTooSoon(KingdomWar otherWar){
		if(otherWar.starter == starter && otherWar.other == other){
			if(start.before(otherWar.end) && otherWar.start.before(end))
				return true;
			long difference = Math.abs(otherWar.end.getTimeInMillis() - start.getTimeInMillis());
			long difference2 = Math.abs(end.getTimeInMillis() - otherWar.start.getTimeInMillis());
			return difference < 3600000 * KingdomData.restHours() || difference2 < 3600000 * KingdomData.restHours();
		}
		return false;
	}
	
	public Kingdom getWarStarter(){
		return starter;
	}
	
	public Kingdom getWarOpponent(){
		return other;
	}
	
	public void requestCancel(Kingdom requester){
		if(requester == starter){
			if(requestedCancel == 1)
				cancel();
			else
				requestedCancel = -1;
		}
		else if(requester == other){
			if(requestedCancel == -1)
				cancel();
			else
				requestedCancel = 1;
		}
		else
			return;
		ArrayList<Player> players = other.getOnlinePlayers(Bukkit.getServer());
		players.addAll(starter.getOnlinePlayers(Bukkit.getServer()));
		int i = 0;
		while(i < players.size()){
			players.get(i).sendMessage(requester.getColoredName() + ChatColor.YELLOW + " has requested to cancel the war against " + (requester == starter ? other.getColoredName() : starter.getColoredName()));
			++i;
		}
	}
	
	public void cancel(){
		KingdomData.unregisterWar(this, true);
	}
	
	public void expire(){
		KingdomData.unregisterWar(this, false);
	}
	
	public boolean contains(Kingdom kd){
		return kd == starter || kd == other;
	}
	
	public Kingdom cancelRequester(){
		if(requestedCancel == -1)
			return starter;
		if(requestedCancel == 1)
			return other;
		return null;
	}
	
	private KingdomWar setRequestCancel(byte request){
		requestedCancel = request;
		return this;
	}
	
	private static Calendar startAfterDeclare(){
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR_OF_DAY, KingdomData.minHoursBeforeWar);
		return now;
	}
	
	private static Calendar endAfterDeclare(){
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR_OF_DAY, KingdomData.minHoursBeforeWar + KingdomData.warDurationInHours);
		return now;
	}
	
	private static Calendar endAfterStart(Calendar start){
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.HOUR_OF_DAY, KingdomData.warDurationInHours);
		return end;
	}
	
	public static KingdomWar fromString(String string){
		try {
			int index1 = string.indexOf(" ");
			int index2 = string.indexOf(" ", index1 + 1);
			int index3 = string.indexOf(" ", index2 + 1);
			Kingdom kdStart = KingdomData.getKingdom(string.substring(0, index1));
			Kingdom kdOther = KingdomData.getKingdom(string.substring(index1 + 1, index2));
			long startTime = Long.parseLong(string.substring(index2 + 1, index3));
			byte requestedCancel = Byte.valueOf(string.substring(index3 + 1));
			Calendar start = new Calendar.Builder().setInstant(startTime).build();
			return new KingdomWar(start, endAfterStart(start), kdStart, kdOther).setRequestCancel(requestedCancel);
		} catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
}
