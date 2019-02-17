package summative;
import java.awt.Color;
import java.util.*;
import becker.robots.*;

public class BetterFighterRobot extends FighterRobot{
	private static int defence = 3;
	private static int numMoves = 2;
	private static int attack = 5;
	private int health;
	private int energy;
	private int roundsNotFought = 0;
	private int chasingSameRobot = 0;
	private int IDofPrevRobotChased = -1;
	private int sameRobotFought = 0;

	/**
	 * Constructor
	 * @param c
	 * @param a
	 * @param s
	 * @param d
	 * @param id
	 * @param health
	 */
	public BetterFighterRobot(City c, int a, int s, Direction d, int id, int health) {
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
			this.setColor(Color.BLUE);
		this.setLabel(label);
	}


	/**
	 * given the end coordinates, move to them
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
	 */
	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {

		TurnRequest request;
		int endStreet;
		int endAvenue;
		int fightId = sortOppData(data, energy); // ID of robot to attack 
		
		this.energy = energy; // updates energy variable

		this.health = data[this.getID()].getHealth(); // updates health variable

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

				// returns in avenue, street, fightID order
				int [] tempArray = attackOrFollowRobot(targetAvenue, targetStreet, data, fightId);

				endAvenue = tempArray[0];
				endStreet = tempArray[1];
				fightId = tempArray[2];
				// final check to avoid fighting self
				if (this.getID() == fightId) {
					fightId = -1;
				}
			}
		}
		// if dead or no energy
		else { 
			endAvenue = this.getAvenue();
			endStreet = this.getStreet();
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
	 * @return
	 */
	private int [] attackOrFollowRobot(int goToAvenue, int goToStreet, OppData [] oppData, int fightID) {
		int endA = 0;
		int endS = 0;
		int fighterID = fightID;

		int streetDistance = this.getStreet() - goToStreet;
		int avenueDistance = this.getAvenue() - goToAvenue;

		/*
		 * if target robot further away than max num moves
		 */
		if (Math.abs(streetDistance) + Math.abs(avenueDistance) > this.numMoves) {

			fighterID = -1; // change fight ID to avoid penalty as you cannot reach opponent (don't fight anyone this round, just approach them)
			this.roundsNotFought += 1; // keep track of rounds not fought to minimize penalty

			// if robot north of target robot
			if (this.getStreet() < goToStreet) { 
				// move until robot is on same street as target robot (staying under max num moves)
				for (int i = 0; i < this.numMoves; i++) {
					if (this.getStreet() != goToStreet) { 
						endS = this.getStreet() + i;
					}
				}
				endA = this.getAvenue(); // stay on same avenue, only move closer by streets
			}
			// if robot south of target robot
			else if (this.getStreet() > goToStreet) {
				for (int i = 0; i < this.numMoves; i++) {
					if (this.getStreet() != goToStreet) { 
						endS = this.getStreet() - i;
					}
				}
				endA = this.getAvenue(); // stay on same avenue, only move closer by streets
			}
			// if robot and target robot on same street, then move closer by avenues
			else if (this.getStreet() == goToStreet) { 
				
				// if robot to the left of target robot
				if (this.getAvenue() < goToAvenue) { 
					for (int i = 0; i < this.numMoves; i++) {
						if (this.getAvenue() != goToAvenue) { 
							endA = this.getAvenue() + i;
						}
					}
				}
				// robot to the right of target robot
				else if (this.getAvenue() > goToAvenue) { 
					for (int i = 0; i < this.numMoves; i++) {
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
	 * @param data
	 * @return
	 */
	private int sortOppData(OppData[] data, int energy) {
		
		this.energy = energy;
		
		PavlovicOppData [] playerData = new PavlovicOppData[data.length];

		/*
		 * adds oppData robots to new PavlovicOppData
		 */
		for (int i = 0; i < data.length; i++){
			// adds ID, avenue, street, health
			playerData[i] = new PavlovicOppData(data[i].getID(), data[i].getAvenue(), data[i].getStreet(), data[i].getHealth(), this.getAvenue(), this.getStreet());
		}
		
		/*
		 * adding only the robots that are close enough to fight
		 * to array lists of the ID and their distance (s and a)
		 * the tactic is to only fight nearby robots, not to travel
		 * several turns to reach a weak robot. this will lead into
		 * more attacks and less energy points lost
		 */

		// sort playerData (PavlovicOppData) by distances
		insertionSort(playerData, false, true); 
		
		/*
		 * put robots that are close enough to attack in the same turn
		 * (less than or equal max number of moves) in an arrayList 
		 * (don't know size so can't be array) 
		 * also checks if robot is not own ID, not dead, and you have 
		 * enough energy to move to other robots location
		 */ 
		ArrayList <PavlovicOppData> closeRobotsTemp = new ArrayList<PavlovicOppData>();
		for (int i = 0; i < playerData.length; i++){
			if (playerData[i].totalDistance() <= this.numMoves && this.getID() != playerData[i].getID()  && playerData[i].getHealth() > 0 ) { 
				closeRobotsTemp.add(new PavlovicOppData(playerData[i].getID(), playerData[i].getAvenue(),playerData[i].getStreet(), playerData[i].getHealth(), this.getAvenue(), this.getStreet()));
			}
		} // closeRobots might have size of zero!
				
		/*
		 * turns the arrayList into array
		 */
		PavlovicOppData [] closeRobots = new PavlovicOppData[closeRobotsTemp.size()];
		for (int i = 0; i < closeRobots.length; i++) {
			closeRobots[i] = closeRobotsTemp.get(i);
		}
		
		insertionSort(closeRobots, true, false); // sort nearby robots by health
		

		int IDtoFight = fightDecision(closeRobots, playerData);
		// playerData sorted by distance

		return IDtoFight;
	}


	
	/**
	 * decides who to fight based on the sorted list
	 * @param closeRobots
	 * @param playerData
	 * @return
	 */
	private int fightDecision(PavlovicOppData [] closeRobots, PavlovicOppData [] playerData) {
		// playerData is already sorted by distances (all robots, even dead ones)
		// closeRobots is only close robots (alive) already sorted by health
		
//		for (int i = 0; i < closeRobots.length; i++){
//			System.out.println("!!!!! ID: " + closeRobots[i].getID() + "	Health: " + closeRobots[i].getHealth() + "	Distance: " + (closeRobots[i].getStreetDist() + closeRobots[i].getAvenueDist()));
//		}
		
		
		int IDtoFight = 0;

		// can fight
		if (this.health > 0 && this.energy > 0){ 

			// if there are close robots
			if (closeRobots.length > 0) { 
			
				// if the closest robot isn't one that was fought previously
				// (avoids playing the same robot repeatedly until one dies)
				if (this.sameRobotFought < 1) {
					IDtoFight = closeRobots[0].getID(); // attack close robot with lowest health (already checked that it is not yourself & not already dead)
//					System.out.println("!!!!!! chose to fight closest robot.");
					this.sameRobotFought += 1;
					this.roundsNotFought = 0; // reset rounds not fought in a row
					this.chasingSameRobot = 0;
				}
				else {
					// attack next closest robot if there is one
					if (closeRobots.length > 1) {
						IDtoFight = closeRobots[1].getID(); // attack next closest robot
//						System.out.println("!!!!!! chose to fight NEXT closest robot bc i already fought other closest one previously.");
						this.sameRobotFought = 0;
						this.roundsNotFought = 0; // reset rounds not fought in a row
						this.chasingSameRobot = 0;
					}
					// if no close robots when all close robots have been fought in previous round, move on
					else {
						IDtoFight = turnWhenNoCloseRobots(IDtoFight, playerData);
					}
				}	
			}
			
			// if no close robots
			else { 
				IDtoFight = turnWhenNoCloseRobots(IDtoFight, playerData);
			}
			
		}
		// if dead or no energy left
		else { 
			IDtoFight = -1;
			this.chasingSameRobot += 1;
			this.roundsNotFought += 1;
			this.sameRobotFought = 0;
		}
		return IDtoFight;

	}
	

	/**
	 * decides what to do when no robots nearby to approach and attack in the same turn
	 * @param IDtoFight
	 * @param playerData
	 * @return
	 */
	private int turnWhenNoCloseRobots(int IDtoFight, PavlovicOppData [] playerData) {
		
			insertionSort(playerData, true, false); // sort ALL players by health
			
			if (this.chasingSameRobot < 3) { // don't chase same robot with lowest health forever (otherwise they will all end up in same spot)
				
				for (int i = 0; i < playerData.length; i++) { // goes through all robots, sorted health
					
					if (playerData[i].getID() != this.getID() && playerData[i].getHealth() > 0 && this.health > 0 && this.energy > 0) { 
						
						IDtoFight = playerData[i].getID(); // lowest health robot
						
						this.IDofPrevRobotChased = IDtoFight;

//						System.out.println("!!!!!! chose to CHASE lowest health robot which is robot ID: " + IDtoFight + " at avenue, street at " + playerData[i].getAvenue() + ", " + playerData[i].getStreet());
						this.chasingSameRobot += 1;
						this.roundsNotFought = 0;
						this.sameRobotFought = 0;
						break;
					}
					
					else {
						if (i == playerData.length - 1) { // if no players to fight
							IDtoFight = -1;
//							System.out.println("!!!!!! no one left to fight or I cant fight.");
							this.chasingSameRobot = 0; // reset variable
							this.roundsNotFought += 1;
							this.sameRobotFought = 0;
						}
					}
				}
			}
			// if been chasing same robot for a while
			else {
				int prevFightID = IDtoFight;
				for (int i = 0; i < playerData.length; i ++) { // goes through all robots, sorted health
					if (playerData[i].getID() != this.getID() && playerData[i].getHealth() != 0 && playerData[i].getID() != prevFightID && this.health > 0 && this.energy > 0) { 
						IDtoFight = playerData[i].getID(); // lowest health robot that is not the same one as before
//						System.out.println("!!!!!! chose to CHASE NEXT lowest health robot as i've been chasing prev one too much.");
						this.chasingSameRobot = 0;
						this.roundsNotFought = 0;
						this.sameRobotFought = 0;
						break;
					}
					else {
						if (i == playerData.length - 1) { // if no players to fight
							IDtoFight = -1;
//							System.out.println("!!!!!! no one left to fight or I cant fight.");
							this.chasingSameRobot = 0; // reset variable
							this.roundsNotFought += 1;
							this.sameRobotFought = 0;
						}
					}
				}
			}
//		}
		return IDtoFight;
	}
	

	/**
	 * sorts by insertion sort
	 * @param list
	 * @param health
	 * @param dist
	 * @return
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
	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health -= healthLost;
	}




}
