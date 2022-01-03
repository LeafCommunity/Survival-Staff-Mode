/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots.defaults;

import com.rezzedup.util.valuables.Adapter;
import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.snapshots.Snapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record PotionEffectsSnapshot(List<PotionEffect> effects) implements Snapshot
{
    private static final Adapter<Map<String, Object>, PotionEffect> POTION_EFFECT =
        Adapter.of(
            map -> {
                try { return Optional.of(new PotionEffect(map)); }
                catch (RuntimeException ignored) { return Optional.empty(); }
            },
            effect -> Optional.of(effect.serialize())
        );
    
    public static final SnapshotSource<PotionEffectsSnapshot> SOURCE =
        new SnapshotSource<>()
        {
            @Override
            public boolean isApplicable(SnapshotContext context) { return context.mode() == Mode.SURVIVAL; }
            
            @Override
            public PotionEffectsSnapshot capture(SnapshotContext context) { return of(context.player()); }
            
            @SuppressWarnings("unchecked")
            @Override
            public Optional<PotionEffectsSnapshot> get(ConfigurationSection storage, String key)
            {
                List<Map<?, ?>> data = storage.getMapList(key);
                
                return (data.isEmpty())
                    ? Optional.of(EMPTY)
                       : Optional.of(new PotionEffectsSnapshot(
                        data.stream()
                            .map(map -> (Map<String, Object>) map)
                            .map(POTION_EFFECT::deserialize)
                            .flatMap(Optional::stream)
                            .toList()
                    ));
            }
            
            @Override
            public void set(ConfigurationSection storage, String key, @NullOr PotionEffectsSnapshot updated)
            {
                if (updated == null || updated.effects.isEmpty()) { storage.set(key, null); }
                else { storage.set(key, updated.effects.stream().map(PotionEffect::serialize).toList()); }
            }
        };
    
    public static final PotionEffectsSnapshot EMPTY = new PotionEffectsSnapshot(List.of());
    
    public static PotionEffectsSnapshot of(Player player)
    {
        return new PotionEffectsSnapshot(List.copyOf(player.getActivePotionEffects()));
    }
    
    public PotionEffectsSnapshot(List<PotionEffect> effects)
    {
        this.effects = List.copyOf(effects);
    }
    
    @Override
    public void apply(SnapshotContext context)
    {
        List.copyOf(context.player().getActivePotionEffects()).stream()
            .map(PotionEffect::getType)
            .forEach(context.player()::removePotionEffect);
        
        context.player().addPotionEffects(effects());
    }
}
