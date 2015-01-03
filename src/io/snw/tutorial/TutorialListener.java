package io.snw.tutorial;

import io.snw.tutorial.api.EndTutorialEvent;
import io.snw.tutorial.data.Caching;
import io.snw.tutorial.data.DataLoading;
import io.snw.tutorial.data.Getters;
import io.snw.tutorial.enums.MessageType;
import io.snw.tutorial.util.TutorialUtils;
import io.snw.tutorial.util.UUIDFetcher;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TutorialListener implements Listener {
	
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        if (event.getAction() != Action.PHYSICAL) {
            if (Getters.getGetters().isInTutorial(name)) {
                if (player.getItemInHand().getType() == Getters.getGetters().getCurrentTutorial(name).getItem()) {
                    if (Getters.getGetters().getCurrentTutorial(name).getTotalViews() == Getters.getGetters().getCurrentView(name)) {
                            ServerTutorial.getInstance().getEndTutorial().endTutorial(player);
                        } else {
                            ServerTutorial.getInstance().incrementCurrentView(name);
                            TutorialUtils.getTutorialUtils().textUtils(player);
                            player.teleport(Getters.getGetters().getTutorialView(name).getLocation());
                            if (Getters.getGetters().getTutorialView(name).getMessageType() == MessageType.TEXT) {
                                player.sendMessage(ServerTutorial.PREFIX + TutorialUtils.getTutorialUtils().tACC(Getters.getGetters().getTutorialView(name).getMessage()));
                            }
                        }
                }
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK && !Getters.getGetters().isInTutorial(name)) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.stripColor(Getters.getGetters().getConfigs().signSetting()))) {
                    if (sign.getLine(1) == null) return;
                    ServerTutorial.getInstance().startTutorial(sign.getLine(1), player);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (Getters.getGetters().isInTutorial(player.getName())) {
            player.teleport(Getters.getGetters().getTutorialView(player.getName()).getLocation());               
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Getters.getGetters().isInTutorial(event.getPlayer().getName())) {
            ServerTutorial.getInstance().removeFromTutorial(event.getPlayer().getName());
        }
        if (!ServerTutorial.getInstance().getServer().getOnlineMode()) {
            Caching.getCaching().getResponse().remove(player.getName());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (Getters.getGetters().isInTutorial(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (Getters.getGetters().isInTutorial(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (Getters.getGetters().isInTutorial(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(PlayerDropItemEvent event) {
        if (Getters.getGetters().isInTutorial(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWhee(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (Getters.getGetters().isInTutorial(((Player) event.getEntity()).getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        final String playerName = player.getName();
        if (!ServerTutorial.getInstance().getServer().getOnlineMode()) {
            ServerTutorial.getInstance().getServer().getScheduler().runTaskAsynchronously(ServerTutorial.getInstance(), new Runnable() {
                @Override
                public void run() {
                    try {
                        Caching.getCaching().getResponse().put(playerName, UUIDFetcher.getUUIDOf(playerName));
                    } catch (Exception e) {
                        
                    }
                }
            });
        }
        for (String name : Getters.getGetters().getAllInTutorial()) {
            Player tut = ServerTutorial.getInstance().getServer().getPlayerExact(name);
            tut.hidePlayer(player);
            player.hidePlayer(tut);
        }
        if (!player.hasPlayedBefore()) {
            if (Getters.getGetters().getConfigs().firstJoin()) {
               ServerTutorial.getInstance().startTutorial(Getters.getGetters().getConfigs().firstJoinTutorial(), player);
            }
        }
    }
    
    @EventHandler
    public void onTutorialEnd(EndTutorialEvent event) {
        DataLoading.getDataLoading().getPlayerData().set("players." + event.getPlayer().getUniqueId() + ".tutorials." + event.getTutorial().getName(), "true");
        DataLoading.getDataLoading().savePlayerData();
        Caching.getCaching().reCachePlayerData();
    }

    public boolean seenTutorial(String name, String tutorial) {
        if (Getters.getGetters().getPlayerData().containsKey(name)) {
            if (Getters.getGetters().getPlayerData(name).getPlayerTutorialData().containsKey(tutorial)) {
                return Getters.getGetters().getPlayerData(name).getPlayerTutorialData().get(tutorial).getSeen();
            }
        }
        return false;
    }
}
