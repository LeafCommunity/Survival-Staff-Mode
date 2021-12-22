/*
 * Copyright © 2021, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.util;

import community.leaf.tasks.minecraft.Ticks;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

public class NightVision
{
	private NightVision() {}
	
	public static final PotionEffect EFFECT =
		PotionEffectType.NIGHT_VISION.createEffect(Ticks.fromInteger(30, TimeUnit.MINUTES), 3);
	
	public static void apply(Player player) { player.addPotionEffect(EFFECT); }
	
	public static void remove(Player player) { player.removePotionEffect(PotionEffectType.NIGHT_VISION); }
}
