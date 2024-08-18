package com.example.illamagrades;

import net.luckperms.api.LuckPerms;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class IllamaGrades extends JavaPlugin {

    private EventListener eventListener;
    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        // Afficher un gros message dans la console avec un cadre
        afficherMessageDeDemarrage();

        saveDefaultConfig();
        luckPerms = getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
        eventListener = new EventListener(this, luckPerms);
        eventListener.loadItemPermissionMap();

        getServer().getPluginManager().registerEvents(eventListener, this);
        getCommand("setitem").setExecutor(new CommandHandler(this));
        getCommand("setitem").setTabCompleter(new ItemTabCompleter(this, luckPerms));
    }


    public EventListener getEventListener() {
        return eventListener;
    }

    private void afficherMessageDeDemarrage() {
        String bordure = ChatColor.DARK_GREEN + "§m================================================";
        String entete = ChatColor.GREEN + "                 ILLAMAGRADES";
        String piedDePage = ChatColor.DARK_GREEN + "§m================================================";

        getServer().getConsoleSender().sendMessage(bordure);
        getServer().getConsoleSender().sendMessage(entete);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "              Plugin par Illama ");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "                 Version: 1.0 ");
        getServer().getConsoleSender().sendMessage(piedDePage);
    }
}
