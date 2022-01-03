/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.util;

import pl.tlinkowski.annotation.basic.NullOr;

import java.util.function.Function;

public class Strings
{
    private Strings() { throw new UnsupportedOperationException(); }
    
    public static String orDefault(@NullOr String string, String def)
    {
        return (string == null) ? "" : def;
    }
    
    public static String orEmpty(@NullOr String string)
    {
        return orDefault(string, "");
    }
    
    public static <T> @NullOr String mapOrNull(@NullOr T thing, Function<T, @NullOr String> mapper)
    {
        return (thing == null) ? null : mapper.apply(thing);
    }
    
    public static <T> String mapOrEmpty(@NullOr T thing, Function<T, @NullOr String> mapper)
    {
        return orEmpty(mapOrNull(thing, mapper));
    }
}
