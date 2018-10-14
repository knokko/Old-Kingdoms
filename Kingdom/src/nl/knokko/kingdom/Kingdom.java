package nl.knokko.kingdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.event.KingdomAttack;
import nl.knokko.kingdom.event.KingdomWar;
import nl.knokko.main.KingdomEventHandler;
import nl.knokko.util.EntityUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Tameable;
import org.bukkit.util.Vector;

public class Kingdom {
	
	private List<String> players;
	private List<String> golems;
	private List<String> snowmans;
	public List<String> invites;
	public List<KingdomWar> wars;
	public List<KingdomAttack> attacks;
	
	private HashMap<String, Rank> ranks = new HashMap<String, Rank>();
	private HashMap<String, Title> titles = new HashMap<String, Title>();
	
	private Location spawn;
	
	private String king;
	private String name;
	
	private ChatColor color;
	
	private int claimedChunks;
	private boolean[] letGolemsHelp = new boolean[]{true, true};
	
	public Kingdom(String name){
		this.name = name;
		this.wars = new ArrayList<KingdomWar>();
		this.attacks = new ArrayList<KingdomAttack>();
		this.spawn = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
	}

	public Kingdom(String name, Player king) {
		this.name = name;
		this.king = king.getUniqueId().toString();
		this.players = new ArrayList<String>();
		this.ranks.put(king.getUniqueId().toString(), Rank.KING);
		addPlayer(king);
		this.invites = new ArrayList<String>();
		this.golems = new ArrayList<String>();
		this.snowmans = new ArrayList<String>();
		this.wars = new ArrayList<KingdomWar>();
		this.attacks = new ArrayList<KingdomAttack>();
		this.spawn = king.getLocation();
	}
	
	public ArrayList<Player> getOnlinePlayers(World world){
		ArrayList<Player> members = new ArrayList<Player>();
		List<Player> onlinePlayers = world.getPlayers();
		int i = 0;
		while(i < onlinePlayers.size()){
			if(players.contains(onlinePlayers.get(i).getUniqueId().toString()))
				members.add(onlinePlayers.get(i));
			++i;
		}
		return members;
	}
	
	public ArrayList<Player> getOnlinePlayers(Server server){
		ArrayList<Player> members = new ArrayList<Player>();
		Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
		int i = 0;
		while(i < onlinePlayers.size()){
			if(players.contains(((Player) onlinePlayers.toArray()[i]).getUniqueId().toString()))
				members.add((Player) onlinePlayers.toArray()[i]);
			++i;
		}
		return members;
	}
	
	public OfflinePlayer getPlayerByName(String name){
		ArrayList<OfflinePlayer> offPlayers = getPlayers(Bukkit.getServer());
		int i = 0;
		while(i < offPlayers.size()){
			OfflinePlayer player = offPlayers.get(i);
			if(player != null && player.getName().equals(name))
				return player;
			++i;
		}
		return null;
	}
	
	public ArrayList<OfflinePlayer> getPlayers(Server server){
		ArrayList<OfflinePlayer> members = new ArrayList<OfflinePlayer>();
		int i = 0;
		while(i < players.size()){
			OfflinePlayer off = server.getOfflinePlayer(UUID.fromString(players.get(i)));
			if(off != null)
				members.add(off);
			else {
				Player on = server.getPlayer(UUID.fromString(players.get(i)));
				if(on != null)
					members.add(on);
			}
			++i;
		}
		return members;
	}
	
	public Player getKing(World world){
		List<Player> onlinePlayers = world.getPlayers();
		int i = 0;
		while(i < onlinePlayers.size()){
			if(onlinePlayers.get(i).getUniqueId().toString().equals(king))
				return onlinePlayers.get(i);
			++i;
		}
		return null;
	}
	
	public OfflinePlayer getKing(Server server){
		return server.getOfflinePlayer(UUID.fromString(king));
	}
	
	public String getName(){
		return name;
	}
	
	public String getColoredName(){
		return color != null ? color + getName() : getName();
	}
	
	public boolean isInKingdom(String uuid){
		return players.contains(uuid);
	}
	
	public boolean isInKingdom(UUID uuid){
		return isInKingdom(uuid.toString());
	}
	
	public boolean isInKingdom(Player player){
		return isInKingdom(player.getUniqueId());
	}
	
	public boolean isInvited(Player player){
		return invites.contains(player.getName());
	}
	
	public int getAmountOfPlayers(){
		return players.size();
	}
	
	public void addPlayer(Player player){
		addPlayer(player.getUniqueId().toString());
		KingdomEventHandler.updatePlayerName(player);
		broadcast(ChatColor.GREEN + player.getName() + " has joined your kingdom.");
	}
	
