/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/SurvivalStaffMode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import com.rezzedup.util.valuables.Adapter;
import community.leaf.configvalues.bukkit.data.YamlDataFile;
import community.leaf.configvalues.bukkit.util.Sections;
import community.leaf.survival.staffmode.snapshots.GameplaySnapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import community.leaf.tasks.Concurrency;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class StaffModeManager extends YamlDataFile implements StaffManager
{
	private static final String PROFILES_PATH = "survival-staff-mode.profiles";
	
	private final Map<UUID, StaffModeProfile> profilesByUuid = new HashMap<>();
	
	private final StaffModePlugin plugin;
	private final SnapshotSource<GameplaySnapshot> snapshot;
	private final OnlineStaffMemberList online;
	private final StaffModeProfile.Dependencies dependencies;
	
	StaffModeManager(StaffModePlugin plugin)
	{
		super(plugin.directory().resolve("data"), "survival-staff-mode.data.yml");
		
		this.plugin = plugin;
		this.snapshot = GameplaySnapshot.source(plugin.snapshots());
		this.online = new OnlineStaffMemberList(plugin);
		
		this.dependencies = new StaffModeProfile.Dependencies()
		{
			@Override
			public void updated() { StaffModeManager.this.updated(true); }
			
			@Override
			public SnapshotSource<GameplaySnapshot> snapshot() { return snapshot; }
			
			@Override
			public ConfigurationSection profileDataSection(UUID uuid)
			{
				return Sections.getOrCreate(profilesDataSection(), uuid.toString());
			}
		};
	}
	
	void loadDataFromDisk()
	{
		reloadsWith(() ->
		{
			Logger logger = plugin.getLogger();;
			
			if (isInvalid())
			{
				logger.log(Level.SEVERE, "Unable to load data", getInvalidReason());
				logger.log(Level.SEVERE, "Saving data backup just in case...");
				
				backupThenSave(plugin.backups(), "error");
				return;
			}
			
			profilesByUuid.clear();
			
			for (String uuid : profilesDataSection().getKeys(false))
			{
				Adapter.ofString().intoUuid().deserialize(uuid).ifPresent(this::existingProfile);
			}
		});
	}
	
	public void saveIfUpdated(Concurrency concurrency)
	{
		if (!isUpdated()) { return; }
		
		String output = toYamlString();
		
		Runnable task = () ->
		{
			try
			{
				Path path = getFilePath();
				Files.createDirectories(path.getParent());
				Files.writeString(path, output);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		};
		
		if (concurrency == Concurrency.ASYNC) { plugin.async().run(task); }
		else { plugin.sync().run(task); }
	}
	
	public OnlineStaffMemberList online() { return online; }
	
	@Override
	public Stream<StaffMember> streamOnlineStaffMembers()
	{
		return online.streamOnlineStaff().map(this::member).flatMap(Optional::stream);
	}
	
	private ConfigurationSection profilesDataSection() { return Sections.getOrCreate(data(), PROFILES_PATH); }
	
	public @NullOr StaffModeProfile existingProfile(UUID uuid)
	{
		@NullOr StaffModeProfile existing = profilesByUuid.get(uuid);
		if (existing != null) { return existing; }
		
		@NullOr ConfigurationSection section = Sections.get(profilesDataSection(), uuid.toString()).orElse(null);
		if (section == null) { return null; }
		
		StaffModeProfile profile = new StaffModeProfile(dependencies, uuid);
		profilesByUuid.put(uuid, profile);
		return profile;
	}
	
	public @NullOr StaffModeProfile playerProfile(Player player)
	{
		UUID uuid = player.getUniqueId();
		
		@NullOr StaffModeProfile existing = existingProfile(player.getUniqueId());
		if (existing != null) { return existing; }
		
		if (Permissions.STAFF_MEMBER.denies(player)) { return null; }
		
		StaffModeProfile profile = new StaffModeProfile(dependencies, uuid);
		profilesByUuid.put(uuid, profile);
		return profile;
	}
	
	@Override
	public Optional<StaffMember> member(UUID uuid)
	{
		return Optional.ofNullable(existingProfile(uuid));
	}
	
	@Override
	public Optional<StaffMember> member(Player player)
	{
		return Optional.ofNullable(playerProfile(player));
	}
}
