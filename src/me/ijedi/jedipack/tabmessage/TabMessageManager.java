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
import java.util.HashMap;
import java.util.List;

public class TabMessageManager {

    /*
    * Arguments:
      <d1> - Date format: MM/DD/YYYY
      <d2> - Date format: DD/MM/YYYY
      <t1> - 12 Hour time
      <t2> - 24 Hour time

      Color codes: https://www.digminecraft.com/lists/color_list_pc.php
    * */

    private static final String CONFIG_NAME = "tabMessageConfig.yml";
    private static final String ENABLED = "enabled";
    private static final String COLOR_SYMBOL = "colorSymbol";
    private static final String HEADER = "header";
    private static final String FOOTER = "footer";
    private static final String ANIMATED = "animated";
    private static final String MESSAGES = "message";

    private static FileConfiguration tabMessageConfiguration;
    private static File tabMessageFile;
    private static PacketPlayOutPlayerListHeaderFooter tabListPacket;

    // Config values
    private static boolean isEnabled = false;
    private static char colorSymbol = '$';
    private static HashMap<Integer, String> headerMap = new HashMap<>();
    private static HashMap<Integer, String> footerMap = new HashMap<>();
    private static boolean isHeadersAnimated = false;
    private static boolean isFootersAnimated = false;

    // Current values
    private static int currentHeaderInt;
    private static int currentFooterInt;



    // Load the configuration for tab messages
    public static void intializeTabMessages(){

        // Load the config
        tabMessageFile = getFile();
        tabMessageConfiguration = getFileConfiguration();

        if(isEnabled){
            JediPackMain.getThisPlugin().getLogger().info(formatTabMessageString("TabMessages are enabled!", false));
        } else {
            JediPackMain.getThisPlugin().getLogger().info(formatTabMessageString("TabMessages are not enabled!", false));
            return;
        }

        // Start the task to send packets
        new BukkitRunnable(){
            @Override
            public void run() {
                if(isEnabled){

                    // Set the tab message and send to all players
                    setNextTableList();
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
            tabMessageConfiguration.save(file);

        }catch(IOException e){
            String errorMessage = formatTabMessageString("JediPack Parkour - Error saving configuration file.", true);
            JediPackMain.getThisPlugin().getLogger().info(errorMessage);
            JediPackMain.getThisPlugin().getLogger().info(e.toString());
        }
    }

    // Get the File object for the TabMessageManager.
    private static File getFile(){

        if(tabMessageFile != null){
            return tabMessageFile;
        }

        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/tabMessage";
        String fileName = CONFIG_NAME;
        File configFile = new File(folder, fileName);

        return configFile;
    }

    // Get the FileConfiguration object for the TabMessageManager
    private static FileConfiguration getFileConfiguration(){

        if(tabMessageConfiguration != null){
            return tabMessageConfiguration;
        }

        // Create the file if it doesn't exist.
        if(!tabMessageFile.exists()){
            tabMessageFile.getParentFile().mkdirs();
            FileConfiguration config = YamlConfiguration.loadConfiguration(tabMessageFile);

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
            config.set(HEADER + "." + MESSAGES, defaultHeaders);

            String[] defaultFooters = new String[1];
            defaultFooters[0] = "$fDefault Footer";
            config.set(FOOTER + "." + MESSAGES, defaultFooters);

            fillMessageMaps(defaultHeaders, defaultFooters);

            try{
                config.save(tabMessageFile);
            }
            catch(IOException e){
                String errorMessage = formatTabMessageString("JediPack TabMessage - Error saving configuration file.", true);
                JediPackMain.getThisPlugin().getLogger().info(errorMessage);
                JediPackMain.getThisPlugin().getLogger().info(e.toString());
            }
            return config;

        } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(tabMessageFile);

            // Read config
            isEnabled = config.getBoolean(ENABLED);
            colorSymbol = config.getString(COLOR_SYMBOL).toCharArray()[0];
            isHeadersAnimated = config.getBoolean(HEADER + "." + ANIMATED);
            isFootersAnimated = config.getBoolean(FOOTER + "." + ANIMATED);
            List<String> headerList = config.getStringList(HEADER + "." + MESSAGES);
            List<String> footerList = config.getStringList(FOOTER + "." + MESSAGES);
            fillMessageMaps(headerList.toArray(new String[headerList.size()]), footerList.toArray(new String[footerList.size()]));

            return config;
        }
    }

    // Fill message hash maps from the configuration
    private static void fillMessageMaps(String[] headers, String[] footers){
        headerMap = new HashMap<>();
        footerMap = new HashMap<>();

        // Fill headers
        if(headers != null){
            int x = 0;
            for(String str : headers){
                headerMap.put(x, str);
                x++;
            }
        }

        // Fill footers
        if(footers != null){
            int x = 0;
            for(String str : footers){
                footerMap.put(x, str);
                x++;
            }
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



    // Set the next tab list messages
    public static void setNextTableList(){
        // Get the header
        currentHeaderInt = getNextHeaderInt();
        String headerStr = ChatColor.translateAlternateColorCodes(colorSymbol, headerMap.get(currentHeaderInt));

        // Get the footer
        currentFooterInt = getNextFooterInt();
        String footerStr = ChatColor.translateAlternateColorCodes(colorSymbol, footerMap.get(currentFooterInt));

        // Set up the packet
        ByteBuf byteBuffer = ByteBufAllocator.DEFAULT.buffer();
        PacketDataSerializer packetDataSerializer = new PacketDataSerializer(byteBuffer);
        tabListPacket = new PacketPlayOutPlayerListHeaderFooter();

        try {
            packetDataSerializer.a(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + headerStr + "\"}"));
            packetDataSerializer.a(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footerStr + "\"}"));
            tabListPacket.a(packetDataSerializer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Return the int key for the next header.
    private static int getNextHeaderInt(){
        int nextInt = currentHeaderInt;
        if(currentHeaderInt < headerMap.size() && currentHeaderInt + 1 < headerMap.size()){
            nextInt++;
        } else {
            nextInt = 0;
        }
        return nextInt;
    }

    // Return the int key for the next footer.
    private static int getNextFooterInt(){
        int nextInt = currentFooterInt;
        if(currentFooterInt < footerMap.size() && currentFooterInt + 1 < footerMap.size()){
            nextInt++;
        } else {
            nextInt = 0;
        }
        return nextInt;
    }

    // Send packets to the player
    public static void sendTabList(Player player){
        if(tabListPacket != null){
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(tabListPacket);
        }
    }
}
