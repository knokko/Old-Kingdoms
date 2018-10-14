package nl.knokko.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nl.knokko.command.CommandClaim;
import nl.knokko.command.CommandGolems;
import nl.knokko.data.KingdomData;
import nl.knokko.golems.GolemAction;
import nl.knokko.golems.GolemPriority;
import nl.knokko.golems.GolemTasks;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.kingdom.Rank;
import nl.knokko.kingdom.Title;
import nl.knokko.util.EntityUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Golem;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KingdomEventHandler implements Listener {
	
	public static ArrayList<IEntityAttackListener> attackListeners = new ArrayList<IEntityAttackListener>();
	public static ArrayList<IKingdomSwitchListener> kingdomSwitchListeners = new ArrayList<IKingdomSwitchListener>();
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event){
		updatePlayerName(event.getPlayer());
	}
	
	@EventHandler
	public void onEntityAttack(EntityDamageByEntityEvent event){
		Entity victim = event.getEntity();
		Kingdom akd = KingdomData.getEntityKingdom(event.getDamager());
		Kingdom dkd = KingdomData.getEntityKingdom(victim);
		if(akd != null && dkd == akd){
			event.getDamager().sendMessage(ChatColor.RED + "You can't hurt members of your own kingdom");
			event.setCancelled(true);
			return;
		}
		if(!canEntityEdit(victim.getLocation().getChunk(), event.getDamager()) && (!(victim instanceof Monster || victim instanceof Flying))){
			event.setCancelled(true);
			event.getDamager().sendMessage(ChatColor.RED + "You can't attack non-monsters in another kingdom unless you are attacking it.");
			return;
		}
		int i = 0;
		while(i < attackListeners.size()){
			attackListeners.get(i).onEntityAttack(event);
			++i;
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		if(block == null){
			if(event.getAction() == Action.LEFT_CLICK_AIR){
				ItemStack stack = player.getInventory().getItemInMainHand();
				if(stack != null && stack.getType() == Material.GOLD_HOE && stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().equals("Claim Tool")){
					ItemMeta meta = stack.getItemMeta();
					List<String> lore = meta.getLore();
					if(lore != null && lore.size() >= 7){
						if(lore.get(6).equals("Current Mode: Claim"))
							player.getInventory().setItemInMainHand(CommandClaim.getReleaseTool());
						else
							player.getInventory().setItemInMainHand(CommandClaim.getClaimTool());
						return;
					}
					lore = new ArrayList<String>();
					lore.add("Right click with this hoe on a block to ");
					lore.add("claim the chunk which the block belongs to.");
					lore.add("You need to be in a kingdom to claim ground");
					lore.add("for it and you need the right permissions.");
					lore.add("When you drop this item, it will disappear.");
					lore.add("Left click on the air to swap mode.");
					lore.add("Current Mode: Claim");
					meta.setLore(lore);
					stack.setItemMeta(meta);
				}
				if(stack != null && stack.getType() == Material.IRON_HOE && stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().equals("Golem Tool")){
					if(!KingdomData.canGolemsHelp()){
						player.sendMessage(CommandGolems.GOLEMSDISABLED);
						return;
					}
					int range = CommandGolems.getToolRange(stack.getItemMeta());
					GolemPriority priority = CommandGolems.getToolPriority(stack.getItemMeta());
					ArrayList<Golem> golems = EntityUtils.getAvailableGolems(player, range);
					Entity entity = EntityUtils.getEntityInSight(player);
					if(!(entity instanceof LivingEntity)){
						player.sendMessage(ChatColor.RED + "Can't find the entity you are looking at.");
						return;
					}
					int i = 0;
					while(i < golems.size()){
						KingdomData.getGolemTasks(golems.get(i)).addAction(new GolemAction((LivingEntity) entity), priority);
						++i;
					}
					player.sendMessage(ChatColor.GREEN + "" + i + " golems are commanded to attack " + entity.getCustomName());
				}
			}
			else if(event.getAction() == Action.RIGHT_CLICK_AIR){
				ItemStack stack = player.getInventory().getItemInMainHand();
				if(stack != null && stack.getType() == Material.IRON_HOE && stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().equals("Golem Tool")){
					if(!KingdomData.canGolemsHelp()){
						player.sendMessage(CommandGolems.GOLEMSDISABLED);
						return;
					}
					int range = CommandGolems.getToolRange(stack.getItemMeta());
					GolemPriority priority = CommandGolems.getToolPriority(stack.getItemMeta());
					ArrayList<Golem> golems = EntityUtils.getAvailableGolems(player, range);
					if(golems != null){
						HashSet<Material> ignore = new HashSet<Material>();
						ignore.add(Material.AIR);
						ignore.add(Material.WATER);
						ignore.add(Material.LAVA);
						List<Block> blocks = player.getLineOfSight(new HashSet<Material>(), 200);
						Block target = null;
						int i = 0;
						while(i < blocks.size() && target == null){
							Block b = blocks.get(i);
							target = b;
							++i;
						}
						if(target == null){
							player.sendMessage(ChatColor.RED + "Can't find the block you are looking at.");
							return;
						}
						i = 0;
						while(i < golems.size()){
							KingdomData.getGolemTasks(golems.get(i)).addAction(new GolemAction(target.getLocation().add(0, 1, 0)), priority);
							++i;
						}
						if(i > 0)
							player.sendMessage(ChatColor.GREEN + "" + i + " golems are commanded to move.");
						else
							player.sendMessage(ChatColor.RED + "There are no available golems in your range.");
					}
				}
			}
			return;
		}
		if(!canPlayerEdit(player, block.getChunk())){
			player.sendMessage(ChatColor.RED + "You can't interact with blocks in another kingdom unless you are attacking it.");
			event.setCancelled(true);
			return;
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND){
			ItemStack stack = player.getInventory().getItemInMainHand();
			if(stack != null && stack.getType() == Material.GOLD_HOE && stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().equals("Claim Tool")){
				List<String> lore = stack.getItemMeta().getLore();
				if(lore == null)
					return;
				byte claim = 0;
				if(lore.size() >= 7 && lore.get(6).equals("Current Mode: Claim"))
					claim = 1;
				if(lore.size() >= 7 && lore.get(6).equals("Current Mode: Release"))
					claim = -1;
				if(claim == 0)
					return;
				event.setCancelled(true);
				Kingdom kd = KingdomData.getKingdom(player);
				Chunk chunk = block.getChunk();
				if(kd != null && kd.canClaimGround(player)){
					if(claim == 1)
						player.sendMessage(KingdomData.claimGround(kd, chunk.getX(), chunk.getZ(), chunk.getWorld()));
					if(claim == -1)
						player.sendMessage(KingdomData.releaseGround(kd, chunk.getX(), chunk.getZ(), chunk.getWorld()));
				}
				else if(kd != null)
					player.sendMessage(ChatColor.RED + "You do not have permissions to claim ground for your kingdom.");
				else if(kd == null)
					player.sendMessage(ChatColor.RED + "You need a kingdom to claim ground for.");
			}
		}
	}
	
	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent event){
		ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
		if(stack != null && stack.getType() == Material.IRON_HOE && stack.hasItemMeta() && stack.getItemMeta().getDisplayName().equals("Golem Tool")){
			if(!KingdomData.canGolemsHelp()){
				event.getPlayer().sendMessage(CommandGolems.GOLEMSDISABLED);
				return;
			}
			int range = CommandGolems.getToolRange(stack.getItemMeta());
			GolemPriority priority = CommandGolems.getToolPriority(stack.getItemMeta());
			ArrayList<Golem> golems = EntityUtils.getAvailableGolems(event.getPlayer(), range);
			if(golems != null){
				int i = 0;
				while(i < golems.size()){
					KingdomData.getGolemTasks(golems.get(i)).addAction(new GolemAction(event.getRightClicked().getLocation()), priority);
					++i;
				}
				if(i > 0)
					event.getPlayer().sendMessage(ChatColor.GREEN + "" + i + " golems are commanded to move.");
				else
					event.getPlayer().sendMessage(ChatColor.RED + "There are no available golems in this range.");
			}
			event.setCancelled(true);
		}
		if(!canPlayerEdit(event.getPlayer(), event.getRightClicked().getLocation().getChunk())){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You can't interact with entities in another kingdom unless you are attacking it.");
		}
	}
	
	@EventHandler
	public void onPotionSplash(PotionSplashEvent event){
		if(!canEntityEdit(event.getEntity().getLocation().getChunk(), event.getEntity())){
			((Player)event.getEntity().getShooter()).sendMessage(ChatColor.RED + "You can't use splash potions in another kingdom unless you are attacking it.");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		Chunk from = event.getFrom().getChunk();
		Chunk to = event.getTo().getChunk();
		if(from != to){
			Kingdom kdFrom = KingdomData.getClaim(from.getWorld(), from.getX(), from.getZ());
			Kingdom kdTo = KingdomData.getClaim(to.getWorld(), to.getX(), to.getZ());
			if(kdFrom != kdTo)
				onKingdomSwitch(kdFrom, kdTo, event.getPlayer());
		}
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event){
		if(event.getItemDrop() != null){
			ItemStack stack = event.getItemDrop().getItemStack();
			if(stack != null && stack.getType() == Material.GOLD_HOE && stack.getItemMeta().getDisplayName().equals("Claim Tool")){
				event.getItemDrop().remove();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat(AsyncPlayerChatEvent event){
		updatePlayerName(event.getPlayer());
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event){
		if(event.getEntity() instanceof IronGolem){
			IronGolem golem = (IronGolem) event.getEntity();
			Kingdom kd = KingdomData.getClaim(golem.getLocation().getChunk());
			if(kd != null && KingdomData.getGolemOwner(golem) == null){
				golem.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100);
				kd.addIronGolem(golem);
				if(KingdomData.canGolemsHelp())
					KingdomData.getGolemTasks(golem);
			}
		}
		if(event.getEntity() instanceof Snowman){
			Snowman golem = (Snowman) event.getEntity();
			Kingdom kd = KingdomData.getClaim(golem.getLocation().getChunk());
			if(kd != null){
				golem.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100);
				kd.addSnowGolem(golem);
				if(KingdomData.canGolemsHelp)
					KingdomData.getGolemTasks(golem);
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		Kingdom kd = KingdomData.getKingdom(player);
		player.sendMessage(ChatColor.GOLD + "You are in " + getKingdomString(KingdomData.getClaim(player.getLocation().getChunk())));
		if(kd != null){
			int i = 0;
			while(i < kd.wars.size()){
				String message = KingdomData.getWarMessage(kd.wars.get(i));
				if(!message.isEmpty()){
					player.sendMessage(message);
					++i;
				}
			}
			i = 0;
			while(i < kd.attacks.size()){
				String message = KingdomData.getAttackMessage(kd.attacks.get(i));
				if(!message.isEmpty()){
					player.sendMessage(message);
					++i;
				}
			}
		}
		else
			player.sendMessage(ChatColor.YELLOW + "You are not in a kingdom at the moment, use " + ChatColor.GREEN + "/kingdom help" + ChatColor.YELLOW + " for more info about kingdoms. You can use " + ChatColor.GREEN + "/kingdom invites " + ChatColor.YELLOW + "to see your current invites.");
	}
	
	@EventHandler
	public void onTargetSet(EntityTargetLivingEntityEvent event){
		if(event.getTarget() == null || event.getEntity() == null || event.getTarget().getUniqueId() == null || event.getEntity().getUniqueId() == null)
			return;
		Kingdom kda = KingdomData.getEntityKingdom(event.getEntity());
		Kingdom kdt = KingdomData.getEntityKingdom(event.getTarget());
		if(kda != null && kdt == kda)
			event.setCancelled(true);
		Chunk chunk = event.getTarget().getLocation().getChunk();
		if(kda != null && !canKingdomEdit(kda, chunk) && !(event.getTarget() instanceof Monster) && !(event.getTarget() instanceof Flying))
			event.setCancelled(true);
		if(event.getEntity() instanceof Golem && KingdomData.canGolemsHelp()){
			Golem golem = (Golem) event.getEntity();
			GolemAction action = KingdomData.getGolemTasks(golem).getCurrentAction();
			if(action != null && action.getTarget() != event.getTarget())
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockChange(EntityChangeBlockEvent event){
		if(event.getEntity() instanceof Golem && KingdomData.canGolemsHelp()){
			Golem golem = (Golem) event.getEntity();
			GolemTasks tasks = KingdomData.getGolemTasks(golem);
			tasks.verifyMovement(event.getBlock());
		}
	}
	
	public void onKingdomSwitch(Kingdom from, Kingdom to, Player player){
		player.sendMessage(ChatColor.YELLOW + "You left " + getKingdomString(from) + ChatColor.YELLOW + " and you entered " + getKingdomString(to));
		Kingdom kd = KingdomData.getKingdom(player);
		if(to != null && kd != null && to.isUnderAttack(kd)){
			player.sendMessage(ChatColor.DARK_RED + "You have entered enemy territory, during the attack, you can grief and kill other players.");
			ArrayList<Player> players = to.getOnlinePlayers(Bukkit.getServer());
			int i = 0;
			while(i < players.size()){
				players.get(i).sendMessage(ChatColor.DARK_RED + "Enemy player " + player.getName() + " has entered your kingdom, kill him before he griefs your kingdom!");
				++i;
			}
		}
		int i = 0;
		while(i < kingdomSwitchListeners.size()){
			kingdomSwitchListeners.get(i).onPlayerKingdomSwitch(player, from, to);
			++i;
		}
	}
	
	public static void updatePlayerName(Player player){
		Kingdom kd = KingdomData.getKingdom(player);
		if(kd != null){
			String color = kd.getColor() != null ? kd.getColor().toString() : "";
			Rank rank = kd.getRank(player);
			Title title = kd.getTitle(player);
			String name;
			if(title != null)
				name = "[" + color + kd.getName() + ChatColor.WHITE + "] [" + title.getColoredName() + ChatColor.WHITE + "] " + player.getName();
			else if(rank != null)
				name = "[" + color + kd.getName() + ChatColor.WHITE + "] [" + rank.color + rank.toString() + ChatColor.WHITE + "] " + player.getName();
			else
				name = "[" + color + kd.getName() + ChatColor.WHITE + "] " + player.getName();
			player.setDisplayName(name);
			player.setPlayerListName(name);
			player.setCustomName(name);
		}
		else {
			player.setDisplayName(player.getName());
			player.setPlayerListName(player.getName());
		}
	}
	
	public static String getKingdomString(Kingdom kd){
		return kd != null ? ("kingdom " + (kd.getColor() != null ? kd.getColor() : "") + kd.getName()) : "unclaimed territory";
	}
	
	public static boolean canPlayerEdit(Player player, Chunk chunk){
		Kingdom pkd = KingdomData.getKingdom(player);
		Kingdom kd = KingdomData.getClaim(chunk);
		return canKingdomEdit(pkd, kd);
	}
	
	public static boolean canKingdomEdit(Kingdom kd, Chunk chunk){
		Kingdom owner = KingdomData.getClaim(chunk);
		return canKingdomEdit(kd, owner);
	}
	
	public static boolean canKingdomEdit(Kingdom kd, Kingdom owner){
		return owner == null || kd == owner || owner.isUnderAttack(kd) || KingdomData.canEditOtherKingdoms();
	}
	
	public static boolean canEntityEdit(Chunk chunk, Entity entity){
		return canKingdomEdit(KingdomData.getEntityKingdom(entity), KingdomData.getClaim(chunk));
	}
}
