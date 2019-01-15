// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import hlt.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map;
// import java.util.Dictionary;

public class MyBot {
    
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

    public static Position optimalMoveHelper(int depth,Position pos,GameMap gameMap,Map<Position,Boolean> visited_position){
        if(depth==0){
            // if(visited_position.get(pos)==null||visited_position.get(pos)==false){
                // visited_position.put(pos,true);
                // return gameMap.at(pos).halite;
                return pos;
            // }else{
                // return 0;
            // }
        }
        else{
            int max = 0;
            Position toReturn = pos;
            ArrayList<Position> nei = neighbours(pos,gameMap);
            for(int i=0;i<nei.size();i++){
                // if(visited_position.get(nei.get(i))==null||visited_position.get(nei.get(i))==false){
                    // visited_position.put(nei.get(i),true);
                    Position value = optimalMoveHelper(depth-1,nei.get(i),gameMap,visited_position);
                    if(gameMap.at(value).halite>=max){
                        max = gameMap.at(value).halite;
                        toReturn = value;
                    }
                // }
            }
            return toReturn;
        }
    }

    

    // -1 for stay still and 0,1,2,3 for respective directions
    public static Direction optimalMove(int depth,Position pos, Ship ship , GameMap gameMap){
        int max2 = 0;
        int opM2 = 0;
        for(int i=0;i<Direction.ALL_CARDINALS.size();i++){
            int sum=0;
            Position current =pos;
            for(int j=depth;j>0;j--){
                current = current.directionalOffset(Direction.ALL_CARDINALS.get(i));
                current = gameMap.normalize(current);
                sum+=gameMap.at(current).halite;
            }
            if(sum>=max2){
                max2 = sum;
                opM2 = i;
            }
        }
        return Direction.ALL_CARDINALS.get(opM2);





        // ArrayList<Position> nei = neighbours(pos,gameMap);
        // Map<Position,Boolean> visited_position = new LinkedHashMap<>();
        // visited_position.put(pos,true);
        // int max = 0;
        // int opM = 0;
        // Position toReturn = pos;
        // for(int i=0;i<nei.size();i++){
        //     visited_position.put(nei.get(i),true);
        //     Position value = optimalMoveHelper(depth-1,nei.get(i),gameMap,visited_position);
        //     Log.log("value :"+Integer.toString(gameMap.at(value).halite));
        //     if(gameMap.at(value).halite>=max){
        //         opM = i;
        //         max = gameMap.at(value).halite;
        //         toReturn = value;
        //     }
        // }
        // Log.log("max : "+Integer.toString(max));

        // return gameMap.naiveNavigate(ship,toReturn);
    }

    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        int return_halite_limit=900;

        Game game = new Game();
        GameMap tempMap = game.gameMap;
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        for(int i=0;i<tempMap.height;i++){

        }
        int number_of_dropoff = 0;
        int ships_limit_initial=10;
        int ships_limit = 8;
        int depth_search = 7;
        int number_blocking_ships =0;
        int distance_between_drops = 15;
        int alloted_turns = 500;
        switch (game.gameMap.height) {
            case 32:
                distance_between_drops = 10;
                ships_limit_initial = 7;
                alloted_turns = 401;
                break;
            case 40:
                distance_between_drops = 11;
                ships_limit_initial = 9;
                alloted_turns = 426;
                break;
            case 48:
                distance_between_drops = 12;
                ships_limit_initial = 11;
                alloted_turns = 451;
                break;
            case 56:
                distance_between_drops = 13;
                ships_limit_initial = 12;
                alloted_turns = 476;
                break;
            default:
                distance_between_drops = 15;
                ships_limit_initial = 13;
                alloted_turns = 501;
                break;
        }
        // distance_between_drops = 25;
        game.ready("Aditya's 0.0");

