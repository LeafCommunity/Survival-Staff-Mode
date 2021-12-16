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
import community.leaf.survival.staffmode.commands.TestSnapshotsCommand;
import community.leaf.survival.staffmode.configs.StaffModeConfig;
import community.leaf.survival.staffmode.snapshots.SnapshotRegistry;
import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.tlinkowski.annotation.basic.NullOr;

import java.nio.file.Path;
import java.util.function.Consumer;

public class StaffModePlugin extends JavaPlugin implements BukkitEventSource, BukkitTaskSource
{
	public static final int BSTATS = 13608;
	
	private final Path directory;
	private final Path backups;
	private final Version version;
	private final StaffModeConfig config;
	private final SnapshotRegistry registry;
	
	public StaffModePlugin()
	{
		this.directory = getDataFolder().toPath();
		this.backups = directory.resolve("backups");
		this.version = Version.valueOf(getDescription().getVersion());
		
		getLogger().info("Initializing v" + version);
		
		this.config = new StaffModeConfig(this);
		this.registry = new SnapshotRegistry(this);
	}
	
	@Override
	public Plugin plugin() { return this; }
	
	public Path directory() { return directory; }
	
	public Path backups() { return backups; }
	
	public Version version() { return version; }
	
	public StaffModeConfig config() { return config; }
	
	public NamespacedKey key(String key) { return new NamespacedKey(this, key); }
	
	public SnapshotRegistry registry() { return registry; }
	
	@Override
	public void onEnable()
	{
		if (config.getOrDefault(StaffModeConfig.METRICS_ENABLED))
		{
			Metrics metrics = new Metrics(this, BSTATS);
			// TODO: add more charts
		}
		
		getLogger().warning("ADDING TEST COMMANDS!");
		command("test-snapshots", new TestSnapshotsCommand(this));
	}
	
	private void command(String name, CommandExecutor executor)
	{
		@NullOr PluginCommand command = getCommand(name);
		if (command == null) { throw new IllegalArgumentException("Undefined command: " + name); }
		
		command.setExecutor(executor);
		if (executor instanceof TabCompleter tab) { command.setTabCompleter(tab); }
	}
}
