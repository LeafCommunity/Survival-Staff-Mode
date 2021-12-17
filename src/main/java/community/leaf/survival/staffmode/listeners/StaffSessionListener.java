/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.listeners;

import community.leaf.eventful.bukkit.annotations.EventListener;
import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.Permissions;
import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.StaffModePlugin;
import community.leaf.survival.staffmode.events.StaffModeDisableEvent;
import community.leaf.survival.staffmode.events.StaffModeEnableEvent;
import community.leaf.survival.staffmode.snapshots.defaults.PotionEffectsSnapshot;
import community.leaf.survival.staffmode.snapshots.defaults.StatsSnapshot;
import community.leaf.tasks.Concurrency;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffSessionListener implements Listener
{
	private final StaffModePlugin plugin;
	
	public StaffSessionListener(StaffModePlugin plugin)
	{
		this.plugin = plugin;
		
		plugin.sync().every(30).seconds().run(context ->
		{
			plugin.staff().online().refresh();
			
			if (context.iterations() % 4 == 0)
			{
				plugin.staff().streamOnlineStaffMembers().forEach(StaffMember::capture);
				plugin.staff().saveIfUpdated(Concurrency.ASYNC);
			}
		});
		
		BaseComponent[] notification =
			new ComponentBuilder()
				.append("STAFF MODE")
					.color(ChatColor.RED)
					.bold(true)
					.italic(true)
				.create();
		
		plugin.sync().every(2).ticks().run(() -> {
			plugin.staff().streamOnlineStaffMembers()
				.filter(member -> member.activeMode() == Mode.STAFF)
				.flatMap(member -> member.player().stream())
				.forEach(player ->
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, notification)
				);
		});
	}
	
	@EventListener
	public void onStaffJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if (Permissions.STAFF_MEMBER.denies(player)) { return; }
		
		plugin.staff().online().join(player);
		StaffMember member = plugin.staff().member(player).orElseThrow();
		
		if (member.activeMode() != Mode.STAFF) { return; }
		
		// TODO: better message
		event.getPlayer().sendMessage("Staff mode enabled.");
	}
	
	@EventListener
	public void onStaffQuit(PlayerQuitEvent event)
	{
		// Remove from list regardless
		plugin.staff().online().quit(event.getPlayer());
	}
	
	@EventListener
	public void onStaffModeEnable(StaffModeEnableEvent event)
	{
		event.player().sendMessage("Enabled staff mode.");
		
		StatsSnapshot.HEALTHY.apply(event.context());
		PotionEffectsSnapshot.EMPTY.apply(event.context());
		
		event.player().setAllowFlight(true);
		event.player().setFlying(true);
	}
	
	@EventListener
	public void onStaffModeDisable(StaffModeDisableEvent event)
	{
		event.player().sendMessage("Disabled staff mode.");
		
		event.player().setAllowFlight(false);
		event.player().setFlying(false);
	}
}
