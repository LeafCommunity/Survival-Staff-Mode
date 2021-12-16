/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.commands;

import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.StaffModePlugin;
import community.leaf.survival.staffmode.snapshots.GameplaySnapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.SnapshotRegistry;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;

public class TestSnapshotsCommand implements CommandExecutor
{
	private final StaffModePlugin plugin;
	private final SnapshotRegistry registry;
	private final SnapshotSource<GameplaySnapshot> gameplay;
	
	public TestSnapshotsCommand(StaffModePlugin plugin)
	{
		this.plugin = plugin;
		this.registry = new SnapshotRegistry(plugin);
		this.gameplay = GameplaySnapshot.source(registry);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player player)) { return false; }
		
		YamlConfiguration config = new YamlConfiguration();
		gameplay.set(config, player.getUniqueId().toString(), gameplay.capture(new SnapshotContext(player, Mode.STAFF)));
		
		try
		{
			Files.writeString(plugin.directory().resolve("test.yml"), config.saveToString());
			player.sendMessage("Success. Check: " + plugin.directory());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			player.sendMessage("Failure! See console...");
		}
		
		return true;
	}
}
