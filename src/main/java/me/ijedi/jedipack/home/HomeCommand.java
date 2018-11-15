package me.ijedi.jedipack.home;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HomeCommand implements TabExecutor {

    public static final String BASE_COMMAND = "home";
    private static final String SET = "set";
    private static final String UNSET = "unset";
    private static final String LIMIT = "limit";
    private static final String HELP = "help";
    private static final String LIST = "list";

    // Permissions
    private static final String HOMEPERM_USE = "jp.home.use";
    private static final String HOMEPERM_SET = "jp.home.set";
    private static final String HOMEPERM_UNSET = "jp.home.unset";
    private static final String HOMEPERM_LIMIT = "jp.home.limit";
    private static final String HOMEPERM_LIST = "jp.home.list";

    private final ArrayList<String> HELP_LIST = new ArrayList<String>(){{
        add(ChatColor.GREEN + "" + ChatColor.BOLD + "======= " + ChatColor.AQUA + "JediPack Homes" + ChatColor.GREEN + "" + ChatColor.BOLD + " =======");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " <homeName>" + ChatColor.GREEN + ": Teleport to the specified home.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + SET  + " <homeName>" + ChatColor.GREEN + ": Set a new home for the current location.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + UNSET  + " <homeName>" + ChatColor.GREEN + ": Removes the specified home.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + LIMIT  + " <newLimit>" + ChatColor.GREEN + ": Update the home limit.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + HELP + ChatColor.GREEN + ": Shows this help text.");
    }};

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        // Check for player
        if(!(commandSender instanceof Player)){
            MessageTypeEnum.HomeMessage.sendMessage("You must be a player to execute this!", commandSender, true);
        }
        Player player = (Player) commandSender;
        HomePlayerInfo playerInfo = HomeManager.getPlayerInfo(player.getUniqueId());

        // Handle args
        if(args.length > 0){
            String firstArg = args[0].toLowerCase();

            // Check the first argument
            if(firstArg.equals(SET)){
                //region SET HOME

                // Check perms
                if(!player.hasPermission(HOMEPERM_SET)){
                    MessageTypeEnum.HomeMessage.sendMessage("You do not have permission to use this command!", player, true);
                    return true;
                }

                // Check the second arg
                if(args.length > 1){
                    String homeName = args[1];

                    // Check for existing home
                    if(playerInfo.hasHome(homeName)){
                        MessageTypeEnum.HomeMessage.sendMessage("You already have a home named '" + homeName + "'", player, true);
                        return true;
                    }

                    // See if the player is over the home limit
                    if(playerInfo.getHomeCount() >= JediPackMain.homesLimit){
                        MessageTypeEnum.HomeMessage.sendMessage("You cannot set any more homes!", player, true);
                        return true;
                    }

                    // Set a new home
                    playerInfo.addHome(homeName, player.getLocation());
                    MessageTypeEnum.HomeMessage.sendMessage("Home '" + homeName + "' was set!", player, false);
                    return true;


                } else {
                    MessageTypeEnum.HomeMessage.sendMessage("You must specify a name for this home!", player, true);
                    return true;
                }

                //endregion

            } else if(firstArg.equals(UNSET)){
                //region UNSET HOME

                // Check perms
                if(!player.hasPermission(HOMEPERM_UNSET)){
                    MessageTypeEnum.HomeMessage.sendMessage("You do not have permission to use this command!", player, true);
                    return true;
                }

                // Check for second arg
                if(args.length > 1){
                    String homeName = args[1];

                    // If the home exists, remove it
                    if(playerInfo.hasHome(homeName)){
                        playerInfo.removeHome(homeName);
                        MessageTypeEnum.HomeMessage.sendMessage("Removed home '" + homeName + "'!", player, false);
                    } else {
                        MessageTypeEnum.HomeMessage.sendMessage("You do not have a home named '" + homeName + "'.", player, true);
                    }

                    return true;

                } else {
                    MessageTypeEnum.HomeMessage.sendMessage("You must specify a home to remove!", player, true);
                    return true;
                }

                //endregion

            } else if(firstArg.equals(LIMIT)) {
                //region LIMIT

                // Check perms
                if(!player.hasPermission(HOMEPERM_LIMIT)){
                    MessageTypeEnum.HomeMessage.sendMessage("You do not have permission to use this command!", player, true);
                    return true;
                }

                // If a second arg was given, update the limit. Else return the current limit.
                if (args.length > 1) {
                    String limitStr = args[1];
                    if (!Util.isInteger(limitStr)) {
                        MessageTypeEnum.HomeMessage.sendMessage("Invalid limit specified!", player, true);
                        return true;
                    }

                    int newLimit = Integer.parseInt(limitStr);
                    JediPackMain.homesLimit = newLimit;
                    JediPackMain.getThisPlugin().getConfig().set(JediPackMain.HOMES_LIMIT, newLimit);
                    JediPackMain.getThisPlugin().saveConfig();
                    MessageTypeEnum.HomeMessage.sendMessage("The homes limit has been updated!", player, false);
                    return true;
                }

                MessageTypeEnum.HomeMessage.sendMessage("The current homes limit is: " + JediPackMain.homesLimit, player, false);
                return true;

                //endregion

            } else if(firstArg.equals(HELP)) {
                //region HELP LIST
                for (String str : HELP_LIST) {
                    player.sendMessage(str);
                }
                return true;
                //endregion

            }else if(firstArg.equals(LIST)){

                //region LIST

                // Check perms
                if(!player.hasPermission(HOMEPERM_LIST)){
                    MessageTypeEnum.HomeMessage.sendMessage("You do not have permission to use this command!", player, true);
                    return true;
                }

                // Return a list of home names for this player
                if(playerInfo.getHomeCount() == 0){
                    MessageTypeEnum.HomeMessage.sendMessage("You don't have any homes set!", player, true);
                    return true;
                }

                ArrayList<String> homeNames = playerInfo.getHomeNames();
                String msg = "Homes: ";
                for(String str : homeNames){
                    msg += str + ", ";
                }
                msg = msg.substring(0, msg.length() - 2);
                MessageTypeEnum.HomeMessage.sendMessage(msg, player, false);
                return true;
                //endregion

            } else {
                //region TP to the specified home

                // Check perms
                if(!player.hasPermission(HOMEPERM_USE)){
                    MessageTypeEnum.HomeMessage.sendMessage("You do not have permission to use this command!", player, true);
                    return true;
                }

                // See if the home exists
                if(!playerInfo.hasHome(firstArg)){
                    MessageTypeEnum.HomeMessage.sendMessage("You do not have a home named '" + firstArg + "'.", player, true);
                } else {
                    sendPlayerHome(player, playerInfo, firstArg);
                }
                return true;
                //endregion

            }
        }

        //region TP to the default home
        // Check perms
        if(!player.hasPermission(HOMEPERM_USE)){
            MessageTypeEnum.HomeMessage.sendMessage("You do not have permission to use this command!", player, true);
            return true;
        }

        // Else no args were given.. If they have more than 1 home set, warn them, else send to their only home.
        if(playerInfo.getHomeCount() > 1){
            MessageTypeEnum.HomeMessage.sendMessage("You must specify a home!", player, true);
        } else {
            sendPlayerHome(player, playerInfo, "");
        }
        //endregion

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

        ArrayList<String> results = new ArrayList<>();
        if(!(commandSender instanceof Player)){
            return results;
        }

        switch(args.length){
            case 0:
            case 1:
                results.add(SET);
                results.add(UNSET);
                results.add(LIMIT);
                results.add(HELP);
                results.add(LIST);

                Player player = (Player) commandSender;
                HomePlayerInfo info = HomeManager.getPlayerInfo(player.getUniqueId());
                results.addAll(info.getHomeNames());
            default:
                break;
        }
        return results;
    }

    // Method to send the player home
    private void sendPlayerHome(Player player, HomePlayerInfo info, String homeName){

        // See if the player has any homes set
        if(info.getHomeCount() == 0){
            MessageTypeEnum.HomeMessage.sendMessage("You don't have any homes set!", player, true);
        } else {
            info.teleportHome(player, homeName);
        }
    }
}
