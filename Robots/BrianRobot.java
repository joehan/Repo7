package Messing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import battlecode.client.viewer.GameState;
import battlecode.common.*;
import battlecode.world.GameMap;

public class RobotPlayer {
	// intitialize some global variables here
	static int distTo=0;
	static boolean stuck=false;
	static Random rand;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static List<MapLocation> previousStates = new ArrayList<MapLocation>();
	static Direction wallAt = Direction.NORTH;
	static String bugMove = "right";
	static int distToBug = 0;
	static boolean bugInit = false;
	static int moveAround = 0;
	
	public static void run(RobotController rc){
		rand = new Random();
		
		
		while(true) {
			if (rc.getType() == RobotType.HQ) {
				try {					
					runHQ(rc);
				} catch (Exception e) {
					System.out.println("HQ Exception");
				}
			}
			
			if (rc.getType() == RobotType.SOLDIER) {
				try {
					runSoldier(rc);
				} catch (Exception e) {
					System.out.println("Soldier Exception");
				}
			}
			
			rc.yield();
		}
	}
	
	public static void runHQ(RobotController rc) throws GameActionException{
		//kill enemies if they are around the base
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 20, rc.getTeam().opponent());
		if (enemies.length>0 && rc.isActive()){	
			for(int i=0;i==enemies.length;i++){
				MapLocation enemySquare = rc.senseRobotInfo(enemies[i]).location;
				if (rc.canAttackSquare(enemySquare)){
					rc.attackSquare(enemySquare);				
				}
			}
		}
		
		//spawn a soldier
		Direction toEnemy = rc.senseEnemyHQLocation().directionTo(rc.getLocation());
		if(rc.isActive() && rc.canMove(toEnemy)){
			rc.spawn(toEnemy);
		}
		
