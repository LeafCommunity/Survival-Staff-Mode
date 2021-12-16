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
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlayerGameplaySnapshot implements Snapshot
{
	private static final YamlValue<Instant> UPDATED = YamlValue.ofInstant("updated").maybe();
	
	public static YamlAccessor<PlayerGameplaySnapshot> yaml(SnapshotManager manager)
	{
		return new YamlAccessor<>()
		{
			@Override
			public Optional<PlayerGameplaySnapshot> get(ConfigurationSection storage, String key)
			{
				return Sections.get(storage, key).map(data ->
				{
					@NullOr Instant updated = UPDATED.get(data).orElse(null);
					if (updated == null) { return null; }
					
					@NullOr ConfigurationSection section = Sections.get(data, "snapshots").orElse(null);
					if (section == null) { return null; }
					
					List<Snapshot> snapshots = new ArrayList<>();
					
					for (String sectionKey : section.getKeys(false))
					{
						@NullOr NamespacedKey namespacedKey = NamespacedKey.fromString(sectionKey);
						if (namespacedKey == null) { continue; }
						
						manager.registrationByKey(namespacedKey)
							.flatMap(registration -> registration.yaml().get(section, sectionKey))
							.ifPresent(snapshots::add);
					}
					
					return (snapshots.isEmpty()) ? null : new PlayerGameplaySnapshot(updated, snapshots);
				});
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void set(ConfigurationSection storage, String key, @NullOr PlayerGameplaySnapshot updated)
			{
				if (updated == null)
				{
					storage.set(key, null);
					return;
				}
				
				ConfigurationSection data = Sections.getOrCreate(storage, key);
				UPDATED.set(data, updated.updated);
				
				ConfigurationSection section = Sections.getOrCreate(data, "snapshots");
				
				for (Snapshot snapshot : updated.snapshotsByType.values())
				{
					@NullOr Registration<Snapshot> registration =
						(Registration<Snapshot>) manager.registrationByType(snapshot.getClass()).orElse(null);
					
					if (registration == null) { continue; } // TODO: throw instead of silently fail?
					
					registration.yaml().set(section, registration.key().toString(), snapshot);
				}
			}
		};
	}
	
	public static PlayerGameplaySnapshot of(List<Snapshot> snapshots)
	{
		return new PlayerGameplaySnapshot(Instant.now(), snapshots);
	}
	
	private final Map<Class<? extends Snapshot>, Snapshot> snapshotsByType = new LinkedHashMap<>();
	
	private final Instant updated;
	
	public PlayerGameplaySnapshot(Instant updated, List<Snapshot> snapshots)
	{
		this.updated = updated;
		for (Snapshot snapshot : snapshots) { snapshotsByType.put(snapshot.getClass(), snapshot); }
	}
	
	@Override
	public void apply(Player player)
	{
		snapshotsByType.values().forEach(snapshot ->
		{
			try { snapshot.apply(player); }
			catch (RuntimeException e) { e.printStackTrace(); } // TODO: send message to player?
		});
	}
	
	@SuppressWarnings("unchecked")
	private <S extends Snapshot> Optional<S> snapshotByType(Class<S> type)
	{
		return Optional.ofNullable((S) snapshotsByType.get(type));
	}
}
