/*
 * Copyright © 2021-2022, RezzedUp <https://github.com/LeafCommunity/Survival-Staff-Mode>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.staffmode.commands;

import com.rezzedup.util.constants.Aggregates;
import com.rezzedup.util.constants.annotations.AggregatedResult;
import com.rezzedup.util.constants.types.TypeCapture;
import community.leaf.survival.staffmode.Mode;
import community.leaf.survival.staffmode.Permissions;
import community.leaf.survival.staffmode.StaffModePlugin;
import community.leaf.survival.staffmode.StaffModeProfile;
import community.leaf.survival.staffmode.ToggleSwitch;
import community.leaf.survival.staffmode.snapshots.SnapshotContext;
import community.leaf.survival.staffmode.util.NightVision;
import community.leaf.textchain.adventure.TextChain;
import community.leaf.textchain.adventure.TextProcessor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

public class StaffModeCommand implements CommandExecutor, TabCompleter
{
    // Staff Mode Toggles
    public static final Set<String> STAFF_TOGGLE_SWITCH = Set.of("toggle", "switch");
    
    public static final Set<String> STAFF_TOGGLE_ON = Set.of("on", "enable");
    
    public static final Set<String> STAFF_TOGGLE_OFF = Set.of("off", "disable");
    
    @AggregatedResult
    public static final Set<String> STAFF_TOGGLES =
        Aggregates.set(
            StaffModeCommand.class,
            TypeCapture.type(String.class),
            Aggregates.matching().all("TOGGLE").collections(true)
        );
    
    public static final Set<String> STAFF_CHECK = Set.of("check");
    
    // Staff Mode Tools
    
    public static final Set<String> STAFF_RUN_TOOL = Set.of("run", "then");
    
    public static final Set<String> STAFF_NIGHT_VISION_TOOL = Set.of("nightvision", "nv");
    
    public static final Set<String> STAFF_SPECTATE_TOOL = Set.of("spectator", "spectate", "spec");
    
    public static final Set<String> STAFF_FLY_TOOL = Set.of("fly");
    
    @AggregatedResult
    public static final Set<String> STAFF_TOOLS =
        Aggregates.set(
            StaffModeCommand.class,
            TypeCapture.type(String.class),
            Aggregates.matching().all("TOOL").collections(true)
        );
    
    // Managerial Commands
    public static final Set<String> ADMIN_RELOAD = Set.of("reload");
    
    public static final Set<String> ADMIN_INFO = Set.of("info");
    
    // Usage
    public static final Set<String> STAFF_USAGE = Set.of("help", "usage", "?");
    
    @AggregatedResult
    public static final Set<String> ALL_STAFF_ARGUMENTS =
        Aggregates.set(
            StaffModeCommand.class,
            TypeCapture.type(String.class),
            Aggregates.matching().all("STAFF").not("ADMIN").collections(true)
        );
    
    @AggregatedResult
    public static final Set<String> ALL_ADMIN_ARGUMENTS =
        Aggregates.set(
            StaffModeCommand.class,
            TypeCapture.type(String.class),
            Aggregates.matching().all().collections(true)
        );
    
    private static final Field COMMAND_MAP_FIELD;
    
    static
    {
        try
        {
            COMMAND_MAP_FIELD = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);
        }
        catch (NoSuchFieldException  e)
        {
            throw new IllegalStateException("Server implementation is missing commandMap field", e);
        }
    }
    
    private final StaffModePlugin plugin;
    
    public StaffModeCommand(StaffModePlugin plugin)
    {
        this.plugin = plugin;
    }
    
    private CommandMap commandMap()
    {
        try { return (CommandMap) COMMAND_MAP_FIELD.get(plugin.getServer()); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String choice = (args.length >= 1) ? args[0].toLowerCase(Locale.ROOT) : "toggle";
        
        if (STAFF_TOGGLES.contains(choice)) { return toggle(sender, choice); }
        if (STAFF_CHECK.contains(choice)) { return check(sender, args); }
        if (STAFF_TOOLS.contains(choice)) { return tools(sender, args); }
        if (ADMIN_RELOAD.contains(choice)) { return reload(sender); }
        if (ADMIN_INFO.contains(choice)) { return info(sender); }
        if (STAFF_USAGE.contains(choice)) { return usage(sender); }
        
        return error(sender, "Unknown argument: " + choice);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        List<String> suggestions = new ArrayList<>();
        boolean filter = true;
        
        int last = args.length - 1;
        String lastArg = (last < 0) ? "" : args[last];
        String lastArgLowerCase = lastArg.toLowerCase(Locale.ROOT);
        
        out: if (args.length <= 1)
        {
            if (Permissions.ADMIN.allows(sender)) { suggestions.addAll(ALL_ADMIN_ARGUMENTS); }
            else { suggestions.addAll(ALL_STAFF_ARGUMENTS); }
        }
        else
        {
            String choice = args[0].toLowerCase(Locale.ROOT);
            
            if (STAFF_CHECK.contains(choice))
            {
                if (args.length <= 2)
                {
                    plugin.getServer().getOnlinePlayers().stream().map(Player::getName).forEach(suggestions::add);
                }
                else
                {
                    suggestions.add("<No more arguments>");
                }
            }
            else if (STAFF_TOOLS.contains(choice))
            {
                // Search for 'run' tool suggestions
                for (int i = 0; i < args.length; i++)
                {
                    if (STAFF_RUN_TOOL.contains(args[i].toLowerCase(Locale.ROOT)))
                    {
                        if (i == last)
                        {
                            suggestions.addAll(STAFF_RUN_TOOL);
                            break out;
                        }
                        
                        StringJoiner joiner = new StringJoiner(" ");
                        
                        for (int start = ++i; i < args.length; i++)
                        {
                            joiner.add((i == start) ? args[i].replaceFirst("/", "") : args[i]);
                        }
                        
                        @NullOr List<String> delegated = commandMap().tabComplete(sender, joiner.toString());
                        
                        if (delegated == null || delegated.isEmpty())
                        {
                            suggestions.add("<Command>");
                        }
                        else
                        {
                            suggestions.addAll(delegated);
                            filter = false; // no filtering, just send command's suggestions
                        }
                        
                        // No more suggestions necessary.
                        break out;
                    }
                }
                
                suggestions.addAll(STAFF_TOOLS);
            }
            else
            {
                suggestions.add("<No more arguments>");
            }
        }
        
        if (filter)
        {
            suggestions.removeIf(entry ->
                !(entry.startsWith("<") || entry.toLowerCase(Locale.ROOT).contains(lastArgLowerCase))
            );
        
            if (suggestions.isEmpty()) { suggestions.add("<Unknown argument>"); }
            else { suggestions.sort(String.CASE_INSENSITIVE_ORDER); }
        }
        
        return suggestions;
    }
    
    private boolean error(CommandSender sender, String message)
    {
        TextChain.using(plugin).chain()
            .then("Uhoh. ").bold().color(NamedTextColor.RED)
            .then(message, TextProcessor.legacyAmpersand())
            .sendToRecipient(sender);
        return true;
    }
    
    private boolean toggle(CommandSender sender, String choice)
    {
        if (!(sender instanceof Player player))
        {
            return error(sender, "Only players may use this command");
        }
        
        StaffModeProfile profile = plugin.staff().onlineStaffProfile(player);
        Mode mode;
        
        if (STAFF_TOGGLE_ON.contains(choice)) { mode = Mode.STAFF; }
        else if (STAFF_TOGGLE_OFF.contains(choice)) { mode = Mode.SURVIVAL; }
        else { mode = profile.mode().toggle(); }
        
        ToggleSwitch status = profile.mode(mode);
        
        if (status == ToggleSwitch.ALREADY)
        {
            TextChain.using(plugin).chain()
                .then("Hey! ")
                    .color(NamedTextColor.RED).bold().italic()
                .then("You're already in " + mode.name().toLowerCase(Locale.ROOT) + " mode!")
                .sendToRecipient(player);
        }
        else if (status == ToggleSwitch.FAILURE)
        {
            error(player, "Could not switch to " + mode.name().toLowerCase(Locale.ROOT) + " mode.");
        }
        // Rare case: server admin has the 'staffmode.enabled' permission still
        // and cannot actually leave staff mode.
        else if (mode == Mode.SURVIVAL && profile.mode() != Mode.SURVIVAL)
        {
            error(
                player,
                "You still have access to the &c&n" + Permissions.STAFF_MODE_ENABLED.node() + "&r " +
                "permission node and cannot leave staff mode until it is removed from your rank."
            );
            
            profile.forceRestoreSnapshot(new SnapshotContext(player, Mode.STAFF));
        }
        
        return true;
    }
    
    private boolean check(CommandSender sender, String[] args)
    {
        sender.sendMessage("check");
        return true;
    }
    
    private boolean tools(CommandSender sender, String[] args)
    {
        if (!(sender instanceof Player player)) { return error(sender, "Only players may use this command"); }
        
        // Only apply tools once (avoids /staffmode spec spec spec spec, etc.)
        Map<String, BiConsumer<StaffModeProfile, Boolean>> tools = new LinkedHashMap<>();
        List<String> unknown = new ArrayList<>();
        
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            String tool = arg.toLowerCase(Locale.ROOT);
            
            if (STAFF_RUN_TOOL.contains(tool))
            {
                StringJoiner joiner = new StringJoiner(" ");
                for (++i; i < args.length; i++) { joiner.add(args[i]); }
                tools.put("run", (profile, enabled) -> run(profile, enabled, joiner.toString()));
            }
            else if (STAFF_NIGHT_VISION_TOOL.contains(tool)) { tools.put("night-vision", this::nightVision); }
            else if (STAFF_SPECTATE_TOOL.contains(tool)) { tools.put("spectator", this::spectator); }
            else if (STAFF_FLY_TOOL.contains(tool)) { tools.put("fly", this::fly); }
            else { unknown.add(arg); }
        }
        
        if (!tools.isEmpty())
        {
            StaffModeProfile profile = plugin.staff().onlineStaffProfile(player);
            ToggleSwitch toggle = profile.mode(Mode.STAFF);
            
            // Couldn't toggle...
            if (toggle == ToggleSwitch.FAILURE) { return true; }
            
            tools.values().forEach(tool -> tool.accept(profile, toggle == ToggleSwitch.SUCCESS));
        }
        
        if (!unknown.isEmpty())
        {
            error(player, "Unknown argument(s): " + String.join(" ", unknown));
        }
        
        return true;
    }
    
    private void run(StaffModeProfile profile, boolean enabledStaffMode, String command)
    {
        Player player = profile.online();
        
        if (command.isEmpty())
        {
            error(player, "Missing command to run");
            return;
        }
        
        command = (command.startsWith("/")) ? command : "/" + command;
        
        TextChain.using(plugin).chain()
            .then("Executing: ")
            .then(command)
                .italic().color(NamedTextColor.GRAY)
                .click(ClickEvent.copyToClipboard(command))
            .sendToRecipient(player);
        
        player.chat(command);
    }
    
    private void nightVision(StaffModeProfile profile, boolean enabledStaffMode)
    {
        Player player = profile.online();
        boolean toggle = enabledStaffMode || !profile.nightVision();
        
        profile.nightVision(toggle);
        
        if (toggle)
        {
            NightVision.apply(player);
            
            TextChain.using(plugin).legacy().chain()
                .then("&oEnabled night vision.")
                .sendToRecipient(player);
        }
        else
        {
            NightVision.remove(player);
            
            TextChain.using(plugin).legacy().chain()
                .then("&oDisabled night vision.")
                .sendToRecipient(player);
        }
    }
    
    private void spectator(StaffModeProfile profile, boolean enabledStaffMode)
    {
        Player player = profile.online();
        boolean spectator = enabledStaffMode || player.getGameMode() != GameMode.SPECTATOR;
        
        if (spectator)
        {
            player.setGameMode(GameMode.SPECTATOR);
            
            TextChain.using(plugin).legacy().chain()
                .then("&oEnabled spectator mode.")
                .sendToRecipient(player);
        }
        else
        {
            player.setGameMode(profile.gameModePriorToSpectator());
            
            TextChain.using(plugin).legacy().chain()
                .then("&oDisabled spectator mode.")
                .sendToRecipient(player);
        }
    }
    
    private void fly(StaffModeProfile profile, boolean enabledStaffMode)
    {
        Player player = profile.online();
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    private boolean reload(CommandSender sender)
    {
        sender.sendMessage("reload");
        return true;
    }
    
    private boolean info(CommandSender sender)
    {
        sender.sendMessage("info");
        return true;
    }
    
    private boolean usage(CommandSender sender)
    {
        sender.sendMessage("usage");
        return true;
    }
}
