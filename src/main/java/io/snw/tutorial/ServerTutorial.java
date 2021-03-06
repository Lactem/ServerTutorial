package io.snw.tutorial;

import io.snw.tutorial.api.StartTutorialEvent;
import io.snw.tutorial.api.ViewSwitchEvent;
import io.snw.tutorial.commands.TutorialMainCommand;
import io.snw.tutorial.conversation.ConfigConversation;
import io.snw.tutorial.conversation.ViewConversation;
import io.snw.tutorial.data.Caching;
import io.snw.tutorial.data.DataLoading;
import io.snw.tutorial.data.Getters;
import io.snw.tutorial.data.Setters;
import io.snw.tutorial.enums.ViewType;
import io.snw.tutorial.util.TutorialTask;
import io.snw.tutorial.util.TutorialUtils;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerTutorial extends JavaPlugin {

	public static String PREFIX;
	
    private static ServerTutorial instance;

    private HashMap<String, Location> startLoc = new HashMap<String, Location>();
    private HashMap<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
    private HashMap<String, Boolean> flight = new HashMap<String, Boolean>();
    private ViewConversation viewConversation = new ViewConversation();
    private EndTutorial endTutorial = new EndTutorial(this);
    private ConfigConversation configConversation = new ConfigConversation();

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(new TutorialListener(), this);
        this.getCommand("tutorial").setExecutor(new TutorialMainCommand());
        this.saveDefaultConfig();
        PREFIX = ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix"));
        DataLoading.getDataLoading().loadData();
        DataLoading.getDataLoading().loadPlayerData();
        Caching.getCaching().casheAllData();
        Caching.getCaching().cacheConfigs();
        Caching.getCaching().cachePlayerData();
        TutorialTask.getTutorialTask().tutorialTask();
    }
    
/**
 * Start's tutorial
 * @param tutorialName tutorial name
 * @param player Player
 */
    public void startTutorial(String tutorialName, Player player) {
        String name = this.getServer().getPlayer(player.getUniqueId()).getName();
        if (DataLoading.getDataLoading().getData().getConfigurationSection("tutorials") == null) {
            player.sendMessage(ChatColor.RED + "You need to set up a tutorial first! /tutorial create <message>");
            return;
        }
        if (Getters.getGetters().getTutorial(tutorialName) == null) {
            player.sendMessage("Invalid tutorial");
            return;
        }
        if (DataLoading.getDataLoading().getData().getConfigurationSection("tutorials." + tutorialName + ".views") == null) {
            player.sendMessage(ChatColor.RED + "You need to set up a view first! /tutorial addview <tutorial name>");
            return;
        }
        this.startLoc.put(name, player.getLocation());
        this.addInventory(name, player.getInventory().getContents());
        this.addFlight(name, player.getAllowFlight());
        player.getInventory().clear();
        player.setAllowFlight(true);
        player.setFlying(true);
        this.initializeCurrentView(name);
        Setters.getSetters().addCurrentTutorial(name, tutorialName);
        Setters.getSetters().addToTutorial(name);
        for (Player online : this.getServer().getOnlinePlayers()) {
            online.hidePlayer(player);
            player.hidePlayer(online);
        }
        this.getServer().getPlayer(player.getUniqueId()).teleport(Getters.getGetters().getTutorialView(tutorialName, name).getLocation());
        if (Getters.getGetters().getTutorial(tutorialName).getViewType() == ViewType.TIME) {
            Getters.getGetters().getTutorialTimeTask(tutorialName, name);
        }
        TutorialUtils.getTutorialUtils().textUtils(player);
        StartTutorialEvent event = new StartTutorialEvent(player, Getters.getGetters().getTutorial(tutorialName));
        this.getServer().getPluginManager().callEvent(event);
        if(DataLoading.getDataLoading().getPlayerData().get("players." + player.getUniqueId()) == null) {
            DataLoading.getDataLoading().getPlayerData().set("players." + player.getUniqueId() + ".seen", "true");
            DataLoading.getDataLoading().getPlayerData().set("players." + player.getUniqueId() + ".tutorials." + tutorialName, "false");
            DataLoading.getDataLoading().savePlayerData();
            Caching.getCaching().reCachePlayerData();
        } else if(DataLoading.getDataLoading().getPlayerData().get("players." + player.getUniqueId() + ".tutorials." + tutorialName) == null) {
            DataLoading.getDataLoading().getPlayerData().set("players." + player.getUniqueId() + ".tutorials." + tutorialName, "false");
            DataLoading.getDataLoading().savePlayerData();
            Caching.getCaching().reCachePlayerData();
        }
    }
