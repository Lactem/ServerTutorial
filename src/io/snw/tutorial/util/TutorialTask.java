package io.snw.tutorial.util;

import io.snw.tutorial.ServerTutorial;
import io.snw.tutorial.data.Getters;
import io.snw.tutorial.enums.MessageType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class TutorialTask {
    private static TutorialTask instance;

    public void tutorialTask() {
        new BukkitRunnable() {

            @Override
            public void run() {
                for (String name : Getters.getGetters().getAllInTutorial()) {

                    Player player = ServerTutorial.getInstance().getServer().getPlayerExact(name);
                    if (!player.isDead()) {
                        player.closeInventory();
                        if (Getters.getGetters().getTutorialView(name).getMessageType() == MessageType.META) {
                            setPlayerItemName(player);
                        }
                    }
                }
            }
        }.runTaskTimer(ServerTutorial.getInstance(), 0L, 5L);
    }

    public void tutorialTimeTask(String tutorialName, final String name) {
        long num = (long) Getters.getGetters().getTutorial(tutorialName).getTimeLength();
        Long timeLength = num * 20L;

        new BukkitRunnable() {

            @Override
            public void run() {
                Player player = ServerTutorial.getInstance().getServer().getPlayerExact(name);
                if (Getters.getGetters().getCurrentTutorial(name).getTotalViews() == Getters.getGetters().getCurrentView(name)) {
                    ServerTutorial.getInstance().getEndTutorial().endTutorial(player);
                    cancel();
                    return;
                }
                if (Getters.getGetters().getTutorialView(name).getMessageType() == MessageType.META) {
                    setPlayerItemName(player);
                }
                ServerTutorial.getInstance().incrementCurrentView(name);
                TutorialUtils.getTutorialUtils().textUtils(player);
                player.teleport(Getters.getGetters().getTutorialView(name).getLocation());
            }

        }.runTaskTimer(ServerTutorial.getInstance(), timeLength, timeLength);
    }

    public String tACC(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    boolean reset = true;
    String alt;

    public void setPlayerItemName(Player player) {
        ItemStack i = new ItemStack(Getters.getGetters().getCurrentTutorial(player.getName()).getItem());
        ItemMeta data = i.getItemMeta();
        if (reset) {
            alt = "" + ChatColor.RESET;
            reset = false;
        } else {
            alt = "";
            reset = true;
        }
        data.setDisplayName(tACC(Getters.getGetters().getTutorialView(player.getName()).getMessage()) + alt);

        i.setItemMeta(data);
        player.setItemInHand(i);
    }
    
    public static TutorialTask getTutorialTask() {
        if (instance == null) {
            instance = new TutorialTask();
        }
        return instance;
    }
}
