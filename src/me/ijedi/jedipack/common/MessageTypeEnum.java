package me.ijedi.jedipack.common;

import me.ijedi.jedipack.JediPackMain;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum MessageTypeEnum {

    // Declare enums
    GeneralMessage("[JediPack]", ChatColor.GOLD, ChatColor.BOLD, ""),
    TabMessage("[TabMessage]", ChatColor.GOLD, ChatColor.BOLD, ""),
    MOTDMessage("[MOTD]", ChatColor.GOLD, ChatColor.BOLD, ""),
    ParkourMessage("[Parkour]", ChatColor.GOLD, ChatColor.BOLD, ""),
    SignLockMessage("[SignLock]", ChatColor.GOLD, ChatColor.BOLD, ChatColor.GREEN + "" + ChatColor.BOLD + "======= " + ChatColor.AQUA + "JediPack Sign Locks" + ChatColor.GREEN + "" + ChatColor.BOLD + " ======="),
    SmiteMessage("[Smite]", ChatColor.GOLD, ChatColor.BOLD, ""),
    HomeMessage("[Home]", ChatColor.GOLD, ChatColor.BOLD, ""),
    SleepMessage("[Sleep]", ChatColor.GOLD, ChatColor.BOLD, ""),
    MailMessage("[Mail]", ChatColor.GOLD, ChatColor.BOLD, ChatColor.GREEN + "" + ChatColor.BOLD + "======= " + ChatColor.AQUA + "JediPack Mail" + ChatColor.GREEN + "" + ChatColor.BOLD + " =======");



    // Fields
    private String header;
    private ChatColor headerColor1;
    private ChatColor headerColor2;
    private String listHeader;

    private static ChatColor messageColor = ChatColor.GREEN;
    private static ChatColor errorColor = ChatColor.RED;

    MessageTypeEnum(String header, ChatColor headerColor1, ChatColor headerColor2, String listHeader){
        this.header = header;
        this.headerColor1 = headerColor1;
        this.headerColor2 = headerColor2;
        this.listHeader = listHeader;
    }

    public String getListHeader(){
        return listHeader;
    }

    // Sends a message to the given sender.
    public void sendMessage(String msg, CommandSender sender, boolean isError){
        boolean useColors = false;
        if(sender != null && sender instanceof Player){
            useColors = true;
        }

        String newMsg = formatMessage(msg, useColors, isError);
        sender.sendMessage(newMsg);
    }

    // Sends a message to the given player.
    public void sendMessage(String msg, Player player, boolean isError){
        String newMsg = formatMessage(msg, true, isError);
        player.sendMessage(newMsg);
    }

    public void sendMessage(BaseComponent component, Player player){
        TextComponent msg = new TextComponent(headerColor1 + "" + headerColor2 + header + " ");
        msg.addExtra(component);
        player.spigot().sendMessage(msg);
    }

    // Logs a message to the plugin console.
    public void logMessage(String msg){
        String newMsg = formatMessage(msg, false, false);
        JediPackMain.getThisPlugin().getLogger().info(msg);
    }

    // Formats a message based on the given input.
    public String formatMessage(String msg, boolean useColors, boolean isError){
        if(useColors){

            // Build the colored message
            String coloredMessage = String.format("%s%s%s %s%s",
                    // Build header
                    (headerColor1 != null ? headerColor1.toString() : ""),
                    (headerColor2 != null ? headerColor2.toString() : ""),
                    //(headerColor3 != null ? headerColor3.toString() : ""),
                    header,

                    // Build message
                    (isError ? errorColor : messageColor),
                    msg
                    );
            return coloredMessage;

        } else {
            // No colors..
            msg = header + " " + msg;
            return msg;
        }
    }
}
