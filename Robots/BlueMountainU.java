package NoisePastr;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
	//Let's puts some constants here.  Mmhhh
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static Random rand;
	static int[] distance = {15, 13, 11, 9, 7, 5, 3};
	static int NT = 0;
	
	public static void run(RobotController rc){
		rand = new Random();
		
		
		while(true){
			 if (rc.getType() == RobotType.HQ){
				 try {
					runHQ(rc);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 else if (rc.getType() == RobotType.SOLDIER && rc.senseRobotCount()==1){
				 try {
					MakePastr(rc);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 else if (rc.getType() == RobotType.SOLDIER && rc.senseRobotCount() == 2){
				 try {
					MakeNoiseTwr(rc);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 else if (rc.getType() == RobotType.SOLDIER && rc.senseRobotCount() >2){
				 runSoldier(rc);
			 }
			 else if (rc.getType() == RobotType.NOISETOWER){
				 try {
					runNoiseTwr(rc);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		Direction spawnDirection = directions[rand.nextInt(8)];
		Direction awayEnemy = rc.senseEnemyHQLocation().directionTo(rc.getLocation());
		if(rc.isActive() && rc.senseRobotCount()<25){
			if (rc.senseRobotCount() == 0  && rc.canMove(awayEnemy.rotateRight())){
				rc.spawn(awayEnemy.rotateRight());
			}
			else if (rc.senseRobotCount() == 1 && rc.canMove(awayEnemy.rotateLeft())){
				rc.spawn(awayEnemy.rotateLeft());
			}
			else {
				rc.spawn(spawnDirection);
			}
		}
		
		//Do some computation here
		
	}
	public static void MakePastr(RobotController rc) throws GameActionException{
		if (rc.isActive()){
			rc.construct(RobotType.PASTR);
		}
	}
	 public static void MakeNoiseTwr(RobotController rc) throws GameActionException{
		 if (rc.isActive()){
			 rc.construct(RobotType.NOISETOWER);
		 }
	 }
	 public static void runSoldier(RobotController rc){
		 
	 }
	 
	 public static void runNoiseTwr(RobotController rc) throws GameActionException{
		 Direction dir  = directions[(NT/7)%8];
		 int length = distance[NT%7];
		 MapLocation attackSquare = rc.getLocation();
		 MapLocation square = attackSquare.add(dir, length);
		 if (rc.canAttackSquare(square)){
			 rc.attackSquare(square);
			 rc.yield();
		 }
		 NT += 1;
	 }
}