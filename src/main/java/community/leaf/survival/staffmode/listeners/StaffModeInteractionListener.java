/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.listeners;

import community.leaf.eventful.bukkit.ListenerOrder;
import community.leaf.eventful.bukkit.annotations.EventListener;
import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.Permissions;
import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.StaffModePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class StaffModeInteractionListener implements Listener
{
	private final StaffModePlugin plugin;
	
	public StaffModeInteractionListener(StaffModePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	private boolean invulnerable(Player player)
	{
		return Permissions.STAFF_MEMBER.allows(player)
			&& plugin.staff().onlineStaffMember(player).mode() == Mode.STAFF;
	}
	
	private boolean cooldown(Player player)
	{
		return plugin.staff().member(player)
			.flatMap(StaffMember::sinceLastToggle)
			.filter(toggle -> ChronoUnit.SECONDS.between(toggle, Instant.now()) < 3)
			.isPresent();
	}
	
	@EventListener(ListenerOrder.FIRST)
	public void onGamemodeChange(PlayerGameModeChangeEvent event)
	{
		Player player = event.getPlayer();
		
		// Allow flight if player is changing to survival mode
		// from another gamemode *while* in staff mode.
		// mode mode mode.
		plugin.sync().run(() ->
		{
			if (invulnerable(player))
			{
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		});
	}
	
	@EventListener(ListenerOrder.FIRST)
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player player && (invulnerable(player) || cooldown(player)))
		{
			event.setCancelled(true);
		}
	}
	
	@EventListener(ListenerOrder.FIRST)
	public void onHungerDecrease(FoodLevelChangeEvent event)
	{
		if (event.getEntity() instanceof Player player && invulnerable(player)) { event.setCancelled(true); }
	}
	
	@EventListener(ListenerOrder.FIRST)
	public void onEntityTarget(EntityTargetEvent event)
	{
		if (event.getTarget() instanceof Player player && invulnerable(player)) { event.setCancelled(true); }
	}
	
	@EventListener(ListenerOrder.FIRST)
	public void onDeath(PlayerDeathEvent event)
	{
		if (!invulnerable(event.getEntity())) { return; }
		
		event.setKeepInventory(true);
		event.getDrops().clear();
		
		event.setKeepLevel(true);
		event.setDroppedExp(0);
	}
}
