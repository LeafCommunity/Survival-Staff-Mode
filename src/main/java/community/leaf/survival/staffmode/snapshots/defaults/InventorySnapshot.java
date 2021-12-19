/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.snapshots.defaults;

import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.util.Sections;
import community.leaf.survival.staffmode.snapshots.Snapshot;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.snapshots.SnapshotSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

// Can't actually be a record due to defensive array cloning.
@SuppressWarnings("ClassCanBeRecord")
public final class InventorySnapshot implements Snapshot
{
	private static final YamlValue<String> MAIN = YamlValue.ofString("main").maybe();
	
	private static final YamlValue<String> ARMOR = YamlValue.ofString("armor").maybe();
	
	private static final YamlValue<String> EXTRA = YamlValue.ofString("extra").maybe();
	
	public static final SnapshotSource<InventorySnapshot> SOURCE =
		new SnapshotSource<>()
		{
			@Override
			public InventorySnapshot capture(SnapshotContext context) { return of(context.player()); }
			
			@Override
			public Optional<InventorySnapshot> get(ConfigurationSection storage, String key)
			{
				return Sections.get(storage, key).map(data ->
				{
					try
					{
						@NullOr ItemStack[] inventory = itemsFromBase64(MAIN.get(data).orElse(""));
						@NullOr ItemStack[] armor = itemsFromBase64(ARMOR.get(data).orElse(""));
						@NullOr ItemStack[] extra = itemsFromBase64(EXTRA.get(data).orElse(""));
						
						return new InventorySnapshot(inventory, armor, extra);
					}
					catch (RuntimeException e)
					{
						e.printStackTrace();
						return null;
					}
				});
			}
			
			@Override
			public void set(ConfigurationSection storage, String key, @NullOr InventorySnapshot updated)
			{
				if (updated == null)
				{
					storage.set(key, null);
					return;
				}
				
				ConfigurationSection data = Sections.getOrCreate(storage, key);
				
				MAIN.set(data, itemsToBase64(updated.main));
				ARMOR.set(data, itemsToBase64(updated.armor));
				EXTRA.set(data, itemsToBase64(updated.extra));
			}
		};
	
	public static final InventorySnapshot EMPTY = new InventorySnapshot(new ItemStack[0], new ItemStack[0], new ItemStack[0]);
	
	public static InventorySnapshot of(Player player)
	{
		PlayerInventory inv = player.getInventory();
		return new InventorySnapshot(inv.getContents(), inv.getArmorContents(), inv.getExtraContents());
	}
	
	private static String itemsToBase64(@NullOr ItemStack[] items)
	{
		try
		(
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			BukkitObjectOutputStream output = new BukkitObjectOutputStream(bytes);
		)
		{
			output.writeInt(items.length);
			for (@NullOr ItemStack item : items) { output.writeObject(item); }
			return Base64.getEncoder().encodeToString(bytes.toByteArray());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not encode items into Base64", e);
		}
	}
	
	private static @NullOr ItemStack[] itemsFromBase64(String base64)
	{
		try
		(
			ByteArrayInputStream bytes = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
			BukkitObjectInputStream input = new BukkitObjectInputStream(bytes);
		)
		{
			@NullOr ItemStack[] items = new ItemStack[input.readInt()];
			for (int i = 0; i < items.length; i++) { items[i] = (ItemStack) input.readObject(); }
			return items;
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not decode items from Base64", e);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static @NullOr ItemStack[] clone(@NullOr ItemStack[] items)
	{
		@NullOr ItemStack[] cloned = new ItemStack[items.length];
		
		for (int i = 0; i < items.length; i++)
		{
			@NullOr ItemStack item = items[i];
			cloned[i] = (item == null) ? null : item.clone();
		}
		
		return cloned;
	}
	
	private final @NullOr ItemStack[] main;
	private final @NullOr ItemStack[] armor;
	private final @NullOr ItemStack[] extra;
	
	public InventorySnapshot(@NullOr ItemStack[] main, @NullOr ItemStack[] armor, @NullOr ItemStack[] extra)
	{
		this.main = clone(main);
		this.armor = clone(armor);
		this.extra = clone(extra);
	}
	
	public @NullOr ItemStack[] main() { return clone(main); }
	
	public @NullOr ItemStack[] armor() { return clone(armor); }
	
	public @NullOr ItemStack[] extra() { return clone(extra); }
	
	@Override
	public void apply(SnapshotContext context)
	{
		PlayerInventory inv = context.player().getInventory();
		
		inv.clear();
		
		inv.setContents(main());
		inv.setArmorContents(armor());
		inv.setExtraContents(extra());
		
		context.player().updateInventory();
	}
}
