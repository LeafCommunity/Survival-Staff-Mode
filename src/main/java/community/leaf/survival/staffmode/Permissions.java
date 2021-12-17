package community.leaf.survival.staffmode;

import org.bukkit.permissions.Permissible;

public enum Permissions
{
	STAFF_MEMBER("staff"),
	STAFF_MODE_ENABLED("enabled");
	
	private final String node;
	
	Permissions(String name)
	{
		this.node = "survivalstaffmode." + name;
	}
	
	public String node() { return node; }
	
	public boolean allows(Permissible permissible) { return permissible.hasPermission(node); }
	
	public boolean denies(Permissible permissible) { return !allows(permissible); }
}
