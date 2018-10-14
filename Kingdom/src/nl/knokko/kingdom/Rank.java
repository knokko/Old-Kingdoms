package nl.knokko.kingdom;

import org.bukkit.ChatColor;

public enum Rank {
	
	RECRUITER(true, false, false, false, false, false, false, ChatColor.GREEN),
	LUTENANT(true, true, false, false, false, false, false, ChatColor.RED),
	DUKE(true, true, false, true, false, false, false, ChatColor.YELLOW),
	GENERAL(true, true, true, false, false, false, false, ChatColor.DARK_RED),
	GOVERNER(true, true, true, true, true, true, true, ChatColor.DARK_GRAY),
	KING(true, true, true, true, true, true, true, ChatColor.GOLD);
	
	public final boolean canInvite;
	public final boolean canStartAttack;
	public final boolean canDeclareWar;
	public final boolean canClaimGround;
	public final boolean canChangeColor;
	public final boolean canKick;
	public final boolean canPromote;
	
	public final ChatColor color;
	
	private Rank(boolean invite, boolean startAttack, boolean declareWar, boolean claim, boolean changeColor, boolean kick, boolean promote, ChatColor chatColor){
		canInvite = invite;
		canStartAttack = startAttack;
		canDeclareWar = declareWar;
		canClaimGround = claim;
		canChangeColor = changeColor;
		canKick = kick;
		canPromote = promote;
		color = chatColor;
	}
	
	@Override
	public String toString(){
		return super.toString().substring(0, 1) + super.toString().toLowerCase().substring(1);
	}
	
	public static Rank fromString(String string){
		return Rank.valueOf(string.toUpperCase());
	}
}
