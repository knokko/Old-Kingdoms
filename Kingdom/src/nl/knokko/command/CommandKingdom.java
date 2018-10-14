package nl.knokko.command;

import java.util.ArrayList;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.kingdom.Rank;
import nl.knokko.kingdom.Title;
import nl.knokko.main.KingdomEventHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class CommandKingdom implements CommandExecutor {
	
	public static final String USEAGE = ChatColor.RED + "/kingdom help/join/invites/create/leave/invite/list/color/remove/kick/promote/demote/title/spawn/setspawn/options";

	public CommandKingdom() {}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		if(arguments.length == 0){
			sender.sendMessage(USEAGE);
			return false;
		}
		if(arguments[0].equals("create")){
			if(sender.isOp() || KingdomData.canEveryoneCreateKingdom()){
				if(arguments.length == 2){
					if(sender instanceof Player)
						KingdomData.createKingdom((Player) sender, arguments[1]);
					else
						sender.sendMessage(ChatColor.RED + "Only players can create kingdoms.");
				}
				else
					sender.sendMessage(ChatColor.RED + "You should use /kingdom create [kingdomname]");
			}
			else
				sender.sendMessage(ChatColor.RED + "Only operators can create kingdoms.");
		}
		else if(arguments[0].matches("list")){
			if(arguments.length == 1){
				if(sender instanceof Player)
					KingdomData.sendKingdoms((Player) sender);
				else
					sender.sendMessage(KingdomData.getKingdoms());
			}
			else if(arguments.length == 2){
				Kingdom kingdom = KingdomData.getKingdom(arguments[1]);
				if(kingdom == null){
					sender.sendMessage(ChatColor.RED + "The kingdom '" + arguments[1] + "' doesn't exist.");
				}
				if(sender instanceof Player){
					ArrayList<Player> players = kingdom.getOnlinePlayers(((Player) sender).getWorld());
					String reply = "players:[";
					int i = 0;
					while(i < players.size()){
						reply += players.get(i).getDisplayName() + ",";
						++i;
					}
					if(players.size() > 0)
						reply = reply.substring(0, reply.length() - 1);
					reply += "]";
					sender.sendMessage(reply);
				}
				else
					sender.sendMessage(ChatColor.RED + "Only players can use /kingdom list [kingdomname], you can only use /kingdom list.");
			}
		}
		else if(arguments[0].matches("leave")){
			if(sender instanceof Player){
				Kingdom kd = KingdomData.removePlayerFromKingdom((Player) sender);
				if(kd != null && !kd.isKing((Player)sender)){
					sender.sendMessage(ChatColor.GREEN + "You succesfully left kingdom " + kd.getName() + ".");
					ArrayList<Player> players = kd.getOnlinePlayers(Bukkit.getServer());
					int i = 0;
					while(i < players.size()){
						players.get(i).sendMessage(ChatColor.YELLOW + sender.getName() + " has left your kingdom.");
						++i;
					}
				}
				else if(kd == null)
					sender.sendMessage(ChatColor.RED + "You need a kingdom to leave.");
				else if(kd != null)
					sender.sendMessage(ChatColor.RED + "The king can't leave his kingdom, he can only remove it.");
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can leave their kingdom.");
		}
		else if(arguments[0].matches("invite")){
			if(sender instanceof Player){
				if(arguments.length != 2 && arguments.length != 3)
					sender.sendMessage(ChatColor.RED + "You should use /kingdom invite list/cancel/[playername]");
				else {
					Kingdom kd = KingdomData.getKingdom((Player) sender);
					if(kd != null){
						String playerName = arguments[1];
						if(playerName.equals("list")){
							sender.sendMessage(ChatColor.YELLOW + "Your kingdom has invited " + kd.invites);
						}
						else if(playerName.equals("cancel")){
								if(arguments.length == 3){
									if(kd.canInvite(sender)){
										if(kd.invites.remove(arguments[2]))
											sender.sendMessage(ChatColor.GREEN + "You cancelled the invite of player " + arguments[2]);
										else
											sender.sendMessage(ChatColor.RED + "That player was not invited to join your kingdom.");
									}
									else
										sender.sendMessage(ChatColor.RED + "You are not allowed to invite other players to this kingdom.");
								}
								else
									sender.sendMessage(ChatColor.RED + "You should use /kingdom invite cancel [playername]");
						}
						else {
							if(kd.canInvite(sender))
								kd.invitePlayer(playerName, sender);
							else
								sender.sendMessage(ChatColor.RED + "You are not allowed to invite other players to this kingdom.");
						}
					}
					else
						sender.sendMessage(ChatColor.RED + "You can only invite players if you are in a kingdom yourself.");
				}
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can invite other players to their kingdom.");
		}
		else if(arguments[0].matches("join")){
			if(sender instanceof Player){
				if(arguments.length != 2)
					sender.sendMessage(ChatColor.RED + "You should use /kingdom join [kingdomname]");
				else {
					String name = arguments[1];
					Kingdom kd = KingdomData.getKingdom(name);
					if(kd != null){
						if(kd.isInvited((Player) sender)){
							kd.addPlayer((Player) sender);
							sender.sendMessage(ChatColor.GREEN + "You succesfully joined kingdom " + name + ".");
						}
						else
							sender.sendMessage(ChatColor.RED + "You are not invited to this kingdom.");
					}
					else
						sender.sendMessage(ChatColor.RED + "The kingdom '" + name + "' doesn't exist.");
				}
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can join kingdoms.");
		}
		else if(arguments[0].matches("color")){
			if(sender instanceof Player){
				Kingdom kd = KingdomData.getKingdom((Player)sender);
				if(kd.canChangeColor(sender)){
					if(arguments.length == 2){
						try {
							ChatColor color = ChatColor.valueOf(arguments[1].toUpperCase());
							kd.setColor(color);
							sender.sendMessage(ChatColor.GREEN + "The color of your kingdom has been set to " + (color != null ? color.name().toLowerCase() : "none"+ "."));
						} catch(Exception ex){
							sender.sendMessage(ChatColor.RED + arguments[1] + " is no valid minecraft color.");
						}
					}
					else
						sender.sendMessage(ChatColor.RED + "You should use /kingdom color [color]");
				}
				else
					sender.sendMessage(ChatColor.RED + "You are not allowed to change the color of this kingdom.");
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can change the color of their kingdom.");
		}
		else if(arguments[0].matches("remove")){
			if(sender instanceof Player){
				if(arguments.length == 1){
					Kingdom kd = KingdomData.getKingdom((Player)sender);
					if(kd != null){
						if(kd.isKing((Player)sender)){
							if(KingdomData.requestedRemoveKingdom.contains(sender)){
								sender.sendMessage(ChatColor.RED + "You already requested to remove your kingdom, use /kingdom confirm to remove your kingdom.");
							}
							else {
								sender.sendMessage(ChatColor.YELLOW + "You are about to remove your kingdom, if you do this, there is no way to undo it.");
								sender.sendMessage(ChatColor.YELLOW + "If you remove your kingdom, all claimed ground will be released and all players will be able to join another kingdom.");
								sender.sendMessage(ChatColor.YELLOW + "Use /kingdom confirm if you are sure you want to remove your kingdom, use /kingdom cancel otherwise.");
								KingdomData.requestedRemoveKingdom.add((Player) sender);
							}
						}
						else
							sender.sendMessage(ChatColor.RED + "Only the king can remove his kingdom.");
					}
					else
						sender.sendMessage(ChatColor.RED + "You need a kingdom to remove.");
				}
				else if(arguments.length == 2){
					if(sender.isOp()){
						Kingdom kd = KingdomData.getKingdom(arguments[1]);
						if(kd != null){
							kd.broadcast(ChatColor.RED + "Your kingdom has been removed by staff.");
							KingdomData.removeKingdom(kd);
							sender.sendMessage(ChatColor.GREEN + "You succesfully removed kingdom " + arguments[1]);
						}
						else
							sender.sendMessage(ChatColor.RED + "There is no kingdom with name " + arguments[1]);
					}
					else
						sender.sendMessage(ChatColor.RED + "Only operators can remove other kingdoms.");
				}
				else {
					if(sender.isOp())
						sender.sendMessage(ChatColor.RED + "You should use /kingdom remove (name)");
					else
						sender.sendMessage(ChatColor.RED + "You should use /kingdom remove");
				}
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can remove their kingdom.");
		}
		else if(arguments[0].matches("options")){
			if(arguments.length == 1){
				sender.sendMessage(ChatColor.YELLOW + "Option 'chunksPerPlayer' is the amount of chunks that a kingdom can claim for each player it has, currently: "  + KingdomData.chunksPerPlayer());
				sender.sendMessage(ChatColor.YELLOW + "Option 'minHoursBeforeWar' is the minimum amount of hours between the time players can declare a war and the time the war begins, currently: " + KingdomData.minHoursBeforeWar());
				sender.sendMessage(ChatColor.YELLOW + "Option 'warDurationInHours' is the amount of time a war lasts after the start, this time is given in hours, currently: " + KingdomData.warDurationInHours());
				sender.sendMessage(ChatColor.YELLOW + "Option 'restHours' is the time the same kingdom can't start another war after the end of the previous war against that kingdom, currently: " + KingdomData.restHours());
				sender.sendMessage(ChatColor.YELLOW + "Option 'minAttackTime' is the earliest time at a day that kingdoms can attack eachother, currently: " + KingdomData.minAttackTime()[0] + ":" + KingdomData.minAttackTime()[1]);
				sender.sendMessage(ChatColor.YELLOW + "Option 'maxAttackTime' is the latest time at a day that kingdoms can attack eachother, currently: " + KingdomData.maxAttackTime()[0] + ":" + KingdomData.maxAttackTime()[1]);
				sender.sendMessage(ChatColor.YELLOW + "Option 'canEditOtherKingdoms' is whether players can break and place blocks in other kingdoms, or not, currently: " + KingdomData.canEditOtherKingdoms());
				sender.sendMessage(ChatColor.YELLOW + "Option 'canGolemsHelp' is whether golems will help with defending their kingdom during wars, or not, currently: " + KingdomData.canGolemsHelp());
				sender.sendMessage(ChatColor.YELLOW + "Option 'canEveryoneCreateKingdom' is whether everyone can create kingdoms, or only staff can create kingdoms, currently: " + KingdomData.canEveryoneCreateKingdom());
			}
			else if(arguments.length == 2){
				String option = arguments[1];
				if(option.equals("chunksPerPlayer"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'chunksPerPlayer' is the amount of chunks that a kingdom can claim for each player it has, currently: "  + KingdomData.chunksPerPlayer());
				else if(option.equals("minHoursBeforeWar"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'minHoursBeforeWar' is the minimum amount of hours between the time players can declare a war and the time the war begins, currently: " + KingdomData.minHoursBeforeWar());
				else if(option.equals("warDurationInHours"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'warDurationInHours' is the amount of time a war lasts after the start, this time is given in hours, currently: " + KingdomData.warDurationInHours());
				else if(option.equals("restHours"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'restHours' is the time the same kingdom can't start another war after the end of the previous war against that kingdom, currently: " + KingdomData.restHours());
				else if(option.equals("minAttackTime"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'minAttackTime' is the earliest time at a day that kingdoms can attack eachother, currently: " + KingdomData.minAttackTime()[0] + ":" + KingdomData.minAttackTime()[1]);
				else if(option.equals("maxAttackTime"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'maxAttackTime' is the latest time at a day that kingdoms can attack eachother, currently: " + KingdomData.maxAttackTime()[0] + ":" + KingdomData.maxAttackTime()[1]);
				else if(option.equals("canEditOtherKingdoms"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'canEditOtherKingdoms' is whether players can break and place blocks in other kingdoms, or not, currently: " + KingdomData.canEditOtherKingdoms());
				else if(option.equals("canGolemsHelp"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'canGolemsHelp' is whether golems will help with defending their kingdom during wars, or not, currently: " + KingdomData.canGolemsHelp());
				else if(option.equals("canEveryoneCreateKingdom"))
					sender.sendMessage(ChatColor.YELLOW + "Option 'canEveryoneCreateKingdom' is whether everyone can create kingdoms, or only staff can create kingdoms, currently: " + KingdomData.canEveryoneCreateKingdom());
				else
					sender.sendMessage(ChatColor.RED + option + " is not a known option.");
			}
			else if(arguments.length == 3){
				if(sender.isOp()){
					String option = arguments[1];
					if(option.equals("chunksPerPlayer") || option.equals("minHoursBeforeWar") || option.equals("warDurationInHours") || option.equals("restHours")){
						try {
							int number = Integer.decode(arguments[2]);
							if(option.equals("chunksPerPlayer"))
								KingdomData.chunksPerPlayer = number;
							else if(option.equals("minHoursBeforeWar"))
								KingdomData.minHoursBeforeWar = number;
							else if(option.equals("warDurationInHours"))
								KingdomData.warDurationInHours = number;
							else
								KingdomData.restHours = number;
							sender.sendMessage(ChatColor.GREEN + "Option " + option + " has been set to " + number);
						} catch(Exception ex){
							sender.sendMessage(ChatColor.RED + "Argument " + arguments[2] + " should be a number.");
						}
					}
					else if(option.equals("canEditOtherKingdoms") || option.equals("canGolemsHelp") || option.equals("canEveryoneCreateKingdom")){
						if(arguments[2].equals("true")){
							if(option.equals("canEditOtherKingdoms")){
								KingdomData.canEditOtherKingdoms = true;
								sender.sendMessage(ChatColor.GREEN + "Players can now edit blocks in other kingdoms.");
							}
							else if(option.equals("canGolemsHelp")){
								KingdomData.canGolemsHelp = true;
								sender.sendMessage(ChatColor.GREEN + "Golems will now help defending their kingdom during wars.");
							}
							else {
								KingdomData.canEveryoneCreateKingdom = true;
								sender.sendMessage(ChatColor.GREEN + "Everybody can create kingdoms now.");
							}
						}
						else if(arguments[2].equals("false")){
							if(option.equals("canEditOtherKingdoms")){
								KingdomData.canEditOtherKingdoms = false;
								sender.sendMessage(ChatColor.GREEN + "Players can't edit blocks in other kingdoms anymore.");
							}
							else if(option.equals("canGolemsHelp")){
								KingdomData.canGolemsHelp = false;
								sender.sendMessage(ChatColor.GREEN + "Golems won't help during wars anymore.");
							}
							else {
								KingdomData.canEveryoneCreateKingdom = false;
								sender.sendMessage(ChatColor.GREEN + "Only staff members can create kingdoms now.");
							}
						}
						else
							sender.sendMessage(ChatColor.RED + "You should use /kingdom options canEditOtherKingdoms/canGolemsHelp true/false");
					}
					else if(option.equals("minAttackTime") || option.equals("maxAttackTime")){
						try {
							int index = arguments[2].indexOf(":");
							int hour = Integer.decode(arguments[2].substring(0, index));
							int minute = Integer.decode(arguments[2].substring(index + 1));
							if(hour < 0 || hour > 23){
								sender.sendMessage(ChatColor.RED + "Argument hour(" + hour + ") should be a number between 0 and 23 (both inclusive)");
								return false;
							}
							if(minute < 0 || minute > 59){
								sender.sendMessage(ChatColor.RED + "Argument minute(" + minute + ") should be a number between 0 and 59 (both inclusive)");
								return false;
							}
							if(option.equals("minAttackTime")){
								KingdomData.minAttackTime = new int[]{hour, minute};
								sender.sendMessage(ChatColor.GREEN + "Option 'minAttackTime' has been set to " + hour + ":" + minute);
							}
							else {
								KingdomData.maxAttackTime = new int[]{hour, minute};
								sender.sendMessage(ChatColor.GREEN + "Option 'maxAttackTime' has been set to " + hour + ":" + minute);
							}
						} catch(Exception ex){
							sender.sendMessage(ChatColor.RED + "The time agument should be like HOURS:MINUTES, where hours is a number between 0 and 23 and minutes is a number between 0 and 59.");
						}
					}
					else
						sender.sendMessage(ChatColor.RED + option + " is not a known option.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Only operators can change the options.");
			}
			else
				sender.sendMessage(ChatColor.RED + "You should use /kingdom options (optionname) (new value)");
		}
		else if(arguments[0].matches("kick")){
			if(arguments.length == 2){
				if(sender instanceof Player){
					Player player = (Player) sender;
					Kingdom kd = KingdomData.getKingdom(player);
					if(kd != null)
						sender.sendMessage(kd.kickPlayer(sender, arguments[1]));
					else
						sender.sendMessage(ChatColor.RED + "You need a kingdom to kick players from.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Only players can kick other players from their kingdom.");
			}
			else
				sender.sendMessage(ChatColor.RED + "You shoud use /kingdom kick [player]");
		}
		else if(arguments[0].matches("promote")){
			if(arguments.length == 3){
				if(sender instanceof Player){
					Player player = (Player) sender;
					Kingdom kd = KingdomData.getKingdom(player);
					if(kd != null){
						OfflinePlayer target = kd.getPlayerByName(arguments[1]);
						if(target != null){
							if(kd.getRank(target) != Rank.KING){
								if(kd.canPromote(player)){
									try {
										Rank rank = Rank.fromString(arguments[2]);
										if(rank != Rank.KING){
											kd.setRank(target, rank);
											player.sendMessage(ChatColor.GREEN + "The rank of player " + target.getName() + " has been set to " + rank);
										}
										else if(kd.isKing(player)){
											kd.setRank(target, rank);
											kd.setRank(player, Rank.GOVERNER);
											ArrayList<Player> players = kd.getOnlinePlayers(Bukkit.getServer());
											int i = 0;
											while(i < players.size()){
												players.get(i).sendMessage(ChatColor.GOLD + target.getName() + " is your new king!");
												++i;
											}
										}
										else
											sender.sendMessage(ChatColor.RED + "Only the king can crown someone else.");
									} catch(Exception ex){
										sender.sendMessage(ChatColor.RED + arguments[2] + " is not a known rank, known ranks are: recruiter,lutenant,duke,general,governer and king.");
									}
								}
								else
									sender.sendMessage(ChatColor.RED + "You are not allowed to promote players in this kingdom.");
							}
							else
								sender.sendMessage(ChatColor.RED + "You can't 'promote' the king.");
						}
						else
							sender.sendMessage(ChatColor.RED + "You can only promote players in your own kingdom.");
					}
					else
						sender.sendMessage(ChatColor.RED + "You need a kingdom to promote players in.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Only players can promote other players.");
			}
			else
				sender.sendMessage(ChatColor.RED + "You should use /kingdom promote [player] recruiter/lutenant/duke/general/governer/king");
		}
		else if(arguments[0].matches("demote")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				Kingdom kd = KingdomData.getKingdom(player);
				if(kd != null){
					if(kd.canPromote(player)){
						if(arguments.length == 2){
							OfflinePlayer target = kd.getPlayerByName(arguments[1]);
							if(target != null){
								if(kd.getRank(target) != Rank.KING){
									if(kd.getRank(target) != null){
										kd.setRank(target, null);
										sender.sendMessage(ChatColor.GREEN + "You demoted player " + target.getName() + " succesfully.");
									}
									else
										sender.sendMessage(ChatColor.RED + "You can't demote this player because he has no rank to remove.");
								}
								else
									sender.sendMessage(ChatColor.RED + "You can't demote the king.");
							}
							else
								sender.sendMessage(ChatColor.RED + "You can only demote players in your kingdom.");
						}
						else
							sender.sendMessage(ChatColor.RED + "You should use /kingdom demote [player]");
					}
					else
						sender.sendMessage("You are not allowed to demote players in this kingdom.");
				}
				else
					sender.sendMessage(ChatColor.RED + "You need a kingdom to demote players in.");
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can demote other players.");
		}
		else if(arguments[0].matches("help")){
			if(arguments.length == 1){
				sender.sendMessage(ChatColor.YELLOW + "Met deze plug-in kun je een kingdom oprichten of lid worden van een kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "Staff kan een kingdom voor je oprichten met " + ChatColor.GREEN + "/kingdom create [naam van je kingdom]");
				sender.sendMessage(ChatColor.YELLOW + "De staff moet dan " + ChatColor.GREEN + "/kingdom invite [jouw naam]" + ChatColor.YELLOW + " en jij moet dan " + ChatColor.GREEN + "/kingdom join [naam van het kingdom]" + ChatColor.YELLOW + " doen.");
				sender.sendMessage(ChatColor.YELLOW + "Daarna moet de staff " + ChatColor.GREEN + "/kingdom promote [jouw naam] king" + ChatColor.YELLOW + " gebruiken, dan ben jij de koning.");
				sender.sendMessage(ChatColor.YELLOW + "De staff kan daarna " + ChatColor.GREEN + "/kingdom leave" + ChatColor.YELLOW + " om het kingdom weer te verlaten.");
				sender.sendMessage(ChatColor.YELLOW + "Of je kunt lid worden van een bestaand kingdom met " + ChatColor.GREEN + "/kingdom join [naam van het kingdom]");
				sender.sendMessage(ChatColor.YELLOW + "Om te zien door welke kingdoms je bent uitgenodigt, kun je " + ChatColor.GREEN + "/kingdom invites " + ChatColor.YELLOW + "gebruiken.");
				sender.sendMessage(ChatColor.YELLOW + "Om op deze manier lid te worden, moet iemand met bevoegdheid in dat kingdom eerst " + ChatColor.GREEN + "/kingdom invite [jouw spelersnaam] " + ChatColor.YELLOW + "gebruiken.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt andere spelers uitnodigen met " + ChatColor.GREEN + "/kingdom invite [gebruikersnaam van de speler]");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt deze uitnodigingen intrekken met " + ChatColor.GREEN + "/kingdom invite cancel [spelersnaam]");
				sender.sendMessage(ChatColor.YELLOW + "Om te zien wie je allemaal hebt uitgenodigt, kun je gebruik maken van " + ChatColor.GREEN + "/kingdom invite list");
				sender.sendMessage(ChatColor.YELLOW + "Om mensen uit te nodigen, heb je wel de juiste rechten nodig, deze rechten kun je van je koning krijgen.");
				sender.sendMessage(ChatColor.YELLOW + "De koning heeft altijd de rechten om alles met zijn kingdom te doen.");
				sender.sendMessage(ChatColor.YELLOW + "Om een lijst te zien van bestaande kingdoms, kun je " + ChatColor.GREEN + "/kingdom list " + ChatColor.YELLOW + "gebruiken.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt mensen verwijderen uit je kingdom met " + ChatColor.GREEN + "/kingdom kick [naam van de speler]");
				sender.sendMessage(ChatColor.YELLOW + "Ook hiervoor heb je de juiste rechten nodig.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt de chat kleur van je kingdom veranderen met " + ChatColor.GREEN + "/kingdom color [naam van de kleur die je wilt]");
				sender.sendMessage(ChatColor.YELLOW + "Voor dit command heb je ook de juiste rechten nodig.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt je kingdom verlaten met " + ChatColor.GREEN + "/kingdom leave");
				sender.sendMessage(ChatColor.YELLOW + "Om dit te doen, heb je geen rechten nodig ;)");
				sender.sendMessage(ChatColor.YELLOW + "Maar de koning kan zijn kingdom niet verlaten, hij kan het alleen verwijderen met " + ChatColor.GREEN + "/kingdom remove");
				sender.sendMessage(ChatColor.YELLOW + "Om een andere speler speciale rechten te geven, kun je " + ChatColor.GREEN + "/kingdom promote [naam van de speler] [recruiter/lutenant/duke/general/governer/king " + ChatColor.YELLOW + "gebruiken.");
				sender.sendMessage(ChatColor.YELLOW + "Bij iedere rang horen andere rechten, uiteraard heb je zelf ook rechten nodig om dit commando uit te voeren.");
				sender.sendMessage(ChatColor.YELLOW + "Als je iemand anders tot koning benoemt, wordt je zelf Governer.");
				sender.sendMessage(ChatColor.YELLOW + "Met het commando " + ChatColor.GREEN + "/demote [naam van de speler] " + ChatColor.YELLOW + "kun je de rechten van een gepromoveerde speler weer afnemen.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt ook titels geven aan leden, hierdoor krijgen ze een titel in de chat, hun rechten veranderen hierdoor niet.");
				sender.sendMessage(ChatColor.YELLOW + "Dit kun je doen met " + ChatColor.GREEN + "/kingdom title [spelersnaam] [titelnaam] [titelkleur]");
				sender.sendMessage(ChatColor.YELLOW + "Als je " + ChatColor.GREEN + "/claim " + ChatColor.YELLOW + " gebruikt, krijg je de claimtool, hiermee kun je grond claimen voor je kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt geen grond claimen die al van een ander kingdom is, en het maximaal aantal chunks dat je kunt claimen hangt af van de instellingen en het aantal leden van je kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "Met het commando " + ChatColor.GREEN + "/kingdom options chunksPerPlayer" + ChatColor.YELLOW + " kun je zien hoeveel chunks je kunt claimen per lid van je kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "Met het commando " + ChatColor.GREEN + "/kingdom options canEditOtherKingdoms " + ChatColor.YELLOW + " kun je zien of je zonder oorlog blocken kunt slopen/bouwen in andere kingdoms.");
				sender.sendMessage(ChatColor.YELLOW + "Als je staff bent, kun je de waarden van deze opties veranderen met " + ChatColor.GREEN + "/kingdom options chunksPerPlayer [getal] " + ChatColor.YELLOW + " en " + ChatColor.GREEN + "/kingdom options canEditOtherKingdoms [true/false]");
				sender.sendMessage(ChatColor.YELLOW + "Voor de volledige lijst van opties: " + ChatColor.GREEN + "/kingdom options");
				sender.sendMessage(ChatColor.YELLOW + "Als je ook de info over oorlogen wilt lezen, kun je gebruik maken van " + ChatColor.GREEN + "/war help");
				sender.sendMessage(ChatColor.YELLOW + "Ik hoop dat dit je heeft geholpen om de kingdom plug-in te begrijpen.");
				sender.sendMessage(ChatColor.YELLOW + "Waarschijnlijk kun je de bovenste tekst niet zien, je kunt dit wel lezen als je de chat opent en omhoog scrolt.");
			}
			else if(arguments.length == 2){
				sender.sendMessage("Gebruik gewoon /help");
			}
			else
				sender.sendMessage(ChatColor.RED + "You should use /kingdom help (deelcommand)");
		}
		else if(arguments[0].matches("title")){
			if(arguments.length == 4){
				if(sender instanceof Player){
					Player player = (Player) sender;
					Kingdom kd = KingdomData.getKingdom(player);
					if(kd != null){
						if(kd.canPromote(player)){
							OfflinePlayer target = kd.getPlayerByName(arguments[1]);
							if(target != null){
								String color = arguments[3];
								try {
									ChatColor chatColor = ChatColor.valueOf(color.toUpperCase());
									kd.setTitle(target, new Title(arguments[2], chatColor));
									sender.sendMessage(ChatColor.GREEN + "You have set the title of player " + target.getName() + " to " + kd.getTitle(target));
								} catch(Exception ex){
									sender.sendMessage(ChatColor.RED + color + " is not a known color.");
								}
							}
							else
								sender.sendMessage(ChatColor.RED + "You can only give titles to players in your own kingdom.");
						}
						else
							sender.sendMessage(ChatColor.RED + "You are not allowed to give titles in this kingdom.");
					}
					else
						sender.sendMessage(ChatColor.RED + "You need a kingdom in order to give titles to members.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Only players can give titles in their kingdoms.");
			}
			else
				sender.sendMessage(ChatColor.RED + "You should use /kingdom title [playername] [title] [color]");
		}
		else if(arguments[0].matches("invites")){
			if(sender instanceof Player){
				ArrayList<Kingdom> inviters = new ArrayList<Kingdom>();
				String message = ChatColor.YELLOW + "";
				Player player = (Player) sender;
				int i = 0;
				while(i < KingdomData.kingdoms.size()){
					if(KingdomData.kingdoms.get(i).isInvited(player))
						inviters.add(KingdomData.kingdoms.get(i));
					++i;
				}
				if(inviters.size() == 0)
					message += "No kingdom has invited you yet.";
				else if(inviters.size() == 1)
					message += "You are invited by " + KingdomEventHandler.getKingdomString(inviters.get(0));
				else {
					message += "You are invited by ";
					int i1 = 0;
					while(i1 < inviters.size() - 1){
						message += KingdomEventHandler.getKingdomString(inviters.get(i1));
						if(i1 != inviters.size() - 2)
							message += ChatColor.YELLOW + ", ";
						++i1;
					}
					message += ChatColor.YELLOW + " and " + KingdomEventHandler.getKingdomString(inviters.get(i1));
				}
				player.sendMessage(message);
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can be invited to join a kingdom.");
		}
		else if(arguments[0].matches("setspawn")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				Kingdom kd = KingdomData.getKingdom(player);
				if(kd != null){
					if(kd.canPromote(sender)){
						kd.setSpawnPoint(player.getLocation());
						sender.sendMessage(ChatColor.GREEN + "You have set the spawn of your kingdom to " + kd.getSpawn());
					}
					else
						player.sendMessage(ChatColor.RED + "You need at least the rank of governer to set the kingdom spawn.");
				}
				else
					player.sendMessage(ChatColor.RED + "You can only set the spawn of your kingdom if you are in a kingdom.");
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can set the spawn of their kingdom.");
		}
		else if(arguments[0].matches("spawn")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				Kingdom kd = KingdomData.getKingdom(player);
				if(kd != null){
					Location spawn = kd.getSpawn();
					if(spawn != null && spawn.getWorld() != null){
						player.teleport(spawn, TeleportCause.COMMAND);
						player.sendMessage(ChatColor.GREEN + "You have been teleported to your kingdom spawn.");
					}
					else
						player.sendMessage(ChatColor.RED + "Your kingdom doesn't have a spawnpoint.");
				}
				else
					player.sendMessage(ChatColor.RED + "You can only teleport to your kingdom spawn if you are member of a kingdom.");
			}
			else
				sender.sendMessage(ChatColor.RED + "Only players can teleport to their kingdom spawn.");
		}
		else if(arguments[0].matches("confirm")){
			if(KingdomData.requestedRemoveKingdom.contains(sender)){
				if(sender instanceof Player){
					Kingdom kd = KingdomData.getKingdom((Player)sender);
					if(kd != null){
						if(kd.isKing((Player)sender)){
							KingdomData.requestedRemoveKingdom.remove(sender);
							KingdomData.removeKingdom(kd);
							sender.sendMessage(ChatColor.GREEN + "You succesfully removed your kingdom.");
						}
						else
							sender.sendMessage(ChatColor.RED + "Only the king can remove his kingdom.");
					}
					else
						sender.sendMessage(ChatColor.RED + "You can only remove your own kingdom.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Only players can remove their kingdom.");
			}
			else
				sender.sendMessage(ChatColor.RED + "You do not have pending requests you have to confirm.");
		}
		else if(arguments[0].matches("cancel")){
			if(KingdomData.requestedRemoveKingdom.contains(sender)){
				KingdomData.requestedRemoveKingdom.remove(sender);
				sender.sendMessage(ChatColor.YELLOW + "You cancelled the request to remove your kingdom.");
			}
			else
				sender.sendMessage(ChatColor.RED + "You haven't requested to remove your kingdom.");
		}
		else {
			sender.sendMessage(USEAGE);
			return false;
		}
		return true;
	}

}
