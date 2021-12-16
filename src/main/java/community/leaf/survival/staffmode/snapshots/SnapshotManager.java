/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots;

import community.leaf.configvalues.bukkit.YamlAccessor;
import community.leaf.survival.staffmode.StaffModePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class SnapshotManager
{
	private final Map<NamespacedKey, Snapshot.Registration<?>> registrationsByKey = new LinkedHashMap<>();
	private final Map<Class<? extends Snapshot>, Snapshot.Registration<?>> registrationsByType = new LinkedHashMap<>();
	
	private final YamlAccessor<PlayerGameplaySnapshot> playerSnapshotYamlAccessor;
	
	public SnapshotManager(StaffModePlugin plugin)
	{
		this.playerSnapshotYamlAccessor = PlayerGameplaySnapshot.yaml(this);
		
		// Register default types
		register(StatsSnapshot.class, plugin.key("stats"), StatsSnapshot.YAML, StatsSnapshot::of);
		register(PositionSnapshot.class, plugin.key("position"), PositionSnapshot.YAML, PositionSnapshot::of);
		register(InventorySnapshot.class, plugin.key("inventory"), InventorySnapshot.YAML, InventorySnapshot::of);
		register(PotionsSnapshot.class, plugin.key("potion-effects"), PotionsSnapshot.YAML, PotionsSnapshot::of);
	}
	
	public <S extends Snapshot> void register(Class<S> type, NamespacedKey key, YamlAccessor<S> yaml, Function<Player, S> constructor)
	{
		register(Snapshot.Registration.of(type, key, yaml, constructor));
	}
	
	public <S extends Snapshot> void register(Snapshot.Registration<S> registration)
	{
		if (PlayerGameplaySnapshot.class.isAssignableFrom(registration.type()))
		{
			throw new IllegalArgumentException("Cannot register type: " + PlayerGameplaySnapshot.class);
		}
		
		registrationsByKey.put(registration.key(), registration);
		registrationsByType.put(registration.type(), registration);
	}
	
	public Optional<Snapshot.Registration<?>> registrationByKey(NamespacedKey key)
	{
		return Optional.ofNullable(registrationsByKey.get(key));
	}
	
	@SuppressWarnings("unchecked")
	public <S extends Snapshot> Optional<Snapshot.Registration<S>> registrationByType(Class<S> type)
	{
		return Optional.ofNullable((Snapshot.Registration<S>) registrationsByType.get(type));
	}
	
	public YamlAccessor<PlayerGameplaySnapshot> yaml() { return playerSnapshotYamlAccessor; }
	
	public PlayerGameplaySnapshot capture(Player player)
	{
		List<Snapshot> snapshots = new ArrayList<>();
		
		for (Snapshot.Registration<?> registration : registrationsByKey.values())
		{
			if (registration.isApplicable(player)) { snapshots.add(registration.capture(player)); }
		}
		
		return PlayerGameplaySnapshot.of(snapshots);
	}
}