        Log.log("Successfully created Aditya 0.0 bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        final Map<EntityId,Boolean> ships_exploring_status = new LinkedHashMap<>();
        final Map<Integer,Integer> block_others_ship = new LinkedHashMap<>();
        final Map<Integer,Boolean> player_blocked = new LinkedHashMap<>();

        for (;;) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;
            final int number_of_ships = me.ships.size();


            final ArrayList<Command> commandQueue = new ArrayList<>();

            final Map<Position,Boolean> occupied_position = new LinkedHashMap<>();
            ships_limit = ships_limit_initial + (5*me.dropoffs.size());
            if(me.halite>=10000){
                ships_limit += 3;
            }
            if(me.halite>=20000){
                ships_limit += 4;
            }
            if(me.halite>=30000){
                ships_limit += 4;
            }


            for (final Ship ship : me.ships.values()){
                occupied_position.put(ship.position,true);
            }
            

            for (final Ship ship : me.ships.values()) {
                //For each ship do the following
                //---------------------------------------------------------------------------------------------------------------------------------
                //adding ships for blocking
                if(number_blocking_ships<game.players.size()-1){
                    if(block_others_ship.get(ship.id.id)==null||block_others_ship.get(ship.id.id)==-1){
                        int playerToBlock=0;
                        boolean temp1 = true;
                        for(int i=0;i<game.players.size() && temp1;i++){
                            if(me.id.id!=game.players.get(i).id.id){
                                if(player_blocked.get(i)==null||player_blocked.get(i)==false){
                                    player_blocked.put(i,true);
                                    block_others_ship.put(ship.id.id,i);
                                    number_blocking_ships += 1;
                                    temp1 = false;
                                }
                            }
                        }
                        // block_others_ship.put(ship.id.id,true);
                        
                    }
                }
                if(block_others_ship.get(ship.id.id)==null||block_others_ship.get(ship.id.id)==-1){

                }else{
                    int playerToBlock = block_others_ship.get(ship.id.id);
                    Position finalDrop = game.players.get(playerToBlock).shipyard.position;
                    Direction temp = gameMap.naiveNavigate(ship,finalDrop);
                    Position newPosition = ship.position.directionalOffset(temp);
                    newPosition = gameMap.normalize(newPosition);
                    if(ship.halite < gameMap.at(ship).halite/10 && gameMap.at(ship).halite!=0){
                        commandQueue.add(ship.stayStill());
                        continue;
                    }
                    if(occupied_position.get(newPosition)==null||occupied_position.get(newPosition)==false||gameMap.at(newPosition).isOccupied()==false){
                        occupied_position.put(newPosition,true);
                        occupied_position.put(ship.position,false);
                        commandQueue.add(ship.move(temp));
                    }else{
                        commandQueue.add(ship.stayStill());
                    }
                    continue;
                }
                //end of blocking strategy
                
                //if ship is new
                if(ships_exploring_status.get(ship.id)==null){
                    ships_exploring_status.put(ship.id,true);
                }
                //if ship is returning
                if(ships_exploring_status.get(ship.id)==false){
                    //if ships is returning and just emptied
                    if(ship.halite==0){
                        if(gameMap.at(me.shipyard).isOccupied() && gameMap.at(me.shipyard).ship.id.id == ship.id.id){
                            ships_exploring_status.put(ship.id,true);
                        }
                        for(Dropoff drop : me.dropoffs.values()){
                            if(gameMap.at(drop.position).isOccupied()&& gameMap.at(drop.position).ship.id.id == ship.id.id){
                                ships_exploring_status.put(ship.id,true);
                            }
                        }
                    }
                    else{
                        //adding condition for making it dropoff point
                        if(game.turnNumber<=400 && me.dropoffs.size()<1){
                            boolean farEnoughPoint = true;
                            for(Dropoff drop : me.dropoffs.values()){
                                if(gameMap.calculateDistance(ship.position,drop.position)<distance_between_drops){
                                    farEnoughPoint = false;
                                }
                            }
                            if(gameMap.calculateDistance(ship.position,me.shipyard.position)<distance_between_drops){
                                farEnoughPoint = false;
                            }
                            if(farEnoughPoint){
                                int sum=ship.halite;
                                ArrayList<Position> nei = neighbours(ship.position,gameMap);
                                for(int i=0;i<nei.size();i++){
                                    sum+=gameMap.at(nei.get(i)).halite;
                                }
                                if(sum>1500&& (me.halite+ship.halite)>=8000 ){
                                    commandQueue.add(ship.makeDropoff());
                                    continue;
                                }
                            }
                        }
                        //Dropoff condition above
                        //now selecting the nearest dropoff
                        Position finalDrop = me.shipyard.position;
                        if(me.dropoffs.size()!=0){
                            if(me.dropoffs.get(0)==null){

                            }else{
                                finalDrop = me.dropoffs.get(0).position;
                            }
                        }
                        for(Dropoff drop : me.dropoffs.values()){
                            if(gameMap.calculateDistance(ship.position,drop.position)<=gameMap.calculateDistance(ship.position,finalDrop)){
                                finalDrop = drop.position;
                            }
                        }
                        

                        //uncommet below if you want to take shipyard into consideration too
                        // if(gameMap.calculateDistance(ship.position,finalDrop)-gameMap.calculateDistance(ship.position,me.shipyard.position) > 25){
                        //     finalDrop = me.shipyard.position;
                        // }
                        Direction temp = gameMap.naiveNavigate(ship,finalDrop);
                        int dis_for_drop = gameMap.calculateDistance(ship.position,finalDrop);
                        if(dis_for_drop==1){
                            temp = gameMap.getUnsafeMoves(ship.position,finalDrop).get(0);
                        }
                        Position newPosition = ship.position.directionalOffset(temp);
                        newPosition = gameMap.normalize(newPosition);
                        if(ship.halite < gameMap.at(ship).halite/10 && gameMap.at(ship).halite!=0){
                            commandQueue.add(ship.stayStill());
                            continue;
                        }
                        if(occupied_position.get(newPosition)==null||occupied_position.get(newPosition)==false){
                            occupied_position.put(newPosition,true);
                            occupied_position.put(ship.position,false);
                            commandQueue.add(ship.move(temp));
                        }else{
                            boolean forThisIf = true;
                            for(int i=0;i<4 && forThisIf;i++){
                                final Direction dir = Direction.ALL_CARDINALS.get(i);
                                newPosition = ship.position.directionalOffset(dir);
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
                            // commandQueue.add(ship.stayStill());
                        }
                        continue;
                    }
                }else if(ships_exploring_status.get(ship.id)==true){//Constants.MAX_HALITE
                    //if has sufficient halite then return 
                    if(ship.halite >= return_halite_limit){
                        ships_exploring_status.put(ship.id,false);
                    }
                    // all ships coming to stop at the end
                    Position finalDrop = me.shipyard.position;
                    if(me.dropoffs.size()!=0){
                        if(me.dropoffs.get(0)==null){

                        }else{
                            finalDrop = me.dropoffs.get(0).position;
                        }
                    }
                    for(Dropoff drop : me.dropoffs.values()){
                        if(gameMap.calculateDistance(ship.position,drop.position)<=gameMap.calculateDistance(ship.position,finalDrop)){
                            finalDrop = drop.position;
                        }
                    }
                    if(gameMap.calculateDistance(ship.position,finalDrop)>= (alloted_turns - game.turnNumber - 2)){
                        ships_exploring_status.put(ship.id,false);
                    }
                    
                }
                //Above contains the returning code
                //sufficient halite to move forward
                if(ship.halite < gameMap.at(ship).halite/10 && gameMap.at(ship).halite!=0){
                    commandQueue.add(ship.stayStill());
                    continue;
                }

                //getting out of stuck position at shipyard
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


                if (gameMap.at(ship).halite < 40 || ship.isFull()) {//gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || 
                    //introducing code for moving in the cell with max halite
                    
                    final Direction dirToMove = optimalMove(depth_search,ship.position,ship,gameMap);
                    
                    Position newPosition = ship.position.directionalOffset(dirToMove);
                    newPosition = gameMap.normalize(newPosition);
                    if(occupied_position.get(newPosition)==null||occupied_position.get(newPosition)==false){
                        occupied_position.put(newPosition,true);
                        occupied_position.put(ship.position,false);
                        commandQueue.add(ship.move(dirToMove));
                    }else{
                        //if optimal move is blocked then
                        boolean forThisIf = true;
                        for(int i=0;i<4 && forThisIf;i++){
                            final Direction dir = Direction.ALL_CARDINALS.get(i);
                            newPosition = ship.position.directionalOffset(dir);
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

                        // commandQueue.add(ship.stayStill());
                    }
                    
                    //above code represents the max move
                } else {
                    commandQueue.add(ship.stayStill());
                }
                //End of one loop for each ship ---------------------------------------------------------------------------------------------------------
            }

            //below code spawns a new ship if we have sufficient halite and shipyard is not occupied
            if(me.ships.size()<ships_limit && game.turnNumber <=400 ){
                if(me.halite>=Constants.SHIP_COST && !gameMap.at(me.shipyard).isOccupied()){
                    if(occupied_position.get(me.shipyard.position)==null||occupied_position.get(me.shipyard.position)==false){
                        commandQueue.add(me.shipyard.spawn());
                    }
                }
            }

            if(game.turnNumber == 100){
                return_halite_limit = 800;
            }
            if(game.turnNumber == 200){
                return_halite_limit = 900;
            }
            if(game.turnNumber == 300){
                return_halite_limit = 700;
            }
            if(game.turnNumber == 400){
                return_halite_limit = 600;
            }


            game.endTurn(commandQueue);
        }
    }
}
