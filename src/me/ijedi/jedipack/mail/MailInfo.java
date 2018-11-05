package me.ijedi.jedipack.mail;

import me.ijedi.jedipack.common.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class MailInfo {

    private int mailNumber;
    private UUID receiverId;
    private String[] messages;
    private ItemStack item;
    private boolean isRead;
    private boolean isAttachmentRead;
    private String subject;
    private String senderName;

    public MailInfo(String senderName, UUID receiverId, int mailNumber){
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.mailNumber = mailNumber;
    }


    //region Getters and setters for fields

    public UUID getReceiverId() {
        return receiverId;
    }

    public String[] getMessage(){
        return messages;
    }

    public ItemStack getItem(){
        return item;
    }

    public int getMailNumber() {
        return mailNumber;
    }

    public boolean hasAttachment(){
        return item != null;
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

    public void setItem(ItemStack item){
        this.item = item;
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
            String centeredSubject = Util.centerString(subject, 27);
            String centeredSender = Util.centerString(senderName, 27);
            String header = String.format("%s\n%s", centeredSubject, centeredSender);
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
