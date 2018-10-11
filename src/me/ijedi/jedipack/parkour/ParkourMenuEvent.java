package me.ijedi.jedipack.parkour;

import me.ijedi.menulibrary.Menu;
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
                    if(clickedItem != null){
                        String itemName = clickedItem.getItemMeta().getDisplayName();
                        if(itemName.startsWith("Start") || itemName.startsWith("Checkpoint") || itemName.startsWith("Finish")){



                        }

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
            ItemStack itemStack = new ItemStack(Material.GOLD_PLATE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> itemLore = Arrays.asList(ChatColor.RED + "Click to remove the starting point.");
            itemMeta.setLore(itemLore);
            itemMeta.setDisplayName("Start");
            itemStack.setItemMeta(itemMeta);
            pointItems.add(itemStack);
        }

        // Checkpoints
        for(String pointKey : course.getPointLocations().keySet()){
            Location location = course.getPointLocations().get(pointKey);
            if(location != null){
                ItemStack itemStack = new ItemStack(Material.IRON_PLATE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> itemLore = Arrays.asList(String.format(ChatColor.RED + "Click to remove Checkpoint #%s.", pointKey));
                itemMeta.setLore(itemLore);
                itemMeta.setDisplayName("Checkpoint #" + pointKey);
                itemStack.setItemMeta(itemMeta);
                pointItems.add(itemStack);
            }
        }

        // Finish location
        if(course.getFinishLocation() != null){
            ItemStack itemStack = new ItemStack(Material.GOLD_PLATE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> itemLore = Arrays.asList(ChatColor.RED + "Click to remove the finishing point.");
            itemMeta.setLore(itemLore);
            itemMeta.setDisplayName("Finish");
            itemStack.setItemMeta(itemMeta);
            pointItems.add(itemStack);
        }


        // Set up menu buttons
        ItemStack exitButton = new ItemStack(Material.SPRUCE_DOOR_ITEM);
        ItemMeta exitMeta = exitButton.getItemMeta();
        List<String> exitLore = Arrays.asList(ChatColor.GREEN + "Click to exit.");
        exitMeta.setLore(exitLore);
        exitMeta.setDisplayName("Exit");
        exitButton.setItemMeta(exitMeta);

        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        List<String> nextLore = Arrays.asList(ChatColor.GREEN + "Click to go to the next page.");
        exitMeta.setLore(nextLore);
        nextMeta.setDisplayName("Next");
        nextButton.setItemMeta(nextMeta);

        ItemStack prevButton = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevButton.getItemMeta();
        List<String> prevLore = Arrays.asList(ChatColor.GREEN + "Click to go to the previous page.");
        exitMeta.setLore(prevLore);
        prevMeta.setDisplayName("Previous");
        prevButton.setItemMeta(prevMeta);

        Menu menu = new Menu(String.format("%s %s", MENU_PREFIX, courseId));
        menu.setContents(pointItems.toArray(new ItemStack[pointItems.size()]));
        menu.setButtons(exitButton, prevButton, nextButton);

        return menu;
    }
}
