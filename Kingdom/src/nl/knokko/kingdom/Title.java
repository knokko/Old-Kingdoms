package nl.knokko.kingdom;

import org.bukkit.ChatColor;

public class Title {
	
	private String name;
	
	private ChatColor color;
	
	public static Title load(String save){
		int index = save.indexOf("-");
		if(index < 0)
			throw new RuntimeException("The save string doesn't contain the '-'");
		ChatColor c = ChatColor.valueOf(save.substring(0, index));
		String n = save.substring(index + 1);
		return new Title(n, c);
	}
	
	public Title(String titleName, ChatColor chatColor){
		name = titleName;
		color = chatColor;
	}
	
	@Override
	public String toString(){
		return getColoredName();
	}
	
	public String save(){
		return color.name() + "-" + name;
	}
	
	public String getName(){
		return name;
	}
	
	public ChatColor getColor(){
		return color;
	}
	
	public String getColoredName(){
		return color + name;
	}
}
