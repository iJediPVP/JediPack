package me.ijedi.jedipack.mail;

import io.netty.buffer.Unpooled;
import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.ConfigHelper;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import me.ijedi.jedipack.menu.Menu;
import me.ijedi.jedipack.menu.MenuManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.PacketDataSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutCustomPayload;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class MailPlayerInfo {

    /* File layout:
    <mailNumber>:
        sender: <senderId>
        message: <message>
        attachment: <serialized itemstack> // TODO: figure this out..
    * */

    // Mail config names
    private static final String SENDER_NAME = "senderName";
    private static final String MESSAGE = "message";
    private static final String ATTACHMENT = "attachment";
    private static final String SUBJECT = "subject";
    private static final String IS_READ = "isRead";
    private static final String IS_ATTACH_READ = "isAttachRead";

    // Player config names
    private static final String CONFIG_DIRECTORY = "mail/playerConfig";
    private static final String ALERTS_ENABLED = "alertsEnabled";
    private static final String UI_ENABLED = "uiEnabled";

    // Player config menu names
    public static final String MENU_PREFIX = "Mail Settings";
    public static final String ALERTS_NAME = "Alerts";
    public static final String UI_NAME = "UI";


    // Player config values
    private boolean isAlertsEnabled, isUIEnabled;


    // Fields
    private UUID playerId;
    private HashMap<Integer, MailInfo> mailInfos = new HashMap<>();

    public MailPlayerInfo(UUID playerId){
        this.playerId = playerId;
    }


    // Load info from the player's file.
    public void loadPlayerInfo(){

        // Get the mail file
        String fileName = ConfigHelper.getFullFilePath(MailManager.DIRECTORY, getFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        // Load mail
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


        // Get the player config
        String playerFileName = ConfigHelper.getFullFilePath(CONFIG_DIRECTORY, getFileName());
        File playerFile = ConfigHelper.getFile(playerFileName);
        FileConfiguration playerConfig = ConfigHelper.getFileConfiguration(playerFile);

        // Load config
        if(!playerConfig.contains(ALERTS_ENABLED)){
            // Defaults
            updatePlayerConfig();
        } else {
            isAlertsEnabled = playerConfig.getBoolean(ALERTS_ENABLED);
            isUIEnabled = playerConfig.getBoolean(UI_ENABLED);
        }
    }

    // Returns the file name for this player.
    private String getFileName(){
        return playerId.toString() + ".yml";
    }


    //region Mail Methods

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
        // TODO: reorder numbers..
        // Get the config
        String fileName = ConfigHelper.getFullFilePath(MailManager.DIRECTORY, getFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        config.set(Integer.toString(info.getMailNumber()), null);
        ConfigHelper.saveFile(file, config);
        mailInfos.remove(info.getMailNumber());
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
            String msg = /*ChatColor.YELLOW + Integer.toString(currentInfo.getMailNumber()) + ") "
                    + */ChatColor.AQUA + currentInfo.getSenderName() + ChatColor.YELLOW + " - "
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
            String deleteCommand = String.format("/%s %s %s", MailCommand.BASE_COMMAND, MailCommand.DELETE, currentInfo.getMailNumber());
            deleteBuilder.event(new ClickEvent( ClickEvent.Action.RUN_COMMAND, deleteCommand));


            msgComponent.addExtra(readBuilder.create()[0]);
            msgComponent.addExtra(deleteBuilder.create()[0]);
            infos.add(msgComponent);
        }


        //region Next and previous links
        TextComponent pageComponent = new TextComponent("");

        // Next
        boolean hasNext = pageCount > page;
        boolean hasPrevious = page > 1;
        if(hasNext){
            ComponentBuilder nextBuilder = new ComponentBuilder(ChatColor.GREEN + "" + ChatColor.BOLD + "[Next]");
            nextBuilder.event(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( ChatColor.GREEN + "Click for the next page!" ).create() ));
            String nextCommand = String.format("/%s %s %s", MailCommand.BASE_COMMAND, MailCommand.INFO, page + 1);
            nextBuilder.event(new ClickEvent( ClickEvent.Action.RUN_COMMAND, nextCommand));
            pageComponent.addExtra(nextBuilder.create()[0]);

            // Check for dash
            if(hasPrevious){
                ComponentBuilder builder = new ComponentBuilder(ChatColor.YELLOW + " - ");
                pageComponent.addExtra(builder.create()[0]);
            }
        }

        // Previous
        if(hasPrevious){
            ComponentBuilder previousBuilder = new ComponentBuilder(ChatColor.GREEN + "" + ChatColor.BOLD + "[Previous]");
            previousBuilder.event(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( ChatColor.GREEN + "Click for the previous page!" ).create() ));
            String previousCommand = String.format("/%s %s %s", MailCommand.BASE_COMMAND, MailCommand.INFO, page - 1);
            previousBuilder.event(new ClickEvent( ClickEvent.Action.RUN_COMMAND, previousCommand));
            pageComponent.addExtra(previousBuilder.create()[0]);
        }

        if(hasNext || hasPrevious){
            infos.add(pageComponent);
        }
        //endregion

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

    //endregion


    //region Player methods

    // Toggle alerts
    public void toggleAlertsEnabled(){
        isAlertsEnabled = !isAlertsEnabled;
        updatePlayerConfig();
    }

    // Toggle UI
    public void toggleUIEnabled(){
        isUIEnabled = !isUIEnabled;
        updatePlayerConfig();
    }

    // Update player config
    private void updatePlayerConfig(){
        // Get the player config
        String playerFileName = ConfigHelper.getFullFilePath(CONFIG_DIRECTORY, getFileName());
        File playerFile = ConfigHelper.getFile(playerFileName);
        FileConfiguration playerConfig = ConfigHelper.getFileConfiguration(playerFile);

        // Set values
        playerConfig.set(ALERTS_ENABLED, isAlertsEnabled);
        playerConfig.set(UI_ENABLED, isUIEnabled);
        ConfigHelper.saveFile(playerFile, playerConfig);
    }

    // Returns the inventory for the player's configuration settings.
    public Inventory getConfigInventory(Player player){

        // Set up menu buttons
        ItemStack exitButton = new ItemStack(Material.SPRUCE_DOOR);
        ItemMeta exitMeta = exitButton.getItemMeta();
        List<String> exitLore = Arrays.asList(ChatColor.GREEN + "Click to exit.");
        exitMeta.setLore(exitLore);
        exitMeta.setDisplayName(ChatColor.RED + "Exit");
        exitButton.setItemMeta(exitMeta);

        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        List<String> nextLore = Arrays.asList(ChatColor.GREEN + "Click to go to the next page.");
        exitMeta.setLore(nextLore);
        nextMeta.setDisplayName(ChatColor.GREEN + "Next");
        nextButton.setItemMeta(nextMeta);

        ItemStack prevButton = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevButton.getItemMeta();
        List<String> prevLore = Arrays.asList(ChatColor.GREEN + "Click to go to the previous page.");
        exitMeta.setLore(prevLore);
        prevMeta.setDisplayName(ChatColor.GREEN + "Previous");
        prevButton.setItemMeta(prevMeta);

        // Build config items
        ArrayList<ItemStack> configItems = new ArrayList<>();

        // Alerts
        ArrayList<String> alertLore = new ArrayList<>();
        String alertName;
        ItemStack alertItem = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta alertMeta = alertItem.getItemMeta();
        if(isAlertsEnabled){
            alertName = ChatColor.RED + ALERTS_NAME;
            alertLore.add(ChatColor.GREEN + "Click to enable alerts.");
        } else {
            alertName = ChatColor.GREEN + ALERTS_NAME;
            alertLore.add(ChatColor.RED + "Click to disable alerts.");
        }
        alertMeta.setDisplayName(alertName);
        alertMeta.setLore(alertLore);
        alertItem.setItemMeta(alertMeta);
        configItems.add(alertItem);

        // UI
        ArrayList<String> uiLore = new ArrayList<>();
        String uiName;
        ItemStack uiItem = new ItemStack(Material.ITEM_FRAME);
        ItemMeta uiMeta = uiItem.getItemMeta();
        if(isUIEnabled){
            uiName = ChatColor.RED + UI_NAME;
            uiLore.add(ChatColor.GREEN + "Click to enable the UI.");
        } else {
            uiName = ChatColor.GREEN + UI_NAME;
            uiLore.add(ChatColor.RED + "Click to disable the UI.");
        }
        uiMeta.setDisplayName(uiName);
        uiMeta.setLore(uiLore);
        uiItem.setItemMeta(uiMeta);
        configItems.add(uiItem);


        Menu menu = new Menu(String.format("%s: %s", MENU_PREFIX, player.getName()));
        menu.setContents(configItems.toArray(new ItemStack[configItems.size()]));
        menu.setButtons(exitButton, prevButton, nextButton);
        return new MenuManager().getMenu(menu.getName());
    }

    // Alert the player that they have received mail.
    public void alertPlayer(){
        if(!isAlertsEnabled){
            return;
        }

        // Alerts are enabled, send a message and player a sound.
        Player player = Bukkit.getPlayer(playerId);
        if(player != null && player.isOnline()){
            // TODO: Make this clickable!
            MessageTypeEnum.MailMessage.sendMessage("You have mail! Use /" + MailCommand.BASE_COMMAND + " " + MailCommand.INFO + " to see your inbox!", player, false);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 5, 0.700f);
            new BukkitRunnable(){
                @Override
                public void run(){
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 5, 0.900f);
                    this.cancel();
                }
            }.runTaskLater(JediPackMain.getThisPlugin(), 2L);
        }
    }

    //endregion
}
