package me.ijedi.jedipack.mail;

import me.ijedi.jedipack.common.ConfigHelper;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
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
            MessageTypeEnum.MailMessage.sendMessage("You have mail! Use /mail to see your inbox!", player, false);
        }
    }

    // Return info for the player's sign locks
    public List<String> getMailInfoPage(int page){
        ArrayList<String> infos = new ArrayList<>();
        if(mailInfos.size() == 0){
            infos.add(MessageTypeEnum.MailMessage.formatMessage("You do not have any mail!", true, false));
            return infos;
        }

        infos.add(MessageTypeEnum.MailMessage.getListHeader());

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
            infos.add(ChatColor.RED + "Invalid page number!");
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

            MailInfo currentInfo = mailInfos.get(x);
            String msg = String.format("%s) %s - %s", currentInfo.getMailNumber(), currentInfo.getSenderName(), currentInfo.getSubject());
            infos.add(msg);
        }

        // Add next page number
        if(pageCount > page){
            infos.add(ChatColor.GREEN + "Next page: " + ChatColor.AQUA + "/" + MailCommand.BASE_COMMAND + " " + (page + 1));
        }

        return infos;
    }
}
