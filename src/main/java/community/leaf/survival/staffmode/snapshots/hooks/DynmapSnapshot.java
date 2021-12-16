/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots.hooks;

import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.util.Sections;
import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.configs.StaffModeConfig;
import community.leaf.survival.staffmode.snapshots.Snapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Optional;

public record DynmapSnapshot(boolean visibility) implements Snapshot
{
	private static final String DYNMAP_PLUGIN_NAME = "dynmap";
	
	private static final YamlValue<Boolean> VISIBILITY = YamlValue.ofBoolean("visibility").maybe();
	
	public static SnapshotSource<DynmapSnapshot> source(StaffModeConfig config)
	{
		// TODO: use config to determine applicability
		return new SnapshotSource<>()
		{
			@Override
			public boolean isApplicable(SnapshotContext context)
			{
				return context.mode() == Mode.SURVIVAL
					&& Bukkit.getPluginManager().isPluginEnabled(DYNMAP_PLUGIN_NAME);
			}
			
			@Override
			public DynmapSnapshot capture(SnapshotContext context)
			{
				@NullOr DynmapCommonAPI dynmap = dynmap();
				if (dynmap == null) { throw new IllegalStateException(DYNMAP_PLUGIN_NAME + " is not loaded."); }
				return new DynmapSnapshot(dynmap.getPlayerVisbility(context.player().getName()));
			}
			
			@Override
			public Optional<DynmapSnapshot> get(ConfigurationSection storage, String key)
			{
				return Sections.get(storage, key).flatMap(data -> VISIBILITY.get(data).map(DynmapSnapshot::new));
			}
			
			@Override
			public void set(ConfigurationSection storage, String key, @NullOr DynmapSnapshot updated)
			{
				if (updated == null)
				{
					storage.set(key, null);
					return;
				}
			
				ConfigurationSection data = Sections.getOrCreate(storage, key);
				VISIBILITY.set(data, updated.visibility);
			}
		};
	}
	
	private static @NullOr DynmapCommonAPI dynmap()
	{
		@NullOr Plugin plugin = Bukkit.getPluginManager().getPlugin("dynmap");
		return (plugin == null) ? null : (DynmapCommonAPI) plugin;
	}
	
	@Override
	public void apply(SnapshotContext context)
	{
		@NullOr DynmapCommonAPI dynmap = dynmap();
		if (dynmap == null) { return; }
		
		// still using player names as IDs in the year of our lord :face_palm:
		dynmap.setPlayerVisiblity(context.player().getName(), visibility);
	}
}
