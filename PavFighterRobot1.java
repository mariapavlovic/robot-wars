package summative;
/**
 * robot used for battle
 */
import java.awt.Color;
import java.util.*;
import becker.robots.*;

public class PavFighterRobot1 extends FighterRobot{
	private static int defence = 4;
	private static int numMoves = 2;
	private static int attack = 4;
	private int health;
	private int energy;
	private int roundsNotFought = 0;
	private int closeRobotsAttacked = 0;
	private int attackedLowestHealth = 0;

	/**
	 * Constructor
	 * @param c
	 * @param a
	 * @param s
	 * @param d
	 * @param id
	 * @param health
	 */
	public PavFighterRobot1(City c, int a, int s, Direction d, int id, int health) {
		super(c, a, s, d, id, attack, defence, numMoves);
		this.health = health;
		this.setLabel();
	}	


	/**
	 * Sets colour of robot
	 * Sets colour to black if robot dead
	 * Labels each robot with ID number and health
	 */
	public void setLabel()
	{
		String label = (this.getID() + ": " + this.health);
		if (this.health <= 0) {
			this.setColor(Color.BLACK);
		}
		else
			this.setColor(Color.MAGENTA);
		this.setLabel(label);
	}


	/**
	 * given the end coordinates, move to them
	 * @param a - end avenue coordinate
	 * @param s - end street coordinate
	 */
	@Override
	public void goToLocation(int a, int s) {
		int aveDist = a-this.getAvenue(); // travel dist for avenues
		int strDist = s-this.getStreet(); // for streets

		if (aveDist > 0){
			while (!this.isFacingEast()) { // keep turning until facing East
				this.turnLeft();
			}
		}
		else if (aveDist < 0){
			while (!this.isFacingWest()) { // keep turning until facing West
				this.turnLeft();
			}
		}
		this.move(Math.abs(aveDist));

		if (strDist > 0){
			while (!this.isFacingSouth()) { // keep turning until facing South
				this.turnLeft();
			}
		}
		else if (strDist < 0){
			while (!this.isFacingNorth()) { // keep turning until facing North
				this.turnLeft();
			}
		}
		this.move(Math.abs(strDist));
	}


	
	/**
	 * robot sends a turn request to the battle manager
	 * @param energy - my robots updated energy at the beginning of the turn
	 * @param data - array of all the robots in the arena (including my robot)
	 * @return - object of TurnRequest to BattleManager
	 */
	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		
		TurnRequest request;
		int endStreet;
		int endAvenue;
		int fightId = sortOppData(data); // ID of robot to attack 

		this.energy = energy; // updates energy variable
				
		this.health = data[this.getID()].getHealth(); // updates health global variable
		
		this.setLabel();

		// take turn while health > 0 (alive) and energy > 0 (can move around)
		if (this.health > 0 && this.energy > 0) {
			
			// not attacking, stay where you are
			if (fightId == -1) {
				endStreet = this.getStreet();
				endAvenue = this.getAvenue();
			}
			// if attacking
			else {
				int targetStreet = data[fightId].getStreet(); // where to end up
				int targetAvenue = data[fightId].getAvenue();
				
				int [] tempArray = attackOrFollowRobot(targetAvenue, targetStreet, data, fightId);
				
				endAvenue = tempArray[0];
				endStreet = tempArray[1];
				fightId = tempArray[2];
				
			}
		}
		else { // if dead or no energy
			endAvenue = this.getAvenue();
			endStreet = this.getStreet();
			fightId = -1;
		}
		
		// final check to avoid fighting self
		if (this.getID() == fightId) {
			fightId = -1;
		}
		
