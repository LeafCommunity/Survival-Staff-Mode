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

import java.util.Optional;

public record StatsSnapshot(int level, float exp, double health, int hunger, float saturation) implements Snapshot
{
	private static final YamlValue<Integer> LEVEL = YamlValue.ofInteger("level").maybe();
	
	private static final YamlValue<Float> EXP = YamlValue.ofFloat("exp").maybe();
	
	private static final YamlValue<Double> HEALTH = YamlValue.ofDouble("health").maybe();
	
	private static final YamlValue<Integer> HUNGER = YamlValue.ofInteger("hunger").maybe();
	
	private static final YamlValue<Float> SATURATION = YamlValue.ofFloat("saturation").maybe();
	
	public static final YamlAccessor<StatsSnapshot> YAML =
		new YamlAccessor<>()
		{
			@Override
			public Optional<StatsSnapshot> get(ConfigurationSection storage, String key)
			{
				return Sections.get(storage, key).map(data ->
					new StatsSnapshot(
						LEVEL.get(data).orElse(0),
						EXP.get(data).orElse(0.0F),
						HEALTH.get(data).orElse(20.0),
						HUNGER.get(data).orElse(0),
						SATURATION.get(data).orElse(0.0F)
					)
				);
			}
			
			@Override
			public void set(ConfigurationSection storage, String key, @NullOr StatsSnapshot updated)
			{
				if (updated == null)
				{
					storage.set(key, null);
					return;
				}
				
				ConfigurationSection data = Sections.getOrCreate(storage, key);
				
				LEVEL.set(data, updated.level);
				EXP.set(data, updated.exp);
				HEALTH.set(data, updated.health);
				HUNGER.set(data, updated.hunger);
				SATURATION.set(data, updated.saturation);
			}
		};
	
	public static StatsSnapshot of(Player player)
	{
		return new StatsSnapshot(
		player.getLevel(), player.getExp(), player.getHealth(), player.getFoodLevel(), player.getSaturation()
		);
	}
	
	@Override
	public void apply(Player player)
	{
		player.setLevel(level);
		player.setExp(exp);
		player.setHealth(health);
		player.setFoodLevel(hunger);
		player.setSaturation(saturation);
	}
}
