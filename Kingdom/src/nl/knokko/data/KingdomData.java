package nl.knokko.data;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import nl.knokko.golems.*;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.kingdom.event.KingdomAttack;
import nl.knokko.kingdom.event.KingdomWar;
import nl.knokko.main.KingdomEventHandler;
import nl.knokko.main.KingdomPlugin;
import nl.knokko.util.EntityUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

public class KingdomData {
	
	public static ArrayList<Kingdom> kingdoms = new ArrayList<Kingdom>();
	
	private static HashMap<String, Kingdom> playerKingdoms = new HashMap<String, Kingdom>();
	private static HashMap<String, String> claimedGround = new HashMap<String, String>();
	private static HashMap<String, Kingdom> golems = new HashMap<String,Kingdom>();
	private static HashMap<String, GolemTasks> golemTasks = new HashMap<String, GolemTasks>();
	private static ArrayList<KingdomWar> wars = new ArrayList<KingdomWar>();
	private static ArrayList<KingdomAttack> attacks = new ArrayList<KingdomAttack>();
	
	public static Map<Player, ArrayList<Chunk>> pendingChunks = new HashMap<Player, ArrayList<Chunk>>();
	public static ArrayList<Player> requestedRemoveKingdom = new ArrayList<Player>();
	
	public static int chunksPerPlayer = 20;
	public static int restHours = 48;
	public static int minHoursBeforeWar = 24;
	public static int warDurationInHours = 24;
	
	/**
	 * minAttackTime[0] = hour
	 * minAttackTime[1] = minute
	 */
	public static int[] minAttackTime = new int[]{10,0};
	
	/**
	 * maxAttackTime[0] = hour
	 * maxAttackTime[1] = minute
	 */
	public static int[] maxAttackTime = new int[]{22,0};
	
	public static boolean canEditOtherKingdoms = false;
	public static boolean canGolemsHelp = false;
	public static boolean canEveryoneCreateKingdom = false;

	private KingdomData() {}
	
