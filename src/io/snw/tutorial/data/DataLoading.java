
package io.snw.tutorial.data;

import io.snw.tutorial.ServerTutorial;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataLoading {
	
    private static DataLoading instance;
    private File dataFile;
    private YamlConfiguration data;
    private File playerDataFile;
    private YamlConfiguration playerData;

    public void loadData() {
        File f = new File(ServerTutorial.getInstance().getDataFolder(), "data.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataFile = f;
        data = YamlConfiguration.loadConfiguration(f);
        if (ServerTutorial.getInstance().getConfig().contains("tutorials")) {
            ConfigurationSection section = ServerTutorial.getInstance().getConfig().getConfigurationSection("tutorials");
            data.set("tutorials", section);
            saveData();
            ServerTutorial.getInstance().getConfig().set("tutorials", null);
            ServerTutorial.getInstance().saveConfig();
        }
    }

    public void loadPlayerData() {
        File f = new File(ServerTutorial.getInstance().getDataFolder(), "players.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerDataFile = f;
        playerData = YamlConfiguration.loadConfiguration(f);
    }
    
    public YamlConfiguration getPlayerData() {
        return this.playerData;
    }

    public YamlConfiguration getData() {
        return this.data;
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException exception) {
            ServerTutorial.getInstance().getLogger().warning("Failed to save data :(");
        }
    }

    public void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            ServerTutorial.getInstance().getLogger().warning("Failed to save player data :(");
        }
    }
    
    public static DataLoading getDataLoading() {
        if (instance == null) {
            instance = new DataLoading();
        }
        return instance;
    }
}
