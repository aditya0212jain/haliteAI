// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import hlt.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map;
// import java.util.Dictionary;

public class MyBot {


    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        Game game = new Game();
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        game.ready("Aditya's 0.0");

        Log.log("Successfully created Aditya 0.0 bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        final Map<Ship,Boolean> ships_exploring_status = new LinkedHashMap<>();

        for (;;) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {

                //Below contains the returning code for ships
                if(ships_exploring_status.get(ship)==null){
                    ships_exploring_status.put(ship,true);
                }
                if(ships_exploring_status.get(ship)==false){
                    if(ship.position==me.shipyard.position){
                        ships_exploring_status.put(ship,true);
                    }else{
                        commandQueue.add(ship.move(gameMap.naiveNavigate(ship, me.shipyard.position)));
                        continue;
                    }
                }else if(ships_exploring_status.get(ship)==true && ship.halite >= Constants.MAX_HALITE/2){
                    ships_exploring_status.put(ship,false);
                }
                //Above contains the returning code

                if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
                    final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                    commandQueue.add(ship.move(randomDirection));
                } else {
                    commandQueue.add(ship.stayStill());
                }
            }

            //below code spawns a new ship if we have sufficient halite and shipyard is not occupied
            // if (
            //     game.turnNumber <= 200 &&
            //     me.halite >= Constants.SHIP_COST &&
            //     !gameMap.at(me.shipyard).isOccupied())
            // {
            //     commandQueue.add(me.shipyard.spawn());
            // }

            game.endTurn(commandQueue);
        }
    }
}
