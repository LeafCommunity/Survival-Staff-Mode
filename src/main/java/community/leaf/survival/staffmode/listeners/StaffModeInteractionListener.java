/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.listeners;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import community.leaf.eventful.bukkit.CancellationPolicy;
import community.leaf.eventful.bukkit.ListenerOrder;
import community.leaf.eventful.bukkit.annotations.CancelledEvents;
import community.leaf.eventful.bukkit.annotations.EventListener;
import community.leaf.survival.staffmode.StaffMember;
import community.leaf.survival.staffmode.StaffModePlugin;
import community.leaf.survival.staffmode.StaffModeProfile;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class StaffModeInteractionListener implements Listener
{
    private final StaffModePlugin plugin;
    
    public StaffModeInteractionListener(StaffModePlugin plugin)
    {
        this.plugin = plugin;
    }
    
    @EventListener(ListenerOrder.LAST)
    @CancelledEvents(CancellationPolicy.REJECT)
    public void onGamemodeChange(PlayerGameModeChangeEvent event)
    {
        Player player = event.getPlayer();
        if (!plugin.staff().isInStaffMode(player)) { return; }
        
        StaffModeProfile profile = plugin.staff().onlineStaffProfile(player);
        
        GameMode former = player.getGameMode();
        GameMode latter = event.getNewGameMode();
        
        boolean isEnablingSpectator = former != GameMode.SPECTATOR && latter == GameMode.SPECTATOR;
        boolean isDisabledSpectator = former == GameMode.SPECTATOR && latter != GameMode.SPECTATOR;
        
        // Update spectator status if enabling or disabling spectator mode.
        if (isEnablingSpectator || isDisabledSpectator) { profile.spectator(former); }
        
        // Allow flight if player is changing to survival mode
        // from another gamemode *while* in staff mode.
        // mode mode mode.
        plugin.sync().run(() ->
        {
            // Maybe they were demoted since last checking?
            // Better be safe about it.
            if (plugin.staff().isInStaffMode(player))
            {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        });
    }
    
    private boolean cooldown(Player player)
    {
        return plugin.staff().member(player)
            .flatMap(StaffMember::sinceLastToggle)
            .filter(toggle -> ChronoUnit.SECONDS.between(toggle, Instant.now()) < 3)
            .isPresent();
    }
    
    @EventListener(ListenerOrder.FIRST)
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player player)) { return; }
        
        if (plugin.staff().isInStaffMode(player) || cooldown(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventListener(ListenerOrder.FIRST)
    public void onHungerDecrease(FoodLevelChangeEvent event)
    {
        if (event.getEntity() instanceof Player player && plugin.staff().isInStaffMode(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventListener(ListenerOrder.FIRST)
    public void onEntityTarget(EntityTargetEvent event)
    {
        if (event.getTarget() instanceof Player player && plugin.staff().isInStaffMode(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventListener(ListenerOrder.FIRST)
    public void onDeath(PlayerDeathEvent event)
    {
        if (!plugin.staff().isInStaffMode(event.getEntity())) { return; }
        
        event.setKeepInventory(true);
        event.getDrops().clear();
        
        event.setKeepLevel(true);
        event.setDroppedExp(0);
    }
    
    @EventListener(ListenerOrder.FIRST)
    public void onInsomnia(PlayerStatisticIncrementEvent event)
    {
        if (event.getStatistic() != Statistic.TIME_SINCE_REST) { return; }
        if (event.getNewValue() < event.getPreviousValue()) { return; }
        
        if (plugin.staff().isInStaffMode(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }
    
    public static class Paper implements Listener
    {
        private final StaffModePlugin plugin;
        
        public Paper(StaffModePlugin plugin)
        {
            this.plugin = plugin;
        }
        
        // No advancements while in staff mode
        @EventListener(ListenerOrder.FIRST)
        public void onAdvancement(PlayerAdvancementCriterionGrantEvent event)
        {
            if (plugin.staff().isInStaffMode(event.getPlayer())) { event.setCancelled(true); }
        }
    }
}
