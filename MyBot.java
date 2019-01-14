// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import hlt.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map;
// import java.util.Dictionary;

public class MyBot {

    // public Direction withoutCollision(Ship ship, Player me, GameMap gameMap){

    // }


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

        final Map<EntityId,Boolean> ships_exploring_status = new LinkedHashMap<>();

        for (;;) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;
            final int number_of_ships = me.ships.size();


            final ArrayList<Command> commandQueue = new ArrayList<>();

            final Map<Position,Boolean> occupied_position = new LinkedHashMap<>();

            for (final Ship ship : me.ships.values()){
                occupied_position.put(ship.position,true);
                Log.log("1");
            }
            

            for (final Ship ship : me.ships.values()) {
                Log.log(Integer.toString(ship.halite)+ " ship's halite");
                // if(gameMap.at(me.shipyard).isOccupied()){
                //     Log.log("shipyard2");
                //     int a = gameMap.at(me.shipyard).ship.id.id;
                //     Log.log(Integer.toString(a));
                //     Log.log(Integer.toString(ship.id.id));
                //     if(gameMap.at(me.shipyard).ship.id.id == ship.id.id){
                //         Log.log("shipyard");
                //     }
                // }
                //Below contains the returning code for ships
                if(ships_exploring_status.get(ship.id)==null){
                    Log.log("2");
                    ships_exploring_status.put(ship.id,true);
                }
                if(ships_exploring_status.get(ship.id)==false){
                    if(gameMap.at(me.shipyard).isOccupied() && gameMap.at(me.shipyard).ship.id.id == ship.id.id){
                        Log.log("3");
                        ships_exploring_status.put(ship.id,true);
                    }else{
                        Log.log("4");
                        Direction temp = gameMap.naiveNavigate(ship,me.shipyard.position);
                        Position newPosition = ship.position.directionalOffset(temp);
                        newPosition = gameMap.normalize(newPosition);
                        if(occupied_position.get(newPosition)==null||occupied_position.get(newPosition)==false){
                            occupied_position.put(newPosition,true);
                            occupied_position.put(ship.position,false);
                            commandQueue.add(ship.move(temp));
                            Log.log("5");
                        }else{
                            Log.log("6");
                            commandQueue.add(ship.stayStill());
                        }
                        continue;
                    }
                }else if(ships_exploring_status.get(ship.id)==true && ship.halite >= 600){//Constants.MAX_HALITE
                    Log.log("7");
                    ships_exploring_status.put(ship.id,false);
                }
                //Above contains the returning code

                //below if ship is stuck at shipyard
                // if(ship.position == me.shipyard.position){
                //     final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                //     Position newPosition = ship.position.directionalOffset(randomDirection);
                //     newPosition = gameMap.normalize(newPosition);
                //     if(occupied_position.get(newPosition)==null||occupied_position.get(newPosition)==false){
                //         occupied_position.put(newPosition,true);
                //         occupied_position.put(ship.position,false);
                //         commandQueue.add(ship.move(randomDirection));
                //     }else{
                //         //else move to the position and swap occupied positions
                //         commandQueue.add(ship.stayStill());
                //     }
                //     continue;
                // }
                //sufficient halite to move forward
                if(ship.halite < gameMap.at(ship).halite/10 && gameMap.at(ship).halite!=0){
                    Log.log("8");
                    commandQueue.add(ship.stayStill());
                    continue;
                }


                if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
                    //introducing code for moving in the cell with max halite
                    int toMove =0;
                    int max = 0;
                    for(int i=0;i<4;i++){
                        final Direction dir = Direction.ALL_CARDINALS.get(i);
                        Position newPosition = ship.position.directionalOffset(dir);
                        newPosition = gameMap.normalize(newPosition);
                        int haliteHere = gameMap.at(newPosition).halite;
                        Log.log(Integer.toString(haliteHere)+" halite here");
                        if(max<haliteHere){
                            max = haliteHere;
                            toMove = i;
                        }
                    }
                    Log.log("max halite : "+ Integer.toString(max));
                    final Direction dirToMove = Direction.ALL_CARDINALS.get(toMove);
                    // final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                    Position newPosition = ship.position.directionalOffset(dirToMove);
                    newPosition = gameMap.normalize(newPosition);
                    if(occupied_position.get(newPosition)==null||occupied_position.get(newPosition)==false){
                        occupied_position.put(newPosition,true);
                        occupied_position.put(ship.position,false);
                        commandQueue.add(ship.move(dirToMove));
                    }else{
                        //else move to the position and swap occupied positions
                        commandQueue.add(ship.stayStill());
                    }
                    //above code represents the max move
                    // final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                } else {
                    commandQueue.add(ship.stayStill());
                }
            
            }

            //below code spawns a new ship if we have sufficient halite and shipyard is not occupied
            if(game.turnNumber <=300 ){
                if(me.halite>=Constants.SHIP_COST && !gameMap.at(me.shipyard).isOccupied()){
                    if(me.halite>=4000 && number_of_ships<3){
                        commandQueue.add(me.shipyard.spawn());
                    }
                }
            }


            game.endTurn(commandQueue);
        }
    }
}
