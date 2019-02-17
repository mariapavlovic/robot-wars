package summative;
/**
 * robot used for marking
 */
import java.awt.Color;
import java.util.*;
import becker.robots.*;

public class PavlovicFighterRobot extends FighterRobot {

	private static int defence = 4;
	private static int numMoves = 2;
	private static int attack = 4;
	private int health;
	private int energy;
	private int roundsNotFought = 0;
	private int chasingSameRobot = 0;
	private int sameRobotFought = 0;
	private ArrayList<PavlovicOppData> persistantOppData = new ArrayList<PavlovicOppData>();

	/**
	 * Constructs a robot of type PavlovicFighterRobot
	 * with these initial characteristics
	 * @param c - the city or arena
	 * @param a - avenue coordinate
	 * @param s - street coordinate
	 * @param d - initial direction its facing
	 * @param id - the ID number given
	 * @param health - inital health
	 */
	public PavlovicFighterRobot(City c, int a, int s, Direction d, int id, int health) {
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
			this.setColor(Color.WHITE);
		this.setLabel(label);
	}


	/**
	 * moves the robot to a location given the end coordinates
	 * decides which direction to face depending on difference between end location and current location 
	 * @param a - end avenue coordinate
	 * @param s - end street coordinate
	 */
	@Override
	public void goToLocation(int a, int s) {
		int aveDist = a-this.getAvenue(); // travel dist for avenues
		int strDist = s-this.getStreet(); // for streets

		// if i am to left of target robot
		if (aveDist > 0){ 
			while (!this.isFacingEast()) { 
				this.turnLeft();
			}
		}
		// if i am to right of target robot
		else if (aveDist < 0){
			while (!this.isFacingWest()) { 
				this.turnLeft();
			}
		}
		this.move(Math.abs(aveDist));

		// if i am to north of target robot
		if (strDist > 0){
			while (!this.isFacingSouth()) { 
				this.turnLeft();
			}
		}
		// if i am to south of target robot
		else if (strDist < 0){
			while (!this.isFacingNorth()) {
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
		// variables
		TurnRequest request;
		int endStreet;
		int endAvenue;
		int fightId = sortOppData(data, energy); // ID of robot to attack 

		this.energy = energy; // updates energy variable

		this.health = data[this.getID()].getHealth(); // updates health variable

		this.setLabel();

		// take turn while health > 0 (alive) and energy > 0 (can move)
		if (this.health > 0 && this.energy > 15) {

			// not attacking, stay where you are
			if (fightId == -1) {
				endStreet = this.getStreet();
				endAvenue = this.getAvenue();
			}
			// if attacking
			else {
				int targetStreet = data[fightId].getStreet(); // where to end up
				int targetAvenue = data[fightId].getAvenue();

				/*
				 * decides (based on distance between my robot and target robot and my numMoves),
				 * whether my robot can attack target robot immediately or if it has to follow 
				 * target robot for a while to get closer to it
				 */
				int [] tempArray = attackOrFollowRobot(targetAvenue, targetStreet, data, fightId);
				// returns in avenue, street, fightID order
				endAvenue = tempArray[0];
				endStreet = tempArray[1];
				fightId = tempArray[2];
				
			}
		}
		
		// if dead or no energy
		else { 
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
	 * @return - array of fight ID, avenue, street
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
		if (Math.abs(streetDistance) + Math.abs(avenueDistance) > numMoves) {

			fighterID = -1; // change fight ID to avoid penalty as you cannot reach opponent (don't fight anyone this round, just approach them)
			
			this.roundsNotFought += 1; // keep track of rounds not fought to minimize penalty

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
				endA = this.getAvenue();
			}
			
			// if robot and target robot on same street, then move closer by avenues
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
	 * sorts the opponent data and stores it in both PavlovicOppData array (playerData and persistentOppData)
	 * sorts playerData by distance, and creates a new array for closeRobots
	 * @param data - OppData array
	 * @return - fight ID
	 */
	private int sortOppData(OppData[] data, int energy) {
		
		PavlovicOppData [] playerData = new PavlovicOppData[data.length];
		PavlovicOppData tempPavlovicOppData;
		this.energy = energy;

		/*
		 * PavlovicOppData stays persistent/constant throughout whole battle
		 * only adds new objects the first time through, updates every other time
		 */
		if (this.persistantOppData.size() == 0) {
//			System.out.println("Setting persistantOppData");
			for (int i = 0; i < data.length; i++){
				// adds ID, avenue, street, health
				this.persistantOppData.add(new PavlovicOppData(data[i].getID(), data[i].getAvenue(), data[i].getStreet(), data[i].getHealth(), this.getAvenue(), this.getStreet()));
			}
		} 
		else {
			for (int i = 0; i < data.length; i++){
				this.persistantOppData.get(i).updateData(data[i], this.getStreet(), this.getAvenue());
			}
		}
		
		/*
		 * adds persistantOppData robots to new PavlovicOppData
		 */
		for (int i = 0; i < data.length; i++){
			tempPavlovicOppData = this.persistantOppData.get(i);
			playerData[i] = tempPavlovicOppData;
		}


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
			if (playerData[i].totalDistance() <= numMoves && this.getID() != playerData[i].getID()  && playerData[i].getHealth() > 0 ) { 
				
				tempPavlovicOppData = new PavlovicOppData(playerData[i].getID(), playerData[i].getAvenue(), playerData[i].getStreet(),
						playerData[i].getHealth(), playerData[i].getAvenueDist(), playerData[i].getStreetDist(), playerData[i].getPreviousBattleResult());
				
				closeRobotsTemp.add(tempPavlovicOppData);
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
	 * playerData is already sorted by distances (all robots, even dead ones)
	 * closeRobots is only close robots (alive) already sorted by health
	 * @param closeRobots - array of only close robots (can reach in one turn)
	 * @param playerData - my copy of all robots data
	 * @return - fight ID
	 */
	private int fightDecision(PavlovicOppData [] closeRobots, PavlovicOppData [] playerData) {

		int IDtoFight = -1;

		// can fight
		if (this.health > 0 && this.energy > 0){ 

			// if there are close robots
			if (closeRobots.length > 0) { 

				// if the closest robot isn't one that was fought previously
				// (avoids playing the same robot repeatedly until one dies)
				if (this.sameRobotFought < 1) {
				

					if (this.persistantOppData.get(closeRobots[0].getID()).getWinLossRecord() > 0) { // if won against closest robot
						IDtoFight = closeRobots[0].getID();
					}
					else {
						if (closeRobots.length > 1) {
							IDtoFight = closeRobots[1].getID(); // attack next closest robot
							this.sameRobotFought = 0;
							this.roundsNotFought = 0; // reset rounds not fought in a row
							this.chasingSameRobot = 0;
						}

						// search through rest of players if no close ones
						else {
							turnWhenNoCloseRobots(IDtoFight, playerData);
						}
					}

					this.sameRobotFought += 1;
					this.roundsNotFought = 0; // reset rounds not fought in a row
					this.chasingSameRobot = 0;
				}

				// if same robot fought too many times
				else {

					// attack next closest robot if there is one
					if (closeRobots.length > 1) {						
						IDtoFight = closeRobots[1].getID(); // attack next closest robot
						this.sameRobotFought = 0;
						this.roundsNotFought = 0; // reset rounds not fought in a row
						this.chasingSameRobot = 0;
					}

					// if no close robots when all close robots have been fought in previous round
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
			this.chasingSameRobot = 0;
//			this.roundsNotFought += 1;
			this.sameRobotFought = 0;
		}

		return IDtoFight;

	}


	/**
	 * decides what to do when no robots nearby to approach and attack in the same turn
	 * @param IDtoFight - the fight ID
	 * @param playerData - my copy of all the players data
	 * @return - the fight ID 
	 */
	private int turnWhenNoCloseRobots(int IDtoFight, PavlovicOppData [] playerData) {

		insertionSort(playerData, true, false); // sort ALL players by health

		if (this.chasingSameRobot < 3) { // don't chase same robot with lowest health forever (otherwise they will all end up in same spot)

			for (int i = 0; i < playerData.length; i++) { // goes through all robots, sorted health

				if (playerData[i].getID() != this.getID() && playerData[i].getHealth() > 0 && this.health > 0 && this.energy > 0) { 

					IDtoFight = playerData[i].getID(); // lowest health robot
					this.chasingSameRobot += 1;
					this.roundsNotFought = 0;
					this.sameRobotFought = 0;
					break;
				}

				else {
					if (i == playerData.length - 1) { // if no players to fight
						IDtoFight = -1;
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
					this.chasingSameRobot = 0;
					this.roundsNotFought = 0;
					this.sameRobotFought = 0;
					break;
				}
				else {
					if (i == playerData.length - 1) { // if no players to fight
						IDtoFight = -1;
						// System.out.println("!!!!!! no one left to fight or I cant fight.");
						this.chasingSameRobot = 0; // reset variable
						this.roundsNotFought += 1;
						this.sameRobotFought = 0;
					}
				}
			}
		}
		return IDtoFight;
	}


	/**
	 * sorts by insertion sort
	 * @param list - the array to sort
	 * @param health - sorts by healths
	 * @param dist - sorts by distances between my robot and other robot
	 * @return - returns sorted array
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
	 * gets the results from a battle that was just finished
	 * uses these results to see which robots are stronger or weaker than my robot
	 * @param healthLost - my health lost (positive if I LOST health)
	 * @param oppID - opponent ID
	 * @param oppHealthLost - opponents health lost
	 * @param numRoundsFought - how many rounds happened in the fight
	 */
	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {

		// update health variable after fight
		this.health -= healthLost;
		
		if (oppID != -1 && this.persistantOppData.size() > 0) {
			int healthDifference = oppHealthLost - healthLost;
			this.persistantOppData.get(oppID).setPreviousBattleResult(healthDifference);
						
			// i lost against the opponent
			if (healthDifference < 0) { 
				this.persistantOppData.get(oppID).lostAgainst();
			}
			else if (healthDifference > 0){
				this.persistantOppData.get(oppID).wonAgainst();
			}

		}			

	}




}
