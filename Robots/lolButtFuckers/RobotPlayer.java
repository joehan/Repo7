package lolButtFuckers;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> path = new ArrayList<MapLocation>();
	static int bigBoxSize = 5;
	
	//HQ data:
	static MapLocation rallyPoint;
	static int robotsSpawned = 0;
	//SOLDIER data:
	static int myBand = 100;
	static int pathCreatedRound = -1;
	
	//special typing!
	static String specialType = "normal";
	
	
	
	public static void run(RobotController rcIn) throws GameActionException{
		rc=rcIn;
		Comms.rc = rcIn;
		randall.setSeed(rc.getRobot().getID());
		
		if(rc.getType()==RobotType.HQ){
			rc.broadcast(101,VectorFunctions.locToInt(VectorFunctions.mldivide(rc.senseHQLocation(),bigBoxSize)));//this tells soldiers to stay near HQ to start
			rc.broadcast(102,-1);//and to remain in squad 1
			tryToSpawn();
			BreadthFirst.init(rc, bigBoxSize);
			rallyPoint = VectorFunctions.mladd(VectorFunctions.mldivide(VectorFunctions.mlsubtract(rc.senseEnemyHQLocation(),rc.senseHQLocation()),3),rc.senseHQLocation());
		}else if(rc.readBroadcast(42)==0){
			specialType = "farm";
		}else if(rc.readBroadcast(42)==1){
			specialType = "tower";
		}
		else{
			BreadthFirst.rc=rcIn;//slimmed down init
		}
		//MapLocation goal = getRandomLocation();
		//path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(goal,bigBoxSize), 100000);
		//VectorFunctions.printPath(path,bigBoxSize);
		

		while(true){
			try{
				if(rc.getType()==RobotType.HQ){
					runHQ();
				}else if(specialType == "farm"){
					rc.setIndicatorString(0, "farm");
					runFarm();
				}else if(specialType == "tower"){
					rc.setIndicatorString(0,"tower");
					runTower();
				}
				else if(rc.getType()==RobotType.SOLDIER){
					runSoldier();
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			rc.yield();
		}
	}
	
	private static void runFarm() throws GameActionException {
		if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation())<4){
			Direction away = rc.getLocation().directionTo(rc.senseEnemyHQLocation()).opposite();
		
			rc.move(away);
		}else{
			rc.construct(RobotType.PASTR);
		}
	}
	
	private static void runTower() throws GameActionException {
		System.out.println(rc.getType());
		if (rc.getType() == RobotType.SOLDIER){
			if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation())<2 && !rc.isConstructing()){
				Direction towerDir = rc.getLocation().directionTo(rc.senseEnemyHQLocation()).rotateLeft();
		
				rc.move(towerDir);
			}else{
				rc.construct(RobotType.NOISETOWER);
			}
		}
		else if (rc.getType() == RobotType.NOISETOWER){
			System.out.println("Shooter");
			MapLocation[] maps = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 400);
			MapLocation attackHere = maps[randall.nextInt(maps.length)];
			if (rc.canAttackSquare(attackHere))
				rc.attackSquare(attackHere);
			
			
		}
	}
	
	private static void runHQ() throws GameActionException {
		//TODO consider updating the rally point to an allied pastr 
		//defend self
		rc.broadcast(42, robotsSpawned);
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if (enemyRobots.length !=0){
			MapLocation[] robotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc);
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
			if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){//close enough to shoot
				if(rc.isActive()){
					rc.attackSquare(closestEnemyLoc);
				}
			}
		}
		//tell them to go to the rally point
		Comms.findPathAndBroadcast(1,rc.getLocation(),rallyPoint,bigBoxSize,2);
		
		//if the enemy builds a pastr, tell sqaud 2 to go there.
		MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
		if(enemyPastrs.length>0){
			Comms.findPathAndBroadcast(2,rallyPoint,enemyPastrs[0],bigBoxSize,2);//for some reason, they are not getting this message
		}
		
		//after telling them where to go, consider spawning
		tryToSpawn();
	}

	
	public static void tryToSpawn() throws GameActionException {
		robotsSpawned +=1;
		if(rc.senseRobotCount() < 3){
			Direction away = rc.getLocation().directionTo(rc.senseEnemyHQLocation()).opposite();
			rc.spawn(away);
		}
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
	
	private static void runSoldier() throws GameActionException {
		//follow orders from HQ
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(enemyRobots.length>0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
			MapLocation[] robotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc);
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
			if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){//close enough to shoot
				if(rc.isActive()){
					rc.attackSquare(closestEnemyLoc);
				}
			}else{//not close enough to shoot, so try to go shoot
				Direction towardClosest = rc.getLocation().directionTo(closestEnemyLoc);
				simpleMove(towardClosest);
			}
		}else{//NAVIGATION BY DOWNLOADED PATH
			rc.setIndicatorString(0, "team "+myBand+", path length "+path.size());
			if(path.size()<=1){
				//check if a new path is available
				int broadcastCreatedRound = rc.readBroadcast(myBand);
				if(pathCreatedRound<broadcastCreatedRound){
					rc.setIndicatorString(1, "downloading path");
					pathCreatedRound = broadcastCreatedRound;
					path = Comms.downloadPath();
				}
			}
			if(path.size()>0){
				//follow breadthFirst path
				Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
				double distanceFromHomeToOpp = rc.senseEnemyHQLocation().distanceSquaredTo(rc.senseHQLocation());
				double distanceFromHome = rc.getLocation().distanceSquaredTo(rc.senseHQLocation());
				double distanceFromBasePercent =(Math.sqrt(distanceFromHome))/(Math.sqrt(distanceFromHomeToOpp));
				
				BasicPathing.tryToMove(bdir, true, rc, directionalLooks, allDirections, distanceFromBasePercent<.2);
			}
		}
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//BasicPathing.tryToMove(towardEnemy, true, rc, directionalLooks, allDirections);//was Direction.SOUTH_EAST
		
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//simpleMove(towardEnemy);
		
	}
	
	private static MapLocation getRandomLocation() {
		return new MapLocation(randall.nextInt(rc.getMapWidth()),randall.nextInt(rc.getMapHeight()));
	}

	private static void simpleMove(Direction chosenDirection) throws GameActionException{
		if(rc.isActive()){
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
	
}