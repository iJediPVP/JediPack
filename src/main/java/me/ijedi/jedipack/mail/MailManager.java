package me.ijedi.jedipack.mail;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MailManager {

    public static final String DIRECTORY = "mail";
    public static final String MAIL_KEY = "mailKey";
    public static final String MAIL_NUMBER_KEY = "mailNumberKey";
    public static final String MAIL_KEY_VALUE = "0b93641b-7e96-4c68-8154-a4e81144bc39";
    public static final String RECIPIENT_TAG = "recipientId";
    public static final String ATTACHMENT_KEY = "attach";

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
        book = updateMailAttachmentLore(book, recipientName);
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

    // Add attachment to the mail book
    public static ItemStack attachItem(ItemStack mailBook, ItemStack attachment){
        String itemString = Util.serializeItem(attachment);
        mailBook = Util.setNBTTagString(mailBook, ATTACHMENT_KEY, itemString);
        mailBook = updateMailAttachmentLore(mailBook, null);
        return mailBook;
    }

    // Remove attachment from the mail book
    public static ItemStack removeAttachment(ItemStack mailBook){
        mailBook = Util.removeNBTTag(mailBook, ATTACHMENT_KEY);
        mailBook = updateMailAttachmentLore(mailBook, null);
        return mailBook;
    }

    // Get the attached item from the mail book.
    public static ItemStack getAttachedItem(ItemStack mailBook){
        String attachmentString = Util.getNBTTagString(mailBook, ATTACHMENT_KEY);
        if(Util.isNullOrEmpty(attachmentString)){
            return null;
        }

        ItemStack attachment = Util.deseializeItem(attachmentString);
        return attachment;
    }

    // Update the lore on the given mail book to reflect the attachment
    private static ItemStack updateMailAttachmentLore(ItemStack mailBook, String address){

        ArrayList<String> loreList = new ArrayList<>();

        // Set the display name
        ItemMeta itemMeta = mailBook.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "Right Click To Write Mail");


        // Find the "addressed to" lore and copy it
        if(Util.isNullOrEmpty(address)) {
            for (String str : itemMeta.getLore()) {
                String stripped = ChatColor.stripColor(str);
                String[] split = stripped.split(":");
                if (split[0].toLowerCase().equals("addressed to") && split.length > 1) {
                    address = split[1];
                    break;
                }
            }
        }
        address = address.trim();
        loreList.add(ChatColor.GREEN + "Addressed to: " + ChatColor.GOLD + address);


        // And the attached item
        ItemStack attachedItem = getAttachedItem(mailBook);
        String attachStr = ChatColor.GREEN + "Attachment: ";
        if(attachedItem != null){
            attachStr += ChatColor.YELLOW + Integer.toString(attachedItem.getAmount()) + " x " + Util.getRealItemName(attachedItem);
        }else{
            attachStr += ChatColor.YELLOW + "None";
        }
        loreList.add(attachStr);


        // Add some help text for attachments
        loreList.add("");
        loreList.add(ChatColor.AQUA + "'/" + MailCommand.BASE_COMMAND + " " + MailCommand.ATTACH + "' " + ChatColor.GREEN + "to attach an item!");


        // Update the lore and return
        itemMeta.setLore(loreList);
        mailBook.setItemMeta(itemMeta);
        return mailBook;
    }

}
