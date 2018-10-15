package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.Util;
import me.ijedi.jedipack.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParkourMenuEvent implements Listener {

    public static final String MENU_PREFIX = "Edit";

    @EventHandler
    public void invClick(InventoryClickEvent event){

        // Make sure we're dealing with a player
        if(event.getWhoClicked() instanceof Player){

            // See if the inventory name matches one of our courses
            Inventory inventory = event.getInventory();
            String courseId = inventory.getName();
            if(courseId.split(" ").length > 1){
                courseId = courseId.replace(MENU_PREFIX, "");
                courseId = courseId.replace(" ", "");
                if(ParkourManager.doesCourseExist(courseId)){

                    Player player = (Player) event.getWhoClicked();

                    // See what item was clicked. The menu library will handle next, prev, and exit. All we need to do is handle the removal of points.
                    ItemStack clickedItem = event.getCurrentItem();
                    if(clickedItem != null && clickedItem.hasItemMeta()){
                        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

                        // Starting point
                        if(itemName.toUpperCase().startsWith("START")){
                            // Make sure course exists
                            if(!ParkourManager.doesCourseExist(courseId)){
                                String message = ParkourManager.formatParkourString(String.format("Course '%s' does not exist!", courseId), true);
                                player.sendMessage(message);
                                player.closeInventory();

                            } else if(!ParkourManager.hasStart(courseId)){
                                // Make sure we have a starting point
                                String message = ParkourManager.formatParkourString(String.format("Course '%s' does not have a starting point!", courseId), true);
                                player.sendMessage(message);
                                player.closeInventory();

                            } else{
                                // Remove start
                                ParkourManager.removeStart(courseId);
                                String message = ParkourManager.formatParkourString(String.format("The starting point for course '%s' has been removed!", courseId), false);
                                player.sendMessage(message);
                                ParkourManager.openCourseMenu(courseId, player);
                            }

                        } else if(itemName.toUpperCase().startsWith("FINISH")){
                            // Finish point

                            // Make sure course exists
                            if(!ParkourManager.doesCourseExist(courseId)){
                                String message = ParkourManager.formatParkourString(String.format("Course '%s' does not exist!", courseId), true);
                                player.sendMessage(message);
                                player.closeInventory();

                            } else if(!ParkourManager.hasFinish(courseId)){
                                // Make sure we have a starting point
                                String message = ParkourManager.formatParkourString(String.format("Course '%s' does not have a finishing point!", courseId), true);
                                player.sendMessage(message);
                                player.closeInventory();

                            } else{
                                // Remove start
                                ParkourManager.removeFinish(courseId);
                                String message = ParkourManager.formatParkourString(String.format("The finishing point for course '%s' has been removed!", courseId), false);
                                player.sendMessage(message);
                                ParkourManager.openCourseMenu(courseId, player);
                            }

                        } else if(itemName.toUpperCase().startsWith("CHECKPOINT")){
                            // Checkpoints
                            // Get the point number
                            itemName = itemName.replace("#", "");
                            String[] checkpointArray = itemName.split(" ");
                            if(checkpointArray.length > 1 && Util.IsInteger(checkpointArray[1])){
                                int pointNum = Integer.parseInt(checkpointArray[1]);

                                // Make sure course exists
                                if(!ParkourManager.doesCourseExist(courseId)){
                                    String message = ParkourManager.formatParkourString(String.format("Course '%s' does not exist!", courseId), true);
                                    player.sendMessage(message);
                                    player.closeInventory();

                                } else {
                                    // Remove the point
                                    String message = ParkourManager.removePoint(courseId, pointNum);
                                    player.sendMessage(message);
                                    ParkourManager.openCourseMenu(courseId, player);
                                }
                            } else {
                                String message = ParkourManager.formatParkourString("Invalid checkpoint name.", true);
                                player.sendMessage(message);
                                player.closeInventory();
                            }
                        }
                    }


                } // Else not a parkour course menu
            }

        }

    }

    public static Menu getMenuFromCourse(String courseId, ParkourCourse course){

        // Check for starting point
        ArrayList<ItemStack> pointItems = new ArrayList<>();
        if(course.getStartLocation() != null){
            ItemStack itemStack = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> itemLore = getPointLore(course.getStartLocation(), ChatColor.RED + "Click to remove the starting point."); // Arrays.asList(ChatColor.RED + "Click to remove the starting point.");
            itemMeta.setLore(itemLore);
            itemMeta.setDisplayName(ChatColor.AQUA + "Start");
            itemStack.setItemMeta(itemMeta);
            pointItems.add(itemStack);
        }

        // Checkpoints
        // Make an arraylist so we can sort it
        ArrayList<Integer> pointIntList = new ArrayList<>();
        for(String pointKey : course.getPointLocations().keySet()){
            if(Util.IsInteger(pointKey)){
                pointIntList.add(Integer.parseInt(pointKey));
            }
        }

        Collections.sort(pointIntList);
        for(int pointKey : pointIntList){
            Location location = course.getPointLocations().get(Integer.toString(pointKey));
            if(location != null){
                ItemStack itemStack = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> itemLore = getPointLore(location, String.format(ChatColor.RED + "Click to remove Checkpoint #%s.", pointKey));
                itemMeta.setLore(itemLore);
                itemMeta.setDisplayName(ChatColor.AQUA + "Checkpoint #" + pointKey);
                itemStack.setItemMeta(itemMeta);
                pointItems.add(itemStack);
            }
        }

        // Finish location
        if(course.getFinishLocation() != null){
            ItemStack itemStack = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> itemLore = getPointLore(course.getFinishLocation(),ChatColor.RED + "Click to remove the finishing point.");
            itemMeta.setLore(itemLore);
            itemMeta.setDisplayName(ChatColor.AQUA + "Finish");
            itemStack.setItemMeta(itemMeta);
            pointItems.add(itemStack);
        }


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

        Menu menu = new Menu(String.format("%s %s", MENU_PREFIX, courseId));
        menu.setContents(pointItems.toArray(new ItemStack[pointItems.size()]));
        menu.setButtons(exitButton, prevButton, nextButton);

        return menu;
    }

    private static List<String> getPointLore(Location location, String firstLine){
        ArrayList<String> lore = new ArrayList<>();
        lore.add(firstLine);
        lore.add(ChatColor.GREEN + "World: " + ChatColor.GOLD + location.getWorld().getName());
        lore.add(ChatColor.GREEN + "X: " + ChatColor.GOLD + location.getX());
        lore.add(ChatColor.GREEN + "Y: " + ChatColor.GOLD + location.getY());
        lore.add(ChatColor.GREEN + "Z: " + ChatColor.GOLD + location.getZ());
        return lore;
    }
}
