/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
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
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Optional;

public record StatsSnapshot(int level, float exp, double health, int hunger, float saturation, int insomnia) implements Snapshot
{
	private static final YamlValue<Integer> LEVEL = YamlValue.ofInteger("level").maybe();
	
	private static final YamlValue<Float> EXP = YamlValue.ofFloat("exp").maybe();
	
	private static final YamlValue<Double> HEALTH = YamlValue.ofDouble("health").maybe();
	
	private static final YamlValue<Integer> HUNGER = YamlValue.ofInteger("hunger").maybe();
	
	private static final YamlValue<Float> SATURATION = YamlValue.ofFloat("saturation").maybe();
	
	private static final YamlValue<Integer> INSOMNIA = YamlValue.ofInteger("insomnia").maybe();
	
	public static final SnapshotSource<StatsSnapshot> SOURCE =
		new SnapshotSource<>()
		{
			@Override
			public boolean isApplicable(SnapshotContext context) { return context.mode() == Mode.SURVIVAL; }
			
			@Override
			public StatsSnapshot capture(SnapshotContext context) { return of(context.player()); }
			
			@Override
			public Optional<StatsSnapshot> get(ConfigurationSection storage, String key)
			{
				return Sections.get(storage, key).map(data ->
					new StatsSnapshot(
						LEVEL.get(data).orElse(HEALTHY.level),
						EXP.get(data).orElse(HEALTHY.exp),
						HEALTH.get(data).orElse(HEALTHY.health),
						HUNGER.get(data).orElse(HEALTHY.hunger),
						SATURATION.get(data).orElse(HEALTHY.saturation),
						INSOMNIA.get(data).orElse(HEALTHY.insomnia)
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
				INSOMNIA.set(data, updated.insomnia);
			}
		};
	
	public static final StatsSnapshot HEALTHY = new StatsSnapshot(0, 0.0F, 20.0, 20, 5F, 0);
	
	public static StatsSnapshot of(Player player)
	{
		return new StatsSnapshot(
			player.getLevel(),
			player.getExp(),
			player.getHealth(),
			player.getFoodLevel(),
			player.getSaturation(),
			player.getStatistic(Statistic.TIME_SINCE_REST)
		);
	}
	
	@Override
	public void apply(SnapshotContext context)
	{
		context.player().setLevel(level);
		context.player().setExp(exp);
		context.player().setHealth(health);
		context.player().setFoodLevel(hunger);
		context.player().setSaturation(saturation);
		context.player().setStatistic(Statistic.TIME_SINCE_REST, insomnia);
	}
}
