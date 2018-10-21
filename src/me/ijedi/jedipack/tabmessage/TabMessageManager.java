package me.ijedi.jedipack.tabmessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import me.ijedi.jedipack.JediPackMain;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.PacketDataSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabMessageManager {

    /*
    * Arguments:
      <d1> - Date format: MM/DD/YYYY
      <d2> - Date format: DD/MM/YYYY
      <t1> - 12 Hour time
      <t2> - 24 Hour time
    * */

    private static final String CONFIG_NAME = "tabMessageConfig.yml";
    private static final String ENABLED = "enabled";
    private static final String COLOR_SYMBOL = "colorSymbol";
    private static final String HEADER = "header";
    private static final String FOOTER = "footer";
    private static final String ANIMATED = "animated";
    private static final String MESSAGES = "message";

    private static FileConfiguration TabMessageConfiguration;
    private static File TabMessageFile;
    private static IChatBaseComponent headerChat, footerChat;
    private static PacketPlayOutPlayerListHeaderFooter tabListPacket;

    // Config values
    private static boolean isEnabled = false;
    private static char colorSymbol = '$';
    private static List<String> headers = new ArrayList<>();
    private static List<String> footers = new ArrayList<>();
    private static boolean isHeadersAnimated = false;
    private static boolean isFootersAnimated = false;

    // Current values
    private static String currentHeader;
    private static String currentFooter;


    // Load the configuration for tab messages
    public static void intializeTabMessages(){

        // Load the config
        TabMessageFile = getFile();
        TabMessageConfiguration = getFileConfiguration();

        JediPackMain.getThisPlugin().getLogger().info(Boolean.toString(isEnabled));
        JediPackMain.getThisPlugin().getLogger().info(Character.toString(colorSymbol));
        JediPackMain.getThisPlugin().getLogger().info(Boolean.toString(isHeadersAnimated));
        JediPackMain.getThisPlugin().getLogger().info(Boolean.toString(isFootersAnimated));
        for(String str : headers){
            JediPackMain.getThisPlugin().getLogger().info(str);
        }for(String str : footers){
            JediPackMain.getThisPlugin().getLogger().info(str);
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                if(isEnabled){

                    // TODO: write code to cycle through tablists
                    setNextTableList("This is a header!", "This is a footer!");

                    //Send to all players
                    for(Player player : Bukkit.getOnlinePlayers()){
                        sendTabList(player);
                    }

                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(JediPackMain.getThisPlugin(), 0l, 1 * 20l);

    }


    // Save the ParkourManager configuration file.
    private static void saveConfiguration(){
        try{
            File file = getFile();
            TabMessageConfiguration.save(file);

        }catch(IOException e){
            String errorMessage = formatTabMessageString("JediPack Parkour - Error saving configuration file.", true);
            JediPackMain.getThisPlugin().getLogger().info(errorMessage);
            JediPackMain.getThisPlugin().getLogger().info(e.toString());
        }
    }

    // Get the File object for the TabMessageManager.
    private static File getFile(){

        if(TabMessageFile != null){
            return TabMessageFile;
        }

        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/tabMessage";
        String fileName = CONFIG_NAME;
        File configFile = new File(folder, fileName);

        return configFile;
    }

    // Get the FileConfiguration object for the TabMessageManager
    private static FileConfiguration getFileConfiguration(){

        if(TabMessageConfiguration != null){
            return TabMessageConfiguration;
        }

        // Create the file if it doesn't exist.
        if(!TabMessageFile.exists()){
            TabMessageFile.getParentFile().mkdirs();
            FileConfiguration config = YamlConfiguration.loadConfiguration(TabMessageFile);

            // Defaults
            config.set(ENABLED, false);
            isEnabled = false;
            config.set(COLOR_SYMBOL, "$");
            colorSymbol = '$';
            config.set(HEADER + "." + ANIMATED, false);
            isHeadersAnimated = false;
            config.set(FOOTER + "." + ANIMATED, false);
            isFootersAnimated = false;

            String[] defaultHeaders = new String[1];
            defaultHeaders[0] = "$fDefault Header";
            headers.add("$Default Header");
            config.set(HEADER + "." + MESSAGES, defaultHeaders);

            String[] defaultFooters = new String[1];
            defaultFooters[0] = "$fDefault Footer";
            headers.add("$Default Footer");
            config.set(FOOTER + "." + MESSAGES, defaultFooters);

            try{
                config.save(TabMessageFile);
            }
            catch(IOException e){
                String errorMessage = formatTabMessageString("JediPack TabMessage - Error saving configuration file.", true);
                JediPackMain.getThisPlugin().getLogger().info(errorMessage);
                JediPackMain.getThisPlugin().getLogger().info(e.toString());
            }
            return config;

        } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(TabMessageFile);

            // Read config
            isEnabled = config.getBoolean(ENABLED);
            colorSymbol = config.getString(COLOR_SYMBOL).toCharArray()[0];
            isHeadersAnimated = config.getBoolean(HEADER + "." + ANIMATED);
            isFootersAnimated = config.getBoolean(FOOTER + "." + ANIMATED);
            headers = config.getStringList(HEADER + "." + MESSAGES);
            footers = config.getStringList(FOOTER + "." + MESSAGES);

            return config;
        }
    }


    // Format the given string for tab message chat messages
    public static String formatTabMessageString(String str, boolean isError){
        String finalString = ChatColor.GREEN + "" + ChatColor.BOLD + "[TabMessage] ";
        if(isError){
            finalString += ChatColor.RED;
        }
        finalString += str;
        return finalString;
    }


    public static void setNextTableList(String headerStr, String footerStr){
        currentHeader = headerStr;
        currentFooter = footerStr;

        ByteBuf byteBuffer = ByteBufAllocator.DEFAULT.buffer();
        PacketDataSerializer packetDataSerializer = new PacketDataSerializer(byteBuffer);
        tabListPacket = new PacketPlayOutPlayerListHeaderFooter();

        try {
            packetDataSerializer.a(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + currentHeader + "\"}"));
            packetDataSerializer.a(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + currentFooter + "\"}"));
            tabListPacket.a(packetDataSerializer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Send packets to the player
    public static void sendTabList(Player player){
        if(tabListPacket != null){
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(tabListPacket);
        }
    }
}
