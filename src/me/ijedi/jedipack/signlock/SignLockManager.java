package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class SignLockManager {

    /* Config mark up
    isEnabled: false
        locks:
          '<lockIdHere>':
            owner: '<pGuid>'
            x:
            y:
            z:
            worldId: ''
            shared:
              - '<p1Guid>'
              - '<p2Guid>'
    * */

    private static final String CONFIG_NAME = "signLocks.yml";

    private static FileConfiguration lockConfiguration;
    private static File lockFile;
    private static HashMap<UUID, SignLock> signLocks = new HashMap<>();


    // Initialize
    public static void initializeSignLocks(){

        // Load configs
        lockFile = getFile();
        lockConfiguration = getFileConfiguration();

        if(!JediPackMain.isSignLocksEnabled){
            MessageTypeEnum.SignLockMessage.logMessage("Sign Locks are disabled!");
            return;
        }

        MessageTypeEnum.SignLockMessage.logMessage("Sign Locks are enabled!");

        // Register events
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        plugin.getServer().getPluginManager().registerEvents(new SignLockChangeEvent(), plugin);
    }



    // Get the File object for this manager.
    private static File getFile(){

        if(lockFile != null){
            return lockFile;
        }

        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/signLocks";
        String fileName = CONFIG_NAME;
        File configFile = new File(folder, fileName);

        return configFile;
    }

    // Get the FileConfiguration object for the TabMessageManager
    private static FileConfiguration getFileConfiguration(){

        if(lockConfiguration != null){
            return lockConfiguration;
        }

        // Create the file if it doesn't exist.
        if(!lockFile.exists()){
            lockFile.getParentFile().mkdirs();
            FileConfiguration config = YamlConfiguration.loadConfiguration(lockFile);

            // Defaults
            //isEnabled = false;
            //config.set(ENABLED, isEnabled);

            try{
                config.save(lockFile);
            }
            catch(IOException e){
                MessageTypeEnum.SignLockMessage.logMessage("Error saving configuration file.");
                MessageTypeEnum.SignLockMessage.logMessage(e.toString());
            }
            return config;

        } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(lockFile);

            // Read config
            //isEnabled = config.getBoolean(ENABLED);

            return config;
        }
    }
}
