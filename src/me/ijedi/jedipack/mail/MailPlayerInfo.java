package me.ijedi.jedipack.mail;

import io.netty.buffer.Unpooled;
import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.ConfigHelper;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.PacketDataSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutCustomPayload;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class MailPlayerInfo {

    /* File layout:
    <mailNumber>:
        sender: <senderId>
        message: <message>
        attachment: <serialized itemstack> // TODO: figure this out..
    * */

    // Config names
    private static final String SENDER_NAME = "senderName";
    private static final String MESSAGE = "message";
    private static final String ATTACHMENT = "attachment";
    private static final String SUBJECT = "subject";
    private static final String IS_READ = "isRead";
    private static final String IS_ATTACH_READ = "isAttachRead";


    // Fields
    private UUID playerId;
    private HashMap<Integer, MailInfo> mailInfos = new HashMap<>();

    public MailPlayerInfo(UUID playerId){
        this.playerId = playerId;
    }

    // Load info from the player's file.
    public void loadPlayerInfo(){

        // Get the config
        String fileName = ConfigHelper.getFullFilePath(MailManager.DIRECTORY, getFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        // Read it
        for(String mailStr : config.getKeys(false)){
            if(Util.isInteger(mailStr)){

                int mailNum = Integer.parseInt(mailStr);
                String senderName = config.getString(mailStr + "." + SENDER_NAME);
                List<String> messages = config.getStringList(mailStr + "." + MESSAGE);
                String subject = config.getString(mailStr + "." + SUBJECT);

                MailInfo info = new MailInfo(senderName, playerId, mailNum);
                info.setMessage(messages.toArray(new String[messages.size()]));
                info.setRead(config.getBoolean(mailStr + "." + IS_READ));
                info.setAttachmentRead(config.getBoolean(mailStr + "." + IS_ATTACH_READ));
                info.setItem(null); // TODO: rehydrate this
                info.setSubject(subject);
                mailInfos.put(mailNum, info);
            }
        }

    }

    // Update or add the given mail info
    public void updateMail(MailInfo info){

        // Get the config
        String fileName = ConfigHelper.getFullFilePath(MailManager.DIRECTORY, getFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        // Write it
        String mailNumStr = Integer.toString(info.getMailNumber());
        config.set(mailNumStr + "." + SENDER_NAME, info.getSenderName());
        config.set(mailNumStr + "." + MESSAGE, info.getMessage());
        config.set(mailNumStr + "." + ATTACHMENT, ""); // TODO: serialize this
        config.set(mailNumStr + "." + IS_READ, info.isRead());
        config.set(mailNumStr + "." + IS_ATTACH_READ, info.isAttachmentRead());
        config.set(mailNumStr + "." + SUBJECT, info.getSubject());
        ConfigHelper.saveFile(file, config);

        if(!mailInfos.containsKey(info.getMailNumber())){
            mailInfos.put(info.getMailNumber(), info);

            // Since this is a new mail info, alert the player if they are online.
            alertPlayer();
        }
    }

    // Returns true if this player already has mail with the given number.
    public boolean hasMailNumber(int mailNumber){
        return mailInfos.containsKey(mailNumber);
    }

    // Get the message for the given mail number
    public String[] readMailMessage(int mailNumber){
        MailInfo info = mailInfos.get(mailNumber);
        info.setRead(true);
        updateMail(info);
        return info.getMessage();
    }

    // Get the attached item for the given mail number
    public ItemStack getMailAttachment(int mailNumber){
        MailInfo info = mailInfos.get(mailNumber);
        if(info.hasAttachment()){
            info.setAttachmentRead(true);
            updateMail(info);
            return info.getItem();
        }
        return null;
    }

    // Delete the given mail object.
    public void deleteMail(MailInfo info){
        // Get the config
        String fileName = ConfigHelper.getFullFilePath(MailManager.DIRECTORY, getFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        config.set(Integer.toString(info.getMailNumber()), null);
        ConfigHelper.saveFile(file, config);
        mailInfos.remove(info.getMailNumber());
    }

    // Returns the file name for this player.
    private String getFileName(){
        return playerId.toString() + "yml";
    }

    // Return the next mail number for this player.
    public int getNextMailNumber(){
        // Get all of the used numbers
        ArrayList<Integer> usedNumbers = new ArrayList<>();
        int maxNumber = 0;
        for(MailInfo lock : mailInfos.values()){
            int lockNum = lock.getMailNumber();
            usedNumbers.add(lockNum);
            maxNumber = lockNum > maxNumber ? lockNum : maxNumber;
        }

        // Get the actual sum of the used numbers
        int totalSum = 0;
        for(int x : usedNumbers){
            totalSum += x;
        }

        // If expected - total is not 0, then return the missing number.
        int expectedSum = (maxNumber * (1 + maxNumber)) / 2;
        int diff = expectedSum - totalSum;
        if(diff > 0){
            return diff;
        }

        // Else return the next number.
        return maxNumber + 1;
    }

    // Alert the player that they have received mail.
    public void alertPlayer(){
        Player player = Bukkit.getPlayer(playerId);
        if(player != null && player.isOnline()){
            MessageTypeEnum.MailMessage.sendMessage("You have mail! Use /" + MailCommand.BASE_COMMAND + " " + MailCommand.INFO + " to see your inbox!", player, false);
        }
    }

    // Return info for the player's sign locks
    public List<TextComponent> getMailInfoPage(int page){

        ArrayList<TextComponent> infos = new ArrayList<>();
        if(mailInfos.size() == 0){
            TextComponent errorComponent = new TextComponent(MessageTypeEnum.MailMessage.formatMessage("You do not have any mail!", true, false));
            infos.add(errorComponent);
            return infos;
        }

        infos.add(new TextComponent(MessageTypeEnum.MailMessage.getListHeader()));

        // Sort the numbers
        ArrayList<Integer> mailNumbers = new ArrayList<>(mailInfos.keySet());
        Collections.sort(mailNumbers);

        // Figure out the pages
        int pageSize = 10;
        int pageCount = (int)Math.floor(mailNumbers.size() / pageSize);
        if(mailNumbers.size() % pageSize > 0){
            pageCount++;
        }

        // Verify page number
        if(page == 0 || page > pageCount){
            infos.add(new TextComponent(ChatColor.RED + "Invalid page number!"));
            return infos;
        }

        // Fill in the info
        int currentPageMin = (page - 1) * pageSize;
        int currentPageMax = currentPageMin + pageSize - 1;
        for(int x : mailNumbers){

            // Verify this lock number is on the current page
            if(x < currentPageMin){
                continue;
            }
            if(x > currentPageMax){
                break;
            }

            // Base message
            MailInfo currentInfo = mailInfos.get(x);
            String msg = ChatColor.YELLOW + Integer.toString(currentInfo.getMailNumber()) + ") "
                    + ChatColor.AQUA + currentInfo.getSenderName() + ChatColor.YELLOW + " - "
                    + ChatColor.GOLD + (currentInfo.isRead() ? "" : ChatColor.BOLD) + currentInfo.getSubject() + " ";
            TextComponent msgComponent = new TextComponent(msg);

            // Read command
            ComponentBuilder readBuilder = new ComponentBuilder(ChatColor.GREEN + "" + (currentInfo.isRead() ? "" : ChatColor.BOLD) + "[READ] ");
            readBuilder.event(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( ChatColor.GREEN + "Click to read!" ).create() ));
            String readCommand = String.format("/%s %s %s", MailCommand.BASE_COMMAND, MailCommand.READ, currentInfo.getMailNumber());
            readBuilder.event(new ClickEvent( ClickEvent.Action.RUN_COMMAND, readCommand));

            // Delete command - TODO: Actually code a delete command
            ComponentBuilder deleteBuilder = new ComponentBuilder(ChatColor.RED + "" + ChatColor.BOLD + "[DELETE]");
            deleteBuilder.event(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( ChatColor.RED + "Click to delete!" ).create() ));
            String deleteCommand = "/mail info";
            //String deleteCommand = String.format("/%s %s %s", MailCommand.BASE_COMMAND, MailCommand.READ, currentInfo.getMailNumber());
            deleteBuilder.event(new ClickEvent( ClickEvent.Action.RUN_COMMAND, deleteCommand));


            msgComponent.addExtra(readBuilder.create()[0]);
            msgComponent.addExtra(deleteBuilder.create()[0]);
            infos.add(msgComponent);
        }

        // Add next page number - TODO: Create Back and Next links
        if(pageCount > page){
            infos.add(new TextComponent(ChatColor.GREEN + "Next page: " + ChatColor.AQUA + "/" + MailCommand.BASE_COMMAND + " " + MailCommand.INFO + " " + (page + 1)));
        }

        return infos;
    }

    // Returns the mail info for the given mail number.
    public MailInfo getMailInfo(int mailNumber){
        return mailInfos.get(mailNumber);
    }

    // Opens the specified mail info
    public void openMail(Player player, MailInfo mailInfo){

        // Get the book representing the mail and the player's current held item.
        ItemStack book = mailInfo.getBook();
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack oldItem = player.getInventory().getItem(slot);

        // Give the player the book and open it.
        player.getInventory().setItemInMainHand(book);
        final PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(MinecraftKey.a("minecraft:book_open"), new PacketDataSerializer(Unpooled.buffer()).a(EnumHand.MAIN_HAND));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        // Give back their item
        player.getInventory().setItem(slot, oldItem);

        // Set to read
        if(!mailInfo.isRead()){
            mailInfo.setRead(true);
            updateMail(mailInfo);
        }
    }
}