/**
 * Removes player from tutorial
 * @param name Player name 
 */
    public void removeFromTutorial(String name) {
        Caching.getCaching().playerInTutorial().remove(name);
        this.startLoc.remove(name);
        Caching.getCaching().currentTutorial().remove(name);
        Caching.getCaching().currentTutorialView().remove(name);
        this.flight.remove(name);
    }

    public void initializeCurrentView(String name) {
        Caching.getCaching().currentTutorialView().put(name, 1);
    }

    public void incrementCurrentView(String name) {
        TutorialView fromTutorialView = Getters.getGetters().getTutorialView(name);
        Caching.getCaching().currentTutorialView().put(name, Getters.getGetters().getCurrentView(name) + 1);
        TutorialView toTutorialView = Getters.getGetters().getTutorialView(name);
        ViewSwitchEvent event = new ViewSwitchEvent(Bukkit.getPlayerExact(name), fromTutorialView, toTutorialView, Getters.getGetters().getCurrentTutorial(name));
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public Location getFirstLoc(String name) {
        return this.startLoc.get(name);
    }

    public void cleanFirstLoc(String name) {
        this.startLoc.remove(name);
    }

    public ItemStack[] getInventory(String name) {
        return this.inventories.get(name);
    }

    public void addInventory(String name, ItemStack[] items) {
        this.inventories.put(name, items);
    }

    public void cleanInventory(String name) {
        this.inventories.remove(name);
    }

    public boolean getFlight(String name) {
        return this.flight.get(name);
    }

    public void addFlight(String name, boolean flight) {
        this.flight.put(name, flight);
    }
    
    public void removeFlight(String name) {
        this.flight.remove(name);
    }

    public EndTutorial getEndTutorial() {
        return endTutorial;
    }

    public String tACC(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public ViewConversation getViewConversation() {
        return viewConversation;
    }

    public ConfigConversation getConfigConversation() {
        return configConversation;
    }

    public void removeTutorial(String tutorialName) {
        DataLoading.getDataLoading().getData().set("tutorials." + tutorialName, null);
        DataLoading.getDataLoading().saveData();
        Caching.getCaching().reCasheTutorials();
    }

    public void removeTutorialView(String tutorialName, int viewID) {
        int viewsCount = Getters.getGetters().getTutorial(tutorialName).getTotalViews();
        DataLoading.getDataLoading().getData().set("tutorials." + tutorialName + ".views." + viewID, null);
        DataLoading.getDataLoading().saveData();
        if(viewsCount != viewID) {
            for(String vID : DataLoading.getDataLoading().getData().getConfigurationSection("tutorials." + tutorialName + ".views").getKeys(false)) {
                int currentID = Integer.parseInt(vID);
                int newViewID = Integer.parseInt(vID) - 1;
                String message = DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".views." + currentID + ".message");
                String messageType = DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".views." + currentID + ".messagetype");
                String location = DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".views." + currentID + ".location");
                DataLoading.getDataLoading().getData().set("tutorials." + tutorialName + ".views." + newViewID + ".message", message);
                DataLoading.getDataLoading().getData().set("tutorials." + tutorialName + ".views." + newViewID + ".messagetype", messageType);
                DataLoading.getDataLoading().getData().set("tutorials." + tutorialName + ".views." + newViewID + ".location" , location);
                DataLoading.getDataLoading().getData().set("tutorials." + tutorialName + ".views." + currentID, null);
                }
        }
        DataLoading.getDataLoading().saveData();
        Caching.getCaching().reCasheTutorials();
    }

    public static ServerTutorial getInstance() {
        return instance;
    }
}
