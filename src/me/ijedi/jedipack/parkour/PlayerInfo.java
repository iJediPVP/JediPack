package me.ijedi.jedipack.parkour;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;

public class PlayerInfo {

    //Variables
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;
    private String path;
    private Player player;
    private static HashMap<Player, String> lastCPHM = new HashMap<>();
    private static HashMap<Player, Boolean> hasStartCD = new HashMap<>();
    private static HashMap<Player, Boolean> hasFinishCD = new HashMap<>();
    private static HashMap<Player, Boolean> hasStartedHM = new HashMap<>();

    //Constructor
    public PlayerInfo(Player p, JavaPlugin jp){
        player = p;
        plugin = jp;
        file = new File(plugin.getDataFolder() + "/playerData/" + player.getUniqueId() + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
        path = player.getWorld().getName();
    }

    //*************** FILE STUFF ***************

    //Save file
    public void saveFile(){
        try{
            config.save(file);
        }catch (IOException ioe){
            plugin.getLogger().info("PlayerInfo - Error saving player file.");
            return;
        }
    }

    //Set record time
    public void setRecordTime(Long time){
        config.set(path + ".recordTime", time);
        saveFile();
    }

    //Get record time
    public Long getRecordTime(){
        return config.getLong(path + ".recordTime");
    }

    //Set start time
    public void setStartTime(Long time){
        config.set(path + ".startTime", time);
        saveFile();
    }

    //Get start time
    public Long getStartTime(){
        return config.getLong(path + ".startTime");
    }



    //*************** HASHMAP STUFF ***************

    //Set has started
    public void setHasStarted(boolean bool){
        hasStartedHM.put(player, bool);
    }

    //Get has started
    public boolean getHasStarted(){
        if(hasStartedHM.containsKey(player)){
            return hasStartedHM.get(player);
        }else{
            return false;
        }
    }

    //Set last checkpoint
    public void setLastCP(String cpName){
       lastCPHM.put(player, cpName);
    }

    //Get last checkpoint
    public String getLastCP(){
        if(lastCPHM.containsKey(player)){
            return lastCPHM.get(player);
        }else{
            return "null";
        }
    }

    //Set has start cooldown
    public void setHasStartCD(boolean bool){
        hasStartCD.put(player, bool);

        //Check to see if a timer is needed
        if(bool){
            new BukkitRunnable(){
                @Override
                public void run(){
                    hasStartCD.put(player, false);
                    this.cancel();
                }
            }.runTaskLater(plugin, 3 * 20L);
        }//Else do nothing!
    }

    //Get has start cooldown
    public boolean getHasStartCD(){
        if(hasStartCD.containsKey(player)){
            return hasStartCD.get(player);
        }else{
            return false;
        }
    }

    //Set has finish cooldown
    public void setHasFinishCD(boolean bool){
        hasFinishCD.put(player, bool);

        //Check to see if a timer is needed
        if(bool){
            new BukkitRunnable(){
                @Override
                public void run(){
                    hasFinishCD.put(player, false);
                    this.cancel();
                }
            }.runTaskLater(plugin, 3 * 20L);
        }//Else do nothing!
    }

    //Get has finish cooldown
    public boolean getHasFinishCD(){
        if(hasFinishCD.containsKey(player)){
            return hasFinishCD.get(player);
        }else{
            return false;
        }
    }

    //Check if the player's file exists
    public boolean fileExists(){
        if(file.exists()){
            return true;
        }
        return false;
    }

    //Set defaults
    public void setDefaults(){
        config.set(path + ".hasStarted", false);
        config.set(path + ".recordTime", null);
        config.set(path + ".starTime", null);
        saveFile();
    }

    //Check for new time record & if there is a new record set it.
    public void checkForRecord(Long timeEnd){
        //Format
        SimpleDateFormat format = new SimpleDateFormat("mm:ss:SSS");

        //Calculate players time & store record time
        Long runTime = timeEnd - getStartTime();
        Long recordTime = getRecordTime();

        //Check the time
        if(recordTime > 0){
            if(recordTime <= runTime) { //No new record
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[iParkour] " + ChatColor.GOLD + "Unfortunately, you did not beat your previous record of " +
                ChatColor.GREEN + "" + ChatColor.BOLD + format.format(recordTime) + ChatColor.GOLD + ". Your time was: " +
                ChatColor.GREEN + "" + ChatColor.BOLD + format.format(runTime) + ChatColor.GOLD + ".");
            }else { //New record
                setRecordTime(runTime);
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[iParkour] " + ChatColor.GOLD + "Congratulations! You set a " +
                        ChatColor.GREEN + "" + ChatColor.BOLD + "NEW RECORD " + ChatColor.GOLD + "with the time of " +
                        ChatColor.GREEN + "" + ChatColor.BOLD + format.format(runTime) + ChatColor.GOLD + "! Your old time: " +
                        ChatColor.GREEN + "" + ChatColor.BOLD + format.format(recordTime));
            }

        //Else the record is 0 or less, automatically a new record.
        }else{
            setRecordTime(runTime);
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[iParkour] " + ChatColor.GOLD + "Congratulations! You set a record time of " +
            ChatColor.GREEN + "" + ChatColor.BOLD + format.format(runTime) + ChatColor.GOLD + "!");
        }
    }



    public static void InitializePlayerInfos(Collection<? extends Player> players, JavaPlugin plugin){
        //Make sure all online player files are there
        for(Player p : players){
            PlayerInfo pInfo = new PlayerInfo(p, plugin);
            if(!pInfo.fileExists()){
                pInfo.setDefaults();
            }
        }
    }

}
