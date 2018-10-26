package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SignLockEvents implements Listener {

    private final String SIGNLOCK = "[SignLock]";
    public static ArrayList<Material> LOCKABLE_CONTAINERS = new ArrayList<Material>(){{
        add(Material.CHEST);
        add(Material.TRAPPED_CHEST);
        add(Material.FURNACE);
        add(Material.HOPPER);
        add(Material.DISPENSER);
        add(Material.DROPPER);
    }};

    // Event for handling when sign locks are broken.
    @EventHandler
    public void onSignLockBreak(BlockBreakEvent event){

        Block block = event.getBlock();

        if(!block.getType().equals(Material.WALL_SIGN) && !LOCKABLE_CONTAINERS.contains(block.getType())){
            return;
        }

        Location blockLoc = Util.centerSignLockLocation(block.getLocation());

        // If this location is locked, don't allow it to be broken.
        if(SignLockManager.isSignLockLocation(blockLoc)){
            Player player = event.getPlayer();

            // See if the player can break this block
            SignLock lock = SignLockManager.getLockFromLocation(blockLoc);
            if(lock.hasBreakableAccess(player.getUniqueId())){

                // If it's not a wall sign, warn the player
                if(!block.getType().equals(Material.WALL_SIGN)){
                    MessageTypeEnum.SignLockMessage.sendMessage("You must break this lock on this container first!", player, true);
                    event.setCancelled(true);
                    return;

                }
                // Else allow it and remove the sign lock.
                SignLockManager.removeSignLock(lock);
                MessageTypeEnum.SignLockMessage.sendMessage("Sign Lock #" + Integer.toString(lock.getLockNumber()) + " has been removed!", player, false);
                return;
            }

            if(event.getBlock().getType().equals(Material.WALL_SIGN)){
                MessageTypeEnum.SignLockMessage.sendMessage("This sign lock cannot be broken!", player, true);
            } else {
                MessageTypeEnum.SignLockMessage.sendMessage("This container is locked by a player!", player, true);
            }
            event.setCancelled(true);
        }
    }

    // Event for handling sign text changes
    @EventHandler
    public void onSignChange(SignChangeEvent event){

        // We only care about wall signs
        Block eventBlock = event.getBlock();
        if(eventBlock.getType().equals(Material.WALL_SIGN)){

            // We don't care if the sign lock "command" wasn't given
            boolean isSignLock = event.getLine(0).toLowerCase().equals(SIGNLOCK.toLowerCase());
            if(!isSignLock){
                return;
            }

            // Only when placed on chests and trapped chests
            Block placedOnBlock = Util.getBlockFromPlacedSign(eventBlock);
            if(placedOnBlock == null){
                return;
            }
            Material placedType = placedOnBlock.getType();
            if(LOCKABLE_CONTAINERS.contains(placedType)){

                Player player = event.getPlayer();
                SignLockPlayerInfo playerInfo = SignLockManager.getPlayerInfo(player.getUniqueId());
                Location blockLocation = Util.centerSignLockLocation(eventBlock.getLocation());
                Location placedOnBlockLocation = Util.centerSignLockLocation(placedOnBlock.getLocation());

                if(playerInfo.hasLockAtLocation(blockLocation, placedOnBlockLocation)){
                    MessageTypeEnum.SignLockMessage.sendMessage("There is already a sign lock on this location.", player, true);
                    event.setCancelled(true);

                    // Set to air and give the player their sing back.
                    eventBlock.getLocation().getBlock().setType(Material.AIR);
                    if(player.getGameMode().equals(GameMode.SURVIVAL)){
                        player.getInventory().addItem(new ItemStack(Material.SIGN));
                    }

                } else {
                    // Create the lock and update the sign text
                    SignLock newLock = playerInfo.addNewLock(null, 0, blockLocation, false, true);
                    int lockNum = newLock.getLockNumber();
                    event.setLine(0, ChatColor.GREEN + SIGNLOCK);
                    event.setLine(1, ChatColor.GREEN + "#" + Integer.toString(lockNum));

                    MessageTypeEnum.SignLockMessage.sendMessage("Lock created!", player, false);
                }
            }

        }

    }

    // Event for handling when a player interacts with a sign lock.
    @EventHandler
    public void onSignLockInteract(PlayerInteractEvent event){

        // We only care about right clicks
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            return;
        }

        // If this block can be locked, check if it is locked and see if this player has permission.
        Block block = event.getClickedBlock();
        if(LOCKABLE_CONTAINERS.contains(block.getType())){

            Location blockLoc = Util.centerSignLockLocation(block.getLocation());
            if(SignLockManager.isSignLockLocation(blockLoc)){
                SignLock lock = SignLockManager.getLockFromLocation(blockLoc);

                Player player = event.getPlayer();
                if(!lock.hasContainerAccess(player.getUniqueId())){
                    MessageTypeEnum.SignLockMessage.sendMessage("You do not have permission to use this container!", player, true);
                    event.setCancelled(true);
                }

            }

        }

    }

    // Prevent items from being removed from a locked container
    @EventHandler
    public void signLockInventoryMoveEvent(InventoryMoveItemEvent event){

        // The purpose of this is to prevent items from being removee from or placed in a locked container.
        // We shouldn't need to bother with players, since a player without access can't interact with a locked container..
        if(event.getDestination().getHolder() instanceof Hopper){

            // See if this is a locked location
            Location eventLocation = Util.centerSignLockLocation(event.getSource().getLocation());
            if(SignLockManager.isSignLockLocation(eventLocation)){
                SignLock lock = SignLockManager.getLockFromLocation(eventLocation);
                if(!lock.isHoppersEnabled()){
                    event.setCancelled(true);
                }
            }

        } else if(event.getSource().getHolder() instanceof Hopper){

            // See if this is a locked location
            Location eventLocation = Util.centerSignLockLocation(event.getDestination().getLocation());
            if(SignLockManager.isSignLockLocation(eventLocation)){
                SignLock lock = SignLockManager.getLockFromLocation(eventLocation);
                if(!lock.isHoppersEnabled()){
                    event.setCancelled(true);
                }
            }
        }
    }

}
