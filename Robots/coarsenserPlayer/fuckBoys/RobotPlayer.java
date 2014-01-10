package fuckBoys;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> path;
	static ArrayList<MapLocation> farmLocs;
	static int bigBoxSize = 5;
	static int mapSize;
	
	public static void run(RobotController rcIn) throws GameActionException{
		rc=rcIn;
		randall.setSeed(rc.getRobot().getID());
		mapSize = rc.getMapHeight()*rc.getMapWidth();
		int farmerFreqMod = 15 - (mapSize/300);
		if(rc.getType()==RobotType.HQ){
			tryToSpawn();
		}else if(rc.getType()==RobotType.SOLDIER && ((rc.getRobot().getID())%farmerFreqMod) !=0){
			BreadthFirst.init(rc, bigBoxSize);
			MapLocation[] oppPastrs= rc.sensePastrLocations(rc.getTeam().opponent());
			MapLocation goal;
			if (oppPastrs.length>0){
				goal = oppPastrs[randall.nextInt(oppPastrs.length)];
			}else{
				goal = VectorFunctions.getNewPastrLoc(rc, 25, 0);
			}
			path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(goal,bigBoxSize), 100000);
			//VectorFunctions.printPath(path,bigBoxSize);
		}else{
			MapLocation goal = VectorFunctions.getNewPastrLoc(rc, 100, 25);
			BreadthFirst.init(rc, bigBoxSize);
			path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(goal,bigBoxSize), 100000);
		}
		
		
		
		//generate a coarsened map of the world
		//TODO only HQ should do this. The others should download it.
		//cowMapAssessment.assessMap(4, rc);
//		MapAssessment.printBigCoarseMap();
//		MapAssessment.printCoarseMap();

		while(true){
			try{
				
				if(rc.getType()==RobotType.HQ){
					runHQ();
				}else if(rc.getType()==RobotType.SOLDIER && ((rc.getRobot().getID())%10) !=0){
					rc.setIndicatorString(0, "Soldier");
					runSoldier();
				}else{
					rc.setIndicatorString(0,"farmer");
					runFarmer();
				}
			}catch (Exception e){
				//e.printStackTrace();
			}
			rc.yield();
		}
	}

	private static void runHQ() throws GameActionException {
		//tell robots where to go
		tryToSpawn();
	}

	public static void tryToSpawn() throws GameActionException {
		if(rc.isActive()&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
			for(int i=0;i<8;i++){
				Direction trialDir = allDirections[i];
				if(rc.canMove(trialDir)){
					rc.spawn(trialDir);
					break;
				}
			}
		}
	}
	
	private static void runFarmer() throws GameActionException {
		if(path.size()==0){
			rc.construct(RobotType.PASTR);
		}
		//follow breadthFirst path
		Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
		BasicPathing.tryToMove(bdir, true, rc, directionalLooks, allDirections);
		
	}
	
	private static void runSoldier() throws GameActionException {
		//follow orders from HQ
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//BasicPathing.tryToMove(towardEnemy, true, rc, directionalLooks, allDirections);//was Direction.SOUTH_EAST

		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(enemyRobots.length>0){//if there are enemies
			rc.setIndicatorString(0, "There are enemies");
			MapLocation[] robotLocations = new MapLocation[enemyRobots.length];
			for(int i=0;i<enemyRobots.length;i++){
				Robot anEnemy = enemyRobots[i];
				RobotInfo anEnemyInfo = rc.senseRobotInfo(anEnemy);
				robotLocations[i] = anEnemyInfo.location;
			}
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
			if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
				rc.setIndicatorString(1, "trying to shoot");
				if(rc.isActive()){
					rc.attackSquare(closestEnemyLoc);
				}
			}else{
				rc.setIndicatorString(1, "trying to go closer");
				Direction towardClosest = rc.getLocation().directionTo(closestEnemyLoc);
				simpleMove(towardClosest);
			}
		}else{

			if(path.size()==0){
				MapLocation goal = VectorFunctions.getNewPastrLoc(rc, 25, 0);
				path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(rc.senseEnemyHQLocation(),bigBoxSize), 100000);
			}
			//follow breadthFirst path
			Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
			BasicPathing.tryToMove(bdir, true, rc, directionalLooks, allDirections);
		}
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//simpleMove(towardEnemy);
		
	}
	
	public static MapLocation getRandomLocation() {
		return new MapLocation(randall.nextInt(rc.getMapWidth()),randall.nextInt(rc.getMapHeight()));
	}

	private static void simpleMove(Direction chosenDirection) throws GameActionException{
		for(int directionalOffset:directionalLooks){
			int forwardInt = chosenDirection.ordinal();
			Direction trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
			if(rc.canMove(trialDir)){
				rc.move(trialDir);
				break;
			}
		}
	}
	
}