/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.eventful.bukkit.Events;
import community.leaf.survival.staffmode.events.StaffModeDisableEvent;
import community.leaf.survival.staffmode.events.StaffModeEnableEvent;
import community.leaf.survival.staffmode.events.StaffModeToggleRequestEvent;
import community.leaf.survival.staffmode.snapshots.GameplaySnapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import pl.tlinkowski.annotation.basic.NullOr;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class StaffModeProfile implements StaffMember
{
	public interface Dependencies
	{
		void updated();
		
		SnapshotSource<GameplaySnapshot> snapshot();
		
		ConfigurationSection profileDataSection(UUID uuid);
	}
	
	private static final YamlValue<String> META_MODE = YamlValue.ofString("meta.mode").maybe();
	
	private static final YamlValue<Instant> META_TIMESTAMP = YamlValue.ofInstant("meta.timestamp").maybe();
	
	private final Dependencies core;
	private final UUID uuid;
	
	public StaffModeProfile(Dependencies core, UUID uuid)
	{
		this.core = core;
		this.uuid = uuid;
	}
	
	private ConfigurationSection profileDataSection() { return core.profileDataSection(uuid); }
	
	@Override
	public UUID uuid() { return uuid; }
	
	@Override
	public Optional<Instant> sinceLastToggle()
	{
		return META_TIMESTAMP.get(profileDataSection());
	}
	
	@Override
	public Optional<Mode> lastToggledMode()
	{
		return META_MODE.get(profileDataSection()).flatMap(Mode.adapter()::deserialize);
	}
	
	@Override
	public Optional<GameplaySnapshot> capture()
	{
		@NullOr Player player = player().orElse(null);
		if (player == null) { return Optional.empty(); }
		
		Mode mode = activeMode();
		GameplaySnapshot saved = core.snapshot().capture(new SnapshotContext(player, mode));
		
		core.snapshot().set(profileDataSection(), mode.name(), saved);
		core.updated();
		
		return Optional.of(saved);
	}
	
	@Override
	public Optional<GameplaySnapshot> snapshot(Mode mode)
	{
		return core.snapshot().get(profileDataSection(), mode.name());
	}
	
	@Override
	public Mode activeMode()
	{
		if (player().filter(Permissions.STAFF_MODE_ENABLED::allows).isPresent()) { return Mode.STAFF; }
		return lastToggledMode().orElse(Mode.SURVIVAL);
	}
	
	@Override
	public void mode(Mode mode)
	{
		if (mode == activeMode()) { return; }
		
		@NullOr SnapshotContext context = player()
			.map(player -> new SnapshotContext(player, mode))
			.orElse(null);
		
		if (context == null) { return; }
		
		// Check whether player can toggle their staff mode (abort if cancelled)
		if (Events.dispatcher().call(new StaffModeToggleRequestEvent(this, context)).isCancelled()) { return; }
		
		// Capture and save current gameplay state
		capture();
		
		// Set updated mode
		ConfigurationSection data = profileDataSection();
		
		META_MODE.set(data, mode.name());
		META_TIMESTAMP.set(data, Instant.now());
		
		core.updated();
		
		// Restore toggled mode's gameplay state
		@NullOr GameplaySnapshot restored = snapshot(mode).orElse(null);
		
		if (restored != null) { restored.apply(context); }
		else if (mode == Mode.STAFF) { GameplaySnapshot.RESPAWN.apply(context); }
		
		// Dispatch enable/disable event
		Event event = switch (mode) {
			case STAFF -> new StaffModeEnableEvent(this, context);
			case SURVIVAL -> new StaffModeDisableEvent(this, context);
		};
		
		Events.dispatcher().call(event);
	}
}
