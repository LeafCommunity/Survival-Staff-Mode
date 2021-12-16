/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots;

import community.leaf.survival.staffmode.StaffModePlugin;
import org.bukkit.NamespacedKey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class SnapshotRegistry
{
	public record Registration<S extends Snapshot>(Class<S> type, NamespacedKey key, SnapshotSource<S> source) {}
	
	private final Map<NamespacedKey, Registration<?>> registrationsByKey = new LinkedHashMap<>();
	private final Map<Class<? extends Snapshot>, Registration<?>> registrationsByType = new LinkedHashMap<>();
	
	public SnapshotRegistry(StaffModePlugin plugin)
	{
		// Register default types
		register(StatsSnapshot.class, plugin.key("stats"), StatsSnapshot.SOURCE);
		register(PositionSnapshot.class, plugin.key("position"), PositionSnapshot.SOURCE);
		register(InventorySnapshot.class, plugin.key("inventory"), InventorySnapshot.SOURCE);
		register(PotionEffectsSnapshot.class, plugin.key("potion-effects"), PotionEffectsSnapshot.SOURCE);
	}
	
	public <S extends Snapshot> void register(Class<S> type, NamespacedKey key, SnapshotSource<S> source)
	{
		register(new Registration<>(type, key, source));
	}
	
	public <S extends Snapshot> void register(Registration<S> registration)
	{
		if (Snapshot.DoNotRegister.class.isAssignableFrom(registration.type()))
		{
			throw new IllegalArgumentException(
				"Type explicitly cannot be registered (Snapshot.DoNotRegister): " + registration.type()
			);
		}
		
		registrationsByKey.put(registration.key(), registration);
		registrationsByType.put(registration.type(), registration);
	}
	
	public Stream<Registration<?>> streamAllRegistrations()
	{
		return registrationsByKey.values().stream();
	}
	
	public Optional<Registration<?>> registrationByKey(NamespacedKey key)
	{
		return Optional.ofNullable(registrationsByKey.get(key));
	}
	
	@SuppressWarnings("unchecked")
	public <S extends Snapshot> Optional<Registration<S>> registrationByType(Class<S> type)
	{
		return Optional.ofNullable((Registration<S>) registrationsByType.get(type));
	}
}
