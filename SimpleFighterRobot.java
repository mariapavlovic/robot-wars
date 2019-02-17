package summative;
/**
 * tactic is to stay still for most of the battle
 * move further away from any close robots
 * if too many penalties, attack (last resort)
 */
import java.awt.Color;
import java.util.*;
import becker.robots.*;

public class SimpleFighterRobot extends FighterRobot{
	private static int defence = 3;
	private static int numMoves = 6;
	private static int attack = 1;
	private int health;
	private int energy;


	/**
	 * Constructor
	 * @param c
	 * @param a
	 * @param s
	 * @param d
	 * @param id
	 * @param health
	 */
	public SimpleFighterRobot(City c, int a, int s, Direction d, int id, int health) {
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
			this.setColor(Color.CYAN);
		this.setLabel(label);
	}



	/**
	 * given the end coordinates, move to them
	 * @param a
	 * @param s
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
	 * @param energy
	 * @param data
	 * @return
	 */
	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		TurnRequest request;
		int [] turnDecision = new int[3];
		int endStreet;
		int endAvenue;
		
		turnDecision = sortOppData(data); // ID, street, avenue of robot to attack 

		int fightId = turnDecision[0];
		endStreet = turnDecision[1];
		endAvenue = turnDecision[2];
		
		this.energy = energy; // updates energy variable
				
		this.health = data[this.getID()].getHealth(); // updates health global variable
		
		this.setLabel();

		request = new TurnRequest(endAvenue, endStreet, fightId, this.attack);
		return request;
		
	}
	

	/**
	 * sorts opp data
	 * @param data
	 * @return
	 */
	private int [] sortOppData(OppData[] data) {
		PavlovicOppData [] playerData = new PavlovicOppData[data.length];
		
		int sortedData[] = new int[3];
		
		int streets;
		int avenues;
		int id;

		
		for (int i = 0; i < data.length; i++){
			id = data[i].getID();
			playerData[i] = new PavlovicOppData(id, data[i].getAvenue(), data[i].getStreet(), data[i].getHealth(), this.getAvenue(), this.getStreet());
		}
		
		
		// sort robots by distances
		insertionSort(playerData, false, true); 

		for (int i = 0 ; i < playerData.length; i ++) {
			System.out.println(playerData[i].getID());
		}
		
		int MAX_NUM_MOVES = 6; // max num of steps for any robot (not specifically for mine)
		

		/*
		 * put robots that are close enough to attack in the same turn
		 * (less than or equal max number of moves) in an arrayList 
		 * (don't know size so can't be array) 
		 * also checks if robot is not own ID, not dead, and enough energy to move
		 */ 
		ArrayList <PavlovicOppData> closeRobotsTemp = new ArrayList<PavlovicOppData>();
		for (int i = 0; i < playerData.length; i++){
			if (playerData[i].totalDistance() <= MAX_NUM_MOVES && playerData[i].getID() != this.getID()  && playerData[i].getHealth() > 0) { 
				closeRobotsTemp.add(new PavlovicOppData(playerData[i].getID(), playerData[i].getAvenue(), playerData[i].getStreet(), 
						playerData[i].getHealth(), this.getAvenue(), this.getStreet()));
			}
		}
		// closeRobots might have size of zero!

		
		/*
		 * turning the array list into array
		 */
		PavlovicOppData [] closeRobots = new PavlovicOppData[closeRobotsTemp.size()];
		for (int i = 0; i < closeRobots.length; i++) {
			closeRobots[i] = closeRobotsTemp.get(i);
		}

		
		/*
		 * strategy is to just keep avoiding nearby robots
		 */
		
		int [] turnDecision = new int[3];
		turnDecision = fightDecision(closeRobots, playerData);

		return turnDecision;
	}


	/**
	 * decides who to AVOID based on the close robots list
	 * @param closeRobots
	 * @param playerData
	 * @return
	 */
	private int [] fightDecision(PavlovicOppData [] closeRobots, PavlovicOppData [] playerData) {
		int [] fightDsn = new int[3];
		int IDtoFight = -1;
		int endStreet = 0;
		int endAvenue = 0;
		
		int IDtoAvoidFirst;
		int roundsNotFought = 0;
		if (roundsNotFought < 15) { // allows up to three penalties
			if (closeRobots.length > 0) { // if there are any close robots nearby
				IDtoAvoidFirst = closeRobots[0].getID(); // run away from closest robot
				int [] endCoords = this.moveAway(IDtoAvoidFirst, playerData);
				endStreet = endCoords[0];
				endAvenue = endCoords[1];
				roundsNotFought += 1;
				IDtoFight = -1;
			}
			else {
				endStreet = this.getStreet();
				endAvenue = this.getAvenue();
				roundsNotFought += 1;
				IDtoFight = -1;
			}
		}
		else {
			if (closeRobots.length > 0) { // only fight when taken too many penalties, only attack close robots
				IDtoFight = closeRobots[0].getID(); 
				endStreet = closeRobots[0].getStreet();
				endAvenue = closeRobots[0].getAvenue();
				roundsNotFought = 0; // reset
			}
			else {
				IDtoFight = -1; // don't look for far away robots, just take the penalty instead
				endStreet = this.getStreet();
				endAvenue = this.getAvenue();
				roundsNotFought += 1; // update

			}

		}
		
		fightDsn[0] = IDtoFight;
		fightDsn[1] = endStreet;
		fightDsn[2] = endAvenue;

		return fightDsn;
	}
	
	
	/**
	 * moves away given the ID and coordinates of another robot
	 * @param avoidID
	 * @param data
	 * @return
	 */
	private int [] moveAway(int avoidID, PavlovicOppData [] data) {
		int endAvenue = 0;
		int endStreet = 0;
		
		if (data[avoidID].getStreet() < this.getStreet()) { // avoid robot is above you
			endStreet = this.getStreet() + numMoves;
		}
		else if (data[avoidID].getStreet() > this.getStreet()) { // avoid robot is below you)
			endStreet = this.getStreet() - numMoves;
		}
		else { // on same street
			endStreet = this.getStreet();
			if (data[avoidID].getAvenue() < this.getAvenue()) { // avoid robot is to left of you
				endAvenue = this.getAvenue() + numMoves;
			}
			else if (data[avoidID].getAvenue() > this.getAvenue()) { // avoid robot is to right of you)
				endAvenue = this.getAvenue() - numMoves;
			}
			else { // on same avenue
				endStreet = this.getStreet() - numMoves/2;
				endAvenue = this.getAvenue() + numMoves/2;
			}
		}
		
		int [] endCoords = {endStreet, endAvenue};
		
		return endCoords;
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



