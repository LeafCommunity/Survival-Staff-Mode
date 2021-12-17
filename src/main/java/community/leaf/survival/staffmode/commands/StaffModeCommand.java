/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.commands;

import com.rezzedup.util.constants.Aggregates;
import com.rezzedup.util.constants.annotations.AggregatedResult;
import com.rezzedup.util.constants.types.TypeCapture;
import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.StaffModePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StaffModeCommand implements CommandExecutor, TabCompleter
{
	public static final Set<String> TOGGLE = Set.of("toggle", "on", "off");
	
	public static final Set<String> RUN = Set.of("run", "then");
	
	public static final Set<String> CHECK = Set.of("check");
	
	public static final Set<String> RELOAD = Set.of("reload");
	
	public static final Set<String> INFO = Set.of("info");
	
	public static final Set<String> USAGE = Set.of("help", "usage", "?");
	
	@AggregatedResult
	public static final Set<String> ARGUMENTS =
		Aggregates.set(StaffModeCommand.class, TypeCapture.type(String.class));
	
	private final StaffModePlugin plugin;
	
	public StaffModeCommand(StaffModePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		String choice = (args.length >= 1) ? args[0].toLowerCase(Locale.ROOT) : "toggle";
		
		if (TOGGLE.contains(choice)) { return toggle(sender, args); }
		if (RUN.contains(choice)) { return toggleThenRunCommand(sender, args); }
		if (CHECK.contains(choice)) { return check(sender); }
		if (RELOAD.contains(choice)) { return reload(sender); }
		if (INFO.contains(choice)) { return info(sender); }
		if (USAGE.contains(choice)) { return usage(sender); }
		
		return error(sender, "Unknown argument: " + choice);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		List<String> result = new ArrayList<>();
		int last = args.length - 1;
		String lastArg = (last >= 0) ? args[last] : "";
		String lastArgLowerCase = lastArg.toLowerCase(Locale.ROOT);
		
		if (last <= 1)
		{
			result.add("<Argument>");
			result.addAll(ARGUMENTS);
		}
		else
		{
			String choice = args[0].toLowerCase(Locale.ROOT);
			
			if (RUN.contains(choice))
			{
				result.add("<Command>");
			}
			else
			{
				result.add("<No more arguments>");
			}
		}
		
		result.removeIf(suggestion ->
			!(suggestion.startsWith("<") || suggestion.toLowerCase(Locale.ROOT).contains(lastArgLowerCase))
		);
		
		result.sort(String.CASE_INSENSITIVE_ORDER);
		return result;
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
		member.mode(member.activeMode().toggle());
		return true;
	}
	
	private boolean toggleThenRunCommand(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player player))
		{
			return error(sender, "Only players may use this command");
		}
		
		return true;
	}
	
	private boolean check(CommandSender sender)
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
