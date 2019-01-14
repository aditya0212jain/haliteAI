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
    
    public static ArrayList<Position> neighbours(Position pos,GameMap gameMap){
        ArrayList<Position> ans = new ArrayList<>();
        for(int i=0;i<4;i++){
            final Direction dir = Direction.ALL_CARDINALS.get(i);
            Position newPosition = pos.directionalOffset(dir);
            newPosition = gameMap.normalize(newPosition);
            ans.add(newPosition);
        }
        return ans;
    }

    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        int return_halite_limit=250;

        Game game = new Game();
        GameMap tempMap = game.gameMap;
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        for(int i=0;i<tempMap.height;i++){

        }
        int number_of_dropoff = 0;
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
                //For each ship do the following
                //---------------------------------------------------------------------------------------------------------------------------------

                Log.log(Integer.toString(ship.halite)+ " ship's halite");
                if(ships_exploring_status.get(ship.id)==null){
                    Log.log("2");
                    ships_exploring_status.put(ship.id,true);
                }
                if(ships_exploring_status.get(ship.id)==false){
                    if(gameMap.at(me.shipyard).isOccupied() && gameMap.at(me.shipyard).ship.id.id == ship.id.id){
                        ships_exploring_status.put(ship.id,true);
                    }else{
                        //adding condition for making it dropoff point
                        if(me.dropoffs.size()<1){
                            if(gameMap.calculateDistance(ship.position,me.shipyard.position)>=10){
                                int sum=ship.halite;
                                ArrayList<Position> nei = neighbours(ship.position,gameMap);
                                for(int i=0;i<nei.size();i++){
                                    sum+=gameMap.at(nei.get(i)).halite;
                                }
                                if(sum>1500&& (me.halite+ship.halite)>=Constants.DROPOFF_COST ){
                                    commandQueue.add(ship.makeDropoff());
                                    continue;
                                }
                            }
                        }
                        Direction temp = gameMap.naiveNavigate(ship,me.shipyard.position);
                        Position newPosition = ship.position.directionalOffset(temp);
                        newPosition = gameMap.normalize(newPosition);
                        if(occupied_position.get(newPosition)==null||occupied_position.get(newPosition)==false){
                            occupied_position.put(newPosition,true);
                            occupied_position.put(ship.position,false);
                            commandQueue.add(ship.move(temp));
                        }else{
                            commandQueue.add(ship.stayStill());
                        }
                        continue;
                    }
                }else if(ships_exploring_status.get(ship.id)==true && ship.halite >= return_halite_limit){//Constants.MAX_HALITE
                    Log.log("7");
                    ships_exploring_status.put(ship.id,false);
                }
                //Above contains the returning code
                //sufficient halite to move forward
                if(ship.halite < gameMap.at(ship).halite/10 && gameMap.at(ship).halite!=0){
                    Log.log("8");
                    commandQueue.add(ship.stayStill());
                    continue;
                }

                if(gameMap.at(me.shipyard).isOccupied() && gameMap.at(me.shipyard).ship.id.id == ship.id.id){
                    boolean forThisIf = true;
                    for(int i=0;i<4 && forThisIf;i++){
                        final Direction dir = Direction.ALL_CARDINALS.get(i);
                        Position newPosition = ship.position.directionalOffset(dir);
                        newPosition = gameMap.normalize(newPosition);
                        if(occupied_position.get(newPosition)==null||occupied_position.get(newPosition)==false){
                            occupied_position.put(newPosition,true);
                            occupied_position.put(ship.position,false);
                            commandQueue.add(ship.move(dir));
                            forThisIf = false;
                        }
                    }
                    if(forThisIf){
                        commandQueue.add(ship.stayStill());
                    }
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
                    
                    final Direction dirToMove = Direction.ALL_CARDINALS.get(toMove);
                    
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
                //End of one loop for each ship ---------------------------------------------------------------------------------------------------------
            }

            //below code spawns a new ship if we have sufficient halite and shipyard is not occupied
            if(game.turnNumber <=100 ){
                if(me.halite>=Constants.SHIP_COST && !gameMap.at(me.shipyard).isOccupied()){
                    // if(me.halite>=4000 && number_of_ships<15){
                        commandQueue.add(me.shipyard.spawn());
                    // }
                }
            }else{
                if(number_of_ships<15&&game.turnNumber<450){
                    if(me.halite>=Constants.SHIP_COST && !gameMap.at(me.shipyard).isOccupied()){
                            commandQueue.add(me.shipyard.spawn());
                    }
                }
            }

            if(game.turnNumber == 100){
                return_halite_limit = 500;
            }
            if(game.turnNumber == 200){
                return_halite_limit = 600;
            }
            if(game.turnNumber == 300){
                return_halite_limit = 700;
            }
            if(game.turnNumber == 400){
                return_halite_limit = 800;
            }


            game.endTurn(commandQueue);
        }
    }
}
