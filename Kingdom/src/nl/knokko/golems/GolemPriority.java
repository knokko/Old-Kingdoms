package nl.knokko.golems;

public enum GolemPriority {
	
	NONE,
	LOW,
	MEDIUM,
	HIGH;
	
	public boolean isMoreImportant(GolemPriority other){
		return ordinal() > other.ordinal();
	}
	
	public boolean isLessImportant(GolemPriority other){
		return other.ordinal() > ordinal();
	}
	
	public static GolemPriority fromString(String priority){
		return valueOf(priority.toUpperCase());
	}
}
