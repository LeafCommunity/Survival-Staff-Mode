/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.survival.staffmode.snapshots.GameplaySnapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class StaffModeProfile implements StaffMember
{
	private static final YamlValue<String> META_MODE = YamlValue.ofString("meta.mode").maybe();
	
	private static final YamlValue<Instant> META_TIMESTAMP = YamlValue.ofInstant("meta.timestamp").maybe();
	
	private final StaffModeManager.Dependencies core;
	private final UUID uuid;
	
	public StaffModeProfile(StaffModeManager.Dependencies core, UUID uuid)
	{
		this.core = core;
		this.uuid = uuid;
	}
	
	private ConfigurationSection profileDataSection() { return core.profileDataSection(uuid); }
	
	@Override
	public UUID uuid() { return uuid; }
	
	@Override
	public Mode mode()
	{
		if (player().filter(Permissions.STAFF_MODE_ENABLED::allows).isPresent()) { return Mode.STAFF; }
		
		return META_MODE.get(profileDataSection())
			.flatMap(Mode.adapter()::deserialize)
			.orElse(Mode.SURVIVAL);
	}
	
	@Override
	public Optional<Instant> sinceLastToggle()
	{
		return META_TIMESTAMP.get(profileDataSection());
	}
	
	public Optional<GameplaySnapshot> capture()
	{
		@NullOr Player player = player().orElse(null);
		if (player == null) { return Optional.empty(); }
		
		Mode mode = mode();
		GameplaySnapshot saved = core.snapshot().capture(new SnapshotContext(player, mode));
		
		core.snapshot().set(profileDataSection(), mode.name(), saved);
		core.manager().updated(true);
		
		return Optional.of(saved);
	}
	
	public Optional<GameplaySnapshot> snapshot(Mode mode)
	{
		return core.snapshot().get(profileDataSection(), mode.name());
	}
	
	@Override
	public void mode(Mode mode)
	{
		Mode current = mode();
		if (mode == current) { return; }
		
		@NullOr Player player = player().orElse(null);
		if (player == null) { return; }
		
		ConfigurationSection data = profileDataSection();
		
		META_MODE.set(data, mode.name());
		META_TIMESTAMP.set(data, Instant.now());
		
		// Capture and save current gameplay state
		GameplaySnapshot saved = core.snapshot().capture(new SnapshotContext(player, current));
		core.snapshot().set(data, current.name(), saved);
		
		// Restore toggled mode's gameplay state
		@NullOr GameplaySnapshot restored = core.snapshot().get(data, mode.name()).orElse(null);
		SnapshotContext context = new SnapshotContext(player, mode);
		
		if (restored != null) { restored.apply(context); }
		else if (mode == Mode.STAFF) { GameplaySnapshot.RESET.apply(context); }
		
		core.manager().updated(true);
	}
}
