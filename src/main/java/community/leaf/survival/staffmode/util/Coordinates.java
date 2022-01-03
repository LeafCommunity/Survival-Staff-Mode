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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Optional;

public record Coordinates(double x, double y, double z, float yaw, float pitch)
{
    private static final YamlValue<Double> X = YamlValue.ofDouble("x").maybe();
    
    private static final YamlValue<Double> Y = YamlValue.ofDouble("y").maybe();
    
    private static final YamlValue<Double> Z = YamlValue.ofDouble("z").maybe();
    
    private static final YamlValue<Float> YAW = YamlValue.ofFloat("yaw").maybe();
    
    private static final YamlValue<Float> PITCH = YamlValue.ofFloat("pitch").maybe();
    
    public static final YamlAccessor<Coordinates> YAML =
        new YamlAccessor<>()
        {
            @Override
            public Optional<Coordinates> get(ConfigurationSection storage, String key)
            {
                return Sections.get(storage, key).map(data ->
                {
                    Optional<Double> x = X.get(data);
                    Optional<Double> y = Y.get(data);
                    Optional<Double> z = Z.get(data);
                    Optional<Float> yaw = YAW.get(data);
                    Optional<Float> pitch = PITCH.get(data);
                    
                    if (x.isEmpty() || y.isEmpty() || z.isEmpty()) { return null; }
                    
                    return new Coordinates(x.get(), y.get(), z.get(), yaw.orElse(0.0F), pitch.orElse(0.0F));
                });
            }
            
            @Override
            public void set(ConfigurationSection storage, String key, @NullOr Coordinates updated)
            {
                if (updated == null)
                {
                    storage.set(key, null);
                    return;
                }
                
                ConfigurationSection data = Sections.getOrCreate(storage, key);
                
                X.set(data, updated.x);
                Y.set(data, updated.y);
                Z.set(data, updated.z);
                YAW.set(data, updated.yaw);
                PITCH.set(data, updated.pitch);
            }
        };
    
    public static Coordinates of(Location location)
    {
        return new Coordinates(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }
    
    public Location location(@NullOr World world)
    {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