	private void addPlayer(String uuid){
		players.add(uuid);
		KingdomData.setKingdom(uuid, this);
	}
	
	public void removePlayer(Player player){
		removePlayer(player.getUniqueId().toString());
		KingdomEventHandler.updatePlayerName(player);
	}
	
	private void removePlayer(String uuid){
		players.remove(uuid);
		KingdomData.setKingdom(uuid, null);
	}
	
	public boolean canInvite(CommandSender sender){
		return sender instanceof Player && getRank((Player)sender) != null && getRank((Player)sender).canInvite;
	}
	
	public boolean canChangeColor(CommandSender sender){
		return sender instanceof Player && getRank((Player)sender) != null && getRank((Player)sender).canChangeColor;
	}
	
	public boolean canClaimGround(CommandSender sender){
		return sender instanceof Player && getRank((Player)sender) != null && getRank((Player)sender).canClaimGround;
	}
	
	public boolean canKickPlayer(CommandSender sender){
		return sender instanceof Player && getRank((Player)sender) != null && getRank((Player)sender).canKick;
	}
	
	public boolean canPromote(CommandSender sender){
		return sender instanceof Player && getRank((Player)sender) != null && getRank((Player)sender).canPromote;
	}
	
	public boolean canDeclareWar(Player player){
		return getRank(player) != null && getRank(player).canDeclareWar;
	}
	
	public boolean canStartAttack(Player player){
		return getRank(player) != null && getRank(player).canStartAttack;
	}
	
	public boolean isKing(String uuid){
		return king.equals(uuid);
	}
	
	public boolean isKing(Player player){
		return isKing(player.getUniqueId().toString());
	}
	
	public void invitePlayer(String name, CommandSender sender){
		if(!canInvite(sender))
			sender.sendMessage("You are not allowed to invite players to this kingdom.");
		if(invites.contains(name))
			sender.sendMessage("That player is already invited to this kingdom.");
		else {
			invites.add(name);
			sender.sendMessage("You invited player " + name + " succesfully.");
			broadcast(ChatColor.YELLOW + sender.getName() + " has invited player " + name + " to your kingdom.");
		}
	}
	
