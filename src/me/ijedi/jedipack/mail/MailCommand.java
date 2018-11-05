package me.ijedi.jedipack.mail;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MailCommand implements TabExecutor {

    public static final String BASE_COMMAND = "mail";
    private static final String SEND = "send";
    private static final String INFO = "info";
    private static final String READ = "read";

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

            //region SEND
            if(firstArg.equals(SEND)){

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

                    // Give the player a book to write in
                    ItemStack book = new MailManager().getNewWritableBook(recipient.getName());
                    player.getInventory().setItemInMainHand(book);
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

                MailPlayerInfo info = MailManager.getMailPlayerInfo(player.getUniqueId());
                List<String> infoMsgs = info.getMailInfoPage(pageNumber); // TODO: parse page numbers
                for(String msg : infoMsgs){
                    player.sendMessage(msg);
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

            }

        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        return new ArrayList<>();
    }

    /*private void openBook(Player player){
        // Works to open a written book
        final int slot = player.getInventory().getHeldItemSlot();
        final ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, old);
        final PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(MinecraftKey.a("minecraft:book_open"), new PacketDataSerializer(Unpooled.buffer()).a(EnumHand.MAIN_HAND));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        player.getInventory().setItem(slot, old);
    }*/

}
