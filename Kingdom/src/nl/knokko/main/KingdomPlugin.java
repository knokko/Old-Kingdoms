package nl.knokko.main;

import nl.knokko.command.CommandClaim;
import nl.knokko.command.CommandGolems;
import nl.knokko.command.CommandKingdom;
import nl.knokko.command.CommandWar;
import nl.knokko.data.KingdomData;
import nl.knokko.util.EntityUtils;

import org.bukkit.plugin.java.JavaPlugin;

public class KingdomPlugin extends JavaPlugin {
	
	public static KingdomPlugin instance;
	
	@Override
	public void onEnable(){
		instance = this;
		KingdomData.load(this);
		getCommand("kingdom").setExecutor(new CommandKingdom());
		getCommand("claim").setExecutor(new CommandClaim());
		getCommand("war").setExecutor(new CommandWar());
		getCommand("golems").setExecutor(new CommandGolems());
		getServer().getPluginManager().registerEvents(new KingdomEventHandler(), this);
		getServer().getPluginManager().registerEvents(new EntityUtils(), this);
	}
	
	@Override
	public void onDisable(){
		KingdomData.save(this);
	}
}
