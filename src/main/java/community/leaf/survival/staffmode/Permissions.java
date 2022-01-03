/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode;

import org.bukkit.permissions.Permissible;

public enum Permissions
{
    STAFF_MEMBER("staff"),
    STAFF_MODE_ENABLED("enabled"),
    ADMIN("admin");
    
    private final String node;
    
    Permissions(String name)
    {
        this.node = "survival-staff-mode." + name;
    }
    
    public String node() { return node; }
    
    public boolean allows(Permissible permissible) { return permissible.hasPermission(node); }
    
    public boolean denies(Permissible permissible) { return !allows(permissible); }
}
