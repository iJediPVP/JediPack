package me.ijedi.jedipack.mail;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MailCommand implements TabExecutor {

    public static final String BASE_COMMAND = "mail";
    private static final String SEND = "send";
    public static final String INFO = "info";
    public static final String READ = "read";
    public static final String DELETE = "delete";
    public static final String CANCEL = "cancel";
    public static final String SETTINGS = "settings";
    public static final String ATTACH = "attach";
    public static final String REMOVE = "remove";

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        // Check for player
        if(!(commandSender instanceof Player)){
            MessageTypeEnum.MailMessage.sendMessage("Only a player can run this command!", commandSender, true);
            return true;
        }
        Player player = (Player) commandSender;

        // Handle args
        if(args.length > 0){
            String firstArg = args[0].toLowerCase();

            if(firstArg.equals(SEND)){
                //region SEND
                // Check for player name
                if(args.length > 1){
                    String recipientName = args[1];
                    @SuppressWarnings("deprecation")
                    OfflinePlayer recipient = Bukkit.getOfflinePlayer(recipientName);

                    // Make sure the recipient exists
                    if(recipient == null){
                        MessageTypeEnum.MailMessage.sendMessage("A player with the name " + recipientName + " does not exist!", player, true);
                        return true;
                    }

                    /* Don't allow mail to themselves
                    if(recipient.getUniqueId().equals(player.getUniqueId())){
                        MessageTypeEnum.MailMessage.sendMessage("You cannot send mail to yourself!", player, true);
                        return true;
                    }*/

                    // Make sure the player has an open slot
                    int openSlot = 0;
                    boolean hasFreeSlot = false;
                    for(int x = 0; x < 9; x++){
                        ItemStack item = player.getInventory().getItem(x);
                        if(item == null && !hasFreeSlot){
                            openSlot = x;
                            hasFreeSlot = true;
                            //break;
                        } else {
                            // Don't let this player have more than one mail book.. Creative player's can get a second book if they move the item off their hot bar.
                            String nbtData = Util.getNBTTagString(item, MailManager.MAIL_KEY);
                            if(!Util.isNullOrEmpty(nbtData)){
                                MessageTypeEnum.MailMessage.sendMessage("You already have an unsent mail item!", player, true);
                                return true;
                            }
                        }
                    }
                    if(!hasFreeSlot){
                        MessageTypeEnum.MailMessage.sendMessage("You must have an open spot on your hot bar!", player, true);
                        return true;
                    }

                    // Give the player a book to write in
                    ItemStack book = new MailManager().getNewWritableBook(recipient.getName());
                    player.getInventory().setItem(openSlot, book);
                    MessageTypeEnum.MailMessage.sendMessage("Use this book to send mail to " + recipient.getName() + "!", player, false);
                    return true;
                }
                //endregion

            } else if(firstArg.equals(INFO)){
                //region INFO
                // Check for a page number
                int pageNumber = 1;
                if(args.length > 1){
                    String pageStr = args[1];
                    if(!Util.isInteger(pageStr)){
                        MessageTypeEnum.MailMessage.sendMessage("Invalid page number!", player, true);
                        return true;
                    }
                    pageNumber = Integer.parseInt(pageStr);
                }

                // Try to open the mail box in the UI or chat
                MailPlayerInfo info = MailManager.getMailPlayerInfo(player.getUniqueId());
                if(info.isUIEnabled()){
                    // Try to open the UI
                    if(!info.hasAnyMail()){
                        MessageTypeEnum.MailMessage.sendMessage("You do not have any mail!", player, true);
                        return true;
                    }
                    player.openInventory(info.getMailBoxInventory(player));

                } else {
                    // Else use the chat
                    List<TextComponent> infoMsgs = info.getMailInfoPage(pageNumber);
                    for(TextComponent msg : infoMsgs){
                        player.spigot().sendMessage(msg);
                    }
                }

                return true;
                //endregion

            } else if(firstArg.equals(READ)){
                //region READ
                // Check for mail number
                if(args.length < 2){
                    MessageTypeEnum.MailMessage.sendMessage("You must specify a number to open!", player, true);
                    return true;
                }

                // Make sure it's an int
                String mailNumStr = args[1];
                if(!Util.isInteger(mailNumStr)){
                    MessageTypeEnum.MailMessage.sendMessage("Invalid number!", player, true);
                    return true;
                }
                int mailNumber = Integer.parseInt(mailNumStr);

                // See if this player has a mail with this number.
                MailPlayerInfo playerInfo = MailManager.getMailPlayerInfo(player.getUniqueId());
                if(!playerInfo.hasMailNumber(mailNumber)){
                    MessageTypeEnum.MailMessage.sendMessage("You do not have a mail #" + mailNumber + "!", player, true);
                    return true;
                }

                // Build and open the book for this mail.
                MailInfo mailInfo = playerInfo.getMailInfo(mailNumber);
                playerInfo.openMail(player, mailInfo);
                return true;
                //endregion

            } else if(firstArg.equals(DELETE)){
                //region DELETE
                // Check for mail number
                if(args.length < 2){
                    MessageTypeEnum.MailMessage.sendMessage("You must specify a number to delete!", player, true);
                    return true;
                }

                // Make sure it's an int
                String mailNumStr = args[1];
                if(!Util.isInteger(mailNumStr)){
                    MessageTypeEnum.MailMessage.sendMessage("Invalid number!", player, true);
                    return true;
                }
                int mailNumber = Integer.parseInt(mailNumStr);

                // See if this player has a mail with this number.
                MailPlayerInfo playerInfo = MailManager.getMailPlayerInfo(player.getUniqueId());
                if(!playerInfo.hasMailNumber(mailNumber)){
                    MessageTypeEnum.MailMessage.sendMessage("You do not have a mail #" + mailNumber + "!", player, true);
                    return true;
                }

                // Delete mail
                MailInfo info = playerInfo.getMailInfo(mailNumber);
                playerInfo.deleteMail(info);
                String msg = "'" + info.getSubject() + "' from " + info.getSenderName() + " has been deleted!";
                MessageTypeEnum.MailMessage.sendMessage(msg, player, false);
                return true;
                //endregion

            } else if(firstArg.equals(SETTINGS)){
                //region SETTINGS
                MailPlayerInfo info = MailManager.getMailPlayerInfo(player.getUniqueId());
                Inventory configInv = info.getConfigInventory(player);
                player.openInventory(configInv);
                return true;
                //endregion

            } else if(firstArg.equals(CANCEL)){
                //region CANCEL
                // See if the player is holding a mail book
                ItemStack item = player.getInventory().getItemInMainHand();
                if(item != null && MailManager.isMailBook(item)){

                    ItemStack attachment = MailManager.getAttachedItem(item);
                    if(attachment != null){
                        player.getInventory().setItemInMainHand(attachment);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }

                    MessageTypeEnum.MailMessage.sendMessage("Your mail has been canceled and attachments returned!", player, false);
                } else {
                    MessageTypeEnum.MailMessage.sendMessage("You must be holding a mail item!", player, true);
                }
                return true;
                //endregion

            } else if(firstArg.equals(ATTACH)){
                // region ATTACH

                // Check for mail in inventory
                ItemStack[] playerItems = player.getInventory().getStorageContents();
                ItemStack bookItem = null;
                int bookSlot = 0;
                for(int x = 0; x < playerItems.length; x++){
                    ItemStack currentItem = playerItems[x];
                    if(MailManager.isMailBook(currentItem)){
                        bookSlot = x;
                        bookItem = currentItem;
                    }
                }
                if(bookItem == null){
                    MessageTypeEnum.MailMessage.sendMessage("You must have a mail book in your inventory!", player, true);
                    return true;
                }

                // region Check for REMOVE in the second arg
                if(args.length > 1){
                    String secondArg = args[1].toLowerCase();
                    if(secondArg.equals(REMOVE)){

                        // Check for attachment
                        ItemStack attachment = MailManager.getAttachedItem(bookItem);
                        if(attachment != null){
                            // Remove from the book and spawn at the player's feet
                            bookItem = MailManager.removeAttachment(bookItem);
                            player.getInventory().setItem(bookSlot, bookItem);
                            player.getWorld().dropItemNaturally(player.getLocation(), attachment);
                            MessageTypeEnum.MailMessage.sendMessage("The attachment has been removed!", player, false);
                        } else {
                            MessageTypeEnum.MailMessage.sendMessage("There are no attachments!", player, true);
                        }

                        return true;
                    }
                } // ELSE attach an item!
                // endregion

                //region Verify and add the attachment

                // Make sure there isn't already an attachment
                ItemStack existingAttachment = MailManager.getAttachedItem(bookItem);
                if(existingAttachment != null){

                    MessageTypeEnum.MailMessage.sendMessage("There is already an attachment!", player, false);

                    // Remove command
                    ComponentBuilder removeBuilder = new ComponentBuilder(ChatColor.RED + "" + ChatColor.BOLD + "[CLICK HERE]");
                    removeBuilder.event(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( ChatColor.GREEN + "Click to remove the attahcment!" ).create() ));
                    String removeCommand = String.format("/%s %s %s", MailCommand.BASE_COMMAND, MailCommand.ATTACH, MailCommand.REMOVE);
                    removeBuilder.event(new ClickEvent( ClickEvent.Action.RUN_COMMAND, removeCommand));

                    TextComponent msg = new TextComponent("");
                    msg.addExtra(removeBuilder.create()[0]);
                    msg.addExtra(new TextComponent(ChatColor.GREEN + " to remove it!"));

                    MessageTypeEnum.MailMessage.sendMessage(msg, player);
                    return true;
                }

                // Verify the item that the player is holding. Don't allow nulls, shulkerboxes, or mail books.
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if(heldItem == null || heldItem.getType().equals(Material.AIR) || Util.SHULKER_BOXES.contains(heldItem.getType()) || MailManager.isMailBook(heldItem)){
                    MessageTypeEnum.MailMessage.sendMessage("Invalid attachment!", player, true);
                    return true;
                }

                // Check for an amount. Default to 1
                int amount = 1;
                if(args.length > 1){
                    String secondArg = args[1];
                    if(Util.isInteger(secondArg)){
                        amount = Integer.parseInt(secondArg);
                    }
                }

                // Verify amount
                if(amount > heldItem.getAmount()){
                    MessageTypeEnum.MailMessage.sendMessage("You don't have " + amount + " of this item!", player, true);
                    return true;
                }

                // Build the attached item and subtract from the player's held item
                ItemStack attachedItem = heldItem.clone();
                attachedItem.setAmount(amount);
                int newPlayerAmount = heldItem.getAmount() - amount;
                if(newPlayerAmount == 0){
                    player.getInventory().setItemInMainHand(null);
                } else {
                    heldItem.setAmount(newPlayerAmount);
                    player.getInventory().setItemInMainHand(heldItem);
                }

                // Store the item in the book's NBT
                bookItem = MailManager.attachItem(bookItem, attachedItem);
                player.getInventory().setItem(bookSlot, bookItem);
                ChatColor msgColor = MessageTypeEnum.MailMessage.getMessageColor();
                String msg = msgColor + "Attached " + ChatColor.YELLOW + amount + " x " + Util.getRealItemName(attachedItem) + "(s)" + msgColor + "!";
                MessageTypeEnum.MailMessage.sendMessage(msg, player, false);
                //endregion

                //ItemStack test = MailManager.getAttachedItem(bookItem);
                //player.getInventory().setItem(8, test);
                return true;
                // endregion

            }

        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        return new ArrayList<>();
    }

}