	public static void save(JavaPlugin plugin){
		removeExpiredWars();
		removeExpiredAttacks();
		YamlConfiguration config = new YamlConfiguration();
		int i = 0;
		while(i < kingdoms.size()){
			kingdoms.get(i).save(config);
			++i;
		}
		i = 0;
		while(i < wars.size()){
			config.set("war " + i, wars.get(i).toString());
			++i;
		}
		i = 0;
		while(i < attacks.size()){
			config.set("attack " + i, attacks.get(i).toString());
			++i;
		}
		Iterator<Entry<String, String>> iterator = claimedGround.entrySet().iterator();
		ConfigurationSection section = config.createSection("ground claims");
		while(iterator.hasNext()){
			Entry<String,String> entry = iterator.next();
			section.set(entry.getKey(), entry.getValue());
		}
		//TODO QUESTIONABLE
		ConfigurationSection tasks = config.createSection("golem tasks");
		Iterator<Entry<String, GolemTasks>> iterator2 = golemTasks.entrySet().iterator();
		while(iterator2.hasNext()){
			Entry<String, GolemTasks> entry = iterator2.next();
			entry.getValue().save(tasks.createSection(entry.getValue().getGolemID()));
		}
		//TODO QUESTIONABLE
		config.set("chunksPerPlayer", chunksPerPlayer);
		config.set("restHours", restHours);
		config.set("minHoursBeforeWar", minHoursBeforeWar);
		config.set("warDurationInHours", warDurationInHours);
		config.set("minAttackHour", minAttackTime[0]);
		config.set("minAttackMinute", minAttackTime[1]);
		config.set("maxAttackHour", maxAttackTime[0]);
		config.set("maxAttackMinute", maxAttackTime[1]);
		config.set("canEditOtherKingdoms", canEditOtherKingdoms);
		config.set("canGolemsHelp", canGolemsHelp);
		config.set("canEveryoneCreateKingdom", canEveryoneCreateKingdom);
		try {
			config.save(new File(plugin.getDataFolder(), "kingdom data.yml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void load(JavaPlugin plugin){
		File file = new File(plugin.getDataFolder(), "kingdom data.yml");
		if(file.exists()){
			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(file);
				Iterator<String> keys = config.getKeys(false).iterator();
				while(keys.hasNext()){
					String next = keys.next();
					if(next.startsWith("kingdom ")){
						Kingdom kingdom = new Kingdom(next.substring(8));
						kingdom.load(config);
						kingdoms.add(kingdom);
					}
				}
				int i = 0;
				while(config.contains("war " + i)){
					addWar(KingdomWar.fromString(config.getString("war " + i)));
					++i;
				}
				i = 0;
				while(config.contains("attack " + i)){
					addAttack(KingdomAttack.fromString(config.getString("attack " + i)));
					++i;
				}
				ConfigurationSection section = config.getConfigurationSection("ground claims");
				Iterator<Entry<String, Object>> iterator = section.getValues(false).entrySet().iterator();
				while(iterator.hasNext()){
					Entry<String, Object> entry = iterator.next();
					claimedGround.put(entry.getKey(), (String) entry.getValue());
					Kingdom kd = getKingdom((String) entry.getValue());
					kd.increaseChunks();
				}
				try {
				ConfigurationSection tasks = config.getConfigurationSection("golem tasks");
				Iterator<Entry<String, Object>> iterator2 = tasks.getValues(false).entrySet().iterator();
				Bukkit.getLogger().info("[Kingdom Wars] [KingdomData.load()] The size of section golem tasks = " + tasks.getValues(false).size());
				while(iterator2.hasNext()){
					Entry<String, Object> entry = iterator2.next();
					golemTasks.put(entry.getKey(), GolemTasks.load((ConfigurationSection) entry.getValue()));
				}
				} catch(Exception ex){
					Bukkit.getLogger().warning("Failed to load golem tasks");
					Bukkit.getLogger().throwing("nl.knokko.data.KingdomData", "load(JavaPlugin)", ex);
				}
				//TODO QUESTIONABLE
				if(config.contains("chunksPerPlayer"))
					chunksPerPlayer = config.getInt("chunksPerPlayer");
				if(config.contains("restHours"))
					restHours = config.getInt("restHours");
				if(config.contains("minHoursBeforeWar"))
					minHoursBeforeWar = config.getInt("minHoursBeforeWar");
				if(config.contains("warDurationInHours"))
					warDurationInHours = config.getInt("warDurationInHours");
				if(config.contains("minAttackHour"))
					minAttackTime[0] = config.getInt("minAttackHour");
				if(config.contains("minAttackMinute"))
					minAttackTime[1] = config.getInt("minAttackMinute");
				if(config.contains("maxAttackHour"))
					maxAttackTime[0] = config.getInt("maxAttackHour");
				if(config.contains("maxAttackMinute"))
					maxAttackTime[1] = config.getInt("maxAttackMinute");
				if(config.contains("canEditOtherKingdoms"))
					canEditOtherKingdoms = config.getBoolean("canEditOtherKingdoms");
				if(config.contains("canGolemsHelp"))
					canGolemsHelp = config.getBoolean("canGolemsHelp");
				if(config.contains("canEveryoneCreateKingdom"))
					canEveryoneCreateKingdom = config.getBoolean("canEveryoneCreateKingdom");
				//TODO do something with canEveryoneCreateKingdom!
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("[Kingdom Wars] An error occurend during the loading of the data file.");
			}
		}
		else
			System.out.println("[Kingdom Wars] The data file doesn't exist yet.");
	}
	
	public static void createKingdom(Player king, String name){
		if(getKingdom(king) != null){
			king.sendMessage("You have to leave your current kingdom before you can create a new one.");
			return;
		}
		int i = 0;
		while(i < kingdoms.size()){
			if(kingdoms.get(i).getName().equals(name)){
				king.sendMessage("The kingdom '" + name + "' already exists.");
				return;
			}
			++i;
		}
		Kingdom kingdom = new Kingdom(name, king);
		kingdoms.add(kingdom);
		playerKingdoms.put(king.getUniqueId().toString(), kingdom);
		king.sendMessage("You succesfully created kingdom " + name + ".");
	}
	
	public static void removeKingdom(Kingdom kd){
		kd.broadcast(ChatColor.DARK_RED + "Your kingdom has been removed.");
		kingdoms.remove(kd);
		ArrayList<OfflinePlayer> players = kd.getPlayers(KingdomPlugin.instance.getServer());
		int i = 0;
		while(i < players.size()){
			playerKingdoms.remove(players.get(i).getUniqueId().toString());
			if(players.get(i) instanceof Player)
				KingdomEventHandler.updatePlayerName((Player) players.get(i));
			++i;
		}
		claimedGround.values().removeAll(Collections.singleton(kd.getName()));
		i = 0;
		while(i < wars.size()){
			KingdomWar war = wars.get(i);
			if(war.contains(kd)){
				war.getWarStarter().broadcast(ChatColor.YELLOW + "Because kingdom " + kd.getColoredName() + " has been removed, this war is over.");
				war.getWarOpponent().broadcast(ChatColor.YELLOW + "Because kingdom " + kd.getColoredName() + " has been removed, this war is over.");
				war.getWarStarter().wars.remove(war);
				war.getWarOpponent().wars.remove(war);
				wars.remove(war);
				--i;
			}
			++i;
		}
	}
	
	public static void sendKingdoms(Player player){
		int i = 0;
		while(i < kingdoms.size()){
			Kingdom kingdom = kingdoms.get(i);
			ArrayList<OfflinePlayer> players = kingdom.getPlayers(player.getServer());
			String line = KingdomEventHandler.getKingdomString(kingdom) + ChatColor.WHITE + ": king: " + kingdom.getKing(player.getServer()).getName() + " " + kingdom.getAmountOfPlayers() + " players:[";
			int i1 = 0;
			while(i1 < players.size()){
				line += players.get(i1).getName() + ",";
				++i1;
			}
			if(players.size() > 0)
				line = line.substring(0, line.length() - 1);
			line += "]";
			player.sendMessage(line);
			++i;
		}
		if(kingdoms.isEmpty())
			player.sendMessage("There are no kingdoms yet.");
	}
	
	public static String getKingdoms(){
		String string = "kingdoms:[";
		int i = 0;
		while(i < kingdoms.size()){
			Kingdom kd = kingdoms.get(i);
			string += kd.getColor() + kd.getName() + " with " + kd.getAmountOfPlayers() + " players,";
			++i;
		}
		if(kingdoms.size() > 0)
			string = string.substring(0, string.length() - 1);
		string += "]";
		return string;
	}
	
	public static Kingdom getKingdom(String name){
		int i = 0;
		while(i < kingdoms.size()){
			if(kingdoms.get(i).getName().equals(name))
				return kingdoms.get(i);
			++i;
		}
		return null;
	}
	
	public static Kingdom getKingdom(Player player){
		return playerKingdoms.get(player.getUniqueId().toString());
	}
	
	public static Kingdom getEntityKingdom(Entity entity){
		Player player = null;
		if(entity instanceof Player)
			player = (Player) entity;
		if(entity instanceof Tameable){
			AnimalTamer tamer = ((Tameable) entity).getOwner();
			if(tamer instanceof Player)
				player = (Player) tamer;
		}
		if(entity instanceof Projectile){
			ProjectileSource shooter = ((Projectile) entity).getShooter();
			if(shooter instanceof Player)
				player = (Player) shooter;
		}
		if(player != null)
			return getKingdom(player);
		else
			return getGolemOwner(entity);
	}
	
	public static void setKingdom(String playerUUID, Kingdom kingdom){
		playerKingdoms.put(playerUUID, kingdom);
	}
	
	public static Kingdom removePlayerFromKingdom(Player player){
		Kingdom kd = getKingdom(player);
		if(kd != null && !kd.isKing(player))
			kd.removePlayer(player);
		return kd;
	}
	
	public static String claimGround(Kingdom kd, int chunkX, int chunkZ, World world){
		String key = world.getUID().toString() + "-" + chunkX + "-" + chunkZ;
		Kingdom claimer = getClaim(world, chunkX, chunkZ);
		if(claimer != null && claimer != kd)
			return ChatColor.RED + "Chunk (" + chunkX + ")(" + chunkZ + ") is already claimed.";
		else if(claimer == kd)
			return ChatColor.RED + "You have already claimed Chunk (" + chunkX + ")(" + chunkZ + ")";
		if(kd.getClaimedChunks() >= kd.getAmountOfPlayers() * chunksPerPlayer())
			return ChatColor.RED + "Your kingdom has already claimed the maximum amount of chunks for this amount of players.";
		claimedGround.put(key, kd.getName());
		kd.increaseChunks();
		return ChatColor.GREEN + "You claimed chunk (" + chunkX + ")(" + chunkZ + ") succesfully.";
	}
	
	public static String releaseGround(Kingdom kd, int chunkX, int chunkZ, World world){
		String key = world.getUID().toString() + "-" + chunkX + "-" + chunkZ;
		Kingdom claimer = getClaim(world, chunkX, chunkZ);
		if(claimer == null)
			return ChatColor.RED + "This ground is not claimed.";
		if(claimer != kd)
			return ChatColor.RED + "You can only release the ground of your own kingdom.";
		claimedGround.remove(key);
		kd.decreaseChunks();
		return ChatColor.GREEN + "You released chunk (" + chunkX + ")(" + chunkZ + ") succesfully.";
	}
	
	public static boolean isClaimed(World world, int chunkX, int chunkZ){
		String key = world.getUID().toString() + "-" + chunkX + "-" + chunkZ;
		return claimedGround.containsKey(key);
	}
	
	public static boolean isClaimed(World world, Kingdom exclude, int chunkX, int chunkZ){
		Kingdom kd = getClaim(world, chunkX, chunkZ);
		return kd != null && kd != exclude;
	}
	
	public static boolean isClaimed(World world, Kingdom exclude, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ){
		int x = minChunkX;
		while(x <= maxChunkX){
			int z = minChunkZ;
			while(z <= maxChunkZ){
				if(isClaimed(world, exclude, x, z))
					return true;
				++z;
			}
			++x;
		}
		return false;
	}
	
	public static Kingdom getClaim(World world, int chunkX, int chunkZ){
		String key = world.getUID().toString() + "-" + chunkX + "-" + chunkZ;
		String name = claimedGround.get(key);
		if(name != null)
			return getKingdom(name);
		return null;
	}
	
	public static Kingdom getClaim(Chunk chunk){
		return getClaim(chunk.getWorld(), chunk.getX(), chunk.getZ());
	}
	
	public static int chunksPerPlayer(){
		return chunksPerPlayer;
	}
	
	public static int restHours(){
		return restHours;
	}
	
	public static int warDurationInHours(){
		return warDurationInHours;
	}
	
	public static int minHoursBeforeWar(){
		return minHoursBeforeWar;
	}
	
	public static int[] minAttackTime(){
		return minAttackTime;
	}
	
	public static int[] maxAttackTime(){
		return maxAttackTime;
	}

	public static boolean canEditOtherKingdoms(){
		return canEditOtherKingdoms;
	}
	
	public static boolean canGolemsHelp(){
		return canGolemsHelp;
	}
	
	public static boolean canEveryoneCreateKingdom(){
		return canEveryoneCreateKingdom;
	}
	
	public static void setGolem(String golemUUID, Kingdom owner){
		golems.put(golemUUID, owner);
	}
	
	public static Kingdom getGolemOwner(String uuid){
		return golems.get(uuid);
	}
	
	public static Kingdom getGolemOwner(Entity entity){
		return (entity != null && entity.getUniqueId() != null)? getGolemOwner(entity.getUniqueId().toString()) : null;
	}
	
	public static String registerWar(KingdomWar war){
		int i = 0;
		while(i < wars.size()){
			if(wars.get(i).isTooSoon(war))
				return ChatColor.RED + "There is too less time between this war and another war you declared against that kingdom, the minimum rest time is " + restHours() + " hours.";
			++i;
		}
		war.getWarStarter().broadcast(getWarMessage(war));
		war.getWarOpponent().broadcast(getWarMessage(war));
		wars.add(war);
		war.getWarStarter().wars.add(war);
		war.getWarOpponent().wars.add(war);
		return ChatColor.GREEN + "You have succesfully declared war to " + war.getWarOpponent().getColoredName() + ChatColor.GREEN + ", the war starts at " + war.getStartTimeString();
	}
	
	public static String registerAttack(KingdomAttack attack){
		Kingdom att = attack.getAttacker();
		Kingdom def = attack.getDefender();
		attack.cutTime();
		if(attack.getStart().after(attack.getEnd()))
			return ChatColor.RED + "The time of the attack is completely outside the allowed times, use /kingdom options    and look at the minAttackTime and maxAttackTime.";
		int j = 0;
		while(j < attacks.size()){
			KingdomAttack a = attacks.get(j);
			if(a.getAttacker() == att && a.getDefender() == def){
				if(attack.isEnabled(a.getStart()) || attack.isEnabled(a.getEnd()))
					return ChatColor.RED + "You can't plan an attack during another attack on the same kingdom.";
				if(a.getStart().after(attack.getEnd())){
					long difference = a.getStart().getTimeInMillis() - attack.getEnd().getTimeInMillis();
					if(difference < 3600000)
						return ChatColor.RED + "You can't plan an attack within 1 hour of another attack on the same kingdom.";
				}
				if(a.getEnd().before(attack.getStart())){
					long difference = attack.getStart().getTimeInMillis() - a.getEnd().getTimeInMillis();
					if(difference < 3600000)
						return ChatColor.RED + "You can't plan an attack within 1 hour of another attack on the same kingdom.";
				}
			}
			++j;
		}
		int i = 0;
		while(i < wars.size()){
			KingdomWar war = wars.get(i);
			if(war.contains(att) && war.contains(def)){
				if(war.isEnabled(attack.getStart()) || war.isEnabled(attack.getEnd())){
					attack.cutTime(war);
					attack.cutTime();
					if(attack.getStart().after(attack.getEnd()))
						return ChatColor.RED + "The time of the attack is completely outside the times of the coming war or outside the allowed times.";
					attacks.add(attack);
					att.attacks.add(attack);
					def.attacks.add(attack);
					String message = getAttackMessage(att, def, attack);
					att.broadcast(message);
					def.broadcast(message);
					return ChatColor.GREEN + "You have registered your attack against " + attack.getDefender().getColoredName();
				}
			}
			++i;
		}
		return ChatColor.RED + "You can only attack other kingdoms during a war.";
	}
	
	public static void unregisterWar(KingdomWar war, boolean cancel){
		wars.remove(war);
		Kingdom starter = war.getWarStarter();
		Kingdom opponent = war.getWarOpponent();
		if(starter != null)
			starter.wars.remove(war);
		if(opponent != null)
			opponent.wars.remove(war);
		if(cancel){
			if(starter != null)
				starter.broadcast(ChatColor.DARK_GREEN + "The war of " + war.getWarStarter().getColoredName() + " against " + war.getWarOpponent().getColoredName() + " at " + war.getStartTimeString() + " has been cancelled.");
			if(opponent != null)
				opponent.broadcast(ChatColor.DARK_GREEN + "The war of " + war.getWarStarter().getColoredName() + " against " + war.getWarOpponent().getColoredName() + " at " + war.getStartTimeString() + " has been cancelled.");
		}
		else {
			if(starter != null)
				starter.broadcast(ChatColor.DARK_GREEN + "The war of " + war.getWarStarter().getColoredName() + " against " + war.getWarOpponent().getColoredName() + " at " + war.getStartTimeString() + " has expired.");
			if(opponent != null)
				opponent.broadcast(ChatColor.DARK_GREEN + "The war of " + war.getWarStarter().getColoredName() + " against " + war.getWarOpponent().getColoredName() + " at " + war.getStartTimeString() + " has expired.");
		}
	}
	
	public static void unregisterAttack(KingdomAttack attack, boolean cancel){
		attacks.remove(attack);
		Kingdom att = attack.getAttacker();
		Kingdom def = attack.getDefender();
		att.attacks.remove(attack);
		def.attacks.remove(attack);
		String message;
		if(cancel)
			message = att.getColoredName() + ChatColor.DARK_GREEN + " has cancelled the attack against " + def.getColoredName() + ChatColor.DARK_GREEN + " at " + attack.getStartTimeString();
		else
			message = ChatColor.DARK_GREEN + "The attack of " + att.getColoredName() + ChatColor.DARK_GREEN + " against " + def.getColoredName() + " has expired.";
		att.broadcast(message);
		def.broadcast(message);
	}
	
	public static void removeExpiredWars(){
		int i = 0;
		while(i < wars.size()){
			if(wars.get(i).isExpired())
				wars.get(i).expire();
			else
				++i;
		}
	}
	
	public static void removeExpiredAttacks(){
		int i = 0;
		while(i < attacks.size()){
			if(attacks.get(i).isExpired())
				attacks.remove(i);
			else
				++i;
		}
	}
	
	private static void addWar(KingdomWar war){
		if(war != null){
			wars.add(war);
			war.getWarStarter().wars.add(war);
			war.getWarOpponent().wars.add(war);
		}
	}
	
	private static void addAttack(KingdomAttack attack){
		if(attack != null){
			attacks.add(attack);
			attack.getAttacker().attacks.add(attack);
			attack.getDefender().attacks.add(attack);
		}
	}
	
	public static String getAttackMessage(Kingdom att, Kingdom def, KingdomAttack attack){
		if(attack.isEnabled())
			return att.getColoredName() + ChatColor.DARK_RED + " is attacking " + def.getColoredName() + ChatColor.DARK_RED + "! The attack will expire at " + attack.getEndTimeString() + "! Until the attack is expired, " + att.getColoredName() + ChatColor.DARK_RED + " will be able to and allowed to attack or destroy anything or anyone in " + def.getColoredName();
		else if(attack.getStart().after(Calendar.getInstance()))
			return att.getColoredName() + ChatColor.DARK_RED + " is going to attack " + def.getColoredName() + ChatColor.DARK_RED + "! The attack begins at " + attack.getStartTimeString() + " and the attack ends at " + attack.getEndTimeString() + ". During that time, " + att.getColoredName() + ChatColor.DARK_RED + " will be able to and allowed to attack and destroy anyone or anything in " + def.getColoredName();
		else {
			unregisterAttack(attack, false);
			return ChatColor.BLACK + "This attack is expired, you shouldn't get this message!";
		}
	}
	
	public static String getWarMessage(KingdomWar war){
		Kingdom requester = war.cancelRequester();
		Kingdom sta = war.getWarStarter();
		Kingdom opp = war.getWarOpponent();
		if(war.isEnabled())
			return sta.getColoredName() + ChatColor.DARK_RED + " is in war with " + opp.getColoredName() + ChatColor.DARK_RED + ", the war ends at " + war.getEndTimeString() + "." + (requester != null ? " " + requester.getColoredName() + ChatColor.DARK_RED + " has requested to cancel this war. The other kingdom can use /war cancel [other kingdom] [year] [month] [day] to cancel the war." : "");
		else if(war.getStart().after(Calendar.getInstance()))
			return sta.getColoredName() + ChatColor.DARK_RED + " has declared war against " + opp.getColoredName() + ChatColor.DARK_RED + "! The war begins at " + war.getStartTimeString() + " and lasts " + warDurationInHours() + " hours." + (requester != null ? " " + requester.getColoredName() + ChatColor.DARK_RED + " has requested to cancel this war. The other kingdom can use /war cancel [other kingdom] [year] [month] [day] to cancel the war." : "");
		else {
			unregisterWar(war, false);
			return ChatColor.BLACK + "This war is expired, you shouldn't get this message!";
		}
	}

	public static String getAttackMessage(KingdomAttack attack) {
		return getAttackMessage(attack.getAttacker(), attack.getDefender(), attack);
	}
	
	public static GolemTasks getGolemTasks(Golem golem){
		return getGolemTasks(golem.getUniqueId());
	}
	
	public static GolemTasks getGolemTasks(UUID golemUUID){
		return getGolemTasks(golemUUID.toString());
	}
	
	public static GolemTasks getGolemTasks(String golemUUID){
		if(!golemTasks.containsKey(golemUUID)){
			GolemTasks tasks = new GolemTasks(EntityUtils.getGolemById(golemUUID), getDefaultTasks(golemUUID));
			KingdomEventHandler.kingdomSwitchListeners.add(tasks);
			KingdomEventHandler.attackListeners.add(tasks);
			golemTasks.put(golemUUID, tasks);
		}
		return golemTasks.get(golemUUID);
	}
	
	public static void addGolemTask(Golem golem, GolemState state, GolemTask task){
		getGolemTasks(golem).addTask(task);
	}
	
	public static GolemTask[] getDefaultTasks(Golem golem){
		return getDefaultTasks(golem.getUniqueId().toString());
	}
	
	public static GolemTask[] getDefaultTasks(String golemID){
		Kingdom kd = getGolemOwner(golemID);
		if(kd != null)
			return new GolemTask[]{new GolemKingdomTask(GolemPriority.NONE, GolemState.ATTACK_MONSTERS, kd), new GolemKingdomTask(GolemPriority.LOW, GolemState.ATTACK_PLAYERS, kd), new GolemKingdomTask(GolemPriority.LOW, GolemState.PROTECTING, kd)};
		return new GolemTask[]{new GolemFreeTask()};
	}
}
