/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots.defaults;

import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.snapshots.Snapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import community.leaf.survival.staffmode.util.Position;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Optional;

public record PositionSnapshot(Position position) implements Snapshot
{
    public static final SnapshotSource<PositionSnapshot> SOURCE =
        new SnapshotSource<>()
        {
            @Override
            public boolean isApplicable(SnapshotContext context) { return context.mode() == Mode.SURVIVAL; }
            
            @Override
            public PositionSnapshot capture(SnapshotContext context) { return of(context.player()); }
            
            @Override
            public Optional<PositionSnapshot> get(ConfigurationSection storage, String key)
            {
                return Position.YAML.get(storage, key).map(PositionSnapshot::new);
            }
            
            @Override
            public void set(ConfigurationSection storage, String key, @NullOr PositionSnapshot updated)
            {
                Position.YAML.set(storage, key, (updated == null) ? null : updated.position);
            }
        };
    
    public static PositionSnapshot of(Player player)
    {
        return new PositionSnapshot(Position.of(player.getLocation()));
    }
    
    @Override
    public void apply(SnapshotContext context)
    {
        context.player().teleport(position().locationInLoadedWorld().orElseThrow(() ->
            new IllegalStateException("Cannot teleport to unloaded world: " + position().world()
        )));
    }
}
