package summative;

public class PavlovicOppData extends OppData {
	
	private int avenueDist;
	private int streetDist;
	private int energy;
	private int previousBattleResult = 0;
	private int winLossRecord = 0;

	
	/**
	 * constructor for PavlovicOppData
	 * @param id
	 * @param a
	 * @param s
	 * @param health
	 */
	public PavlovicOppData(int id, int avenue, int street, int health, int myAvenue, int myStreet) {
		super(id, avenue, street, health);
		this.avenueDist = Math.abs(avenue - myAvenue);
		this.streetDist = Math.abs(street - myStreet);
	}
	
	
	/**
	 * overloading constructor for PavlovicOppData, used for persistantOppData so 
	 * history of battles can be accessed (to see if my robot has won or lost more
	 * times against opponent)
	 * @param id
	 * @param avenue
	 * @param street
	 * @param health
	 * @param avenueDist
	 * @param streetDist
	 * @param previousBattleResult
	 */
	public PavlovicOppData(int id, int avenue, int street, int health, int avenueDist, int streetDist, int previousBattleResult) {
		super(id, avenue, street, health);
		this.streetDist = streetDist;
		this.avenueDist = avenueDist;
		this.previousBattleResult = previousBattleResult;
	}
	
	
	/**
	 * updates persistantOppData (instead of creating new objects every time)
	 * @param oppData
	 * @param myStreet
	 * @param myAvenue
	 */
	public void updateData(OppData oppData, int myStreet, int myAvenue) {
		this.setAvenue(oppData.getAvenue());
		this.setStreet(oppData.getStreet());
		this.setHealth(oppData.getHealth());
		this.streetDist = Math.abs(myStreet - oppData.getStreet());
		this.avenueDist = Math.abs(myAvenue - oppData.getAvenue());
	}
	
	
	/**
	 * setting a battle result
	 * positive healthDifference value = I won 
	 * negative healthDifference value = I lost
	 * @param healthDifference - difference between mine and opponents healthLost 
	 */
	public void setPreviousBattleResult(int healthDifference) {
		this.previousBattleResult = healthDifference;
	}
	
	
	/**
	 * used to access the previous battle result
	 * @return
	 */
	public int getPreviousBattleResult() {
		return this.previousBattleResult;
	}
	
	
	/**
	 * returns the street distance between
	 * robot and its target robot
	 * @return 
	 */
	public int getStreetDist() {
		return this.streetDist;
	}
	
	
	/**
	 * returns the avenue distance between
	 * robot and its target robot
	 * @return
	 */
	public int getAvenueDist() {
		return this.avenueDist;
	}
	
	
	/**
	 * returns both the avenue and street distance
	 * @return sum of avenue and street distance
	 */
	public int totalDistance() {
		return (this.avenueDist + this.streetDist);
	}
	
	
	/**
	 * if my robot beat the opponent, add one to winLossRecord
	 */
	public void wonAgainst() {
		this.winLossRecord += 1;
	}
	
	
	/**
	 * if my robot lost to the opponent, subtract one to winLossRecord
	 */
	public void lostAgainst() {
		this.winLossRecord -= 1;
	}
	
	
	/**
	 * access the winLossRecord
	 * @return - negative value if I lost more times, positive value if I won more times
	 */
	public int getWinLossRecord() {
		return this.winLossRecord;
	}

	
}
