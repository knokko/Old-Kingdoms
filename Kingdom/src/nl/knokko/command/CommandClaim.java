package nl.knokko.command;

import java.util.ArrayList;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandClaim implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		if(sender instanceof Player){
			Player player = (Player) sender;
			Kingdom kd = KingdomData.getKingdom(player);
			if(kd != null){
				if(kd.canClaimGround(sender)){
					addClaimTool(player);
				}
				else
					sender.sendMessage(ChatColor.RED + "You are not allowed to claim ground.");
			}
			else
				sender.sendMessage(ChatColor.RED + "You need a kingdom to claim ground for.");
		}
		else
			sender.sendMessage(ChatColor.RED + "Only players can claim ground for their kingdom.");
		return false;
	}
	
	public static void addClaimTool(Player player){
		player.getInventory().addItem(getClaimTool());
	}
	
	public static ItemStack getClaimTool(){
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Right click with this hoe on a block to ");
		lore.add("claim the chunk which the block belongs to.");
		lore.add("You need to be in a kingdom to claim ground");
		lore.add("for it and you need the right permissions.");
		lore.add("When you drop this item, it will disappear.");
		lore.add("Left click on the air to swap mode.");
		lore.add("Current Mode: Claim");
		ItemStack stack = new ItemStack(Material.GOLD_HOE);
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_HOE);
		meta.setDisplayName("Claim Tool");
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static void addReleaseTool(Player player){
		player.getInventory().addItem(getReleaseTool());
	}
	
	public static ItemStack getReleaseTool(){
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Right click with this hoe on a block to ");
		lore.add("claim the chunk which the block belongs to.");
		lore.add("You need to be in a kingdom to claim ground");
		lore.add("for it and you need the right permissions.");
		lore.add("When you drop this item, it will disappear.");
		lore.add("Left click on the air to swap mode.");
		lore.add("Current Mode: Release");
		ItemStack stack = new ItemStack(Material.GOLD_HOE);
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_HOE);
		meta.setDisplayName("Claim Tool");
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}
}
