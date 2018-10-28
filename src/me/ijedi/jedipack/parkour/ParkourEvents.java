package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import me.ijedi.jedipack.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ParkourEvents implements Listener {

    public static final String MENU_PREFIX = "Edit";

    // Handle when a player clicks on an inventory for a parkour course.
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
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("Course '%s' does not exist!", courseId), player, true);
                                player.closeInventory();

                            } else if(!ParkourManager.hasStart(courseId)){
                                // Make sure we have a starting point
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("Course '%s' does not have a starting point!", courseId), player, true);
                                player.closeInventory();

                            } else{
                                // Remove start
                                ParkourManager.removeStart(courseId);
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("The starting point for course '%s' has been removed!", courseId), player,false);
                                ParkourManager.openCourseMenu(courseId, player);
                            }

                        } else if(itemName.toUpperCase().startsWith("FINISH")){
                            // Finish point

                            // Make sure course exists
                            if(!ParkourManager.doesCourseExist(courseId)){
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("Course '%s' does not exist!", courseId), player,true);
                                player.closeInventory();

                            } else if(!ParkourManager.hasFinish(courseId)){
                                // Make sure we have a starting point
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("Course '%s' does not have a finishing point!", courseId), player,true);
                                player.closeInventory();

                            } else{
                                // Remove start
                                ParkourManager.removeFinish(courseId);
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("The finishing point for course '%s' has been removed!", courseId), player,false);
                                ParkourManager.openCourseMenu(courseId, player);
                            }

                        } else if(itemName.toUpperCase().startsWith("CHECKPOINT")){
                            // Checkpoints
                            // Get the point number
                            itemName = itemName.replace("#", "");
                            String[] checkpointArray = itemName.split(" ");
                            if(checkpointArray.length > 1 && Util.isInteger(checkpointArray[1])){
                                int pointNum = Integer.parseInt(checkpointArray[1]);

                                // Make sure course exists
                                if(!ParkourManager.doesCourseExist(courseId)){
                                    MessageTypeEnum.ParkourMessage.sendMessage(String.format("Course '%s' does not exist!", courseId), player,true);
                                    player.closeInventory();

                                } else {
                                    // Remove the point
                                    String message = ParkourManager.removePoint(courseId, pointNum);
                                    player.sendMessage(message);
                                    ParkourManager.openCourseMenu(courseId, player);
                                }
                            } else {
                                MessageTypeEnum.ParkourMessage.sendMessage("Invalid checkpoint name.", player,true);
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
            if(Util.isInteger(pointKey)){
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


    // Handle when someone tries to break a parkour point.
    @EventHandler
    public void onParkourBlockBreak(BlockBreakEvent event){

        // See if the block location matches one of our parkour points
        Location blockLocation = event.getBlock().getLocation();
        if(ParkourManager.isParkourBlockLocation(blockLocation)){
            Player player = event.getPlayer();
            if(player != null){
                MessageTypeEnum.ParkourMessage.sendMessage("You cannot break this. It's part of a parkour course!", player, true);
            }
            event.setCancelled(true);
        }
    }


    // Handle when a player steps on a parkour pressure plate.
    @EventHandler
    public void onPointInteract(PlayerInteractEvent event){

        // Only check physical action types
        if(event.getAction() == Action.PHYSICAL){

            // Get the block location
            Block block = event.getClickedBlock();
            Location location = block.getLocation();
            Player player = event.getPlayer();

            /*// See if the player has perms
            if(ParkourManager.getPermsEnabled() && !player.hasPermission(ParkourCommand.PKPERM_PARKOUR)){
                player.sendMessage(ParkourManager.formatParkourString("You do not have permission to use this!", true));
                return;
            }*/

            // Check for a course id
            String courseId = ParkourManager.getCourseIdFromLocation(location);
            if(!Util.isNullOrEmpty(courseId)){
                ParkourCourse course = ParkourManager.getCourse(courseId);


                // Starting point
                if(ParkourManager.isStartLocation(location, false, course)){

                    // Get the player info for this player from the parkour manager
                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, courseId);

                    // See if there is a start cool down
                    if(info.hasStartMessageCoolDown()){
                        return;
                    }

                    // If the player has already started, reset their start date.
                    if(info.hasStartedThisCourse(courseId)){
                        Date startDate = new Date();
                        info.setStartDate(startDate, courseId);
                        MessageTypeEnum.ParkourMessage.sendMessage(String.format("Restarting course '%s'!", courseId), player,false);

                    } else {
                        // Else we haven't started this course. So we should start it.
                        Date startDate = new Date();
                        info.setStartDate(startDate, courseId);
                        MessageTypeEnum.ParkourMessage.sendMessage(String.format("Course '%s' started!", courseId), player, false);
                    }
                    info.beginStartMessageCoolDown();


                } else if(ParkourManager.isFinishLocation(location, false, course)){

                    // Get the player info for this player from the parkour manager
                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, courseId);

                    // See if there is a finish cool down
                    if(info.hasFinishMessageCoolDown()){
                        return;
                    }

                    // If the player hasn't started this course, send them a warning.
                    if(!info.hasStartedThisCourse(courseId)){
                        MessageTypeEnum.ParkourMessage.sendMessage(String.format("You haven't started course '%s' yet!", courseId), player,true);

                    } else {

                        // They finished!
                        long courseTime = info.getCourseTime(new Date());
                        boolean isNewRecord = info.checkForCourseRecord();
                        String timeStr = info.formatTime(courseTime);
                        if(isNewRecord){
                            MessageTypeEnum.ParkourMessage.sendMessage(String.format("Congratulations! You have finished course '%s' with a new record time of '%s'!", courseId, timeStr), player,false);

                        } else {
                            long prevRecordTime = info.getRecordTime();
                            if(prevRecordTime == 0){
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("Congratulations! You have finished course '%s' with a time of %s!", courseId, timeStr), player,false);
                            } else {
                                String prevRecordTimeStr = info.formatTime(prevRecordTime);
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("Congratulations! You have finished course '%s' with a time of %s! Your personal best is %s!", courseId, timeStr, prevRecordTimeStr), player,false);
                            }
                            info.finishedCourse();
                        }

                        info.beginFinishMessageCoolDown(true);
                        return;
                    }
                    info.beginFinishMessageCoolDown(false);

                } else if(ParkourManager.isCheckpointLocation(location, false, course)){
                    // Checkpoint location

                    // See if the player has started the course yet
                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, courseId);
                    if(!info.hasStartedThisCourse(courseId)){
                        int activatedCheckpoint = course.getCheckpointFromLocation(event.getClickedBlock().getLocation());
                        if(!info.hasCheckpointMessageCoolDown(activatedCheckpoint)){
                            MessageTypeEnum.ParkourMessage.sendMessage("You haven't started this course yet!", player,true);
                            info.beginCheckpointMessageCoolDown(activatedCheckpoint);
                        }

                        return;
                    }

                    // See if the player has already hit this checkpoint
                    int currentCheckpoint = info.getCurrentCheckpoint();
                    int activatedCheckpoint = course.getCheckpointFromLocation(event.getClickedBlock().getLocation());
                    if(!info.hasCheckpointMessageCoolDown(activatedCheckpoint)){
                        if(activatedCheckpoint > currentCheckpoint){
                            MessageTypeEnum.ParkourMessage.sendMessage("Checkpoint reached!", player,false);
                            info.setCurrentCheckpoint(activatedCheckpoint);

                        } else {
                            MessageTypeEnum.ParkourMessage.sendMessage("You've already reached this checkpoint!", player, true);
                        }
                        info.beginCheckpointMessageCoolDown(activatedCheckpoint);
                    }
                    //info.beginCheckpointMessageCoolDown();
                    return;
                }
            }


        }

    }
}
