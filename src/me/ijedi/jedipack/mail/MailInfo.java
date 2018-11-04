package me.ijedi.jedipack.mail;

import org.bukkit.inventory.ItemStack;

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


    //region Getters and setters

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




}
