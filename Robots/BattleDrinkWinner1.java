package Joe;

import java.util.Random;

import battlecode.common.*;

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
		else if (rc.isActive() && rc.senseRobotCount() < 25) {
			Direction moveDirection = directions[rand.nextInt(8)];
			if (rc.canMove(moveDirection)) {
				rc.spawn(moveDirection);
			}
		}
	}
	
	public static void runSoldier(RobotController rc) throws GameActionException{
		if (rc.isActive()) {
			int xLoc = rc.getLocation().x;
			int yLoc = rc.getLocation().y;
			int action = (rc.getRobot().getID()*rand.nextInt(101) + 50)%101;
			MapLocation[] pastures = rc.sensePastrLocations(rc.getTeam());
			
			//Construct a PASTR/ and towers
			if (action < 2 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 8) {
				double[][] cowGrowthArrays = rc.senseCowGrowth();
				double cowGrowth = cowGrowthArrays[xLoc][yLoc];
				
				if (pastures.length>0){
					MapLocation nearestPasture = pastures[0];
					MapLocation current = rc.getLocation();
					for (MapLocation i: rc.sensePastrLocations(rc.getTeam())){
						if (current.distanceSquaredTo(i) < current.distanceSquaredTo(nearestPasture)){
							nearestPasture = i;
						}
					}
					Robot[] nearbyThings = rc.senseNearbyGameObjects(Robot.class, 9, rc.getTeam());
					if (nearbyThings.length<3){
						if (current.distanceSquaredTo(nearestPasture) < 9){
							rc.construct(RobotType.NOISETOWER);
						}
						else if (cowGrowth>1){
							rc.construct(RobotType.PASTR);
						}
					}
				}
				else if (cowGrowth>1){
					rc.construct(RobotType.PASTR);
				}
			//Basic Herding behavior
			}else if (action < 10 && pastures.length >0){
				
					MapLocation nearestPasture = pastures[0];
					MapLocation current = rc.getLocation();
					for (MapLocation i: rc.sensePastrLocations(rc.getTeam())){
						if (current.distanceSquaredTo(i) < current.distanceSquaredTo(nearestPasture)){
							nearestPasture = i;
						}
					
					Direction herdDirection = rc.getLocation().directionTo(nearestPasture);
					if (rc.canMove(herdDirection)){
						rc.move(herdDirection);
					}
				}
			//Attack a random nearby enemy
			} else if (action < 20) {
				Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,10,rc.getTeam().opponent());
				if (nearbyEnemies.length > 0) {
					RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[0]);
					rc.attackSquare(robotInfo.location);
				}
			//Move in a random direction
			} else {
				Direction moveDirection = directions[rand.nextInt(8)];
				if (rc.canMove(moveDirection)) {
					rc.move(moveDirection);
				}
			}
		}
	}		
}
