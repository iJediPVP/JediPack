package me.ijedi.jedipack.common.confighelper;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigHelper {

    // Return the File for the given file path.
    public static File getFile(String filePath){
        // Create a new file and save it if it does not exist.
        File file = new File(filePath);
        if(!file.exists()){
            file.getParentFile().mkdirs();
            FileConfiguration config = getFileConfiguration(file);
            saveFile(file, config);
        }
        return file;
    }

    // Return the FileConfiguration for the given File.
    public static FileConfiguration getFileConfiguration(File file){
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config;
    }

    // Save the given File and FileConfiguration. Returns true if the save was successful.
    public static boolean saveFile(File file, FileConfiguration fileConfiguration){
        try{
            fileConfiguration.save(file);
        } catch (IOException e) {
            MessageTypeEnum.GeneralMessage.logMessage("Error saving file: " + file.getPath());
            MessageTypeEnum.GeneralMessage.logMessage(e.toString());
            return false;
        }
        return true;
    }

    // Returns the full file path for the given directory and file name.
    public static String getFullFilePath(String directory, String fileName){
        String fullPath = String.format("%s/%s/%s", JediPackMain.getThisPlugin().getDataFolder(), directory, fileName);
        return fullPath;
    }

}
