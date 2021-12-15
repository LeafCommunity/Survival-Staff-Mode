/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots;

import community.leaf.configvalues.bukkit.YamlAccessor;
import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.util.Sections;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.time.Instant;
import java.util.Optional;

public record PlayerSnapshot(Instant updated, StatsSnapshot stats, InventorySnapshot inventory) implements Snapshot
{
	private static final YamlValue<Instant> UPDATED = YamlValue.ofInstant("updated").maybe();
	
	private static final YamlValue<StatsSnapshot> STATS = YamlValue.of("stats", StatsSnapshot.YAML).maybe();
	
	private static final YamlValue<InventorySnapshot> INVENTORY = YamlValue.of("inventory", InventorySnapshot.YAML).maybe();
	
	public static final YamlAccessor<PlayerSnapshot> YAML =
		new YamlAccessor<>()
		{
			@Override
			public Optional<PlayerSnapshot> get(ConfigurationSection storage, String key)
			{
				return Sections.get(storage, key).map(data ->
				{
					Optional<Instant> updated = UPDATED.get(data);
					if (updated.isEmpty()) { return null; }
					
					Optional<StatsSnapshot> stats = STATS.get(data);
					if (stats.isEmpty()) { return null; }
					
					Optional<InventorySnapshot> inventory = INVENTORY.get(data);
					if (inventory.isEmpty()) { return null; }
					
					return new PlayerSnapshot(updated.get(), stats.get(), inventory.get());
				});
			}
			
			@Override
			public void set(ConfigurationSection storage, String key, @NullOr PlayerSnapshot updated)
			{
				if (updated == null)
				{
					storage.set(key, null);
					return;
				}
				
				ConfigurationSection data = Sections.getOrCreate(storage, key);
				
				UPDATED.set(data, updated.updated);
				STATS.set(data, updated.stats);
				INVENTORY.set(data, updated.inventory);
			}
		};
	
	public static PlayerSnapshot of(Player player)
	{
		return new PlayerSnapshot(Instant.now(), StatsSnapshot.of(player), InventorySnapshot.of(player));
	}
	
	@Override
	public void apply(Player player)
	{
		stats.apply(player);
		inventory.apply(player);
	}
}
