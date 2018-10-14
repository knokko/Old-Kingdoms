package nl.knokko.command;

import static org.bukkit.ChatColor.*;

import java.util.ArrayList;

import net.minecraft.server.v1_10_R1.EntityGolem;
import net.minecraft.server.v1_10_R1.NavigationAbstract;
import net.minecraft.server.v1_10_R1.PathEntity;
import nl.knokko.data.KingdomData;
import nl.knokko.golems.GolemFollowTask;
import nl.knokko.golems.GolemPriority;
import nl.knokko.golems.GolemState;
import nl.knokko.golems.GolemTasks;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.util.EntityUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandGolems implements CommandExecutor {
	
	public static final String USEAGE = RED + "You should use /golems enable/disable/call/tool/help";
	public static final String GOLEMSDISABLED = RED + "The use of golems is disabled on this server. Staff can change this with " + YELLOW + "/kingdom options canGolemsHelp true";

	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		if(!KingdomData.canGolemsHelp()){
			sender.sendMessage(GOLEMSDISABLED);
			return false;
		}
		if(sender instanceof Player){
			Player player = (Player) sender;
			Kingdom kd = KingdomData.getKingdom(player);
			if(kd != null){
				if(kd.canStartAttack(player)){
					if(arguments.length == 1){
						if(arguments[0].equals("enable")){
							kd.letIronGolemsHelp(true);
							kd.letSnowGolemsHelp(true);
							sender.sendMessage(GREEN + "All golems can now help in wars.");
						}
						else if(arguments[0].equals("disable")){
							kd.letIronGolemsHelp(false);
							kd.letSnowGolemsHelp(false);
							sender.sendMessage(GREEN + "No golems can help in wars anymore.");
						}
						else if(arguments[0].equals("call")){
							ArrayList<Golem> golems = EntityUtils.getAvailableGolems(player, 100);
							if(!golems.isEmpty()){
								int i = 0;
								while(i < golems.size()){
									Golem golem = golems.get(i);
									GolemTasks tasks = KingdomData.getGolemTasks(golem);
									tasks.addTask(new GolemFollowTask(GolemState.ASSISTING, GolemPriority.MEDIUM, player));
									++i;
								}
								sender.sendMessage(GREEN + "" + i + " golems are commanded to assist you.");
							}
							else
								sender.sendMessage(RED + "Your kingdom doesn't have any available golems in this world.");
						}
						else if(arguments[0].equals("tool")){
							giveGolemTool(player, 100, GolemPriority.LOW);
						}
						else if(arguments[0].equals("help")){
							sender.sendMessage(YELLOW + "Met dit commando kun je de golems in je kingdom om hulp vragen bij gevechten.");
							sender.sendMessage(YELLOW + "Alle golems die spawnen of gebouwd worden " + BOLD + " in het geclaimde gebied van jouw kingdom " + RESET + YELLOW + " (zie /claim) staan onder het bevel van jouw kingdom.");
							sender.sendMessage(YELLOW + "Met het commando " + GREEN + "/golems call (range) (priority)" + YELLOW + " roep je al je golems binnen de '(range)' op om naar jou toe te komen, de 'priority' bepaalt wat ze doen met hun huidige taken.");
							sender.sendMessage(YELLOW + "Wanneer een vijand tijdens een geplande aanval jouw grens oversteekt, zullen al je golems hem automatisch aanvallen.");
							sender.sendMessage(YELLOW + "Met " + GREEN + "/golems disable" + YELLOW + " schakel je je golems uit en zullen ze niet meer helpen totdat ze weer worden aangezet.");
							sender.sendMessage(YELLOW + "Met " + GREEN + "/golems enable" + YELLOW + " zet je alle golems in je kingdom weer aan.");
							sender.sendMessage(YELLOW + "Met het commando " + GREEN + "/golems help enable/disable/call/tool" + YELLOW + " krijg je meer informatie over een specifiek deel van dit commando.");
							sender.sendMessage(YELLOW + "De staff kan het gebruik van golems verbieden met " + GREEN + "/kingdom options canGolemsHelp false" + GREEN + ". Wanneer hij dat doet, zullen golems niet meer luisteren, ongeacht ze aan staan in hun kingdom of niet.");
							sender.sendMessage(YELLOW + "De staff kan het gebruik van golems weer toestaan met " + GREEN + "/kingdom options canGolemsHelp true" + GREEN + ". Dan zullen alle golems weer meehelpen, mits ze niet zijn uitgeschakeld door hun kingdom.");
						}
					}
					else if(arguments.length == 2){
						if(arguments[0].equals("call")){
							try {
								int range = Integer.decode(arguments[1]);
								ArrayList<Golem> golems = EntityUtils.getAvailableGolems(player, range);
								if(!golems.isEmpty()){
									int i = 0;
									while(i < golems.size()){
										Golem golem = golems.get(i);
										KingdomData.getGolemTasks(golem).addTask(new GolemFollowTask(GolemState.ASSISTING, GolemPriority.MEDIUM, player));
										++i;
									}
									sender.sendMessage(GREEN + "" + i + " golems are commanded to assist you.");
								}
								else
									sender.sendMessage(RED + "Your kingdom doesn't have any available golems in this world.");
							} catch(NumberFormatException ex){
								sender.sendMessage(RED + "Argument range(" + arguments[1] + ") should be a number.");
							}
						}
						else if(arguments[0].equals("tool")){
							try {
								int range = Integer.decode(arguments[1]);
								giveGolemTool(player, range, GolemPriority.MEDIUM);
							} catch(NumberFormatException ex){
								sender.sendMessage(RED + "Argument range(" + arguments[1] + ") should be a number.");
							}
						}
						else if(arguments[0].equals("help")){
							if(arguments[1].equals("enable")){
								sender.sendMessage(YELLOW + "Met het commando " + GREEN + "/golems enable" + YELLOW + " sta je de golems in je kingdom toe om te helpen tijdens gevechten en sta je toe dat je leidinggevenden ze commanderen.");
								sender.sendMessage(YELLOW + "Maar als de staff het gebruik van golems heeft uitgeschakeld, kun je ze niet commanderen, of jij het nu toestaat, of niet. De staff kan het gebruik van golems aan/uitschakelen met " + GREEN + "/kingdom options canGolemsHelp true/false");
							}
							else if(arguments[1].equals("disable")){
								sender.sendMessage(YELLOW + "Met het commando " + GREEN + "/golems disable" + YELLOW + " verbied je golems te helpen tijdens oorlogen en kan je kingdom geen golems meer commanderen.");
								sender.sendMessage(YELLOW + "Als de staff het gebruik van golems al heeft uitgeschakeld, heeft dit commando geen effect, maar als de staff het wel toestaat, kan je kingdom toch geen golems commanderen.");
							}
							else if(arguments[1].equals("call")){
								sender.sendMessage(YELLOW + "Met het commando " + GREEN + "/golems call (range) (priority)" + YELLOW + " roep je alle beschikbare golems in je wereld op om naar je toe te komen.");
								sender.sendMessage(YELLOW + "Het argument 'range' is het bereik in blokken/meters, alleen golems binnen dit bereik zullen komen, anderen zullen niet reageren.");
								sender.sendMessage(YELLOW + "Het argument 'priority' geeft aan wat de golems moeten doen met hun huidige taken: Een priority van 'low' betekent dat de golems alleen moeten komen als ze niet aan het vechten zijn, een priority van 'medium' betekent dat golems alleen moeten komen als ze niet tegen spelers aan het vechten zijn (ze zullen gevechten met monsters wel afbreken), een priority van 'high' betekent dat de golems hun huidige taak moeten neerleggen en naar jou moeten komen.");
							}
							else if(arguments[1].equals("tool")){
								sender.sendMessage(YELLOW + "Met het commando " + GREEN + "/golems tool (range) (priority)" + YELLOW + " krijg je de Golem Tool in je inventaris.");
								sender.sendMessage(YELLOW + "Met de Golem Tool kun je golems snel commanderen: Als je het vasthoud en je de linker muisknop gebruikt, zullen de golems binnen het bereik naar de mob/speler lopen waar je je cursor op richt, en hem aanvallen.");
								sender.sendMessage(YELLOW + "Wanneer je je rechter muisknop indrukt met de Golem Tool in je hand, zullen al je golems binnen het bereik naar het blok lopen waar je je cursor op richt.");
								sender.sendMessage(YELLOW + "Het argument 'range' geeft het bereik van je Golem Tool aan in blokken/meters. Alleen golems die een kleiner afstand tot jou hebben dan het bereik, zullen naar je luisteren.");
								sender.sendMessage(YELLOW + "Het argument 'priority' geeft aan wat de golems moeten doen met hun huidige taken: Een priority van 'low' betekent dat de golems alleen moeten komen als ze niet aan het vechten zijn, een priority van 'medium' betekent dat golems alleen moeten komen als ze niet tegen spelers aan het vechten zijn (ze zullen gevechten met monsters wel afbreken), een priority van 'high' betekent dat de golems hun huidige taak moeten neerleggen en meteen moeten beginnen.");
							}
						}
						else
							sender.sendMessage(USEAGE);
					}
					else if(arguments.length == 3){
						if(arguments[0].equals("call")){
							try {
								int range = Integer.decode(arguments[1]);
								GolemPriority priority = GolemPriority.fromString(arguments[2]);
								ArrayList<Golem> golems = EntityUtils.getAvailableGolems(player, range);
								if(!golems.isEmpty()){
									int i = 0;
									while(i < golems.size()){
										Golem golem = golems.get(i);
										KingdomData.getGolemTasks(golem).addTask(new GolemFollowTask(GolemState.ASSISTING, priority, player));
										++i;
									}
									sender.sendMessage(GREEN + "" + i + " golems are commanded to assist you.");
								}
								else
									sender.sendMessage(RED + "Your kingdom doesn't have any available golems in this world.");
							} catch(NumberFormatException ex){
								sender.sendMessage(RED + "Argument range(" + arguments[1] + ") should be a number.");
							} catch(IllegalArgumentException ex){
								sender.sendMessage(RED + "Argument priority(" + arguments[2] + ") should be 'low', 'medium' or 'high'");
							}
						}
						else if(arguments[0].equals("tool")){
							try {
								int range = Integer.decode(arguments[1]);
								GolemPriority priority = GolemPriority.fromString(arguments[2]);
								giveGolemTool(player, range, priority);
							} catch(NumberFormatException ex){
								sender.sendMessage(RED + "Argument range(" + arguments[1] + ") should be a number.");
							} catch(IllegalArgumentException ex){
								sender.sendMessage(RED + "Argument priority(" + arguments[2] + ") should be 'low', 'medium' or 'high'");
							}
						}
						else
							sender.sendMessage(RED + "You should use /golems call/tool (range) (priority)");
					}
					else
						sender.sendMessage(USEAGE);
				}
				else
					sender.sendMessage(RED + "You need to be lutenant, duke, general, governer or king to use this command.");
			}
			else
				sender.sendMessage(RED + "You need a kingdom to use this command.");
		}
		else
			sender.sendMessage(RED + "Only players can use this command.");
		return true;
	}
	
	public static void setGolemGoal(Golem golem, double x, double y, double z){
		CraftGolem craftGolem = (CraftGolem) golem;
		EntityGolem vanGolem = craftGolem.getHandle();
		NavigationAbstract nav = vanGolem.getNavigation();
		nav.a(nav.a(x, y, z), 1);
	}
	
	public static void setGolemGoal(Golem golem, Entity goal){
		setGolemGoal(golem, goal.getLocation().getX(), goal.getLocation().getY(), goal.getLocation().getZ());
	}
	
	public static void setGolemPath(Golem golem, double x, double y, double z){
		CraftGolem craftGolem = (CraftGolem) golem;
		EntityGolem vanGolem = craftGolem.getHandle();
		NavigationAbstract nav = vanGolem.getNavigation();
		nav.a(nav.a(x, y, z), 1);
	}
	
	public static void setGolemPath(Golem golem, Location location){
		if(location.getWorld() == golem.getWorld())
			setGolemPath(golem, location.getX(), location.getY(), location.getZ());
	}
	
	public static PathEntity getGolemPath(Golem golem){
		CraftGolem craftGolem = (CraftGolem) golem;
		EntityGolem vanGolem = craftGolem.getHandle();
		NavigationAbstract nav = vanGolem.getNavigation();
		return nav.k();
	}
	
	public static void giveGolemTool(Player player, int range, GolemPriority priority){
		ItemStack tool = new ItemStack(Material.IRON_HOE);
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.IRON_HOE);
		meta.setDisplayName("Golem Tool");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Left click with this tool to let your");
		lore.add("golems attack the target you are looking at.");
		lore.add("Right click with this tool to call your");
		lore.add("golems to the block or entity you are looking at.");
		lore.add("This tool will only call golems within a range of " + range + " blocks.");
		lore.add("The priority of this tool is: " + priority.name().toLowerCase());
		lore.add("In order to use this tool, you need a kingdom and the right permissions.");
		lore.add("This item will disappear if you drop it.");
		meta.setLore(lore);
		tool.setItemMeta(meta);
		player.getInventory().addItem(tool);
	}
	
	public static int getToolRange(ItemMeta meta){
		if(meta.getLore().size() <= 4)
			return -2;
		String rule = meta.getLore().get(4);
		try {
			return Integer.decode(rule.substring(50, rule.length() - 8));
		} catch(Exception ex){
			ex.printStackTrace();
			return -1;
		}
	}
	
	public static GolemPriority getToolPriority(ItemMeta meta){
		if(meta.getLore().size() <= 5)
			return GolemPriority.LOW;
		String rule = meta.getLore().get(5);
		try {
			return GolemPriority.fromString(rule.substring(30));
		} catch(Exception ex){
			ex.printStackTrace();
			return GolemPriority.LOW;
		}
	}
}
