/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.util.Sections;
import community.leaf.eventful.bukkit.Events;
import community.leaf.survival.staffmode.events.StaffModeDisableEvent;
import community.leaf.survival.staffmode.events.StaffModeEnableEvent;
import community.leaf.survival.staffmode.events.StaffModeToggleRequestEvent;
import community.leaf.survival.staffmode.snapshots.GameplaySnapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import pl.tlinkowski.annotation.basic.NullOr;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
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
	
	private static final YamlValue<String> META_NAME = YamlValue.ofString("meta.name").maybe();
	
	private static final YamlValue<String> META_MODE = YamlValue.ofString("meta.mode").maybe();
	
	private static final YamlValue<Instant> META_TOGGLE_TIMESTAMP = YamlValue.ofInstant("meta.toggle").maybe();
	
	private static final YamlValue<Instant> NIGHT_VISION_SETTING = YamlValue.ofInstant("meta.settings.night-vision").maybe();
	
	private static final YamlValue<String> SPECTATOR_SETTING = YamlValue.ofString("meta.settings.spectator").maybe();
	
	private final Map<Mode, GameplaySnapshot> snapshotsCache = new EnumMap<>(Mode.class);
	
	private final Dependencies core;
	private final UUID uuid;
	
	public StaffModeProfile(Dependencies core, UUID uuid)
	{
		this.core = core;
		this.uuid = uuid;
	}
	
	private ConfigurationSection profileDataSection() { return core.profileDataSection(uuid); }
	
	private ConfigurationSection modesDataSection() { return Sections.getOrCreate(profileDataSection(), "modes"); }
	
	private void validateReceivedPlayer(Player player)
	{
		if (!uuid.equals(player.getUniqueId()))
		{
			throw new IllegalArgumentException(
				"Expected player with UUID " + uuid  + " but instead received: " +
				player.getUniqueId() + " (" +player.getName() + ")"
			);
		}
	}
	
	private void validateReceivedContext(SnapshotContext context)
	{
		validateReceivedPlayer(context.player());
	}
	
	public void updateMetaData()
	{
		player().map(Player::getName).ifPresent(name -> META_NAME.set(profileDataSection(), name));
	}
	
	@Override
	public UUID uuid() { return uuid; }
	
	@Override
	public Optional<Instant> sinceLastToggle()
	{
		return META_TOGGLE_TIMESTAMP.get(profileDataSection());
	}
	
	@Override
	public Optional<Mode> lastToggledMode()
	{
		return META_MODE.get(profileDataSection()).flatMap(Mode.adapter()::deserialize);
	}
	
	public GameplaySnapshot forceCaptureSnapshot(SnapshotContext context)
	{
		validateReceivedContext(context);
		
		GameplaySnapshot saved = core.snapshot().capture(context);
		ConfigurationSection data = modesDataSection();
		
		// Delete old data first to completely overwrite the snapshot
		data.set(context.mode().name(), null);
		
		// Save captured snapshot
		core.snapshot().set(data, context.mode().name(), saved);
		core.updated();
		
		// Update cached entry
		snapshotsCache.put(context.mode(), saved);
		
		return saved;
	}
	
	@Override
	public Optional<GameplaySnapshot> capture()
	{
		return player().map(player -> forceCaptureSnapshot(new SnapshotContext(player, effectiveActiveMode(player))));
	}
	
	@Override
	public Optional<GameplaySnapshot> snapshot(Mode mode)
	{
		@NullOr GameplaySnapshot cached = snapshotsCache.get(mode);
		if (cached != null) { return Optional.of(cached); }
		
		Optional<GameplaySnapshot> existing = core.snapshot().get(modesDataSection(), mode.name());
		existing.ifPresent(snapshot -> snapshotsCache.put(mode, snapshot));
		return existing;
	}
	
	private Mode effectiveActiveMode(Player player)
	{
		validateReceivedPlayer(player);
		return (Permissions.STAFF_MODE_ENABLED.allows(player)) ? Mode.STAFF : lastToggledMode().orElse(Mode.SURVIVAL);
	}
	
	@Override
	public Mode mode()
	{
		return player().map(this::effectiveActiveMode).orElse(Mode.SURVIVAL);
	}
	
	// Restore mode without capturing snapshot first
	public void forceRestoreSnapshot(SnapshotContext context)
	{
		validateReceivedContext(context);
		
		// Update meta with restored mode
		ConfigurationSection data = profileDataSection();
		
		META_MODE.set(data, context.mode().name());
		META_TOGGLE_TIMESTAMP.set(data, Instant.now());
		
		core.updated();
		
		// Restore toggled mode's gameplay state
		@NullOr GameplaySnapshot restored = snapshot(context.mode()).orElse(null);
		
		// Apply restored snapshot if it exists
		if (restored != null) { restored.apply(context); }
		// Otherwise, clear inventory/heal player if toggling staff mode for the first time
		else if (context.mode() == Mode.STAFF) { GameplaySnapshot.RESPAWN.apply(context); }
		
		// Dispatch enable/disable event
		Event event = switch (context.mode()) {
			case STAFF -> new StaffModeEnableEvent(this, context);
			case SURVIVAL -> new StaffModeDisableEvent(this, context);
		};
		
		Events.dispatcher().call(event);
	}
	
	@Override
	public ToggleSwitch mode(Mode mode)
	{
		@NullOr Player player = player().orElse(null);
		if (player == null) { return ToggleSwitch.FAILURE; }
		
		Mode current = effectiveActiveMode(player);
		if (mode == current) { return ToggleSwitch.ALREADY; }
		
		SnapshotContext context = new SnapshotContext(player, mode);
		StaffModeToggleRequestEvent request = new StaffModeToggleRequestEvent(this, context);
		
		// Check whether player can toggle their staff mode (abort if cancelled)
		if (Events.dispatcher().call(request).isCancelled()) { return ToggleSwitch.FAILURE; }
		
		// Capture and save current gameplay state
		forceCaptureSnapshot(new SnapshotContext(player, current));
		
		// Restore and apply snapshot from toggled mode
		forceRestoreSnapshot(context);
		return ToggleSwitch.SUCCESS;
	}
	
	public boolean nightVision()
	{
		return profileDataSection().contains(NIGHT_VISION_SETTING.key());
	}
	
	public void nightVision(boolean enabled)
	{
		if (enabled) { NIGHT_VISION_SETTING.set(profileDataSection(), Instant.now()); }
		else { NIGHT_VISION_SETTING.remove(profileDataSection()); }
		core.updated();
	}
	
	public boolean spectator()
	{
		return profileDataSection().contains(SPECTATOR_SETTING.key());
	}
	
	public void spectator(@NullOr GameMode priorGameMode)
	{
		if (priorGameMode == GameMode.SPECTATOR) { priorGameMode = null; }
		profileDataSection().set(SPECTATOR_SETTING.key(), priorGameMode);
		core.updated();
	}
	
	public GameMode gameModePriorToSpectator()
	{
		return SPECTATOR_SETTING.get(profileDataSection())
			.map(str -> {
				try { return GameMode.valueOf(str); }
				catch (RuntimeException ignored) { return null; }
			})
			.filter(mode -> mode != GameMode.SPECTATOR)
			.orElse(GameMode.SURVIVAL);
	}
}
