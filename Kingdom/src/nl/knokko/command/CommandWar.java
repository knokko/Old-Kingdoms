package nl.knokko.command;

import java.util.Calendar;

import static nl.knokko.data.KingdomData.*;

import nl.knokko.data.KingdomData;
import nl.knokko.kingdom.Kingdom;
import nl.knokko.kingdom.event.KingdomAttack;
import nl.knokko.kingdom.event.KingdomEvent;
import nl.knokko.kingdom.event.KingdomWar;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandWar implements CommandExecutor {
	
	public static final String USEAGE = ChatColor.RED + "You should use /war declare/attack/cancel/list/attacks/help [another kingdom]";

	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		if(arguments.length > 0){
			if(arguments[0].equals("declare")){
				if(arguments.length > 1 && arguments.length <= 7){
					if(sender instanceof Player){
						Player player = (Player) sender;
						Kingdom kd = KingdomData.getKingdom(player);
						if(kd != null){
							if(kd.canDeclareWar(player)){
								Kingdom target = KingdomData.getKingdom(arguments[1]);
								if(target != null){
									if(arguments.length == 2)
										sender.sendMessage(new KingdomWar(kd, target).register());
									else {
										if(arguments.length >= 3){
											try {
												int year = Integer.decode(arguments[2]);
												Calendar c = Calendar.getInstance();
												if(c.get(Calendar.YEAR) >= year){
													if(arguments.length >= 4){
														try {
															int month = Integer.decode(arguments[3]);
															if(month >= 1 && month <= 12){
																if(arguments.length >= 5){
																	try {
																		int day = Integer.decode(arguments[4]);
																		if(day >= 1 && day <= 31){
																			if(arguments.length >= 6){
																				try {
																					int hour = Integer.decode(arguments[5]);
																					if(hour >= 0 && hour <= 23){
																						if(arguments.length >= 7){
																							try {
																								int minute = Integer.decode(arguments[6]);
																								if(minute >= 0 && minute <= 56){
																									if(arguments.length == 7)
																										tryDeclareWar(sender, new Calendar.Builder().set(Calendar.YEAR, year).set(Calendar.MONTH, month).set(Calendar.DAY_OF_MONTH, day).set(Calendar.HOUR_OF_DAY, hour).set(Calendar.MINUTE, minute).build(), kd, target);
																									else
																										sender.sendMessage(ChatColor.RED + "You should use /war declare [other kingdom] (year) (month) (day) (hour) (minute)");
																								}
																								else
																									sender.sendMessage(ChatColor.RED + "Argument minute(" + arguments[6] + ") should be a number between 0 and 59 (inclusive)");
																							} catch(Exception ex){
																								sender.sendMessage(ChatColor.RED + "Argument minute(" + arguments[6] + ") should be a number.");
																							}
																						}
																						else
																							tryDeclareWar(sender, new Calendar.Builder().set(Calendar.YEAR, year).set(Calendar.MONTH, month).set(Calendar.DAY_OF_MONTH, day).set(Calendar.HOUR_OF_DAY, hour).build(), kd, target);
																					}
																					else
																						sender.sendMessage(ChatColor.RED + "Argument hour(" + hour + ") should be a number between 0 and 23 (inclusive)");
																				} catch(Exception ex){
																					sender.sendMessage(ChatColor.RED + "Argument hour(" + arguments[5] + ") should be a number.");
																				}
																			}
																			else
																				tryDeclareWar(sender, new Calendar.Builder().set(Calendar.YEAR, year).set(Calendar.MONTH, month).set(Calendar.DAY_OF_MONTH, day).build(), kd, target);
																		}
																		else
																			sender.sendMessage(ChatColor.RED + "Argument day(" + day + ") should be a number between 1 and 31 (inclusive)");
																	}
																	catch(Exception ex){
																		sender.sendMessage(ChatColor.RED + "Argument day(" + arguments[4] + ") should be a number.");
																	}
																}
																else 
																	tryDeclareWar(sender, new Calendar.Builder().set(Calendar.YEAR, year).set(Calendar.MONTH, month).build(), kd, target);
															}
															else
																sender.sendMessage(ChatColor.RED + "The month should be a number between 1 and 12 (both inclusive");
														} catch(Exception ex){
															sender.sendMessage(ChatColor.RED + "Argument month (" + arguments[3] + ") should be a number.");
														}
													}
													else 
														tryDeclareWar(sender, new Calendar.Builder().set(Calendar.YEAR, year).build(), kd, target);
												}
												else
													sender.sendMessage(ChatColor.RED + "The year " + year + " is already over.");
											} catch(Exception ex){
												sender.sendMessage(ChatColor.RED + "Argument year(" + arguments[2] + ") should be a number.");
											}
										}
									}
								}
								else
									sender.sendMessage(ChatColor.RED + "There is no kingdom with name " + arguments[1]);
							}
							else
								sender.sendMessage(ChatColor.RED + "You don't have permissions to declare war.");
						}
						else
							sender.sendMessage(ChatColor.RED + "You can only declare war if you are in a kingdom.");
					}
					else
						sender.sendMessage(ChatColor.RED + "Only players can declare a war.");
				}
				else
					sender.sendMessage(ChatColor.RED + "You should use /war declare [other kingdom] (year) (month) (day) (hour) (minute)");
			}
			else if(arguments[0].equals("cancel")){
				if(arguments.length > 1){
					if(sender instanceof Player){
						Player player = (Player) sender;
						Kingdom kd = KingdomData.getKingdom(player);
						if(kd != null){
							if(!kd.canDeclareWar(player)){
								player.sendMessage(ChatColor.RED + "You are not allowed to cancel wars for your kingdom.");
								return false;
							}
							Kingdom other = KingdomData.getKingdom(arguments[1]);
							if(other != null){
								if(arguments.length > 2){
									try {
										int year = Integer.decode(arguments[2]);
										if(arguments.length > 3){
											try {
												int month = Integer.decode(arguments[3]);
												if(month >= 1 && month <= 12){
													if(arguments.length > 4){
														try {
															int day = Integer.decode(arguments[4]);
															if(day >= 1 && day <= 31){
																if(arguments.length == 5){
																	int i = 0;
																	boolean none = true;
																	while(i < kd.wars.size()){
																		KingdomWar war = kd.wars.get(i);
																		if(war.contains(other)){
																			Calendar start = war.getStart();
																			Calendar end = war.getEnd();
																			if((start.get(Calendar.YEAR) == year && start.get(Calendar.MONTH) == month && start.get(Calendar.DAY_OF_MONTH) == day) || (end.get(Calendar.YEAR) == year && end.get(Calendar.MONTH) == month && end.get(Calendar.DAY_OF_MONTH) == day)){
																				war.requestCancel(kd);
																				sender.sendMessage(ChatColor.GREEN + "You requested to cancel the war against " + other.getColoredName() + ChatColor.GREEN + " at " + kd.wars.get(i).getStartTimeString());
																				none = false;
																			}
																		}
																		++i;
																	}
																	if(none)
																		sender.sendMessage(ChatColor.RED + "You don't have any wars against " + other.getColoredName() + ChatColor.RED + " at " + day + "/" + month + "/" + year);
																}
																else
																	sender.sendMessage(ChatColor.RED + "You should use /war cancel [other kingdom] (year) (month) (day)");
															}
															else
																sender.sendMessage(ChatColor.RED + "Argument day(" + arguments[4] + ") should be a number between 1 and 31 (inclusive).");
														} catch(Exception ex){
															sender.sendMessage(ChatColor.RED + "Argument day(" + arguments[4] + ") should be a number.");
														}
													}
													else {
														int i = 0;
														boolean none = true;
														while(i < kd.wars.size()){
															KingdomWar war = kd.wars.get(i);
															if(war.contains(other)){
																Calendar start = war.getStart();
																Calendar end = war.getEnd();
																if((start.get(Calendar.YEAR) == year && start.get(Calendar.MONTH) == month) || (end.get(Calendar.YEAR) == year && end.get(Calendar.MONTH) == month)){
																	war.requestCancel(kd);
																	sender.sendMessage(ChatColor.GREEN + "You requested to cancel the war against " + other.getColoredName() + ChatColor.GREEN + " at " + kd.wars.get(i).getStartTimeString());
																	none = false;
																}
															}
															++i;
														}
														if(none)
															sender.sendMessage(ChatColor.RED + "You don't have any wars against " + other.getColoredName() + ChatColor.RED + " in " + month + "/" + year);
													}
												}
												else
													sender.sendMessage(ChatColor.RED + "Argument month(" + arguments[3] + ") should be a number between 1 and 12 (inclusive)");
											} catch(Exception ex){
												sender.sendMessage(ChatColor.RED + "Argument month(" + arguments[3] + ") should be a number");
											}
										}
										else {
											int i = 0;
											boolean none = true;
											while(i < kd.wars.size()){
												KingdomWar war = kd.wars.get(i);
												if(war.contains(other)){
													if(war.getStart().get(Calendar.YEAR) == year || war.getStart().get(Calendar.YEAR) == year){
														war.requestCancel(kd);
														sender.sendMessage(ChatColor.GREEN + "You requested to cancel the war against " + other.getColoredName() + ChatColor.GREEN + " at " + kd.wars.get(i).getStartTimeString());
														none = false;
													}
												}
												++i;
											}
											if(none)
												sender.sendMessage(ChatColor.RED + "You don't have any wars against " + other.getColoredName() + ChatColor.RED + " during " + year);
										}
									} catch(Exception ex){
										sender.sendMessage(ChatColor.RED + "Argument year(" + arguments[2] + ") should be a number.");
									}
								}
								else {
									if(kd.wars.isEmpty()){
										sender.sendMessage(ChatColor.RED + "Your kingdom doesn't have any wars.");
										return false;
									}
									int i = 0;
									boolean none = true;
									while(i < kd.wars.size()){
										if(kd.wars.get(i).contains(other)){
											kd.wars.get(i).requestCancel(kd);
											sender.sendMessage(ChatColor.GREEN + "You requested to cancel the war against " + other.getColoredName() + ChatColor.GREEN + " at " + kd.wars.get(i).getStartTimeString());
											none = false;
										}
										++i;
									}
									if(none)
										sender.sendMessage(ChatColor.RED + "You don't have any wars against " + other.getColoredName());
								}
							}
							else
								sender.sendMessage(ChatColor.RED + "There is no kingdom with name " + arguments[1]);
						}
						else
							sender.sendMessage(ChatColor.RED + "You can only cancel your wars if you are in a kingdom.");
					}
					else
						sender.sendMessage(ChatColor.RED + "Only players can request to cancel their wars.");
				}
				else
					sender.sendMessage(ChatColor.RED + "You should use /war cancel [other kingdom] (year) (month) (day)");
			}
			else if(arguments[0].equals("attack")){
				if(sender instanceof Player){
					Player player = (Player) sender;
					Kingdom kd = KingdomData.getKingdom(player);
					if(kd != null){
						if(kd.canStartAttack(player)){
							if(arguments.length > 1){
								Kingdom target = KingdomData.getKingdom(arguments[1]);
								if(target != null){
									if(arguments.length == 2){
										if(!kd.isAttacking(target))
											sender.sendMessage(new KingdomAttack(kd, target).register());
										else
											sender.sendMessage(ChatColor.RED + "You can't start another attack during an attack or less than an hour after the previous attack.");
									}
									else {
										if(arguments.length > 2){
											try {
												int hour = Integer.decode(arguments[2]);
												if(hour >= 0 && hour <= 23){
													if(arguments.length == 3){
														if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > hour){
															Calendar start = Calendar.getInstance();
															start.set(Calendar.HOUR_OF_DAY, hour);
															start.set(Calendar.MINUTE, 0);
															start.set(Calendar.SECOND, 0);
															start.set(Calendar.MILLISECOND, 0);
															sender.sendMessage(new KingdomAttack(start, kd, target).register());
														}
														else {
															Calendar start = Calendar.getInstance();
															start.add(Calendar.DAY_OF_MONTH, 1);
															start.set(Calendar.HOUR_OF_DAY, hour);
															start.set(Calendar.MINUTE, 0);
															start.set(Calendar.SECOND, 0);
															start.set(Calendar.MILLISECOND, 0);
															sender.sendMessage(new KingdomAttack(start, kd, target).register());
														}
													}
													else {
														if(arguments.length == 4){
															try {
																int minute = Integer.decode(arguments[3]);
																if(minute >= 0 && minute <= 59){
																	Calendar start = Calendar.getInstance();
																	if(start.get(Calendar.HOUR_OF_DAY) > hour || (start.get(Calendar.HOUR_OF_DAY) == hour && start.get(Calendar.MINUTE) > minute)){
																		start.set(Calendar.HOUR_OF_DAY, hour);
																		start.set(Calendar.MINUTE, minute);
																		start.set(Calendar.SECOND, 0);
																		start.set(Calendar.MILLISECOND, 0);
																		sender.sendMessage(new KingdomAttack(start, kd, target).register());
																	}
																	else {
																		start.add(Calendar.DAY_OF_MONTH, 1);
																		start.set(Calendar.HOUR_OF_DAY, hour);
																		start.set(Calendar.MINUTE, minute);
																		start.set(Calendar.SECOND, 0);
																		start.set(Calendar.MILLISECOND, 0);
																		sender.sendMessage(new KingdomAttack(start, kd, target).register());
																	}
																}
																else
																	sender.sendMessage(ChatColor.RED + "Argument minute(" + arguments[3] + ") should be a number between 0 and 59 (both inclusive)");
															} catch(Exception ex){
																sender.sendMessage(ChatColor.RED + "Argument minute(" + arguments[3] + ") should be a number between 0 and 59 (both inclusive)");
															}
														}
														else
															sender.sendMessage(ChatColor.RED + "You should use /war attack [enemy kingdom] (hour) (minute)");
													}
												}
												else
													sender.sendMessage(ChatColor.RED + "Argument hour(" + arguments[2] + ") should be a number between 0 and 23 (both inclusive)");
											} catch(Exception ex){
												sender.sendMessage(ChatColor.RED + "Argument hour(" + arguments[2] + ") should be a number between 0 and 23 (both inclusive)");
											}
										}
										else
											sender.sendMessage(ChatColor.RED + "You should use /war attack [enemy kingdom] (hour) (minute)"); 
									}
								}
								else
									sender.sendMessage(ChatColor.RED + "There is no kingdom with name " + arguments[1]);
							}
							else
								sender.sendMessage(ChatColor.RED + "You should use /war attack [enemy kingdom] (hour) (minute)"); 
						}
						else
							sender.sendMessage(ChatColor.RED + "You don't have permissions to start a kingdom attack.");
					}
					else
						sender.sendMessage(ChatColor.RED + "You can only attack if you are in a kingdom.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Only players can start kingdom attacks.");
			}
			else if(arguments[0].matches("list")){
				if(sender instanceof Player){
					Player player = (Player) sender;
					Kingdom kd = KingdomData.getKingdom(player);
					if(kd != null){
						KingdomData.removeExpiredWars();
						if(!kd.wars.isEmpty()){
							if(arguments.length == 1){
								int i = 0;
								while(i < kd.wars.size()){
									sender.sendMessage(KingdomData.getWarMessage(kd.wars.get(i)));
									++i;
								}
							}
							else if(arguments.length == 2){
								Kingdom target = KingdomData.getKingdom(arguments[1]);
								if(target != null){
									boolean none = true;
									int i = 0;
									while(i < kd.wars.size()){
										if(kd.wars.get(i).contains(target)){
											sender.sendMessage(KingdomData.getWarMessage(kd.wars.get(i)));
											none = false;
										}
										++i;
									}
									if(none)
										sender.sendMessage(ChatColor.GREEN + "You are not in war with " + target.getColoredName());
								}
								else
									sender.sendMessage(ChatColor.RED + "There is no kingdom with name " + arguments[1]);
							}
							else
								sender.sendMessage(ChatColor.RED + "You should use /war list (other kingdom)");
						}
						else
							sender.sendMessage(ChatColor.GREEN + "Your kingdom doesn't have any wars at the moment.");
					}
					else
						sender.sendMessage(ChatColor.RED + "You can only have wars if you are in a kingdom.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Only players can be in a kingdom and have wars.");
			}
			else if(arguments[0].matches("attacks")){
				if(sender instanceof Player){
					Kingdom kd = KingdomData.getKingdom(((Player)sender));
					if(kd != null){
						if(!kd.attacks.isEmpty()){
							if(arguments.length == 1){
								int i = 0;
								while(i < kd.attacks.size()){
									sender.sendMessage(KingdomData.getAttackMessage(kd.attacks.get(i)));
									++i;
								}
							}
							else if(arguments.length == 2){
								Kingdom target = KingdomData.getKingdom(arguments[1]);
								boolean none = true;
								if(target != null){
									int i = 0;
									while(i < kd.attacks.size()){
										if(kd.attacks.get(i).contains(target)){
											sender.sendMessage(KingdomData.getAttackMessage(kd.attacks.get(i)));
											none = false;
										}
										++i;
									}
									if(none)
										sender.sendMessage(ChatColor.GREEN + "There are no planned attacks between " + kd.getColoredName() + ChatColor.GREEN + " and " + target.getColoredName());
								}
								else
									sender.sendMessage(ChatColor.RED + "There is no kingdom with name " + arguments[1]);
							}
							else
								sender.sendMessage(ChatColor.RED + "You should use /war attacks (other kingdom)");
						}
						else
							sender.sendMessage(ChatColor.GREEN + "Your kingdom doesn't participate in any attacks at the moment.");
					}
					else
						sender.sendMessage(ChatColor.RED + "You can only attack other kingdoms if you are in a kingdom.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Only players can be in a kingdom and attack other kingdoms.");
			}
			else if(arguments[0].equals("help")){
				sender.sendMessage(ChatColor.YELLOW + "Het war commando maakt het mogelijk om oorlogen te verklaren en andere kingdoms aan te vallen.");
				sender.sendMessage(ChatColor.YELLOW + "Mits je de juiste rechten hebt, kun je de oorlog verklaren aan een ander kingdom met " + ChatColor.GREEN + "/war declare [naam van het kingdom waar je oorlog mee wilt]");
				sender.sendMessage(ChatColor.YELLOW + "Als je dat doet, begint er over " + minHoursBeforeWar() + " uur een oorlog, deze oorlog duurt " + warDurationInHours() + " uur.");
				sender.sendMessage(ChatColor.YELLOW + "Tijdens de oorlog mag je nog steeds niet zomaar griefen en spelers aanvallen in het andere kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "Maar tijdens een oorlog kun je (als je de juiste rechten hebt) wel gebruik maken van " + ChatColor.GREEN + "/war attack [naam van het andere kingdom]");
				sender.sendMessage(ChatColor.YELLOW + "Wanneer je dat doet, begint er over 1 uur een aanval, deze aanval duurt dan 2 uur.");
				sender.sendMessage(ChatColor.YELLOW + "Maar let op: Tijdens een oorlog kan je vijand jouw kingdom ook aanvallen!");
				sender.sendMessage(ChatColor.YELLOW + "Om nachtaanvallen te voorkomen, kan de aanval op zijn vroegst beginnen om " + minAttackTime[0] + ":" + minAttackTime[1] + " en moet de aanval afgelopen zijn om " + maxAttackTime[0] + ":" + maxAttackTime[1]);
				sender.sendMessage(ChatColor.YELLOW + "Gedurende deze aanval, mag en kan je alles griefen en iedereen afmaken in het andere kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "Als je toch geen oorlog meer wilt voeren, kun je gebruik maken van " + ChatColor.GREEN + "/war cancel [naam van het kingdom waar je geen oorlog meer mee wilt]");
				sender.sendMessage(ChatColor.YELLOW + "Wanneer je dit doet, krijgt het andere kingdom een bericht waarin staat dat je de oorlog wilt afbreken. Als het andere kingdom dan ook " + ChatColor.GREEN + "/war cancel [naam van jouw kingdom]" + ChatColor.YELLOW + " gebruikt, wordt de oorlog beeindigt.");
				sender.sendMessage(ChatColor.YELLOW + "Als je niet precies over " + minHoursBeforeWar() + " uur oolog wilt, kun je gebruik maken van " + ChatColor.GREEN + "/war declare [naam van ander kingdom] (jaar) (maand) (dag) (uren) (minuten)");
				sender.sendMessage(ChatColor.YELLOW + "De oorlog begint dan op het tijdstip dat je hebt ingevult in het commando, maar het moet nog steeds minstens " + minHoursBeforeWar() + " uur van te voren worden aangegeven.");
				sender.sendMessage(ChatColor.YELLOW + "Als je niet wilt dat je aanval over precies 1 uur begint, kun je gebruik maken van " + ChatColor.GREEN + "/war attack [naam van vijandig kingdom] (uren) (minuten)");
				sender.sendMessage(ChatColor.YELLOW + "De aanval begint dan op het tijdstip dat je hebt ingevult in het commando, maar het moet nog steeds minstens 1 uur van te voren worden aangegeven en binnen de toegestane tijdstippen plaatsvinden.");
				sender.sendMessage(ChatColor.YELLOW + "Als het ingevulde tijdstip al voorbij is, zal de aanval morgen om die tijd plaatsvinden.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt ook de oorlog op een specifieke dag proberen te beeindigen met " + ChatColor.GREEN + "/war cancel [vijandig kingdom] (jaar) (maand) (dag)");
				sender.sendMessage(ChatColor.YELLOW + "Deze functie is alleen nuttig als je meerdere oorlogen hebt verklaard aan hetzelfde kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt al je geplande oorlogen zien met " + ChatColor.GREEN + "/war list (ander kingdom)" + ChatColor.YELLOW + ", als je (ander kingdom) invult, zie je alleen de oorlogen tegen dat kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "Je kunt al je geplande aanvallen zien met " + ChatColor.GREEN + "/war attacks (ander kingdom)" + ChatColor.YELLOW + ", als je (ander kingdom) invult, zie je alleen de geplande aanvallen tussen jouw kingdom en dat kingdom.");
				sender.sendMessage(ChatColor.YELLOW + "De staff kan een aantal opties aanpassen, zoals de tijdstippen voor oorlog en het aantal uur dat een oorlog van te voren moet worden aangekondigt.");
				sender.sendMessage(ChatColor.YELLOW + "Dit kan een stafflid doen door middel van " + ChatColor.GREEN + "/kingdom options (optie) (nieuwe waarde)");
				sender.sendMessage(ChatColor.YELLOW + "Waarschijnlijk kun je de bovenste tekst niet zien, dit kan je wel lezen door de chat te openen en naar boven te scrollen.");
			}
			else
				sender.sendMessage(USEAGE);
		}
		else
			sender.sendMessage(USEAGE);
		return false;
	}
	
	private void tryDeclareWar(CommandSender sender, Calendar startTime, Kingdom kd, Kingdom target){
		Calendar c = Calendar.getInstance();
		if(c.before(startTime)){
			if(startTime.getTimeInMillis() - c.getTimeInMillis() >= 3600000 * KingdomData.minHoursBeforeWar())
				sender.sendMessage(new KingdomWar(startTime, kd, target).register());
			else
				sender.sendMessage(ChatColor.RED + "You have to declare the war at least " + KingdomData.minHoursBeforeWar() + " hours before the start.");
		}
		else
			sender.sendMessage(ChatColor.RED + "The time " + KingdomEvent.getTimeString(startTime) + " is already over.");
	}
}
