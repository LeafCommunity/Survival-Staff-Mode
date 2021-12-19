/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots.defaults;

import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.util.Sections;
import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.snapshots.Snapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Optional;

public record FlightSnapshot(boolean isAllowed, boolean isFlying) implements Snapshot
{
	private static final YamlValue<Boolean> ALLOWED = YamlValue.ofBoolean("allowed").maybe();
	
	private static final YamlValue<Boolean> FLYING = YamlValue.ofBoolean("flying").maybe();
	
	public static final SnapshotSource<FlightSnapshot> SOURCE =
		new SnapshotSource<>()
		{
			@Override
			public boolean isApplicable(SnapshotContext context) { return context.mode() == Mode.SURVIVAL; }
			
			@Override
			public FlightSnapshot capture(SnapshotContext context) { return of(context.player()); }
			
			@Override
			public Optional<FlightSnapshot> get(ConfigurationSection storage, String key)
			{
				return Sections.get(storage, key).map(data ->
				{
					boolean isAllowed = ALLOWED.get(data).orElse(false);
					boolean isFlying = FLYING.get(data).orElse(false);
					
					return new FlightSnapshot(isAllowed, isFlying);
				});
			}
			
			@Override
			public void set(ConfigurationSection storage, String key, @NullOr FlightSnapshot updated)
			{
				if (updated == null)
				{
					storage.set(key, null);
					return;
				}
				
				ConfigurationSection data = Sections.getOrCreate(storage, key);
				
				ALLOWED.set(data, updated.isAllowed);
				FLYING.set(data, updated.isFlying);
			}
		};
	
	public static FlightSnapshot of(Player player)
	{
		return new FlightSnapshot(player.getAllowFlight(), player.isFlying());
	}
	
	@Override
	public void apply(SnapshotContext context)
	{
		context.player().setAllowFlight(isAllowed);
		context.player().setFlying(isFlying);
	}
}
