package Team121;

import java.util.Random;

import battlecode.common.*;

public class joeBot{
	static Random rand;
	
	public static void run(RobotController rc){
		rand = new Random();
		Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		
		while(true) {
			if (rc.getType() == RobotType.HQ) {
				try {					
					//Check if a robot is spawnable and spawn one if it is
					if (rc.isActive() && rc.senseRobotCount() < 25) {
						Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
						if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
							rc.spawn(toEnemy);
						}
					}
				} catch (Exception e) {
					System.out.println("HQ Exception");
				}
			}
			
			if (rc.getType() == RobotType.SOLDIER) {
				try {
					if (rc.isActive()) {
						int action = (rc.getRobot().getID()*rand.nextInt(101) + 50)%101;
						//Construct a PASTR
						
						if (action < 2 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 15) {
							MapLocation[] pastures = rc.sensePastrLocations(rc.getTeam());
							if (pastures.length>0){
								
								MapLocation nearestPasture = pastures[0];
								MapLocation current = rc.getLocation();
								for (MapLocation i: rc.sensePastrLocations(rc.getTeam())){
									if (current.distanceSquaredTo(i) < current.distanceSquaredTo(nearestPasture)){
										nearestPasture = i;
									}
								}
								
								if (current.distanceSquaredTo(nearestPasture) < 9){
									rc.construct(RobotType.NOISETOWER);
								}
								else{
									rc.construct(RobotType.PASTR);
								}
							}
							else{
								rc.construct(RobotType.PASTR);
							}
						//Attack a random nearby enemy
						} else if (action < 30) {
							Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,10,rc.getTeam().opponent());
							if (nearbyEnemies.length > 0) {
								RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[0]);
								rc.attackSquare(robotInfo.location);
							}
						//Move in a random direction
						} else if (action < 80) {
							Direction moveDirection = directions[rand.nextInt(8)];
							if (rc.canMove(moveDirection)) {
								rc.move(moveDirection);
							}
						//Sneak towards the enemy
						} else {
							Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
							if (rc.canMove(toEnemy)) {
								rc.sneak(toEnemy);
							}
						}
					}
				} catch (Exception e) {
					System.out.println("Soldier Exception");
				}
			}
			
			rc.yield();
		}
	}
		
}
