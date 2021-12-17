/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots;

import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.util.Sections;
import community.leaf.survival.staffmode.snapshots.defaults.InventorySnapshot;
import community.leaf.survival.staffmode.snapshots.defaults.PotionEffectsSnapshot;
import community.leaf.survival.staffmode.snapshots.defaults.StatsSnapshot;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import pl.tlinkowski.annotation.basic.NullOr;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class GameplaySnapshot implements Snapshot.DoNotRegister
{
	private static final YamlValue<Instant> UPDATED = YamlValue.ofInstant("updated").maybe();
	
	public static SnapshotSource<GameplaySnapshot> source(SnapshotRegistry registry)
	{
		return new SnapshotSource<>()
		{
			@Override
			public GameplaySnapshot capture(SnapshotContext context)
			{
				List<Snapshot> snapshots = registry.streamAllRegistrations()
					.map(SnapshotRegistry.Registration::source)
					.filter(source -> source.isApplicable(context))
					.flatMap(source -> {
						try { return Stream.<Snapshot>of(source.capture(context)); }
						catch (RuntimeException e) { e.printStackTrace(); }
						return Stream.empty();
					})
					.toList();
				
				return GameplaySnapshot.of(snapshots);
			}
			
			@Override
			public Optional<GameplaySnapshot> get(ConfigurationSection storage, String key)
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
						
						registry.registrationByKey(namespacedKey)
							.map(SnapshotRegistry.Registration::source)
							.flatMap(source -> {
								try { return source.get(section, sectionKey); }
								catch (RuntimeException e) { e.printStackTrace(); }
								return Optional.empty();
							})
							.ifPresent(snapshots::add);
					}
					
					return (snapshots.isEmpty()) ? null : new GameplaySnapshot(updated, snapshots);
				});
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void set(ConfigurationSection storage, String key, @NullOr GameplaySnapshot updated)
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
					registry.registrationByType(snapshot.getClass())
						.map(registration -> (SnapshotRegistry.Registration<Snapshot>) registration)
						.ifPresent(registration -> {
							try { registration.source().set(section, registration.key().toString(), snapshot); }
							catch (RuntimeException e) { e.printStackTrace(); }
						});
				}
			}
		};
	}
	
	// A reset state, almost as if respawning:
	public static final GameplaySnapshot RESET = of(List.of(
		StatsSnapshot.HEALTHY, InventorySnapshot.EMPTY, PotionEffectsSnapshot.EMPTY
	));
	
	public static GameplaySnapshot of(List<Snapshot> snapshots)
	{
		return new GameplaySnapshot(Instant.now(), snapshots);
	}
	
	private final Map<Class<? extends Snapshot>, Snapshot> snapshotsByType = new LinkedHashMap<>();
	
	private final Instant updated;
	
	public GameplaySnapshot(Instant updated, List<Snapshot> snapshots)
	{
		this.updated = updated;
		for (Snapshot snapshot : snapshots) { snapshotsByType.put(snapshot.getClass(), snapshot); }
	}
	
	@Override
	public void apply(SnapshotContext context)
	{
		snapshotsByType.values().forEach(snapshot ->
		{
			try { snapshot.apply(context); }
			catch (RuntimeException e) { e.printStackTrace(); } // TODO: send message to player?
		});
	}
	
	public Stream<Snapshot> streamAllSnapshots()
	{
		return snapshotsByType.values().stream();
	}
	
	@SuppressWarnings("unchecked")
	public <S extends Snapshot> Optional<S> snapshotByType(Class<S> type)
	{
		return Optional.ofNullable((S) snapshotsByType.get(type));
	}
}
