/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots;

import com.rezzedup.util.valuables.Adapter;
import community.leaf.configvalues.bukkit.YamlAccessor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PotionsSnapshot implements Snapshot
{
	private static final Adapter<Map<String, Object>, PotionEffect> POTION_EFFECT =
		Adapter.of(
			map -> {
				try { return Optional.of(new PotionEffect(map)); }
				catch (RuntimeException ignored) { return Optional.empty(); }
			},
			effect -> Optional.of(effect.serialize())
		);
	
	public static final PotionsSnapshot EMPTY = new PotionsSnapshot(List.of());
	
	public static final YamlAccessor<PotionsSnapshot> YAML =
		new YamlAccessor<>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Optional<PotionsSnapshot> get(ConfigurationSection storage, String key)
			{
				List<Map<?, ?>> data = storage.getMapList(key);
				
				return (data.isEmpty())
					? Optional.of(EMPTY)
				   	: Optional.of(new PotionsSnapshot(
						data.stream()
							.map(map -> (Map<String, Object>) map)
							.map(POTION_EFFECT::deserialize)
							.flatMap(Optional::stream)
							.toList()
					));
			}
			
			@Override
			public void set(ConfigurationSection storage, String key, @NullOr PotionsSnapshot updated)
			{
				if (updated == null || updated.effects.isEmpty()) { storage.set(key, null); }
				else { storage.set(key, updated.effects.stream().map(PotionEffect::serialize).toList()); }
			}
		};
	
	public static PotionsSnapshot of(Player player)
	{
		return new PotionsSnapshot(List.copyOf(player.getActivePotionEffects()));
	}
	
	private final List<PotionEffect> effects;
	
	public PotionsSnapshot(List<PotionEffect> effects)
	{
		this.effects = List.copyOf(effects);
	}
	
	@Override
	public void apply(Player player)
	{
		List.copyOf(player.getActivePotionEffects()).stream()
			.map(PotionEffect::getType)
			.forEach(player::removePotionEffect);
		
		player.addPotionEffects(effects);
	}
}
