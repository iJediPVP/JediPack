package me.ijedi.jedipack.tabmessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.Util;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.PacketDataSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TabMessageManager {

    /*
    * Arguments:
      <d1> - Date format: MM/DD/YYYY
      <d2> - Date format: DD/MM/YYYY
      <t1> - 12 Hour time
      <t2> - 24 Hour time
      <wt1> - 12 Hour world time
      <wt2> - 24 Hour world time

      \n - new lines (not \r\n)

      Color codes: https://www.digminecraft.com/lists/color_list_pc.php
    * */

    private static final String CONFIG_NAME = "tabMessageConfig.yml";
    private static final String ENABLED = "enabled";
    private static final String COLOR_SYMBOL = "colorSymbol";
    private static final String HEADER = "header";
    private static final String FOOTER = "footer";
    private static final String MESSAGES = "message";
    private static final String WORLD_NAME = "worldForTime";

    private static FileConfiguration tabMessageConfiguration;
    private static File tabMessageFile;
    private static PacketPlayOutPlayerListHeaderFooter tabListPacket;

    private static final HashMap<String, SimpleDateFormat> TAB_DATE_ARGS = new HashMap<String, SimpleDateFormat>(){{
        put("<d1>", new SimpleDateFormat("MM/dd/yyyy"));
        put("<d2>", new SimpleDateFormat("dd/MM/yyyy"));
        put("<t1>", new SimpleDateFormat("hh:mm:ss a z")); //12 hour time
        put("<t2>", new SimpleDateFormat("HH:mm:ss")); //24 hour time
    }};

    private static final String WORLD_TIME_ARG = "<wt1>";
    private static final String WORLD_TIME24_ARG = "<wt2>";

    // Config values
    private static boolean isEnabled = false;
    private static char colorSymbol = '$';
    private static HashMap<Integer, String> headerMap = new HashMap<>();
    private static HashMap<Integer, String> footerMap = new HashMap<>();
    private static String worldNameForTime;
    private static BukkitTask tabMessageTask;

    // Current values
    private static int currentHeaderInt;
    private static int currentFooterInt;



    // Load the configuration for tab messages
    public static void intializeTabMessages(boolean reload){

        // Load the config
        tabMessageFile = getFile();
        tabMessageConfiguration = getFileConfiguration(reload);

        if(!isEnabled){
            JediPackMain.getThisPlugin().getLogger().info(formatTabMessageLogString("TabMessages are not enabled!", false));
            return;
        }
        JediPackMain.getThisPlugin().getLogger().info(formatTabMessageLogString("TabMessages are enabled!", false));

        // Clean up existing task
        if(tabMessageTask != null && !tabMessageTask.isCancelled()){
            tabMessageTask.cancel();
        }

        // Start the task to send packets
        tabMessageTask = new BukkitRunnable(){
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
    private static FileConfiguration getFileConfiguration(boolean reload){

        if(tabMessageConfiguration != null && !reload){
            return tabMessageConfiguration;
        }

        // Create the file if it doesn't exist.
        if(!tabMessageFile.exists()){
            tabMessageFile.getParentFile().mkdirs();
            FileConfiguration config = YamlConfiguration.loadConfiguration(tabMessageFile);

            // Defaults
            isEnabled = false;
            config.set(ENABLED, isEnabled);

            colorSymbol = '$';
            config.set(COLOR_SYMBOL, Character.toString(colorSymbol));

            worldNameForTime = "world";
            config.set(WORLD_NAME, worldNameForTime);

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
                String errorMessage = formatTabMessageLogString("JediPack TabMessage - Error saving configuration file.", true);
                JediPackMain.getThisPlugin().getLogger().info(errorMessage);
                JediPackMain.getThisPlugin().getLogger().info(e.toString());
            }
            return config;

        } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(tabMessageFile);

            // Read config
            isEnabled = config.getBoolean(ENABLED);
            colorSymbol = config.getString(COLOR_SYMBOL).toCharArray()[0];
            List<String> headerList = config.getStringList(HEADER + "." + MESSAGES);
            List<String> footerList = config.getStringList(FOOTER + "." + MESSAGES);
            fillMessageMaps(headerList.toArray(new String[headerList.size()]), footerList.toArray(new String[footerList.size()]));
            worldNameForTime = config.getString(WORLD_NAME);

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




    // Format the given string for tab message log messages
    public static String formatTabMessageLogString(String str, boolean isError){
        String finalString = ChatColor.GREEN + "" + ChatColor.BOLD + "[TabMessage] ";
        if(isError){
            finalString += ChatColor.RED;
        }
        finalString += str;
        return finalString;
    }

    // Format the given string for tab messages that are sent via packets
    public static String formatTabMessage(String str){

        // Fill in date/times
        Date date = new Date();
        for(String arg : TAB_DATE_ARGS.keySet()){
            str = str.replaceAll(arg, TAB_DATE_ARGS.get(arg).format(date));
        }

        // World times
        if(str.contains(WORLD_TIME_ARG) && !Util.IsNullOrEmpty(worldNameForTime)){
            // 12 hour time
            World worldForTime = Bukkit.getWorld(worldNameForTime);

            if(worldForTime != null){
                long worldLong = worldForTime.getTime();
                String formatted = Util.convertWorldTicksToTimeString(worldLong, false);
                str = str.replace(WORLD_TIME_ARG, formatted);
            }

        } else if(str.contains(WORLD_TIME24_ARG) && !Util.IsNullOrEmpty(worldNameForTime)){
            // 24 hour time
            World worldForTime = Bukkit.getWorld(worldNameForTime);

            if(worldForTime != null){
                long worldLong = worldForTime.getTime();
                String formatted = Util.convertWorldTicksToTimeString(worldLong, true);
                str = str.replace(WORLD_TIME24_ARG, formatted);
            }
        }

        // Translate colors
        str = ChatColor.translateAlternateColorCodes(colorSymbol, str);
        return str;
    }





    // Set the next tab list messages
    public static void setNextTableList(){
        // Get the header
        currentHeaderInt = getNextHeaderInt();
        String headerStr = headerMap.get((currentHeaderInt));
        headerStr = formatTabMessage(headerStr);

        // Get the footer
        currentFooterInt = getNextFooterInt();
        String footerStr = footerMap.get(currentFooterInt);
        footerStr = formatTabMessage(footerStr);

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
