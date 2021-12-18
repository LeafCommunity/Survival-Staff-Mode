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
import community.leaf.survival.staffmode.commands.StaffModeCommand;
import community.leaf.survival.staffmode.configs.StaffModeConfig;
import community.leaf.survival.staffmode.listeners.StaffCommandListener;
import community.leaf.survival.staffmode.listeners.StaffModeInteractionListener;
import community.leaf.survival.staffmode.listeners.StaffSessionListener;
import community.leaf.survival.staffmode.snapshots.SnapshotRegistry;
import community.leaf.tasks.Concurrency;
import community.leaf.tasks.bukkit.BukkitTaskSource;
import io.papermc.lib.PaperLib;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.tlinkowski.annotation.basic.NullOr;

import java.nio.file.Path;

public final class StaffModePlugin extends JavaPlugin implements BukkitEventSource, BukkitTaskSource, StaffModeAPI
{
	public static final int BSTATS = 13608;
	
	private final Path directory;
	private final Path backups;
	private final Version version;
	private final StaffModeConfig config;
	private final SnapshotRegistry snapshots;
	private final StaffModeManager staff;
	
	public StaffModePlugin()
	{
		this.directory = getDataFolder().toPath();
		this.backups = directory.resolve("backups");
		this.version = Version.valueOf(getDescription().getVersion());
		
		getLogger().info("Initializing v" + version);
		
		this.config = new StaffModeConfig(this);
		this.snapshots = new SnapshotRegistry(this);
		this.staff = new StaffModeManager(this);
	}
	
	@Override
	public Plugin plugin() { return this; }
	
	public Path directory() { return directory; }
	
	public Path backups() { return backups; }
	
	@Override
	public Version version() { return version; }
	
	public StaffModeConfig config() { return config; }
	
	public NamespacedKey key(String key) { return new NamespacedKey(this, key); }
	
	@Override
	public SnapshotRegistry snapshots() { return snapshots; }
	
	@Override
	public StaffModeManager staff() { return staff; }
	
	@Override
	public void onEnable()
	{
		staff.loadDataFromDisk();
		
		events().register(new StaffCommandListener(this));
		events().register(new StaffModeInteractionListener(this));
		events().register(new StaffSessionListener(this));
		
		if (PaperLib.isPaper())
		{
			events().register(new StaffModeInteractionListener.Paper(this));
		}
		else
		{
			PaperLib.suggestPaper(this);
		}
		
		command("staffmode", new StaffModeCommand(this));
		
		if (config.getOrDefault(StaffModeConfig.METRICS_ENABLED))
		{
			Metrics metrics = new Metrics(this, BSTATS);
			// TODO: add more charts
		}
	}
	
	@Override
	public void onDisable()
	{
		staff.saveIfUpdated(Concurrency.SYNC);
	}
	
	private void command(String name, CommandExecutor executor)
	{
		@NullOr PluginCommand command = getCommand(name);
		if (command == null) { throw new IllegalArgumentException("Undefined command: " + name); }
		
		command.setExecutor(executor);
		if (executor instanceof TabCompleter tab) { command.setTabCompleter(tab); }
	}
}
