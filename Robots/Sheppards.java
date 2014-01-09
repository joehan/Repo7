package team121;

import java.util.HashMap;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;


	
	public class RobotPlayer{
		static Random rand;
		static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
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
			//Check if a robot is spawnable and spawn one if it is
			Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,10,rc.getTeam().opponent());
			if (rc.isActive() && nearbyEnemies.length > 0){
				
				
					RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[0]);
					rc.attackSquare(robotInfo.location);
				
			}
			else if (rc.isActive() && rc.senseRobotCount() < 2) {
				Direction moveDirection = directions[rand.nextInt(8)];
				if (rc.canMove(Direction.EAST)) {
					rc.spawn(Direction.EAST);
				}
			}
		}
		public static void runSoldier(RobotController rc) throws GameActionException{
			//give all robots id number if they don't have one
			MapLocation[] pastures = rc.sensePastrLocations(rc.getTeam());
			if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation())>100 && rc.sensePastrLocations(rc.getTeam()).length<1 && rc.senseBroadcastingRobots().length<1){
				if (rc.isActive()){
					if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation())<144){
						rc.construct(RobotType.PASTR);
						rc.broadcast(1, 1);
					}
					else{
						if (rc.canMove(rc.senseEnemyHQLocation().directionTo(rc.getLocation()))){
							rc.sneak(rc.senseEnemyHQLocation().directionTo(rc.getLocation()));
						}						
					}
				}
			}
			if (pastures.length>0){
				runSheppard(rc);
			}
			if (rc.isConstructing()){
				rc.broadcast(1, 1);
			}
			if (pastures.length<1){
				moveToward(rc.senseEnemyHQLocation(), rc);
			}
		}
		
		public static void runSheppard(RobotController rc) throws GameActionException{
			MapLocation[] pastures = rc.sensePastrLocations(rc.getTeam());
			if (pastures.length>0){
				MapLocation nearestPasture = pastures[0];
				MapLocation current = rc.getLocation();
				for (MapLocation i: rc.sensePastrLocations(rc.getTeam())){
					if (current.distanceSquaredTo(i) < current.distanceSquaredTo(nearestPasture)){
						nearestPasture = i;
					}
				}
				if (rc.isActive()){
					if (current.distanceSquaredTo(nearestPasture)>=25){
						moveToward(nearestPasture, rc);
					}
					else if(16<=current.distanceSquaredTo(nearestPasture) && current.distanceSquaredTo(nearestPasture)<25){
						movePerpindicular(nearestPasture, rc);
						
					}
					else if(16>current.distanceSquaredTo(nearestPasture)){
						moveAway(nearestPasture, rc);						
					}
				}
			}
		}
		public static void moveToward(MapLocation j, RobotController rc) throws GameActionException{
			int x=rc.getLocation().x;
			int y=rc.getLocation().y;
			String vertDirection = "";
			String horDirection = "";
			if (y>j.y){
				vertDirection= "NORTH";
			}
			else if (y<j.y){
				vertDirection= "SOUTH";
			}
			else{
				vertDirection= "";
			}
			if (x>j.x){
				horDirection= "WEST";
			}
			else if(x<j.x){
				horDirection= "EAST";
			}
			else{
				horDirection = "";
			}
			String dir = vertDirection + horDirection;
			Direction direction = stringToDir(dir);
			if (rc.isActive() && rc.canMove(direction)){
				rc.move(direction);
			}
			else{
				moveRandom(rc);
			}
		}
		public static void moveRandom(RobotController rc) throws GameActionException{
			Direction moveDirection = directions[rand.nextInt(8)];
			if (rc.isActive() && rc.canMove(moveDirection)){
				rc.move(moveDirection);
			}
		}
		public static void moveAway(MapLocation j, RobotController rc) throws GameActionException{
			int x=rc.getLocation().x;
			int y=rc.getLocation().y;
			String vertDirection= "";
			String horDirection = "";
			if (y>j.y){
				vertDirection= "SOUTH";
			}
			else if (y<j.y){
				vertDirection= "NORTH";
			}
			else{
				vertDirection= "";
			}
			if (x>j.x){
				horDirection= "EAST";
			}
			else if(x<j.x){
				horDirection= "WEST";
			}
			else{
				horDirection = "";
			}
			String dir = vertDirection + horDirection;
			Direction direction = stringToDir(dir);
			if (rc.isActive() && rc.canMove(direction)){
				rc.move(direction);
			}
			else{
				moveRandom(rc);
			}
		}
		
		private static void movePerpindicular(MapLocation j, RobotController rc) throws GameActionException{
			Direction initial = j.directionTo(rc.getLocation());
			
			
			HashMap<Direction, Direction> perpC = new HashMap<Direction, Direction>();
		    perpC.put(Direction.NORTH, Direction.WEST);
		    perpC.put(Direction.SOUTH, Direction.EAST);
		    perpC.put(Direction.NORTH_WEST, Direction.SOUTH_WEST);
		    perpC.put(Direction.SOUTH_WEST, Direction.SOUTH_EAST);
		    perpC.put(Direction.NORTH_EAST, Direction.NORTH_WEST);
		    perpC.put(Direction.SOUTH_EAST, Direction.NORTH_EAST);
		    perpC.put(Direction.EAST, Direction.NORTH);
		    perpC.put(Direction.WEST, Direction.SOUTH);
		    
		    HashMap<Direction, Direction> perpCc = new HashMap<Direction, Direction>();
		    perpCc.put(Direction.NORTH, Direction.EAST);
		    perpCc.put(Direction.SOUTH, Direction.WEST);
		    perpCc.put(Direction.NORTH_WEST, Direction.NORTH_EAST);
		    perpCc.put(Direction.SOUTH_WEST, Direction.NORTH_WEST);
		    perpCc.put(Direction.NORTH_EAST, Direction.SOUTH_EAST);
		    perpCc.put(Direction.SOUTH_EAST, Direction.SOUTH_WEST);
		    perpCc.put(Direction.EAST, Direction.SOUTH);
		    perpCc.put(Direction.WEST, Direction.NORTH);
		    
		    if (rc.isActive()&&rc.canMove(perpC.get(initial))){
		    	rc.move(perpC.get(initial));
		    }
		    else if(rc.isActive() && rc.canMove(perpCc.get(initial))){
		    	rc.move(perpCc.get(initial));
		    }
		    
		}
		
		private static Direction stringToDir(String j){
			HashMap<String, Direction> m = new HashMap<String, Direction>();
		    m.put("NORTH", Direction.NORTH);
		    m.put("SOUTH", Direction.SOUTH);
		    m.put("NORTHWEST", Direction.NORTH_WEST);
		    m.put("SOUTHWEST", Direction.SOUTH_WEST);
		    m.put("NORTHEAST", Direction.NORTH_EAST);
		    m.put("SOUTHEAST", Direction.SOUTH_EAST);
		    m.put("EAST", Direction.EAST);
		    m.put("WEST", Direction.WEST);
		    return m.get(j);
		}
	}

	
	
	
	//Attack a nearby enemy.  Attack order is PASTR then tower then robots
} else if (action < 20) {
	Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,10,rc.getTeam().opponent());
	if (nearbyEnemies.length > 0) {
		for (int i = -1;i== nearbyEnemies.length; i++){
			RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[i]);
			if (robotInfo.type==RobotType.PASTR){
				rc.attackSquare(robotInfo.location);
			}
			else if (robotInfo.type==RobotType.NOISETOWER){
				rc.attackSquare(robotInfo.location);
			}
			else if (robotInfo.type==RobotType.SOLDIER){
				rc.attackSquare(robotInfo.location);
			}
		}
	}
	else {
		Direction moveDirection = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		if (rc.canMove(moveDirection)) {
			rc.move(moveDirection);
		}
		else{
			movePerpindicular(rc.senseEnemyHQLocation(), rc);
		}
	}
