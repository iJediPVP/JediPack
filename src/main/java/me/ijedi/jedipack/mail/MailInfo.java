package me.ijedi.jedipack.mail;

import me.ijedi.jedipack.common.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MailInfo {

    private int mailNumber;
    private UUID receiverId;
    private String[] messages;
    private String attachmentString;
    private boolean isRead;
    private boolean isAttachmentRead;
    private String subject;
    private String senderName;
    private long mailDateLong;
    private Date mailDate;

    public MailInfo(String senderName, UUID receiverId, int mailNumber){
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.mailNumber = mailNumber;
    }


    //region Getters and setters for fields

    public String[] getMessage(){
        return messages;
    }

    public int getMailNumber() {
        return mailNumber;
    }

    public boolean hasAttachment(){
        return !Util.isNullOrEmpty(attachmentString);
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isAttachmentRead() {
        return isAttachmentRead;
    }

    public void setMessage(String[] messages){
        this.messages = messages;
    }

    public void setRead(boolean isRead){
        this.isRead = isRead;
    }

    public void setAttachmentRead(boolean isAttachmentRead){
        this.isAttachmentRead = isAttachmentRead;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject){
        this.subject = subject;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setMailDate(Date mailDate){
        this.mailDateLong = mailDate.getTime();
        this.mailDate = mailDate;
    }

    public void setMailDate(long mailDateLong){
        this.mailDateLong = mailDateLong;
        this.mailDate = new Date(mailDateLong);
    }

    public void setAttachmentString(String attachmentString){
        this.attachmentString = attachmentString;
    }

    public String getAttachmentString(){
        return attachmentString;
    }

    public long getMailDateLong() {
        return mailDateLong;
    }

    public String getMailDateString(){
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        return format.format(mailDate);
    }

    //endregion

    // Returns a book item that represents this mail object.
    public ItemStack getBook(){

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta itemMeta = book.getItemMeta();

        BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.setAuthor(senderName);
        bookMeta.setTitle(subject);
        if(messages != null){

            // Add the first page with the subject and sender
            // Formatting done by Dtron!
            String header = String.format("%s\n%s\n\n%s\n%s",
                    ChatColor.RED + "" + ChatColor.BOLD + "Sender:",
                    ChatColor.BLACK + senderName,
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Subject:",
                    ChatColor.BLACK + subject);
            bookMeta.addPage(header);

            for(int x = 0; x < messages.length; x++){
                String msg = messages[x];
                bookMeta.addPage(msg);
            }
        }

        book.setItemMeta(bookMeta);

        return book;

    }



}
