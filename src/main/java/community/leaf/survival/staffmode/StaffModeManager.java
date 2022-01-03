/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
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
    private static final String PROFILES_PATH = "staff-mode.profiles";
    
    private final Map<UUID, StaffModeProfile> profilesByUuid = new HashMap<>();
    
    private final StaffModePlugin plugin;
    private final SnapshotSource<GameplaySnapshot> snapshot;
    private final StaffModeProfile.Dependencies dependencies;
    
    StaffModeManager(StaffModePlugin plugin)
    {
        super(plugin.directory().resolve("data"), "staff-mode.data.yml");
        
        this.plugin = plugin;
        this.snapshot = GameplaySnapshot.source(plugin.snapshots());
        
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
                Adapter.ofString().intoUuid().deserialize(uuid).ifPresent(this::existingProfileByUuid);
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
                updated(false);
            }
            catch (IOException e) { e.printStackTrace(); }
        };
        
        if (concurrency == Concurrency.SYNC) { task.run(); }
        else { plugin.async().run(task); }
    }
    
    private ConfigurationSection profilesDataSection() { return Sections.getOrCreate(data(), PROFILES_PATH); }
    
    public void deleteProfile(UUID uuid)
    {
        profilesByUuid.remove(uuid);
        profilesDataSection().set(uuid.toString(), null);
        updated(true);
    }
    
    public Optional<StaffModeProfile> existingProfileByUuid(UUID uuid)
    {
        @NullOr StaffModeProfile existing = profilesByUuid.get(uuid);
        if (existing != null) { return Optional.of(existing); }
        
        @NullOr ConfigurationSection section = Sections.get(profilesDataSection(), uuid.toString()).orElse(null);
        if (section == null) { return Optional.empty(); }
        
        StaffModeProfile profile = new StaffModeProfile(dependencies, uuid);
        profilesByUuid.put(uuid, profile);
        return Optional.of(profile);
    }
    
    public Optional<StaffModeProfile> existingPlayerProfile(Player player)
    {
        UUID uuid = player.getUniqueId();
        
        Optional<StaffModeProfile> existing = existingProfileByUuid(uuid);
        if (existing.isPresent()) { return existing; }
        
        if (Permissions.STAFF_MEMBER.denies(player)) { return Optional.empty(); }
        
        StaffModeProfile profile = new StaffModeProfile(dependencies, uuid);
        profilesByUuid.put(uuid, profile);
        return Optional.of(profile);
    }
    
    public StaffModeProfile onlineStaffProfile(Player player)
    {
        return existingPlayerProfile(player).orElseThrow(() ->
            new IllegalArgumentException("Player is not a staff member and has no existing profile: " + player.getName())
        );
    }
    
    public Stream<StaffModeProfile> streamOnlineStaffProfiles()
    {
        return plugin.getServer().getOnlinePlayers().stream()
            .filter(Permissions.STAFF_MEMBER::allows)
            .flatMap(player -> existingPlayerProfile(player).stream());
    }
    
    @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
    private static <S, T extends S> Optional<S> smuggle(Optional<T> optional)
    {
        return (Optional<S>) optional;
    }
    
    @Override
    public Optional<StaffMember> member(UUID uuid) { return smuggle(existingProfileByUuid(uuid)); }
    
    @Override
    public Optional<StaffMember> member(Player player) { return smuggle(existingPlayerProfile(player)); }
}
