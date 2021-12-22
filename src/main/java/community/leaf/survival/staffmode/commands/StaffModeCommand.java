/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.commands;

import com.rezzedup.util.constants.Aggregates;
import com.rezzedup.util.constants.annotations.AggregatedResult;
import com.rezzedup.util.constants.types.TypeCapture;
import community.leaf.survival.staffmode.Permissions;
import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.StaffModePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

public class StaffModeCommand implements CommandExecutor, TabCompleter
{
	// Staff Mode Toggles
	public static final Set<String> STAFF_TOGGLE_SWITCH = Set.of("toggle", "switch");
	
	public static final Set<String> STAFF_TOGGLE_ON = Set.of("on", "enable");
	
	public static final Set<String> STAFF_TOGGLE_OFF = Set.of("off", "disable");
	
	@AggregatedResult
	public static final Set<String> STAFF_TOGGLES =
		Aggregates.set(
			StaffModeCommand.class,
			TypeCapture.type(String.class),
			Aggregates.matching().all("TOGGLE").collections(true)
		);
	
	public static final Set<String> STAFF_RUN = Set.of("run", "then");
	
	public static final Set<String> STAFF_CHECK = Set.of("check");
	
	// Staff Mode Tools
	
	public static final Set<String> STAFF_NIGHT_VISION_TOOL = Set.of("nightvision", "nv");
	
	public static final Set<String> STAFF_SPECTATE_TOOL = Set.of("spectator", "spectate", "spec");
	
	public static final Set<String> STFF_FLY_TOOL = Set.of("fly");
	
	@AggregatedResult
	public static final Set<String> STAFF_TOOLS =
		Aggregates.set(
			StaffModeCommand.class,
			TypeCapture.type(String.class),
			Aggregates.matching().all("TOOL").collections(true)
		);
	
	// Managerial Commands
	public static final Set<String> ADMIN_RELOAD = Set.of("reload");
	
	public static final Set<String> ADMIN_INFO = Set.of("info");
	
	// Usage
	public static final Set<String> STAFF_USAGE = Set.of("help", "usage", "?");
	
	@AggregatedResult
	public static final Set<String> ALL_STAFF_ARGUMENTS =
		Aggregates.set(
			StaffModeCommand.class,
			TypeCapture.type(String.class),
			Aggregates.matching().all("STAFF").not("ADMIN").collections(true)
		);
	
	@AggregatedResult
	public static final Set<String> ALL_ADMIN_ARGUMENTS =
		Aggregates.set(
			StaffModeCommand.class,
			TypeCapture.type(String.class),
			Aggregates.matching().all().collections(true)
		);
	
	private static final Field COMMAND_MAP_FIELD;
	
	static
	{
		try
		{
			COMMAND_MAP_FIELD = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			COMMAND_MAP_FIELD.setAccessible(true);
		}
		catch (NoSuchFieldException  e)
		{
			throw new IllegalStateException("Server implementation is missing commandMap field", e);
		}
	}
	
	private final StaffModePlugin plugin;
	
	public StaffModeCommand(StaffModePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	private CommandMap commandMap()
	{
		try { return (CommandMap) COMMAND_MAP_FIELD.get(plugin.getServer()); }
		catch (IllegalAccessException e) { throw new RuntimeException(e); }
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		String choice = (args.length >= 1) ? args[0].toLowerCase(Locale.ROOT) : "toggle";
		
		if (STAFF_TOGGLES.contains(choice)) { return toggle(sender, args); }
		if (STAFF_RUN.contains(choice)) { return run(sender, args); }
		if (STAFF_CHECK.contains(choice)) { return check(sender, args); }
		if (STAFF_TOOLS.contains(choice)) { return tools(sender, args); }
		if (ADMIN_RELOAD.contains(choice)) { return reload(sender); }
		if (ADMIN_INFO.contains(choice)) { return info(sender); }
		if (STAFF_USAGE.contains(choice)) { return usage(sender); }
		
		return error(sender, "Unknown argument: " + choice);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		List<String> suggestions = new ArrayList<>();
		boolean filter = true;
		
		int last = args.length - 1;
		String lastArg = (last < 0) ? "" : args[last];
		String lastArgLowerCase = lastArg.toLowerCase(Locale.ROOT);
		
		if (args.length <= 1)
		{
			if (Permissions.ADMIN.allows(sender)) { suggestions.addAll(ALL_ADMIN_ARGUMENTS); }
			else { suggestions.addAll(ALL_STAFF_ARGUMENTS); }
		}
		else
		{
			String choice = args[0].toLowerCase(Locale.ROOT);
			
			if (STAFF_RUN.contains(choice))
			{
				StringJoiner joiner = new StringJoiner(" ");
				
				for (int i = 1; i < args.length; i++)
				{
					joiner.add((i == 1) ? args[i].replaceFirst("/", "") : args[i]);
				}
				
				plugin.getLogger().info("Tab completing: /" + joiner.toString());
				
				@NullOr List<String> delegated = commandMap().tabComplete(sender, joiner.toString());
				
				if (delegated == null || delegated.isEmpty())
				{
					suggestions.add("<Command>");
				}
				else
				{
					suggestions.addAll(delegated);
					filter = false; // no filtering, just send command's suggestions
				}
			}
			else
			{
				suggestions.add("<No more arguments>");
			}
		}
		
		if (filter)
		{
			suggestions.removeIf(entry ->
				!(entry.startsWith("<") || entry.toLowerCase(Locale.ROOT).contains(lastArgLowerCase))
			);
			
			suggestions.sort(String.CASE_INSENSITIVE_ORDER);
		}
		
		return suggestions;
	}
	
	private boolean error(CommandSender sender, String message)
	{
		return true;
	}
	
	private boolean toggle(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player player))
		{
			return error(sender, "Only players may use this command");
		}
		
		StaffMember member = plugin.staff().member(player).orElseThrow();
		member.mode(member.mode().toggle());
		return true;
	}
	
	private boolean run(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player player))
		{
			return error(sender, "Only players may use this command");
		}
		
		return true;
	}
	
	private boolean check(CommandSender sender, String[] args)
	{
		return true;
	}
	
	private boolean tools(CommandSender sender, String[] args)
	{
		return true;
	}
	
	private boolean reload(CommandSender sender)
	{
		return true;
	}
	
	private boolean info(CommandSender sender)
	{
		return true;
	}
	
	private boolean usage(CommandSender sender)
	{
		return true;
	}
}
