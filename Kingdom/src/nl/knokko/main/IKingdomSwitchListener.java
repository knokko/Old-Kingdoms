package nl.knokko.main;

import nl.knokko.kingdom.Kingdom;

import org.bukkit.entity.Player;

public interface IKingdomSwitchListener {
	
	public void onPlayerKingdomSwitch(Player player, Kingdom from, Kingdom to);
}