		request = new TurnRequest(endAvenue, endStreet, fightId, attack);
		return request;
		
	}

	
	
	/**
	 * if attacking a robot, check if it's too far away to move during one turn
	 * if it's not, attack. if it is, then follow it for several turns
	 * @param goToAvenue
	 * @param goToStreet
	 * @param oppData
	 * @param fightID
	 * @return - array of avenue, street, fight ID
	 */
	private int [] attackOrFollowRobot(int goToAvenue, int goToStreet, OppData [] oppData, int fightID) {
		int endA = 0;
		int endS = 0;
		int fighterID = fightID;

		/*
		 * if target robot further away than max num moves
		 */
		if (Math.abs(this.getStreet() - goToStreet) + Math.abs(this.getAvenue() - goToAvenue) > numMoves) {
			
			fighterID = -1; // avoid penalty if cannot reach opponent (don't fight anyone this round, just approach them)
			roundsNotFought += 1; // keep track of rounds not fought to avoid penalty
			
			// if robot north of target robot
			if (this.getStreet() < goToStreet) { 
				
				// move until robot is on same street as target robot (staying under max num moves)
				for (int i = 0; i < numMoves; i++) {
					if (this.getStreet() != goToStreet) { 
						endS = this.getStreet() + i;
					}
				}
				endA = this.getAvenue(); // stay on same avenue, only move closer by streets
			}
			
			// if robot south of target robot
			else if (this.getStreet() > goToStreet) {
				for (int i = 0; i < numMoves; i++) {
					if (this.getStreet() != goToStreet) { 
						endS = this.getStreet() - i;
					}
				}
				endA = this.getAvenue(); // stay on same avenue, only move closer by streets
			}
			
			// if robot and target robot on same street
			else if (this.getStreet() == goToStreet) { 
				// if robot to the left of target robot
				if (this.getAvenue() < goToAvenue) { 
					for (int i = 0; i < numMoves; i++) {
						if (this.getAvenue() != goToAvenue) { 
							endA = this.getAvenue() + i;
						}
					}
				}
			
				// robot to the right of target robot
				else if (this.getAvenue() > goToAvenue) { 
					for (int i = 0; i < numMoves; i++) {
						if (this.getAvenue() != goToAvenue) { 
							endA = this.getAvenue() - i;
						}
					}
					
				}
			
				// if robot on same avenue as target robot 
				// (probably wont happen as this if statement runs if the target robot is too far)
				else {
					endA = this.getAvenue();
				}
				
				endS = this.getStreet(); // don't move streets

			}
			
		}
		else { // if target robot close enough
			endS = oppData[fightID].getStreet();
			endA = oppData[fightID].getAvenue();
		}
		
		int [] returnArray = {endA, endS, fighterID};
		
		return returnArray;
	}
	
	
	

	/**
	 * sorts the opponent data
	 * @param data - copy of everyones data
	 * @return - fight ID
	 */
	private int sortOppData(OppData[] data) {
		PavlovicOppData [] playerData = new PavlovicOppData[data.length];

		/*
		 * calculates street and avenue distances (not coordinates)
		 */
		for (int i = 0; i < data.length; i++){
			playerData[i] = new PavlovicOppData(data[i].getID(), data[i].getAvenue(), data[i].getStreet(), data[i].getHealth(), this.getAvenue(), this.getStreet());
		}

		/*
		 * adding only the robots that are close enough to fight
		 * to array lists of the ID and their distance (s and a)
		 * the tactic is to only fight nearby robots, not to travel
		 * several turns to reach a weak robot. this will lead into
		 * more attacks and less energy points lost
		 */
		
		// sort robots by distances
		insertionSort(playerData, false, true); 

		/*
		 * put robots that are close enough to attack in the same turn
		 * (less than or equal max number of moves) in an arrayList 
		 * (don't know size so can't be array) 
		 * also checks if robot is not own ID, not dead, and enough energy to move
		 */ 
		ArrayList <PavlovicOppData> closeRobotsTemp = new ArrayList<PavlovicOppData>();
		for (int i = 0; i < playerData.length; i++){
			if (playerData[i].totalDistance() <= numMoves && playerData[i].getID() != this.getID()  && playerData[i].getHealth() > 0 ) { 
				closeRobotsTemp.add(new PavlovicOppData(playerData[i].getID(), playerData[i].getAvenue(),playerData[i].getStreet(), playerData[i].getHealth(), this.getAvenue(), this.getStreet()));
			}
		}

		/*
		 * turning the array list into array
		 */
		PavlovicOppData [] closeRobots = new PavlovicOppData[closeRobotsTemp.size()];
		for (int i = 0; i < closeRobots.length; i++) {
			closeRobots[i] = closeRobotsTemp.get(i);
		}

		insertionSort(closeRobots, true, false); // sort nearby robots by health

		int IDtoFight = fightDecision(closeRobots, playerData);
		
		return IDtoFight;
	}

	
	/**
	 * decides who to fight based on the sorted list
	 * @param closeRobots - nearby robot
	 * @param playerData - my copy of everyones data
	 */
	private int fightDecision(PavlovicOppData [] closeRobots, PavlovicOppData [] playerData) {
		int IDtoFight = 0;
		
		// if there are any close robots nearby
		if (closeRobots.length > 0) { 
			IDtoFight = closeRobots[0].getID(); // attack close robot with lowest health (already checked that it is not yourself & not already dead)
		}
		
		// no close robots
		else {
			insertionSort(playerData, true, false); // sorting by health
				/*
				 * checks that robot doesn't attack itself 
				 * and that requested robot isn't already dead
				 */
				for (int i = 0; i < playerData.length; i ++) {
					if (playerData[i].getID() != this.getID() && playerData[i].getHealth() != 0 && this.health > 0 && this.energy > 0)  { 
						IDtoFight = playerData[i].getID(); // lowest health robot
						break;
					}
					else {
						if (i == playerData.length - 1) {
							IDtoFight = -1;
						}
					}
				}
			}
		return IDtoFight;
	}
	
	
	
	/**
	 * sorts by insertion sort
	 * @param list
	 * @param health
	 * @param dist
	 * @return - sorted array
	 */
	private PavlovicOppData[] insertionSort(PavlovicOppData [] list, boolean health, boolean dist){
		if (health) {
			for (int i = 1; i < list.length; i++) {
				int prevIndex = i - 1;
				PavlovicOppData temp = list[i];
				while (temp.getHealth() < list[prevIndex].getHealth() && prevIndex > 0) {
					list[prevIndex + 1] = list[prevIndex];
					prevIndex -= 1;
				}
				if (temp.getHealth() < list[prevIndex].getHealth()) {
					list[prevIndex + 1] = list[prevIndex];
					list[prevIndex] = temp;
				}
				else {
					list[prevIndex + 1] = temp;
				}
			}
			return list;
		}
		else {
			for (int i = 1; i < list.length; i++) {
				int prevIndex = i - 1;
				PavlovicOppData temp = list[i];
				while ((temp.getAvenueDist() + temp.getStreetDist()) < (list[prevIndex].getAvenueDist() + list[prevIndex].getStreetDist()) && prevIndex > 0) {
					list[prevIndex + 1] = list[prevIndex];
					prevIndex -= 1;
				}
				if ((temp.getAvenueDist() + temp.getStreetDist()) < (list[prevIndex].getAvenueDist() + list[prevIndex].getStreetDist())) {
					list[prevIndex + 1] = list[prevIndex];
					list[prevIndex] = temp;
				}
				else {
					list[prevIndex + 1] = temp;
				}
			}
			return list;
		}
	}

	
	/**
	 * my robot gets battleResult from a battle it just participated in
	 * @param healthLost - my health lost
	 * @param oppID - opponents ID
	 * @param oppHealthLost - opponents health lost
	 * @param numRoundsFought
	 */
	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health -= healthLost;
		
	}

}