	public String kickPlayer(CommandSender kicker, String target){
		if(!canKickPlayer(kicker))
			return ChatColor.RED + "You are not allowed to kick players from this kingdom.";
		int i = 0;
		while(i < players.size()){
			OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(players.get(i)));
			if(player != null && player.getName().equals(target)){
				Rank rank = ranks.get(player.getUniqueId().toString());
				if(rank != Rank.KING){
					removePlayer(player.getUniqueId().toString());
					invites.remove(player.getName());
					if(player instanceof Player){
						KingdomEventHandler.updatePlayerName((Player) player);
						((Player) player).sendMessage(ChatColor.DARK_RED + "You have been kicked from your kingdom by " + kicker.getName());
					}
					broadcast(ChatColor.YELLOW + target + " has been kicked by " + kicker.getName());
					return ChatColor.GREEN + "You kicked player " + target + " succesfully.";
				}
				else
					return ChatColor.RED  + "You can't kick the king.";
			}
			++i;
		}
		return ChatColor.RED + "The player " + target + " is no member of your kingdom.";
	}
	
	public ChatColor getColor() {
		return color;
	}

	public void setColor(ChatColor color) {
		this.color = color;
		broadcast(ChatColor.YELLOW + "The color of your kingdom has been changed to " + color + color.name().toLowerCase());
	}
	
	public int getClaimedChunks(){
		return claimedChunks;
	}
	
	public void increaseChunks(){
		claimedChunks++;
	}
	
	public void decreaseChunks(){
		claimedChunks--;
	}
	
	public Title getTitle(String uuid){
		return titles.get(uuid);
	}
	
	public Title getTitle(OfflinePlayer player){
		return getTitle(player.getUniqueId().toString());
	}
	
	public Rank getRank(String uuid){
		return ranks.get(uuid);
	}
	
	public Rank getRank(OfflinePlayer player){
		return getRank(player.getUniqueId().toString());
	}
	
	public void setTitle(OfflinePlayer player, Title title){
		titles.put(player.getUniqueId().toString(), title);
		if(player instanceof Player)
			KingdomEventHandler.updatePlayerName((Player) player);
		broadcast(ChatColor.YELLOW + "The title of player " + player.getName() + " has been set to " + title.getColoredName());
	}
	
	public void setRank(OfflinePlayer player, Rank rank){
		ranks.put(player.getUniqueId().toString(), rank);
		if(rank == Rank.KING)
			king = player.getUniqueId().toString();
		if(player instanceof Player)
			KingdomEventHandler.updatePlayerName((Player) player);
		broadcast(ChatColor.YELLOW + "The rank of player " + player.getName() + " has been set to " + (rank != null ? rank.name().toLowerCase() : "none"));
	}
	
	public ArrayList<IronGolem> getIronGolems(World world){
		ArrayList<IronGolem> ironGolems = new ArrayList<IronGolem>();
		int i = 0;
		while(i < golems.size()){
			IronGolem golem = EntityUtils.getIronGolemById(golems.get(i));
			if(golem != null && golem.getWorld() == world)
				ironGolems.add(golem);
			++i;
		}
		return ironGolems;
	}
	
	public ArrayList<IronGolem> getIronGolems(){
		ArrayList<IronGolem> ironGolems = new ArrayList<IronGolem>();
		int i = 0;
		while(i < golems.size()){
			IronGolem golem = EntityUtils.getIronGolemById(golems.get(i));
			if(golem != null)
				ironGolems.add(golem);
			++i;
		}
		return ironGolems;
	}
	
	public ArrayList<Snowman> getSnowGolems(World world){
		ArrayList<Snowman> snowGolems = new ArrayList<Snowman>();
		int i = 0;
		while(i < snowmans.size()){
			Snowman golem = EntityUtils.getSnowGolemById(snowmans.get(i));
			if(golem != null && golem.getWorld() == world)
				snowGolems.add(golem);
			++i;
		}
		return snowGolems;
	}
	
	public ArrayList<Snowman> getSnowGolems(){
		ArrayList<Snowman> snowGolems = new ArrayList<Snowman>();
		int i = 0;
		while(i < snowmans.size()){
			Snowman golem = EntityUtils.getSnowGolemById(snowmans.get(i));
			if(golem != null)
				snowGolems.add(golem);
			++i;
		}
		return snowGolems;
	}
	
	public ArrayList<Golem> getGolems(World world){
		ArrayList<Golem> golems = new ArrayList<Golem>();
		golems.addAll(getIronGolems(world));
		golems.addAll(getSnowGolems(world));
		return golems;
	}
	
	public ArrayList<Golem> getGolems(){
		ArrayList<Golem> golems = new ArrayList<Golem>();
		golems.addAll(getIronGolems());
		golems.addAll(getSnowGolems());
		return golems;
	}
	
	public void addIronGolem(IronGolem golem){
		String id = golem.getUniqueId().toString();
		if(KingdomData.getGolemOwner(id) == null)
			KingdomData.setGolem(id, this);
		if(!golems.contains(id))
			golems.add(id);
	}
	
	public void addSnowGolem(Snowman golem){
		String id = golem.getUniqueId().toString();
		if(KingdomData.getGolemOwner(id) == null)
			KingdomData.setGolem(id, this);
		if(!snowmans.contains(id))
			snowmans.add(id);
	}
	
	public boolean belongsToKingdom(Entity entity){
		Player player = null;
		if(entity instanceof Player)
			player = (Player) entity;
		if(entity instanceof Tameable){
			AnimalTamer tamer = ((Tameable) entity).getOwner();
			if(tamer instanceof Player)
				player = (Player) tamer;
		}
		if(player != null)
			return KingdomData.getKingdom(player) == this;
		Kingdom kd = KingdomData.getGolemOwner(entity);
		if(kd == this)
			return true;
		return false;
	}
	
	public void broadcast(String text){
		ArrayList<Player> onlinePlayers = getOnlinePlayers(Bukkit.getServer());
		int i = 0;
		while(i < onlinePlayers.size()){
			onlinePlayers.get(i).sendMessage(text);
			++i;
		}
	}
	
	public boolean isInWar(Kingdom other){
		KingdomData.removeExpiredWars();
		int i = 0;
		while(i < wars.size()){
			KingdomWar war = wars.get(i);
			if(war.contains(other) && war.isEnabled())
				return true;
			++i;
		}
		return false;
	}
	
	public boolean isInWar(){
		KingdomData.removeExpiredWars();
		int i = 0;
		while(i < wars.size()){
			if(wars.get(i).isEnabled())
				return true;
			++i;
		}
		return false;
	}
	
	public boolean isAttacking(Kingdom target){
		KingdomData.removeExpiredAttacks();
		if(!isInWar(target))
			return false;
		int i = 0;
		while(i < attacks.size()){
			if(attacks.get(i).getDefender() == target && attacks.get(i).isEnabled())
				return true;
			++i;
		}
		return false;
	}
	
	public boolean isUnderAttack(Kingdom attacker){
		if(attacker == null)
			return false;
		if(!isInWar(attacker))
			return false;
		KingdomData.removeExpiredAttacks();
		int i = 0;
		while(i < attacks.size()){
			if(attacks.get(i).getAttacker() == attacker && attacks.get(i).isEnabled())
				return true;
			++i;
		}
		return false;
	}
	
	public boolean isUnderAttack(){
		if(!isInWar())
			return false;
		KingdomData.removeExpiredAttacks();
		int i = 0;
		while(i < attacks.size()){
			if(attacks.get(i).getDefender() == this && attacks.get(i).isEnabled())
				return true;
			++i;
		}
		return false;
	}
	
	public boolean canIronGolemsHelp(){
		return letGolemsHelp[0];
	}
	
	public boolean canSnowGolemsHelp(){
		return letGolemsHelp[1];
	}
	
	public void letIronGolemsHelp(boolean letHelp){
		letGolemsHelp[0] = letHelp;
	}
	
	public void letSnowGolemsHelp(boolean letHelp){
		letGolemsHelp[1] = letHelp;
	}
	
	public Location getSpawn(){
		return spawn != null ? spawn : Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
	}
	
	public void setSpawnPoint(Location newSpawn){
		spawn = newSpawn.clone();
	}

	public void save(YamlConfiguration config){
		ConfigurationSection section = config.createSection("kingdom " + name);
		section.set("king", king);
		section.set("players", players);
		section.set("invites", invites);
		section.set("iron golems", golems);
		section.set("snow golems", snowmans);
		section.set("iron golems help", canIronGolemsHelp());
		section.set("snow golems help", canSnowGolemsHelp());
		if(spawn != null){
			section.set("spawnpoint", spawn.toVector());
			section.set("spawnworld", spawn.getWorld().getUID().toString());
		}
		if(color != null)
			section.set("color", color.name());
		Iterator<Entry<String, Rank>> iterator = ranks.entrySet().iterator();
		ConfigurationSection rankSection = section.createSection("ranks");
		while(iterator.hasNext()){
			Entry<String,Rank> entry = iterator.next();
			rankSection.set(entry.getKey(), entry.getValue().toString());
		}
		Iterator<Entry<String, Title>> it = titles.entrySet().iterator();
		ConfigurationSection titleSection = section.createSection("titles");
		while(it.hasNext()){
			Entry<String, Title> entry = it.next();
			titleSection.set(entry.getKey(), entry.getValue().save());
		}
	}
	
	public void load(YamlConfiguration config){
		ConfigurationSection section = config.getConfigurationSection("kingdom " + name);
		if(section != null){
			king = section.getString("king");
			players = section.getStringList("players");
			invites = section.getStringList("invites");
			golems = section.getStringList("iron golems");
			snowmans = section.getStringList("snow golems");
			try {
				Vector spawnVector = section.getVector("spawnpoint");
				String worldID = section.getString("spawnworld");
				World world = Bukkit.getWorld(UUID.fromString(worldID));
				spawn = spawnVector.toLocation(world);
			} catch(Exception ex){
				Bukkit.getLogger().warning("Can't load the spawn of kingdom " + name);
				Bukkit.getLogger().throwing("nl.knokko.kingdom.Kingdom", "load(YamlConfiguration)", ex);
			}
			if(section.contains("iron golems help"))
				letGolemsHelp[0] = section.getBoolean("iron golems help");
			if(section.contains("snow golems help"))
				letGolemsHelp[1] = section.getBoolean("snow golems help");
			int i = 0;
			while(i < golems.size()){
				KingdomData.setGolem(golems.get(i), this);
				++i;
			}
			i = 0;
			while(i < snowmans.size()){
				KingdomData.setGolem(snowmans.get(i), this);
				++i;
			}
			String col = section.getString("color");
			if(col != null){
				try {
					color = ChatColor.valueOf(col);
				} catch(Exception ex){
					ex.printStackTrace();
					color = null;
				}
			}
			i = 0;
			while(i < players.size()){
				KingdomData.setKingdom(players.get(i), this);
				++i;
			}
			ConfigurationSection rankSection = section.getConfigurationSection("ranks");
			if(rankSection != null){
				Iterator<Entry<String,Object>> iterator = rankSection.getValues(false).entrySet().iterator();
				while(iterator.hasNext()){
					Entry<String,Object> entry = iterator.next();
					ranks.put(entry.getKey(), Rank.fromString((String) entry.getValue()));
				}
			}
			ConfigurationSection titleSection = section.getConfigurationSection("titles");
			if(titleSection != null){
				Iterator<Entry<String,Object>> iterator = titleSection.getValues(false).entrySet().iterator();
				while(iterator.hasNext()){
					Entry<String,Object> entry = iterator.next();
					titles.put(entry.getKey(), Title.load((String) entry.getValue()));
				}
			}
		}
		else
			System.out.println("Can't load section 'kingdom " + name + "'.");
	}
}
