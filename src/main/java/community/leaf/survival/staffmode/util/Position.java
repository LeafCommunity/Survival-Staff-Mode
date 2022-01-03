/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.util;

import community.leaf.configvalues.bukkit.YamlAccessor;
import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.util.Sections;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Optional;

public record Position(String world, Coordinates coordinates)
{
    private static final YamlValue<String> WORLD = YamlValue.ofString("world").maybe();
    
    private static final YamlValue<Coordinates> COORDINATES = YamlValue.of("coordinates", Coordinates.YAML).maybe();
    
    public static final YamlAccessor<Position> YAML =
        new YamlAccessor<>()
        {
            @Override
            public Optional<Position> get(ConfigurationSection storage, String key)
            {
                return Sections.get(storage, key).map(data ->
                {
                    Optional<String> world = WORLD.get(data);
                    Optional<Coordinates> coordinates = COORDINATES.get(data);
                    
                    if (world.isEmpty() || coordinates.isEmpty()) { return null; }
                    
                    return new Position(world.get(), coordinates.get());
                });
            }
            
            @Override
            public void set(ConfigurationSection storage, String key, @NullOr Position updated)
            {
                if (updated == null)
                {
                    storage.set(key, null);
                    return;
                }
                
                ConfigurationSection data = Sections.getOrCreate(storage, key);
                
                WORLD.set(data, updated.world);
                COORDINATES.set(data, updated.coordinates);
            }
        };
    
    public static Position of(Location location)
    {
        @NullOr World world = location.getWorld();
        if (world == null) { throw new IllegalArgumentException("Location has null world: " + location); }
        return new Position(world.getName(), Coordinates.of(location));
    }
    
    public Location location()
    {
        return coordinates.location(Bukkit.getWorld(world));
    }
    
    public Optional<Location> locationInLoadedWorld()
    {
        return Optional.of(location()).filter(Location::isWorldLoaded);
    }
}
