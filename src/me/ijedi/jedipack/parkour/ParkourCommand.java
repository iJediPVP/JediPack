package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.Util;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ParkourCommand implements CommandExecutor {

    private final String RESTART = "restart";
    private final String CHECKPOINT = "checkpoint";
    private final String[] FIRST_ARG_BLACKLIST = {RESTART, CHECKPOINT};

    private final String CREATE = "create";
    private final String DELETE = "delete";
    private final String START = "start";
    private final String END = "end";



    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        /* Command layout
        / jppk <courseId> <command>

        // Add or delete a course
        /jppk 123 create    // create the course
        /jppk 123 delete    // remove the course

        // Modify a course
        /jppk 123 start // place the starting point of the course
        /jppk 123 1     // place the 'point 1' of the course
        /jppk 123 2     // place the 'point 2' of the course
        /jppk 123 1 remove // remove the 'point 1' from the course
        /jppk 123 1 replace // override existing 'point 1' with a new one
        /jppk 123 end   // place the ending point of the course

        // Interact with the course
        /jppk restart       // return to the start of the course
        /jppk checkpoint    // return to the last checkpoint

        * */


        // Sse if we have args
        if(args.length > 0 && !Util.IsNullOrEmpty(args[0])){
            String firstArg = Util.ToLower(args[0]);

            // Check for a course id. Anything except commands required for this plugin will be allowed.
            if(!ArrayUtils.contains(FIRST_ARG_BLACKLIST, firstArg)){
                // At this point "firstArg" is our course id.

                // Check for our second argument.
                if(args.length > 1 && !Util.IsNullOrEmpty(args[1])){
                    String secondArg = Util.ToLower(args[1]);

                    // Handle create
                    if(secondArg.equals(CREATE)) {

                        String output = ParkourManager.createCourse(firstArg);
                        commandSender.sendMessage(output);
                        return true;

                    }else if(secondArg.equals(DELETE)){ // Handle delete

                        String output = ParkourManager.removeCourse(firstArg);
                        commandSender.sendMessage(output);
                        return true;

                    }else if(secondArg.equals(START)){ // Handle start

                        // Only allow this to be executed by a player
                        if(commandSender instanceof Player){

                            // Get the player's location and try to set the starting point for this course.
                            Player player = (Player)commandSender;
                            Location location =  player.getLocation();
                            String output = ParkourManager.setStart(firstArg, location);
                            commandSender.sendMessage(output);
                            return true;

                        }else{
                            commandSender.sendMessage("This command can only be executed by a player!");
                            return true;
                        }

                    }else if(secondArg.equals(END)){ // Handle end

                        // Only allow this to be executed by a player
                        if(commandSender instanceof Player){

                            // Get the player's location and try to set the finishing point for this course.
                            Player player = (Player)commandSender;
                            Location location =  player.getLocation();
                            String output = ParkourManager.setFinish(firstArg, location);
                            commandSender.sendMessage(output);
                            return true;

                        }else{
                            commandSender.sendMessage("This command can only be executed by a player!");
                            return true;
                        }

                    } else if(Util.IsInteger(secondArg)) { // Handle point numbers.

                        // Only allow this to be executed by a player
                        if(commandSender instanceof Player){

                            // Get the player's location and try to set the starting point for this course.
                            int pointNumber = Integer.parseInt(secondArg);
                            Player player = (Player)commandSender;
                            Location location =  player.getLocation();
                            String output = ParkourManager.setPoint(firstArg, location, pointNumber);
                            commandSender.sendMessage(output);
                            return true;

                        }else{
                            commandSender.sendMessage("This command can only be executed by a player!");
                            return true;
                        }

                    } // TODO: Else, handle invalid second argument.

                } // TODO: Else, handle this..

            } // TODO: Else, handle this...

        }else{
            // TODO: Do something better than this if we don't have arguments..
            return false;
        }

        return false;
    }
}
