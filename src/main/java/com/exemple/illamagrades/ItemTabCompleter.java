package com.example.illamagrades;

import net.luckperms.api.LuckPerms;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemTabCompleter implements TabCompleter {

    private final LuckPerms luckPerms;
    private final IllamaGrades plugin;

    public ItemTabCompleter(IllamaGrades plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Complétion pour les matériaux
            List<String> materials = new ArrayList<>();
            for (Material material : Material.values()) {
                if (material.isItem()) {
                    materials.add(material.name());
                }
            }
            suggestions.addAll(StringUtil.copyPartialMatches(args[0], materials, new ArrayList<>()));
        } else if (args.length == 2) {
            // Complétion pour les groupes
            List<String> groups = luckPerms.getGroupManager().getLoadedGroups().stream()
                .map(group -> group.getName())
                .collect(Collectors.toList());

            suggestions.addAll(StringUtil.copyPartialMatches(args[1], groups, new ArrayList<>()));
        } else if (args.length == 3) {
            // Complétion pour les mondes
            List<String> worlds = plugin.getServer().getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toList());

            suggestions.addAll(StringUtil.copyPartialMatches(args[2], worlds, new ArrayList<>()));
        }

        return suggestions;
    }
}
