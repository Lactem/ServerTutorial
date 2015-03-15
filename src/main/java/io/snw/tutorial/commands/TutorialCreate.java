
package io.snw.tutorial.commands;

import io.snw.tutorial.ServerTutorial;
import io.snw.tutorial.conversation.CreateTutorial;
import io.snw.tutorial.data.Getters;
import io.snw.tutorial.enums.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TutorialCreate implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (Permissions.CREATE.hasPerm(sender)) {
            if (args[0].equalsIgnoreCase("create")) {
                if (!Getters.getGetters().getAllTutorials().contains(args[1].toLowerCase())) {
                    CreateTutorial.getCreateTutorial().createNewTutorial(player, args[1].toLowerCase());
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "This tutorial already exists!");
                    return true;
                }
                } else if (args[0].equalsIgnoreCase("addview")) {
                    if (!Getters.getGetters().getAllTutorials().contains(args[1].toLowerCase())) {
                        sender.sendMessage(ChatColor.RED + "You must create this tutorial first! " + ChatColor.GOLD + "/tutorial create <name>");
                        return true;
                    }
                    ServerTutorial.getInstance().getViewConversation().createNewView(player, args[1].toLowerCase());
                    return true;

                } else {
                    sender.sendMessage(ChatColor.RED + "Try " + ChatColor.GOLD + "/tutorial help");
                    return true;    
                }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission for this!");
            return true;
        }
    }
}
