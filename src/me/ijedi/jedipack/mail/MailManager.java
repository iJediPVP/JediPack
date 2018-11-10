package me.ijedi.jedipack.mail;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MailManager {

    public static final String DIRECTORY = "mail";
    public static final String MAIL_KEY = "mailKey";
    public static final String MAIL_NUMBER_KEY = "mailNumberKey";
    public static final String MAIL_KEY_VALUE = "0b93641b-7e96-4c68-8154-a4e81144bc39";
    public static final String RECIPIENT_TAG = "recipientId";

    private static final HashMap<UUID, MailPlayerInfo> playerInfos = new HashMap<>();

    public static void initialize(){

        // Check if enabled
        if(!JediPackMain.isMailEnabled){
            MessageTypeEnum.MailMessage.logMessage("Mail is disabled!");
            return;
        }
        MessageTypeEnum.MailMessage.logMessage("Mail is enabled!");

        JavaPlugin plugin = JediPackMain.getThisPlugin();
        plugin.getCommand(MailCommand.BASE_COMMAND).setExecutor(new MailCommand());
        plugin.getCommand(MailCommand.BASE_COMMAND).setTabCompleter(new MailCommand());
        plugin.getServer().getPluginManager().registerEvents(new MailEvents(), plugin);

    }

    // Returns the player info for the given player id.
    public static MailPlayerInfo getMailPlayerInfo(UUID playerId){
        if(!playerInfos.containsKey(playerId)){
            MailPlayerInfo info = new MailPlayerInfo(playerId);
            info.loadPlayerInfo();
            playerInfos.put(playerId, info);
            return info;
        }
        return playerInfos.get(playerId);
    }

    // Returns a new book to write mail in
    public ItemStack getNewWritableBook(String recipientName){
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.setDisplayName(ChatColor.GREEN + "Right Click To Write Mail");
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(ChatColor.GREEN + "Addressed to: " + ChatColor.GOLD + recipientName);
        bookMeta.setLore(loreList);
        book.setItemMeta(bookMeta);
        book = Util.setNBTTagString(book, RECIPIENT_TAG, recipientName);
        book = Util.setNBTTagString(book, MAIL_KEY, MAIL_KEY_VALUE);
        return book;
    }

    // Returns true if the given item is a mail book
    public static boolean isMailBook(ItemStack item){
        String mailKey = Util.getNBTTagString(item, MAIL_KEY);
        if(!Util.isNullOrEmpty(mailKey)){
            return true;
        }
        return false;
    }
}
