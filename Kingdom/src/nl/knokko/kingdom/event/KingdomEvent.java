package nl.knokko.kingdom.event;

import java.util.Calendar;

public abstract class KingdomEvent {
	
	protected Calendar start;
	protected Calendar end;

	public KingdomEvent(Calendar startTime, Calendar endTime) {
		start = startTime;
		end = endTime;
	}
	
	public Calendar getStart(){
		return start;
	}
	
	public Calendar getEnd(){
		return end;
	}
	
	public static String getTimeString(Calendar calendar){
		return calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " at " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
	}
	
	public String getStartTimeString(){
		return getTimeString(start);
	}
	
	public String getEndTimeString(){
		return getTimeString(end);
	}
	
	public boolean isEnabled(Calendar time){
		return (start.before(time) || start.equals(time)) && (end.after(time) || end.equals(time));
	}
	
	public boolean isEnabled(){
		return isEnabled(Calendar.getInstance());
	}
	
	/**
	 * This method returns true if the event is enabled all the time during startTime and endTime.
	 * @param startTime The first time this event has to be enabled.
	 * @param endTime The last time this event has to be enabled.
	 * @return whether this event is enabled during startTime and endTime, or not.
	 */
	public boolean isEnabled(Calendar startTime, Calendar endTime){
		return isEnabled(startTime) && isEnabled(endTime);
	}
	
	public boolean isExpired(Calendar time){
		return end.before(time);
	}
	
	public boolean isExpired(){
		return isExpired(Calendar.getInstance());
	}
}
