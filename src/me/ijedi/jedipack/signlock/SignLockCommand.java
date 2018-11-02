package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignLockCommand implements TabExecutor {

    public static final String BASE_COMMAND = "signlock";
    public static final String SHARE = "share";
    public static final String REVOKE = "revoke";
    public static final String HOPPERS = "hoppers";

    // Permissions
    private final String SIGNLOCKPERM_SHARE = "jp.signlock.share";
    private final String SIGNLOCKPERM_REVOKE = "jp.signlock.revoke";
    private final String SIGNLOCKPERM_HOPPERS = "jp.signlock.hoppers";
    public static final String SIGNLOCKPERM_LIMIT_BYPASS = "jp.signlock.limitBypass";


    private final ArrayList<String> HELP_LIST = new ArrayList<String>(){{
        add(ChatColor.GREEN + "" + ChatColor.BOLD + "======= " + ChatColor.AQUA + "JediPack Sign Locks" + ChatColor.GREEN + "" + ChatColor.BOLD + " =======");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + SHARE + " <lockNumber> <playerName>..." + ChatColor.GREEN + ": Grants access to the specified players for the specified lock number.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + REVOKE + " <lockNumber> <playerName>..." + ChatColor.GREEN + ": Revokes access from the specified players for the specified lock number.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + HOPPERS + " <lockNumber>" + ChatColor.GREEN + ": Toggles if hoppers can interact with the locked container.");
    }};

    /*
    Commands:
    /signlock share <lockNumber> <playerName>...    : Give access to another player
    /signlock revoke <lockNumber> <playerName>...   : Remove access from a player
    /signlock hoppers <lockNumber>                 : Toggle if hoppers can interact with the locked container

    * */

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        // Only a player can run this
        if(!(commandSender instanceof Player)) {
            MessageTypeEnum.SignLockMessage.sendMessage("This command can only be executed by a player!", commandSender, true);
            return true;
        }


        // Check for args
        Player player = (Player)commandSender;
        if(args.length > 0){
            String firstArg = args[0].toLowerCase();

            if(firstArg.equals(SHARE)){
                ////// Give access to a container

                // Check perms
                if(Util.hasNoPerms(player, SIGNLOCKPERM_SHARE, MessageTypeEnum.SignLockMessage)){
                    return true;
                }

                // Check for a lock number
                if(args.length > 1){
                    String lockStr = args[1];

                    // Validate
                    if(!Util.isInteger(lockStr)){
                        MessageTypeEnum.SignLockMessage.sendMessage("Invalid lock number specified.", player, true);
                        return true;
                    }
                    int lockNumber = Integer.parseInt(lockStr);

                    SignLockPlayerInfo playerInfo = SignLockManager.getPlayerInfo(player.getUniqueId());
                    SignLock signLock = playerInfo.getLockByNumber(lockNumber);
                    if(signLock == null){
                        MessageTypeEnum.SignLockMessage.sendMessage("Invalid lock number specified.", player, true);
                        return true;
                    }


                    // Search the remaining args for player names
                    if(args.length == 2){
                        MessageTypeEnum.SignLockMessage.sendMessage("You must specify a player!", player, true);
                        return true;
                    }

                    ArrayList<UUID> validPlayerIds = new ArrayList<>();
                    ArrayList<String> foundPlayers = new ArrayList<>();
                    ArrayList<String> invalidNames = new ArrayList<>();
                    ArrayList<String> alreadyAddedNames = new ArrayList<>();
                    // Skip the first & second args
                    for(int x = 2; x < args.length; x++){

                        // Check player name
                        String playerName = args[x];
                        @SuppressWarnings("deprecation")
                        OfflinePlayer newPlayer = Bukkit.getOfflinePlayer(playerName); // I know this is deprecated but I don't think there is another option.
                        if(newPlayer == null){
                            invalidNames.add(playerName);
                        } else {

                            // Don't allow the player to add just themselves..
                            if(newPlayer.getUniqueId().equals(player.getUniqueId())){
                                MessageTypeEnum.SignLockMessage.sendMessage("You cannot share with yourself!", player, true);
                                continue;
                            }

                            // See if we already have this player added
                            if(signLock.hasContainerAccess(newPlayer.getUniqueId())){
                                alreadyAddedNames.add(newPlayer.getName());
                                continue;
                            }

                            // Finally, a valid player
                            foundPlayers.add(newPlayer.getName());
                            validPlayerIds.add(newPlayer.getUniqueId());
                        }
                    }

                    //// Check our search results

                    // Add the found players
                    for(UUID newPlayerId : validPlayerIds){
                        playerInfo.addSharedPlayedToLock(signLock, newPlayerId);
                    }

                    //// Output the results
                    // Output for added players
                    if(foundPlayers.size() > 0){
                        String message = "Players added: ";
                        for(String found : foundPlayers){
                            message += found + ", ";
                        }

                        message = message.substring(0, message.length() - 2);
                        MessageTypeEnum.SignLockMessage.sendMessage(message, player, false);
                    }

                    // Output for already added players
                    if(alreadyAddedNames.size() > 0){
                        String message = "You are already sharing this container with: ";
                        for(String found : alreadyAddedNames){
                            message += found + ", ";
                        }

                        message = message.substring(0, message.length() - 2);
                        MessageTypeEnum.SignLockMessage.sendMessage(message, player, true);
                    }

                    // Output for invalid names
                    if(invalidNames.size() > 0){
                        String message = "Players not found: ";
                        for(String found : invalidNames){
                            message += found + ", ";
                        }

                        message = message.substring(0, message.length() - 2);
                        MessageTypeEnum.SignLockMessage.sendMessage(message, player, true);
                    }

                    // FINALLY DONE
                    return true;

                } else {
                    MessageTypeEnum.SignLockMessage.sendMessage("You must specify a lock number!", player, true);
                    return true;
                }

            } else if(firstArg.equals(REVOKE)){
                ////// Revoke access to a container

                // Check perms
                if(Util.hasNoPerms(player, SIGNLOCKPERM_REVOKE, MessageTypeEnum.SignLockMessage)){
                    return true;
                }

                // Check for a lock number
                if(args.length > 1){
                    String lockStr = args[1];

                    // Validate
                    if(!Util.isInteger(lockStr)){
                        MessageTypeEnum.SignLockMessage.sendMessage("Invalid lock number specified.", player, true);
                        return true;
                    }
                    int lockNumber = Integer.parseInt(lockStr);

                    SignLockPlayerInfo playerInfo = SignLockManager.getPlayerInfo(player.getUniqueId());
                    SignLock signLock = playerInfo.getLockByNumber(lockNumber);
                    if(signLock == null){
                        MessageTypeEnum.SignLockMessage.sendMessage("Invalid lock number specified.", player, true);
                        return true;
                    }


                    // Search the remaining args for player names
                    if(args.length == 2){
                        MessageTypeEnum.SignLockMessage.sendMessage("You must specify a player!", player, true);
                        return true;
                    }

                    ArrayList<UUID> validPlayerIds = new ArrayList<>();
                    ArrayList<String> foundPlayers = new ArrayList<>();
                    ArrayList<String> invalidNames = new ArrayList<>();
                    ArrayList<String> noAccessNames = new ArrayList<>();
                    // Skip the first & second args
                    for(int x = 2; x < args.length; x++){

                        // Check player name
                        String playerName = args[x];
                        @SuppressWarnings("deprecation")
                        OfflinePlayer newPlayer = Bukkit.getOfflinePlayer(playerName); // I know this is deprecated but I don't think there is another option.
                        if(newPlayer == null){
                            invalidNames.add(playerName);
                        } else {

                            // Don't allow the player to add just themselves..
                            if(newPlayer.getUniqueId().equals(player.getUniqueId())){
                                MessageTypeEnum.SignLockMessage.sendMessage("You cannot revoke access from yourself!", player, true);
                                continue;
                            }

                            // See if we don't have this player added
                            if(!signLock.hasContainerAccess(newPlayer.getUniqueId())){
                                noAccessNames.add(newPlayer.getName());
                                continue;
                            }

                            // Finally, a valid player
                            foundPlayers.add(newPlayer.getName());
                            validPlayerIds.add(newPlayer.getUniqueId());
                        }
                    }

                    //// Check our search results

                    // Remove the found players
                    for(UUID newPlayerId : validPlayerIds){
                        playerInfo.removeSharedPlayerFromLock(signLock, newPlayerId);
                    }

                    //// Output the results
                    // Output for added players
                    if(foundPlayers.size() > 0){
                        String message = "Players removed: ";
                        for(String found : foundPlayers){
                            message += found + ", ";
                        }

                        message = message.substring(0, message.length() - 2);
                        MessageTypeEnum.SignLockMessage.sendMessage(message, player, false);
                    }

                    // Output for already added players
                    if(noAccessNames.size() > 0){
                        String message = "These players do not have access: ";
                        for(String found : noAccessNames){
                            message += found + ", ";
                        }

                        message = message.substring(0, message.length() - 2);
                        MessageTypeEnum.SignLockMessage.sendMessage(message, player, true);
                    }

                    // Output for invalid names
                    if(invalidNames.size() > 0){
                        String message = "Players not found: ";
                        for(String found : invalidNames){
                            message += found + ", ";
                        }

                        message = message.substring(0, message.length() - 2);
                        MessageTypeEnum.SignLockMessage.sendMessage(message, player, true);
                    }

                    // FINALLY DONE
                    return true;

                } else {
                    MessageTypeEnum.SignLockMessage.sendMessage("You must specify a lock number!", player, true);
                    return true;
                }

            } else if(firstArg.equals(HOPPERS)){
                ////// Toggle hoppers

                // Check perms
                if(Util.hasNoPerms(player, SIGNLOCKPERM_HOPPERS, MessageTypeEnum.SignLockMessage)){
                    return true;
                }

                // Check for a lock number
                if(args.length > 1){
                    String lockStr = args[1];

                    // Validate
                    if(!Util.isInteger(lockStr)){
                        MessageTypeEnum.SignLockMessage.sendMessage("Invalid lock number specified.", player, true);
                        return true;
                    }
                    int lockNumber = Integer.parseInt(lockStr);

                    SignLockPlayerInfo playerInfo = SignLockManager.getPlayerInfo(player.getUniqueId());
                    SignLock signLock = playerInfo.getLockByNumber(lockNumber);
                    if(signLock == null){
                        MessageTypeEnum.SignLockMessage.sendMessage("Invalid lock number specified.", player, true);
                        return true;
                    }

                    // Toggle the hoppers!
                    boolean hoppersEnabled = playerInfo.toggleHoppersForLock(signLock);
                    String message = "Hoppers are now " + (hoppersEnabled ? "enabled" : "disabled") + "!";
                    MessageTypeEnum.SignLockMessage.sendMessage(message, player, false);
                    return  true;

                } else {
                    MessageTypeEnum.SignLockMessage.sendMessage("You must specify a lock number!", player, true);
                    return true;
                }
            }
        }

        for(String msg : HELP_LIST){
            player.sendMessage(msg);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> results = new ArrayList<String>();

        // Only return for players
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;

            // Figure out what to return
            int argsLength = strings.length;

            switch(argsLength){
                case 1:
                    // Add first args
                    results.add(SHARE);
                    results.add(REVOKE);
                    results.add(HOPPERS);
                    break;
                case 2:
                    //Add second args - numbers of the sign locks this player has
                    SignLockPlayerInfo info = SignLockManager.getPlayerInfo(player.getUniqueId());
                    for(SignLock lock : info.getSignLocks().values()){
                        results.add(Integer.toString(lock.getLockNumber()));
                    }

                    break;
                default:
                    break;
            }


        }

        return results;
    }
}
