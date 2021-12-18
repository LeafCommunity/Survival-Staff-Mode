/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.listeners;

import community.leaf.eventful.bukkit.CancellationPolicy;
import community.leaf.eventful.bukkit.ListenerOrder;
import community.leaf.eventful.bukkit.annotations.CancelledEvents;
import community.leaf.eventful.bukkit.annotations.EventListener;
import community.leaf.survival.staffmode.Permissions;
import community.leaf.survival.staffmode.StaffModePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.regex.Pattern;

public class StaffCommandListener implements Listener
{
	private static final Pattern EASTER_EGG_ALIAS = Pattern.compile("(?i)(smh[-_]?my[-_]?head|smo{2,}thment)");
	
	private final StaffModePlugin plugin;
	
	public StaffCommandListener(StaffModePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	private static String reconstruct(String command, List<String> args)
	{
		return "/" + ((args.isEmpty()) ? command : command + " " + String.join(" ", args));
	}
	
	@EventListener(ListenerOrder.FIRST)
	@CancelledEvents(CancellationPolicy.REJECT)
	public void onCommand(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		boolean isStaffMember = Permissions.STAFF_MEMBER.allows(player);
		
		List<String> parts = List.of(event.getMessage().split("\s+"));
		String command = parts.get(0).replaceFirst("/", "");
		List<String> args = (parts.size() > 1) ? parts.subList(1, parts.size()) : List.of();
		
		if (isStaffMember && EASTER_EGG_ALIAS.matcher(command).matches())
		{
			event.setMessage(reconstruct("staffmode", args));
			return;
		}
	}
}
