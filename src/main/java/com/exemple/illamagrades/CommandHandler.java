package com.example.illamagrades;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class CommandHandler implements CommandExecutor {

    private final IllamaGrades plugin;
    private final LuckPerms luckPerms;

    public CommandHandler(IllamaGrades plugin) {
        this.plugin = plugin;
        this.luckPerms = setupLuckPerms();
    }

    private LuckPerms setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> rsp = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        return rsp != null ? rsp.getProvider() : null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setitem")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Cette commande ne peut être exécutée que par des joueurs.");
                return false;
            }

            Player player = (Player) sender;

            if (args.length < 2) {
                player.sendMessage("Utilisation : /setitem <item> <permission> [world]");
                return false;
            }

            String itemName = args[0].toUpperCase();
            String permission = args[1].toUpperCase();
            String worldName = args.length > 2 ? args[2] : null;

            try {
                Material material = Material.valueOf(itemName);
                if (material == null) {
                    player.sendMessage("Type d'objet invalide.");
                    return false;
                }

                if (worldName != null && plugin.getServer().getWorld(worldName) == null) {
                    player.sendMessage("Monde invalide.");
                    return false;
                }

                plugin.getConfig().set("items." + itemName + ".permission", permission);
                plugin.getConfig().set("items." + itemName + ".world", worldName);
                plugin.saveConfig();
                plugin.reloadConfig(); // Optionnel, si vous souhaitez recharger la config immédiatement

                // Mettre à jour la map en mémoire
                plugin.getEventListener().loadItemPermissionMap();

                player.sendMessage("Permission définie avec succès pour " + itemName + " : " + permission);

                // Attribuer le groupe au joueur
                if (luckPerms != null) {
                    Group group = luckPerms.getGroupManager().getGroup(permission);
                    if (group != null) {
                        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
                            user.data().add(InheritanceNode.builder(permission).build());
                            luckPerms.getUserManager().saveUser(user);
                        });
                        player.sendMessage("Groupe LuckPerms défini : " + permission);
                    } else {
                        player.sendMessage("Groupe LuckPerms invalide : " + permission);
                    }
                } else {
                    player.sendMessage("LuckPerms n'est pas disponible.");
                }
            } catch (IllegalArgumentException e) {
                player.sendMessage("Type d'objet invalide.");
            }

            return true;
        } else if (command.getName().equalsIgnoreCase("reloadplugin")) {
            if (!(sender.hasPermission("illamagrades.reload"))) {
                sender.sendMessage("Vous n'avez pas la permission d'exécuter cette commande.");
                return false;
            }

            plugin.reloadConfig();
            plugin.getEventListener().loadItemPermissionMap();
            sender.sendMessage("Le plugin a été rechargé avec succès.");
            return true;
        }

        return false;
    }
}
