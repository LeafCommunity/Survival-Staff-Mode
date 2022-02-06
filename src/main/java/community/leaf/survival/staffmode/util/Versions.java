/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.util;

import com.github.zafarkhaja.semver.Version;
import com.rezzedup.util.exceptional.Attempt;
import com.rezzedup.util.valuables.Adapter;
import community.leaf.configvalues.bukkit.YamlAccessor;

import java.util.Optional;

public class Versions
{
    private Versions() { throw new UnsupportedOperationException(); }
    
    public static final Version ZERO = Version.forIntegers(0);
    
    public static final YamlAccessor<Version> YAML =
        YamlAccessor.of(Adapter.of(
            object -> Attempt.ignoring().get(() -> Version.valueOf(String.valueOf(object))),
            version -> Optional.of(String.valueOf(version))
        ));
}
