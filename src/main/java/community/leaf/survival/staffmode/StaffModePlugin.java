/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import com.github.zafarkhaja.semver.Version;
import community.leaf.eventful.bukkit.BukkitEventSource;
import community.leaf.survival.staffmode.configs.StaffModeConfig;
import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class StaffModePlugin extends JavaPlugin implements BukkitEventSource, BukkitTaskSource
{
	public static final int BSTATS = 13608;
	
	private final Path directory;
	private final Path backups;
	private final Version version;
	private final StaffModeConfig config;
	
	public StaffModePlugin()
	{
		this.directory = getDataFolder().toPath();
		this.backups = directory.resolve("backups");
		this.version = Version.valueOf(getDescription().getVersion());
		
		getLogger().info("Initializing v" + version);
		
		this.config = new StaffModeConfig(this);
	}
	
	@Override
	public Plugin plugin() { return this; }
	
	public Path directory() { return directory; }
	
	public Path backups() { return backups; }
	
	public Version version() { return version; }
	
	public StaffModeConfig config() { return config; }
	
	@Override
	public void onEnable()
	{
		if (config.getOrDefault(StaffModeConfig.METRICS_ENABLED))
		{
			Metrics metrics = new Metrics(this, BSTATS);
			// TODO: add more charts
		}
	}
}
