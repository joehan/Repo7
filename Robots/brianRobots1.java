package Robots;


//this is the code from the first Battlecode 2014 lecture
//paste this text into RobotPlayer.java in a package called bob
//this code is badly organized. We'll fix it in later lectures.
//you can use this as a reference for how to use certain methods.

import battlecode.common.*;

public class RobotPlayer{
	
	public static void run(RobotController rc){
		while(true){
			if (rc.getType()==RobotType.HQ){
				Direction spawnDir = Direction.NORTH;
				if(rc.isActive()&&rc.canMove(spawnDir)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
					try {
						rc.spawn(spawnDir);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			if (rc.getType()==RobotType.SOLDIER){
				Direction allDirections[] = Direction.values();
				Direction chosenDirection = allDirections[(int)(Math.random()*8)];
				if (rc.isActive()&&rc.canMove(chosenDirection)){
					try {
						rc.move(chosenDirection);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			rc.yield();
		}
	}
}