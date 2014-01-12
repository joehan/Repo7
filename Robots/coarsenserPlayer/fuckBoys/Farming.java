package fuckBoys;

import battlecode.common.*;


public class Farming{
	
	/*evaluateSpace is used to generate a double value evaluating how good of a 
	 *pastr spot a robots current location is.
	 * 
	 * @param rc - the robot controller being used.
	 * @returns totalValue - an evaluation of how good this spot is for pastrs
	 */
	public static double evaluateSpace(RobotController rc) throws GameActionException{
		
		int xCenter = rc.getLocation().x;
		int yCenter = rc.getLocation().y;
		int range = 3; //The radius of the pasture's circle
		double totalValue = 0;
		double terrainValue = 0;
		double cowValue = 0;
		double distValue = distanceToOpponent(rc, rc.getLocation());
		
		for (int x = xCenter-range; x<=xCenter+range; x++){
			
			for (int y = yCenter-range; y<=yCenter+range; y++){
				
				MapLocation analyzedSpot = new MapLocation(x,y);
				
				terrainValue += terrainAtLocation(rc, analyzedSpot);
				
				cowValue += cowsAtLocation(rc, analyzedSpot);
				
			}
			
		}
		
		totalValue = terrainValue+cowValue+distValue;
		System.out.println("TotalValue:"+totalValue);
		System.out.println("TerrainlValue:"+terrainValue);
		System.out.println("cowValue:"+cowValue);
		System.out.println("distValue:"+distValue);
		return totalValue;
	}
	/*
	 *terrainAtLocation checks if there is a wall at this location.
	 *normal = +0
	 *wall = +.3
	 *road = -.3
	 *offMap = -.5
	 *@param rc - the robot controller
	 *@param m - the mapLocation to check
	 */
	public static double terrainAtLocation(RobotController rc, MapLocation m){
		TerrainTile terrain = rc.senseTerrainTile(m);
		TerrainTile wall = TerrainTile.VOID;
		TerrainTile road = TerrainTile.ROAD;
		TerrainTile normal = TerrainTile.NORMAL;
		TerrainTile offMap = TerrainTile.OFF_MAP;
		double value = 0;
		if (terrain == normal){
			value +=0.;
		}else if (terrain == wall){
			value +=0.3;
		}else if (terrain == road){
			value -=0.3;
		}else if (terrain == offMap){
			value -=0.5;
		}
		return value;
	}
	
	/*
	 * cowsAtLocaiton is used to check the number of cows at a location, and multiply it by some modifier;
	 */
	public static double cowsAtLocation(RobotController rc, MapLocation m) throws GameActionException{
		double cows = rc.senseCowsAtLocation(m);
		double modifier = 1.;
		return cows*modifier;
	}
	
	/*
	 * 
	 */
	public static Boolean farEnoughFromOtherPastrs(RobotController rc, MapLocation m){
		MapLocation[] pastrLocations = rc.sensePastrLocations(rc.getTeam());
		int minSqDist = 25;
		for (MapLocation i: pastrLocations){
			if (i.distanceSquaredTo(m)<minSqDist){
				return false;
			}
		}
		return true;
	}
	/*
	 * distanceToOpponent returns the distance squared to the opponent times some modifier
	 */
	public static double distanceToOpponent(RobotController rc, MapLocation m){
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		int dist = m.distanceSquaredTo(enemyHQ);
		double modifier = 1.;
		return dist*modifier;
	}
	
	public static boolean shouldIMakePastr(RobotController rc, MapLocation m){
		
		int pastrCount = rc.sensePastrLocations(rc.getTeam()).length;
		double robotCount = (double) rc.senseRobotCount() -1;
		
		double thresholdPercent = .3;
		double minRobots = 2;
		if (pastrCount/robotCount < thresholdPercent && robotCount > minRobots){
			return true;
		}
		return false;
		
				
	}
	
	
	
}