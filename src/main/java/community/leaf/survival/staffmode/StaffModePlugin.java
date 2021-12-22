/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
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
import community.leaf.textchain.platforms.bukkit.BukkitTextChainSource;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.tlinkowski.annotation.basic.NullOr;

import java.nio.file.Path;
import java.util.logging.Level;

public final class StaffModePlugin extends JavaPlugin implements BukkitEventSource, BukkitTaskSource, BukkitTextChainSource, StaffModeAPI
{
	public static final int BSTATS = 13608;
	
	private final Path directory;
	private final Path backups;
	private final Version version;
	private final StaffModeConfig config;
	private final SnapshotRegistry snapshots;
	private final StaffModeManager staff;
	
	private @NullOr BukkitAudiences adventure;
	
	public StaffModePlugin()
	{
		this.directory = getDataFolder().toPath();
		this.backups = directory.resolve("backups");
		this.version = Version.valueOf(getDescription().getVersion());
		
		getLogger().info("Initializing " + getName() + " v" + version);
		
		this.config = new StaffModeConfig(this);
		this.snapshots = new SnapshotRegistry(this);
		this.staff = new StaffModeManager(this);
	}
	
	private <T> T initialized(@NullOr T thing)
	{
		if (thing != null) { return thing; }
		throw new IllegalStateException();
	}
	
	@Override
	public Plugin plugin() { return this; }
	
	@Override
	public BukkitAudiences adventure() { return initialized(adventure); }
	
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
	public void onLoad()
	{
		PaperLib.suggestPaper(this, Level.WARNING);
	}
	
	@Override
	public void onEnable()
	{
		this.adventure = BukkitAudiences.create(this);
		
		staff.loadDataFromDisk();
		
		events().register(new StaffCommandListener(this));
		events().register(new StaffModeInteractionListener(this));
		events().register(new StaffSessionListener(this));
		
		if (PaperLib.isPaper())
		{
			events().register(new StaffModeInteractionListener.Paper(this));
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
		if (this.adventure != null)
		{
			this.adventure.close();
			this.adventure = null;
		}
		
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
