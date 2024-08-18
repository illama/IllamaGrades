package com.example.illamagrades;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EventListener implements Listener {

    private final IllamaGrades plugin;
    private final LuckPerms luckPerms;
    private final Random random = new Random();
    private final Map<Material, String> itemPermissionMap = new HashMap<>();

    public EventListener(IllamaGrades plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    public void loadItemPermissionMap() {
        itemPermissionMap.clear();
        for (String itemName : plugin.getConfig().getConfigurationSection("items").getKeys(false)) {
            Material material = Material.matchMaterial(itemName);
            String permission = plugin.getConfig().getString("items." + itemName + ".permission");
            String worldName = plugin.getConfig().getString("items." + itemName + ".world");

            if (material != null && permission != null && worldName != null) {
                itemPermissionMap.put(material, permission + ";" + worldName); // Stocker la permission et le monde
            }
        }
    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
                && item != null
                && itemPermissionMap.containsKey(item.getType())
                && event.getPlayer().getInventory().getItemInMainHand().equals(item)) {

            Player player = event.getPlayer();

            // Vérifier le monde
            String[] permissionAndWorld = itemPermissionMap.get(item.getType()).split(";");
            String permission = permissionAndWorld[0];
            String worldName = permissionAndWorld[1];
            World world = player.getWorld();

            if (!world.getName().equals(worldName)) {
                player.sendMessage(ChatColor.RED + "Cet item ne peut être utilisé dans ce monde.");
                return;
            }

            // Supprimer l'objet de la main du joueur
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItemInMainHand(item);
            } else {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }

            // Accorder la permission au joueur
            grantPermission(player, item.getType());

            // Envoyer un message global
            broadcastGradeUp(player.getName(), item.getType());

            // Lancer des feux d'artifice autour du joueur
            launchFireworks(player.getLocation(), 5, player.getWorld());

            // Ajouter des effets de flammes autour du joueur
            createFlameEffects(player.getLocation(), 20, player.getWorld());

            // Ajouter le joueur au groupe défini avec /lp
            addPlayerToGroup(player, item.getType());

            // Jouer un son global à tous les joueurs
            playGlobalLevelUpSound(player.getWorld());
        }
    }

    private void grantPermission(Player player, Material item) {
        String permission = itemPermissionMap.get(item).split(";")[0];
        if (permission != null) {
            player.addAttachment(plugin, permission, true);
        }
    }

    private void addPlayerToGroup(Player player, Material item) {
        String groupName = itemPermissionMap.get(item).split(";")[0];
        if (groupName != null) {
            // Obtenir le joueur LuckPerms
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());

            if (user != null) {
                // Ajouter le groupe au joueur
                Group group = luckPerms.getGroupManager().getGroup(groupName);
                if (group != null) {
                    Node node = InheritanceNode.builder(group.getName()).build();
                    user.data().add(node);
                    luckPerms.getUserManager().saveUser(user);

                    player.sendMessage(ChatColor.GREEN + "Vous avez été ajouté au groupe : " + ChatColor.AQUA + groupName);
                    plugin.getLogger().info("Le joueur " + player.getName() + " a été ajouté au groupe " + groupName + " avec succès.");
                } else {
                    player.sendMessage(ChatColor.RED + "Erreur : Groupe introuvable.");
                    plugin.getLogger().warning("Groupe introuvable : " + groupName);
                }
            } else {
                player.sendMessage(ChatColor.RED + "Erreur : Impossible de trouver votre profil LuckPerms.");
                plugin.getLogger().warning("Profil LuckPerms introuvable pour le joueur : " + player.getName());
            }
        } else {
            player.sendMessage(ChatColor.RED + "Erreur : Aucun groupe configuré pour cet objet.");
            plugin.getLogger().warning("Aucun groupe configuré pour l'objet : " + item.name());
        }
    }

    private void launchFireworks(Location location, int count, World world) {
        for (int i = 0; i < count; i++) {
            Location spawnLocation = location.clone().add(
                random.nextInt(5) - 2,
                random.nextInt(3) + 1,
                random.nextInt(5) - 2
            );

            Firework firework = world.spawn(spawnLocation, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();

            FireworkEffect effect = FireworkEffect.builder()
                .withColor(getRandomColor())
                .with(FireworkEffect.Type.BALL)
                .withFlicker()
                .withTrail()
                .build();

            meta.addEffect(effect);
            meta.setPower(1 + random.nextInt(2));
            firework.setFireworkMeta(meta);
        }
    }

    private void createFlameEffects(Location location, int count, World world) {
        for (int i = 0; i < count; i++) {
            Location spawnLocation = location.clone().add(
                random.nextInt(5) - 2,
                random.nextInt(3) + 1,
                random.nextInt(5) - 2
            );

            world.spawnParticle(Particle.FLAME, spawnLocation, 20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private Color getRandomColor() {
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE, Color.ORANGE};
        return colors[random.nextInt(colors.length)];
    }

    private void broadcastGradeUp(String playerName, Material item) {
        String permission = itemPermissionMap.get(item).split(";")[0];
        String message = ChatColor.GREEN + "Félicitations " + ChatColor.YELLOW + playerName +
                         ChatColor.GREEN + " pour avoir atteint un nouveau grade avec l'objet " + ChatColor.AQUA + item.name() +
                         ChatColor.GREEN + ". Vous avez reçu le grade : " + ChatColor.AQUA + permission + ChatColor.GREEN + ".";

        plugin.getServer().broadcastMessage(message);
    }

    private void playGlobalLevelUpSound(World world) {
        // Assurez-vous que le monde n'est pas null
        if (world == null) {
            plugin.getLogger().warning("Le monde est null, impossible de jouer le son.");
            return;
        }

        // Coordonnées du spawn pour jouer le son
        Location spawnLocation = world.getSpawnLocation();

        // Assurez-vous que le monde contient des joueurs
        if (world.getPlayers().isEmpty()) {
            plugin.getLogger().warning("Aucun joueur trouvé dans le monde, impossible de jouer le son.");
            return;
        }

        // Jouer le son pour tous les joueurs dans le monde
        world.playSound(spawnLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        plugin.getLogger().info("Son 'BLOCK_NOTE_BLOCK_PLING' joué à " + spawnLocation.toString());
    }
}