package me.ijedi.jedipack.mail;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Map;
import java.util.UUID;

public class MailEvents implements Listener {

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event){

        // We only care if the book is being signed
        if(!event.isSigning()){
            return;
        }

        // See if the current item is a "mail" book.
        Player player = event.getPlayer();
        @SuppressWarnings("deprecation")
        int bookSlot = event.getSlot();
        ItemStack heldItem = player.getInventory().getItem(bookSlot);
        String mailKey = Util.getNBTTagString(heldItem, MailManager.MAIL_KEY);
        if(!heldItem.getType().equals(Material.WRITABLE_BOOK) || Util.isNullOrEmpty(mailKey) || !mailKey.equals(MailManager.MAIL_KEY_VALUE)){
            return;
        }

        // Get the recipient info
        String recipientName = Util.getNBTTagString(heldItem, MailManager.RECIPIENT_TAG);
        //MessageTypeEnum.MailMessage.sendMessage(recipientId, player, false);

        // Get the book's info
        BookMeta bookMeta = event.getNewBookMeta();
        String[] bookLines = bookMeta.getPages().toArray(new String[bookMeta.getPages().size()]);
        String subject = bookMeta.getTitle();
        UUID senderId = player.getUniqueId();
        @SuppressWarnings("deprecation")
        OfflinePlayer recipientPlayer = Bukkit.getOfflinePlayer(recipientName);
        String senderName = player.getName();

        // Create mail and send it
        MailPlayerInfo recipientInfo = MailManager.getMailPlayerInfo(recipientPlayer.getUniqueId());
        int nextNumber = recipientInfo.getNextMailNumber();
        MailInfo info = new MailInfo(player.getName(), recipientPlayer.getUniqueId(), nextNumber);
        info.setSubject(subject);
        info.setMessage(bookLines);
        info.setSenderName(senderName);
        recipientInfo.updateMail(info);

        // Delete this item from the sender's inventory
        player.getInventory().setItem(bookSlot, null);

        MessageTypeEnum.MailMessage.sendMessage("Your mail has been sent to " + recipientPlayer.getName() + "!", player, false);
        event.setCancelled(true);
    }
}
