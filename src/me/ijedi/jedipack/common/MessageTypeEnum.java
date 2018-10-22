package me.ijedi.jedipack.common;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum MessageTypeEnum {

    // Declare enums
    TabMessage("[TabMessage]", ChatColor.GREEN, ChatColor.BOLD, null);



    // Fields
    private String header;
    private ChatColor headerColor1;
    private ChatColor headerColor2;
    private ChatColor headerColor3;

    private static ChatColor messageColor = ChatColor.GREEN;
    private static ChatColor errorColor = ChatColor.RED;

    MessageTypeEnum(String header, ChatColor headerColor1, ChatColor headerColor2, ChatColor headerColor3){
        this.header = header;
        this.headerColor1 = headerColor1;
        this.headerColor2 = headerColor2;
        this.headerColor3 = headerColor3;
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

    // Logs a message to the plugin console.
    public void logMessage(String msg){
        String newMsg = formatMessage(msg, false, false);
        JediPackMain.getThisPlugin().getLogger().info(msg);
    }

    // Formats a message based on the given input.
    public String formatMessage(String msg, boolean useColors, boolean isError){
        if(useColors){

            // Build the colored message
            String coloredMessage = String.format("%s%s%s%s %s%s",
                    // Build header
                    (headerColor1 != null ? headerColor1.toString() : ""),
                    (headerColor2 != null ? headerColor2.toString() : ""),
                    (headerColor3 != null ? headerColor3.toString() : ""),
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
