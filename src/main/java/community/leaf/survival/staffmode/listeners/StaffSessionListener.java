/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.listeners;

import com.rezzedup.util.constants.types.Cast;
import community.leaf.eventful.bukkit.annotations.EventListener;
import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.Permissions;
import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.StaffModePlugin;
import community.leaf.survival.staffmode.StaffModeProfile;
import community.leaf.survival.staffmode.events.StaffModeDisableEvent;
import community.leaf.survival.staffmode.events.StaffModeEnableEvent;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.defaults.PotionEffectsSnapshot;
import community.leaf.survival.staffmode.snapshots.defaults.StatsSnapshot;
import community.leaf.tasks.Concurrency;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Mob;
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
		
		plugin.sync().every(1).seconds().run(() ->
			plugin.getServer().getOnlinePlayers().forEach(this::checkForDemotion)
		);
		
		plugin.sync().every(2).minutes().run(() -> {
			plugin.staff().streamOnlineStaffMembers().forEach(StaffMember::capture);
			plugin.staff().saveIfUpdated(Concurrency.ASYNC);
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
				.filter(member -> member.mode() == Mode.STAFF)
				.flatMap(member -> member.player().stream())
				.forEach(player ->
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, notification)
				);
		});
	}
	
	private void applyStaffMode(SnapshotContext context)
	{
		Player player = context.player();
		
		plugin.getLogger().info(player.getName() + " enabled staff mode.");
		
		StatsSnapshot.HEALTHY.apply(context);
		PotionEffectsSnapshot.EMPTY.apply(context);
		
		// Enable flight.
		player.setAllowFlight(true);
		player.setFlying(true);
		
		// Clear targets of aggressive mobs since staff are immune.
		player.getNearbyEntities(256, 256, 256).stream()
			.flatMap(entity -> Cast.as(Mob.class, entity).stream())
			.filter(mob -> player.equals(mob.getTarget()))
			.forEach(mob -> mob.setTarget(null));
		
		// TODO: better message
		player.sendMessage("Staff mode enabled.");
	}
	
	private void checkForDemotion(Player player)
	{
		// Staff member, not demoted.
		if (Permissions.STAFF_MEMBER.allows(player)) { return; }
		
		// Demotion: profile exists, but the player isn't staff anymore.
		plugin.staff().existingPlayerProfile(player).ifPresent(profile ->
		{
			plugin.getLogger().info(
				"Deleting demoted staff member " + player.getName() + "'s staff mode profile."
			);
			
			// Player was last online in staff mode.
			if (profile.mode() == Mode.STAFF)
			{
				plugin.getLogger().info(
					player.getName() + " is in staff mode! " +
					"Restoring their survival mode snapshot because they are no longer staff."
				);
				
				// Forcibly restore their survival gameplay state.
				profile.forceRestoreSnapshot(new SnapshotContext(player, Mode.SURVIVAL));
			}
			
			// Player can't access staff mode, delete their profile.
			plugin.staff().deleteProfile(player.getUniqueId());
		});
	}
	
	@EventListener
	public void onStaffJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		checkForDemotion(player);
		
		if (Permissions.STAFF_MEMBER.denies(player)) { return; }
		
		StaffModeProfile profile = plugin.staff().onlineStaffMemberProfile(player);
		profile.updateMetaData();
		
		if (profile.mode() == Mode.STAFF)
		{
			applyStaffMode(new SnapshotContext(player, Mode.STAFF));
		}
	}
	
	@EventListener
	public void onStaffQuit(PlayerQuitEvent event)
	{
		checkForDemotion(event.getPlayer());
	}
	
	@EventListener
	public void onStaffModeEnable(StaffModeEnableEvent event)
	{
		applyStaffMode(event.context());
	}
	
	@EventListener
	public void onStaffModeDisable(StaffModeDisableEvent event)
	{
		Player player = event.player();
		plugin.getLogger().info(player.getName() + " disabled staff mode.");
		player.sendMessage("Staff mode disabled.");
	}
}
