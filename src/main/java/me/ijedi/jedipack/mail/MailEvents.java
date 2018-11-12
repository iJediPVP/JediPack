package me.ijedi.jedipack.mail;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;

public class MailEvents implements Listener {

    // Called when a player edit's book
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

        // Get the book's info
        BookMeta bookMeta = event.getNewBookMeta();
        String[] bookLines = bookMeta.getPages().toArray(new String[bookMeta.getPages().size()]);
        String subject = bookMeta.getTitle();
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
        info.setMailDate(new Date());
        recipientInfo.updateMail(info);

        // Alert the player and cancel the event
        MessageTypeEnum.MailMessage.sendMessage("Your mail has been sent to " + recipientPlayer.getName() + "!", player, false);
        event.setCancelled(true);

        // Remove the item 1 tick later.. Else the item will reappear in the inventory..
        new BukkitRunnable(){
            @Override
            public void run(){
                player.getInventory().setItem(bookSlot, new ItemStack(Material.AIR));
                this.cancel();
            }
        }.runTaskLater(JediPackMain.getThisPlugin(), 1L);
    }

    // Prevent players from moving a mail book
    @EventHandler
    public void onInvClick(InventoryClickEvent event){

        JediPackMain.getThisPlugin().getLogger().info("inv click");



        if(event.getClickedInventory() != null){

            Player player = (Player) event.getWhoClicked();
            String invName = event.getClickedInventory().getTitle();
            if(!Util.isNullOrEmpty(invName) && event.getCurrentItem() != null){

                ItemStack currentItem = event.getCurrentItem();
                String invPrefix = invName.split(":")[0];

                // Handle different inventories
                if(invPrefix.equals(MailPlayerInfo.SETTINGS_PREFIX)){
                    // region Handle clicks in a settings inventory

                    if(currentItem.getItemMeta() == null){
                        return;
                    }
                    String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

                    if(itemName.equals(MailPlayerInfo.ALERTS_NAME)){
                        // Alerts
                        MailPlayerInfo info = MailManager.getMailPlayerInfo(player.getUniqueId());
                        info.toggleAlertsEnabled();
                        MessageTypeEnum.MailMessage.sendMessage("Alerts were toggled!", player, false);
                        player.closeInventory();

                    } else if(itemName.equals(MailPlayerInfo.UI_NAME)){
                        // UI
                        MailPlayerInfo info = MailManager.getMailPlayerInfo(player.getUniqueId());
                        info.toggleUIEnabled();
                        MessageTypeEnum.MailMessage.sendMessage("UI was toggled!", player, false);
                        player.closeInventory();

                    }
                    event.setCancelled(true);
                    return;
                    //endregion

                } else if(invPrefix.equals(MailPlayerInfo.MAILBOX_PREFIX)){

                    // region Handle clicks in the mail box
                    // Check the item NBT for a mail number
                    String mailNumberStr = Util.getNBTTagString(currentItem, MailManager.MAIL_NUMBER_KEY);
                    if(Util.isNullOrEmpty(mailNumberStr) || !Util.isInteger(mailNumberStr)){
                        return;
                    }

                    // Get the mail info
                    int mailNumber = Integer.parseInt(mailNumberStr);
                    MailPlayerInfo info = MailManager.getMailPlayerInfo(player.getUniqueId());
                    MailInfo mailInfo = info.getMailInfo(mailNumber);

                    if(event.isLeftClick()){
                        // Left click opens
                        info.openMail(player, mailInfo);

                    } else if(event.isRightClick() && event.isShiftClick()){
                        // Shift + Right Click deletes
                        info.deleteMail(mailInfo);
                        MessageTypeEnum.MailMessage.sendMessage("Mail has been deleted!", player, false);
                        if(!info.hasAnyMail()){
                            MessageTypeEnum.MailMessage.sendMessage("Your mail box is empty!", player, false);
                            player.closeInventory();
                        } else {
                            player.openInventory(info.getMailBoxInventory(player));
                        }
                    }
                    event.setCancelled(true);
                    return;
                    // endregion

                } else if(!event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)){
                    // Allow creative to do what they want. The code below dupes items in creative mode..

                    //region Handle hot bar buttons in the player's inventory and other inventories (like chests, anvils)
                    ItemStack item = event.getCurrentItem();
                    int hotbarButton = event.getHotbarButton();
                    if(hotbarButton > -1){
                        item = event.getClickedInventory().getItem(hotbarButton);
                    }
                    if(hotbarButton > -1 && !event.getClickedInventory().equals(player.getInventory())){
                        item = player.getInventory().getItem(hotbarButton);
                    }
                    ItemStack slotItem = event.getClickedInventory().getItem(event.getSlot());

                    // Check for a "mail" book
                    if((item != null && MailManager.isMailBook(item)) || (slotItem != null && MailManager.isMailBook(slotItem))){
                        MessageTypeEnum.MailMessage.sendMessage("You cannot move this item!", player, true);
                        player.closeInventory();
                        player.updateInventory();
                        event.setCancelled(true);
                    }
                    //endregion

                }
            }
        }
    }

    // Prevent players from dropping mail books
    @EventHandler
    public void dropEvent(PlayerDropItemEvent event){
        if(event.getItemDrop() != null
                && event.getItemDrop().getItemStack() != null
                && MailManager.isMailBook(event.getItemDrop().getItemStack())){

            MessageTypeEnum.MailMessage.sendMessage("You cannot drop this item!", event.getPlayer(), true);
            event.setCancelled(true);
        }
    }

    // Do not drop mail books on death.
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        // Get the dropped items and remove mail books.
        ArrayList<ItemStack> itemsToRemove = new ArrayList<>();
        for(ItemStack drop : event.getDrops()){
            if(MailManager.isMailBook(drop)){
                itemsToRemove.add(drop);
            }
        }
        event.getDrops().removeAll(itemsToRemove);
    }

    // Prevent players from switching mail items into their off hand
    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event){
        // Check for mail items
        if((event.getMainHandItem() != null && MailManager.isMailBook(event.getMainHandItem()))
                || (event.getOffHandItem() != null && MailManager.isMailBook(event.getOffHandItem()))){
            MessageTypeEnum.MailMessage.sendMessage("You cannot move this item!", event.getPlayer(), true);
            event.setCancelled(true);
        }
    }

}