		//Do some computation here
		
	}
	
	public static void runSoldier(RobotController rc) throws GameActionException{
		//Combat first
		ArrayList<RobotInfo> enemyInfoList = new ArrayList<RobotInfo>(); //make an array for enemy info
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 35 , rc.getTeam().opponent());
		if (rc.isActive() && enemies.length>0){ 	//eventually add another catch--see if HQ wants it to attack
			for(int i=0;i<enemies.length;i++){		//Pastr loop
				enemyInfoList.add(rc.senseRobotInfo(enemies[i]));	//add to array storing enemy info
				if(enemyInfoList.get(i).type == RobotType.PASTR && rc.canAttackSquare(enemyInfoList.get(i).location)){ 
					rc.attackSquare(enemyInfoList.get(i).location);				
				}
			}
			for (int i=0; i<enemies.length; i++){	//Soldier Loop
				if(enemyInfoList.get(i).type == RobotType.SOLDIER && rc.canAttackSquare(enemyInfoList.get(i).location)){ 
					rc.attackSquare(enemyInfoList.get(i).location);	
				}
			}
			for (int i=0; i<enemies.length; i++){ //NoiseTower Loop
				if(enemyInfoList.get(i).type == RobotType.NOISETOWER && rc.canAttackSquare(enemyInfoList.get(i).location)){ 
					rc.attackSquare(enemyInfoList.get(i).location);	
				}
			}
		}
		
		
		//now movement
		
		if (rc.isActive() ){
			boolean isMoving = false;
			MapLocation[] enemyPastrLocation = rc.sensePastrLocations(rc.getTeam().opponent());
			if (enemyPastrLocation.length>0){	//move to enemy pastr's--aggro
				int lowestDistance = 10000;
				int pastrNumber = 0;
				for(int i=0; i<enemyPastrLocation.length; i++){
					lowestDistance = Math.min(lowestDistance, enemyPastrLocation[i].distanceSquaredTo(rc.getLocation()));
					if (lowestDistance==enemyPastrLocation[i].distanceSquaredTo(rc.getLocation())){
						pastrNumber = i;
					}
				}
				Direction dirToPastr = rc.getLocation().directionTo(enemyPastrLocation[pastrNumber]);
				if (Clock.getRoundNum()%20 ==0 || Clock.getRoundNum()%20==1 && !stuck){
					checkStuck(rc, enemyPastrLocation[pastrNumber]);
					distTo = lowestDistance;
				}
				if (rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation())>16){
					moveCloseTo(rc, enemyPastrLocation[pastrNumber]);
					isMoving = true;
				}	
			}
			else if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation())<(rc.getMapHeight()^2)*4 && !isMoving){
				moveRandom(rc);
				
			}
		}
		
		//end of turn communication
		
		if (previousStates.size()>3){
			previousStates.remove(0);
		}
		if (!previousStates.contains(rc.getLocation())){
			previousStates.add(rc.getLocation());
		}	
	}
	public static void moveRandom(RobotController rc) throws GameActionException{
		Direction moveDirection = directions[rand.nextInt(8)];
		rc.move(moveDirection);		
	}
	public static MapLocation positionAfterMove(RobotController rc, Direction dir){
		MapLocation position = rc.getLocation();
		return position.add(dir);
	}
	public static boolean moveAllowed(RobotController rc, Direction dir){
		return (rc.canMove(dir) && !previousStates.contains(positionAfterMove(rc,dir)) && positionAfterMove(rc, dir).distanceSquaredTo(rc.senseEnemyHQLocation())>16);
	}
	public static void moveCloseTo(RobotController rc, MapLocation loc) throws GameActionException{
		Direction dir = rc.getLocation().directionTo(loc);
		if (!stuck){	
			if (moveAllowed(rc,dir)){
				rc.move(dir);
			}
			else if (moveAllowed(rc,dir.rotateLeft())){
				rc.move(dir.rotateLeft());
			}
			else if (moveAllowed(rc,dir.rotateRight())){
				rc.move(dir.rotateRight());
			}
			else if (moveAllowed(rc,dir.rotateLeft().rotateLeft())){
				rc.move(dir.rotateLeft().rotateLeft());
			}
			else if (moveAllowed(rc,dir.rotateRight().rotateRight())){
				rc.move(dir.rotateRight().rotateRight());
			}
			else if (moveAllowed(rc,dir.rotateLeft().rotateLeft().rotateLeft())){
				rc.move(dir.rotateLeft().rotateLeft().rotateLeft());
			}
			else if (moveAllowed(rc,dir.rotateRight().rotateRight().rotateRight())){
				rc.move(dir.rotateRight().rotateRight().rotateRight());
			}
		}
		else{
			moveAround(rc, dir);
			//if (bugMove == "right"){
				//bugMoveRight(rc, loc);
			//}
			//else{
				//bugMoveLeft(rc, loc);
			//}
		}
	}	
	
	public static void moveAround(RobotController rc, Direction dir) throws GameActionException{
		//readjust stuck variable if not stuck
		if (moveAllowed(rc, dir) && moveAround < 2){
			stuck = false;
		}
		//Hug the right wall
		if (moveAllowed(rc,dir) && moveAround < 2 ){
			rc.move(dir);
			moveAround = 0;
		}
		else if (moveAllowed(rc,dir.rotateLeft()) && moveAround < 3){
			rc.move(dir.rotateLeft());
			moveAround = 1;
		}
		else if (moveAllowed(rc,dir.rotateLeft().rotateLeft()) && moveAround < 4){
			rc.move(dir.rotateLeft().rotateLeft());
			moveAround = 2;
		}
		else if (moveAllowed(rc,dir.rotateLeft().rotateLeft().rotateLeft())){
			rc.move(dir.rotateLeft().rotateLeft().rotateLeft());
			moveAround = 3;
		}
		else if (moveAllowed(rc,dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft())){
			rc.move(dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft());
			moveAround = 4;
		}
		else if (moveAllowed(rc,dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft().rotateLeft())){
			rc.move(dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft().rotateLeft());
			moveAround = 5;
		}
		else{
			
		}
	}
	public static void checkStuck(RobotController rc, MapLocation location){
		if (rc.getLocation().distanceSquaredTo(location)>= distTo){
			stuck = true;
		}
	}
	public static void bugMoveRight(RobotController rc, MapLocation loc) throws GameActionException{
		//Direction dir = rc.getLocation().directionTo(loc);
		if (!bugInit){
			wallAt = rc.getLocation().directionTo(loc);
			bugInit = true;
		}
		Direction moveDir = wallAt.rotateLeft().rotateLeft();
		if (moveAllowed(rc, wallAt)){
			rc.move(wallAt);
			if (rc.senseTerrainTile(rc.getLocation().add(wallAt.rotateRight().rotateRight())) == TerrainTile.VOID){
				wallAt = wallAt.rotateRight().rotateRight();
			}
		}
		if (!moveAllowed(rc,moveDir)){
			wallAt = moveDir;
			if (rc.senseTerrainTile(rc.getLocation().add(moveDir)) == TerrainTile.OFF_MAP){
				bugMove = "left";
			}
		}
		else{
			rc.move(moveDir);
		}
		if (rc.getLocation().distanceSquaredTo(loc)<distToBug){
			stuck = false;
			distToBug = 0;
			bugInit = false;
		}
	}
	
	public static void bugMoveLeft(RobotController rc, MapLocation loc) throws GameActionException{
		//Direction dir = rc.getLocation().directionTo(loc);
		if (!bugInit){
			wallAt = rc.getLocation().directionTo(loc);
			bugInit = true;
		}
		if (distToBug == 0){
			distToBug = rc.getLocation().distanceSquaredTo(loc);
		}
		Direction moveDir = wallAt.rotateRight().rotateRight();
		if (moveAllowed(rc, wallAt)){
			rc.move(wallAt);
			if (rc.senseTerrainTile(rc.getLocation().add(wallAt.rotateLeft().rotateLeft())) == TerrainTile.VOID){
				wallAt = wallAt.rotateLeft().rotateLeft();
			}
		}
		if (!moveAllowed(rc,moveDir)){
			wallAt = moveDir;
			if (rc.senseTerrainTile(rc.getLocation().add(moveDir)) == TerrainTile.OFF_MAP){
				rc.wearHat();
			}
		}
		else{
			rc.move(moveDir);
		}
		if (rc.getLocation().distanceSquaredTo(loc)<distToBug){
			stuck = false;
			bugMove = "right";
			distToBug = 0;
			bugInit = false;
		}
	}
}
