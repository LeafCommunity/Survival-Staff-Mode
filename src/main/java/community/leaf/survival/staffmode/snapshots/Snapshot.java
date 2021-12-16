/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots;

import community.leaf.configvalues.bukkit.YamlAccessor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Snapshot
{
	void apply(Player player);
	
	interface Registration<S extends Snapshot>
	{
		static <S extends Snapshot> Registration<S> of(
			Class<S> type,
			NamespacedKey key,
			YamlAccessor<S> yaml,
			Function<Player, S> capture
		)
		{
			return of(type, key, yaml, p -> true, capture);
		}
		
		static <S extends Snapshot> Registration<S> of(
			Class<S> type,
			NamespacedKey key,
			YamlAccessor<S> yaml,
			Predicate<Player> isApplicable,
			Function<Player, S> capture
		)
		{
			record Impl<S extends Snapshot>(
				Class<S> type,
				NamespacedKey key,
				YamlAccessor<S> yaml,
				Predicate<Player> isApplicable,
				Function<Player, S> capture
			) implements Registration<S>
			{
				@Override
				public boolean isApplicable(Player player) { return isApplicable().test(player); }
				
				@Override
				public S capture(Player player) { return capture().apply(player); }
			}
			
			return new Impl<>(type, key, yaml, isApplicable, capture);
		}
		
		Class<S> type();
		
		NamespacedKey key();
		
		YamlAccessor<S> yaml();
		
		boolean isApplicable(Player player);
		
		S capture(Player player);
	}
}
