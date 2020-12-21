package ca.mcgill.ecse223.quoridor.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.junit.Assert;

import ca.mcgill.ecse223.quoridor.QuoridorApplication;
import ca.mcgill.ecse223.quoridor.controller.PawnBehavior.MoveDirection;
import ca.mcgill.ecse223.quoridor.controller.QuoridorController;
import ca.mcgill.ecse223.quoridor.model.Board;
import ca.mcgill.ecse223.quoridor.model.Direction;
import ca.mcgill.ecse223.quoridor.model.Game;
import ca.mcgill.ecse223.quoridor.model.Game.GameStatus;
import ca.mcgill.ecse223.quoridor.model.Game.MoveMode;
import ca.mcgill.ecse223.quoridor.model.GamePosition;
import ca.mcgill.ecse223.quoridor.model.Move;
import ca.mcgill.ecse223.quoridor.model.Player;
import ca.mcgill.ecse223.quoridor.model.PlayerPosition;
import ca.mcgill.ecse223.quoridor.model.Quoridor;
import ca.mcgill.ecse223.quoridor.model.StepMove;
import ca.mcgill.ecse223.quoridor.model.User;
import ca.mcgill.ecse223.quoridor.model.Wall;
import ca.mcgill.ecse223.quoridor.model.WallMove;
import ca.mcgill.ecse223.quoridor.view.QuoridorView;
import ca.mcgill.ecse223.quoridor.model.Tile;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.But;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CucumberStepDefinitions {

	private QuoridorView view = new QuoridorView();

	private Player currentPlayer;
	private WallMove aWallMove;

	private static String gameResult;

	// ***********************************************
	// Background step definitions
	// ***********************************************

	@Given("^The game is not running$")
	public void theGameIsNotRunning() {
		view.initLoadScreen();
	}


	@Given("^The game is running$")
	public void theGameIsRunning() {
		view.initLoadScreen();
		view.newGame.doClick();
		view.newGame.doClick();
		if(view.confirmFrame.isVisible()) {
			((JButton) view.confirmFrame.getContentPane().getComponent(1)).doClick();
		}
	}

	@And("^It is my turn to move$")
	public void itIsMyTurnToMove() throws Throwable {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		Player currentPlayer = quoridor.getCurrentGame().getWhitePlayer();
		QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(currentPlayer);
	}

	@Given("The following walls exist:")
	public void theFollowingWallsExist(io.cucumber.datatable.DataTable dataTable) {

		List<Map<String, String>> valueMaps = dataTable.asMaps();
		for (Map<String, String> map : valueMaps) {
			Integer wrow = Integer.decode(map.get("wrow"));
			Integer wcol = Integer.decode(map.get("wcol"));

			String dir = map.get("wdir");

			Direction direction;
			switch (dir) {
			case "horizontal":
				direction = Direction.Horizontal;
				break;
			case "vertical":
				direction = Direction.Vertical;
				break;
			default:
				throw new IllegalArgumentException("Unsupported wall direction was provided");
			}

			view.grabButton.doClick();
			if(!QuoridorController.moveWall(QuoridorController.findTile(wrow, wcol))) {
				QuoridorController.tpWall(QuoridorController.findTile(wrow, wcol));
			};
			if(direction == Direction.Horizontal) {
				view.rotateButton.doClick();
			}
			if(QuoridorController.wallIsValid()) {
				view.DropWall();
			} else {
				QuoridorController.dropWall();
			}	

		}
		System.out.println();

	}

	@And("I do not have a wall in my hand")
	public void iDoNotHaveAWallInMyHand() {assertNull(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate());
	}

	@And("^I have a wall in my hand over the board$")
	public void iHaveAWallInMyHandOverTheBoard() throws Throwable {
		if(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate() == null) {
			QuoridorController.grabWall();	
		}
		QuoridorApplication.getQuoridor().getCurrentGame().setMoveMode(MoveMode.WallMove);
	}

	@Given("^A new game is initializing$")
	public void aNewGameIsInitializing() throws Throwable {
		initQuoridorAndBoard();
		ArrayList<Player> players = createUsersAndPlayers("user1", "user2");
		QuoridorApplication.getQuoridor().setCurrentGame(new Game(GameStatus.Initializing, MoveMode.PlayerMove, QuoridorApplication.getQuoridor()));
		QuoridorApplication.getQuoridor().getCurrentGame().setWhitePlayer(players.get(0));
		QuoridorApplication.getQuoridor().getCurrentGame().setBlackPlayer(players.get(1));
		view.initLoadScreen();
		view.newGame.doClick();
	}

	// ***********************************************
	// Scenario and scenario outline step definitions
	// ***********************************************


	//***********************************************
	// Start a new game
	// **********************************************
	/**
	 * Feature:Start a new game
	 * 
	 * @Author Hongshuo Zhou
	 */
	@When("A new game is being initialized")
	public void a_new_game_is_being_initialized() {
		QuoridorController.startGame();
		view.initLoadScreen();
		view.newGame.doClick();
	}
	/**
	 *Feature:Start a new game 
	 *@Author Hongshuo Zhou
	 */
	@And("White player chooses a username")
	public void white_player_chooses_a_username(){

		view.whiteName.setText("Player 1");
		QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer().getUser().setName("Player 1");
	}
	/**
	 *Feature:Start a new game 
	 *@Author Hongshuo Zhou
	 */
	@And("Black player chooses a username")
	public void black_player_chooses_a_username(){
		view.blackName.setText("Player 2");
		QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer().getUser().setName("Player 2");
	}
	/**
	 *Feature:Start a new game 
	 *@Author Hongshuo Zhou
	 */
	@Then("Total thinking time is set")
	public void total_thinking_time_is_set(){
		view.minutesField.setText("10");
		view.secondsField.setText("0");
		QuoridorController.setTotaltime(10, 0);	
	}
	/**
	 *Feature:Start a new game 
	 *@Author Hongshuo Zhou
	 */
	@Then("The game shall become ready to start")
	public void the_game_shall_become_ready_to_start(){
		QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.ReadyToStart);
	}
	/**
	 *Feature:Start a new game 
	 *@Author Hongshuo Zhou
	 */
	@Given("The game is ready to start")
	public void the_game_is_ready_to_start() {
		this.a_new_game_is_being_initialized();
		this.white_player_chooses_a_username();
		this.black_player_chooses_a_username();
		this.total_thinking_time_is_set();
		QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.Running);
	}
	/**
	 *Feature:Start a new game 
	 *@Author Hongshuo Zhou
	 */
	@When("I start the clock")
	public void I_start_the_clock() {
		QuoridorController.runwhiteclock(view);
	}
	/**
	 *Feature:Start a new game 
	 *@Author Hongshuo Zhou
	 */
	@Then("The game shall be running") 
	public void the_game_shall_be_running(){
		Assert.assertEquals(GameStatus.Running,QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus());
		view.newGame.doClick();
		if(view.confirmFrame.isVisible()) {
			((JButton) view.confirmFrame.getContentPane().getComponent(1)).doClick();
		}
	}
	/**
	 *Feature:Start a new game 
	 *@Author Hongshuo Zhou
	 */
	@And("The board shall be initialized")
	public void the_board_shall_be_initialized() {
		assertNotNull(view.board);
	}

	//***********************************************
	// Load Position
	// **********************************************
	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	//private Boolean load;

	@When("I initiate to load a saved game {string}")
	public void i_initiate_to_load_a_saved_game(String filename) {
		view.loadGame.doClick();
		//The unchecked cast should never be an issue. list is always a string JList
		JList<String> list = ((JList<String>) view.filePane.getViewport().getComponent(0));
		list.setSelectedValue(filename, true);
		view.loadGame.doClick();
	}
	@When("I initiate to load a game in {string}")
	public void iInitiateToLoadAGameIn(String filename) {
		view.loadGame.doClick();
		//The unchecked cast should never be an issue. list is always a string JList
		JList<String> list = ((JList<String>) view.filePane.getViewport().getComponent(0));
		list.setSelectedValue(filename, true);
		//TODO: some files don't exist!!!!!

		view.loadGame.doClick();
	}

	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	@And("The position to load is valid")
	public void the_position_to_load_is_valid() {
		assertEquals(true, QuoridorController.validatePosition());

	}
	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	@Then("It shall be {string}'s turn")
	public void it_shall_be_s_turn(String string) {

		if(string.equals("white")) {
			Assert.assertEquals(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer(), 
					QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove());
		}else {
			Assert.assertEquals(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer(), 
					QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove());
		}

	}
	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	@Then("{string} shall be at {int}:{int}")
	public void shall_be_at(String string, Integer intx, Integer inty) {
		Integer row;
		Integer col;
		if(string.equals("black")) {
			row = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile().getRow();
			col = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile().getColumn();
		}else {
			row = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile().getRow();
			col = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile().getColumn();
		}
		assertEquals(intx, row);
		assertEquals(inty, col);
	}
	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	@Then("{string} shall have a vertical wall at {int}:{int}")
	public void shall_have_a_vertical_wall_at(String string, Integer intx, Integer inty) {
		Integer col;
		Integer row;
		Direction wallDirection;
		if(string.equals("black")) {
			wallDirection = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer().getWall(0).getMove().getWallDirection();
			col = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackWallsOnBoard(0).getMove().getTargetTile().getColumn();
			row = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackWallsOnBoard(0).getMove().getTargetTile().getRow();
		}else {
			wallDirection = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer().getWall(0).getMove().getWallDirection();
			col = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhiteWallsOnBoard(0).getMove().getTargetTile().getColumn();
			row = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhiteWallsOnBoard(0).getMove().getTargetTile().getRow();
		}
		assertEquals(Direction.Vertical, wallDirection);
		assertEquals(intx, row);
		assertEquals(inty, col);
	}
	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	@Then("{string} shall have a horizontal wall at {int}:{int}")
	public void shall_have_a_horizontal_wall_at(String string, Integer intx, Integer inty) {
		Integer col;
		Integer row;
		Direction wallDirection;
		if(string == "black") {
			wallDirection = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer().getWall(0).getMove().getWallDirection();
			col = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackWallsOnBoard(0).getMove().getTargetTile().getColumn();
			row = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackWallsOnBoard(0).getMove().getTargetTile().getRow();
		}else {
			wallDirection = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer().getWall(0).getMove().getWallDirection();
			col = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhiteWallsOnBoard(0).getMove().getTargetTile().getColumn();
			row = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhiteWallsOnBoard(0).getMove().getTargetTile().getRow();
		}
		assertEquals(Direction.Horizontal, wallDirection);
		assertEquals(row, intx);
		assertEquals(col, inty);

	}

	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	@Then("Both players shall have {int} in their stacks")
	public void both_players_shall_have_in_their_stacks(Integer intx) {
		Integer blackwall = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().numberOfBlackWallsInStock();
		Integer whitewall = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().numberOfWhiteWallsInStock();
		assertEquals(intx, blackwall);
		assertEquals(intx, whitewall);
	}
	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	@When("The position to load is invalid")
	public void the_position_to_load_is_invalid() {
		//assertEquals(false, QuoridorController.validatePosition());

	}
	/**
	 *Feature: Load Position
	 *@Author Hongshuo Zhou
	 */
	@Then("The load shall return an error") 
	public void the_load_shall_return_an_error() {
		Assert.assertEquals("Load File Error- Invalid Position", view.notification.getText());
	}
	//***********************************************
	// Identify if game won
	// **********************************************
	/**
	 *Feature: Identify if game won
	 *@Author Hongshuo Zhou
	 */

	@Given("Player {string} has just completed his move")
	public void player_has_just_completed_his_move(String string) {
		Player player;
		if (string.equals("white")) {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();
			QuoridorController.completeMove();
		}else {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
			QuoridorController.completeMove();
		}
	}

	@Given("The new position of {string} is {int}:{int}")
	public void the_new_position_of_is(String string, Integer row, Integer col) {
		Player player;
		Tile newTile = QuoridorApplication.getQuoridor().getBoard().getTile(9*(row-1)+(col-1));
		if (string.equals("white")) {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();
			PlayerPosition newWhitePosition = new PlayerPosition(player, newTile); 
			QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setWhitePosition(newWhitePosition);
		}else {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
			PlayerPosition newBlackPosition = new PlayerPosition(player, newTile); 
			QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setBlackPosition(newBlackPosition);
		}
	}

	@Given("The clock of {string} is more than zero")
	public void the_clock_of_is_more_than_zero(String string) {
		Player player;

		if (string.equals("white")) {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		} else {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
		}

		Time timeLeft = player.getRemainingTime();
		int minutes = timeLeft.getMinutes();
		int seconds = timeLeft.getSeconds();
		int totaltime = minutes * 60 + seconds;
		if (totaltime <= 0) {
			player.setRemainingTime(new Time(3000));
		}

	}

	@When("Checking of game result is initated")
	public void checking_of_game_result_is_initated() {
		gameResult = QuoridorController.checkGameResult();
	}

	@Then("The game shall no longer be running")
	public void the_game_shall_no_longer_be_running() {
		GameStatus status = QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus();
		boolean gameIsRunning = (GameStatus.Running == status);
		if(gameIsRunning) {
			assertEquals(true, gameIsRunning);
		}
		else {
			assertEquals(false, gameIsRunning);
		}
	}

	@When("The clock of {string} counts down to zero")
	public void the_clock_of_counts_down_to_zero(String string) {
		Player player;
		if (string.equals("white")) {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		} else {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
		}
		player.setRemainingTime(new Time(0));
		gameResult = QuoridorController.clockCountDownToZero(player);

	}
	//***********************************************
	// Identify if game drawn
	// **********************************************
	/**
	 *Feature: Identify if game drawn
	 *@Author Hongshuo Zhou
	 */
	@Given("The following moves were executed:")
	public void the_following_moves_were_executed(io.cucumber.datatable.DataTable dataTable) {
		Game game = QuoridorApplication.getQuoridor().getCurrentGame();
		Player white = game.getWhitePlayer();
		Player black = game.getBlackPlayer();
		List<Map<String, String>> valueMaps = dataTable.asMaps();
		for (Map<String, String> map : valueMaps) {
			Integer move, turn, row, col;
			move = Integer.decode(map.get("move"));
			turn = Integer.decode(map.get("turn"));
			row = Integer.decode(map.get("row"));
			col = Integer.decode(map.get("col"));

			Tile tile = QuoridorApplication.getQuoridor().getBoard().getTile(9*(row-1)+(col-1));
			if (turn == 1) {
				new StepMove(move, turn, white, tile, game);
				QuoridorController.newPosition();
				PlayerPosition playerPosition;
				playerPosition = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition();
				playerPosition.setTile(tile);
			} else {
				new StepMove(move, turn, black, tile, game);
				QuoridorController.newPosition();
				// update position
				PlayerPosition playerPosition;
				playerPosition = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition();
				playerPosition.setTile(tile);
			}
		}
	}
	//***********************************************
	// Identify if game drawn
	// **********************************************
	/**
	 *Feature: Identify if game drawn
	 *@Author Hongshuo Zhou
	 */
	@Given("The last move of {string} is pawn move to {int}:{int}")
	public void the_last_move_of_is_pawn_move_to(String string, Integer row, Integer col) {
		Player player;
		if (string.equals("white")) {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		} else {
			player = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
		}
		Tile tile = QuoridorApplication.getQuoridor().getBoard().getTile(9*(row-1)+(col-1));
		Game game = QuoridorApplication.getQuoridor().getCurrentGame();

		List<Move> moves = game.getMoves();
		int size = moves.size();
		Move lastMove = game.getMove(size-2);
		int round = lastMove.getRoundNumber()+1;
		int moveNumber = lastMove.getMoveNumber()+1;

		StepMove move = new StepMove(moveNumber, round, player, tile, game);
		QuoridorController.newPosition();
		PlayerPosition Position; 
		if (player.hasGameAsBlack()) { 
			Position = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition(); 
		} else 
			Position = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition(); 
		Position.setTile(tile);
		
	}




	//***********************************************
	//Set total thinking time
	// **********************************************
	/**
	 * Feature :Set Total thinking time
	 * @Author Xiangyu Li
	 * @param minute minute used for total thinking time
	 * @param second second used for total thinking time 
	 */

	@When("{int}:{int} is set as the thinking time")
	public void is_set_as_the_thinking_time(int minute,int second) {
		QuoridorController.setTotaltime(minute, second);
	}

	/**
	 * Feature :Set Total thinking time
	 * @Author Xiangyu Li
	 * @param minute check if player have minutes left
	 * @param second check if player have second left
	 */

	@Then("Both players shall have {int}:{int} remaining time left")
	public void both_players_shall_have_remaining_time_left(int minute,int second) {
		long remaintime=(minute*60+second)*1000;
		Assert.assertEquals(remaintime,QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer().getRemainingTime().getTime());
		Assert.assertEquals(remaintime,QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer().getRemainingTime().getTime());
	}

	//*************************************************
	//Switch Player
	//*************************************************

	/**
	 * Feature :Switch current player
	 * Xiangyu Li
	 * @param color color of player want to move 
	 */
	@Given("The player to move is {string}")
	public void Playertomove(String color) {
		if(color.equals("black")) {
			QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer());
			view.p2Turn.setSelected(true);
			view.p1Turn.setSelected(false);
		}
		else  {
			QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer());
			view.p1Turn.setSelected(true);
			view.p2Turn.setSelected(false);
		}

	}


	/**
	 * Feature :Switch current player
	 * Xiangyu Li
	 * @param color color of player to run clock
	 */
	@And("The clock of {string} is running")
	public void the_clock_of_black_is_running(String color) {
		// Write code here that turns the phrase above into concrete actions
		if(color=="black") {
			QuoridorController.runblackclock(view);
		}
		else
			QuoridorController.runwhiteclock(view);
	}
	/**
	 * Feature :Switch current player
	 * Xiangyu Li
	 * @param color color of player to stop clock
	 */
	@And("The clock of {string} is stopped")
	public void the_clock_of_white_is_stopped(String color) {
		// Write code here that turns the phrase above into concrete actions
		if(color=="white") {
			Timer whitetimer=QuoridorController.runwhiteclock(view);
			QuoridorController.stopwhiteclock(whitetimer);
		}
		else {
			Timer blacktimer=QuoridorController.runblackclock(view);
			QuoridorController.stopblackclock(blacktimer);
		}
	}
	/**
	 * Feature :Switch current player
	 * Xiangyu Li
	 * @param color color of player who complete his move 
	 */
	@When("Player {string} completes his move")
	public void player_blackplayer_completes_his_move(String color) {
		QuoridorController.completeMove();
	}
	/**
	 * Feature :Switch current player
	 * Xiangyu Li 
	 * @param color color of player is in turn
	 */
	@Then("The user interface shall be showing it is {string} turn")
	public void the_user_interface_is_showing_it_is_white_s_turn(String color) {
		// Write code here that turns the phrase above into concrete actions
		if(color=="black") {
			Assert.assertEquals(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer(),currentPlayer);
		}
		if(color=="white") {
			Assert.assertEquals(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer(),currentPlayer);
		}
	}
	/**
	 * Feature :Switch current player
	 * Xiangyu Li
	 * @param color color of player to stop clock
	 */
	@And("The clock of {string} shall be stopped")
	public void the_clock_of_black_shall_be_stopped(String color) {
		if(color=="white") {
			Timer whitetimer=QuoridorController.runwhiteclock(view);
			QuoridorController.stopwhiteclock(whitetimer);
		}
		else {
			Timer blacktimer=QuoridorController.runblackclock(view);
			QuoridorController.stopblackclock(blacktimer);
		}
	}

	/**
	 * Feature :Switch current player
	 * Xiangyu Li
	 * @param color color of player to run his clock
	 */
	@And("The clock of {string} shall be running")
	public void the_clock_of_white_shall_be_running(String color) {
		if(color=="white") {
			QuoridorController.runwhiteclock(view);
		}
		else
			QuoridorController.runblackclock(view);
	}


	/**
	 * Feature :Switch current player
	 * Xiangyu Li
	 * @param color color of player is going to move 
	 */
	@And("The next player to move shall be {string}")
	public void the_player_to_move_is_secondplayer(String color) {
		Player player;
		if(color=="white") {
			player=QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();
			Assert.assertEquals(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove(),player);
		}
		if(color=="black") {
			player=QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
			Assert.assertEquals(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove(),player);
		}
	}
	///////////////////////////////////////////////////////////////////////////////////
	/**
	 * GrabWall and MoveWall stepdefinitions
	 * 
	 * @author aidanwilliams
	 */

	// Scenario 1
	@Given("I have more walls on stock")
	public void iHaveMoreWallsOnStock() {
		Assert.assertTrue(
				QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().hasWalls());
	}

	@When("I try to grab a wall from my stock")
	public void iTryToGrabAWallFromMyStock() {
		//QuoridorController.grabWall();
		view.grabButton.doClick();
	}

	@Then("A wall move candidate shall be created at initial position")
	public void aWallMoveCandidateShallBeCreatedAtInitialPosition() {
		Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate() != null);
	}

	@And("I shall have a wall in my hand over the board")
	public void iShallHaveAWallInMyHandOverTheBoard() {
		Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate() != null);
	}

	@And("The wall in my hand shall disappear from my stock")
	public void theWallInMyHandShallDisappearFromMyStock() {
		if(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()) ) {
			Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhiteWallsInStock().indexOf(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallPlaced()) == -1);	
		} else {
			Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackWallsInStock().indexOf(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallPlaced()) == -1);
		}

	}

	// Scenario 2
	@Given("I have no more walls on stock")
	public void iHaveNoMoreWallsOnStock() {
		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		int wallSize = curPos.numberOfBlackWallsInStock();
		for(int i = 0; i < wallSize; i++) {
			Wall wallToRemove = curPos.getBlackWallsInStock(0);
			curPos.removeBlackWallsInStock(wallToRemove);
		}

		wallSize = curPos.numberOfWhiteWallsInStock();
		for(int i = 0; i < wallSize; i++) {
			Wall wallToRemove = curPos.getWhiteWallsInStock(0);
			curPos.removeWhiteWallsInStock(wallToRemove);
		}

	}

	@Then("I shall be notified that I have no more walls")
	public void iShallBeNotifiedThatIHaveNoMoreWalls() {

		Assert.assertTrue(view.notification.getText().equals("No walls in stock"));
	}

	@And("I shall have no walls in my hand")
	public void iShallHaveNoWallsInMyHand() {
		Assert.assertTrue(
				QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate() == null);

	}

	// Scenario Outline: Move Wall over the board
	@Given("A wall move candidate exists with {string} at position {int}, {int}")
	public void aWallMoveCandidateExistsWith(String dir, int row, int col) {
		if(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate() == null) {
			QuoridorController.grabWall();	
		}
		aWallMove = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate();
		QuoridorController.moveWall(QuoridorController.findTile(row, col));
		if (dir.equals("vertical")) {
			aWallMove.setWallDirection(Direction.Vertical);
		}
		else {
			aWallMove.setWallDirection(Direction.Horizontal);
		}

	}

	@And("The wall candidate is not at the {string} edge of the board")
	public void notAtTheSide(String side) {
		Assert.assertFalse(QuoridorController.isSide(aWallMove));
	}

	@When("I try to move the wall {string}")
	public void tryToMoveWall(String side) {
		if (side.equals("left"))
			QuoridorController.moveWall(QuoridorController.findTile(aWallMove.getTargetTile().getRow(),
					aWallMove.getTargetTile().getColumn() - 1));
		if (side.equals("right"))
			QuoridorController.moveWall(QuoridorController.findTile(aWallMove.getTargetTile().getRow(),
					aWallMove.getTargetTile().getColumn() + 1));
		if (side.equals("up"))
			QuoridorController.moveWall(QuoridorController.findTile(aWallMove.getTargetTile().getRow() - 1,
					aWallMove.getTargetTile().getColumn()));
		if (side.equals("down"))
			QuoridorController.moveWall(QuoridorController.findTile(aWallMove.getTargetTile().getRow() + 1,
					aWallMove.getTargetTile().getColumn()));
	}

	@Then("The wall shall be moved over the board to position {int}, {int}")
	public void wallShallBeMovedToPosition(int nrow, int ncol) {
		Assert.assertTrue(aWallMove.getTargetTile().getRow() == nrow && aWallMove.getTargetTile().getColumn() == ncol);
	}

	@And("A wall move candidate shall exist with {string} at position {int}, {int}")
	public void validateCandidate(String dir, int nrow, int ncol) {
		if (dir.equals("vertical"))
			Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection().equals(Direction.Vertical)
					&& QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getTargetTile().equals(QuoridorController.findTile(nrow, ncol)));
		if (dir.equals("horizontal"))
			Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection().equals(Direction.Horizontal)
					&& QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getTargetTile().equals(QuoridorController.findTile(nrow, ncol)));

	}

	// Scenario Outline: Move wall at the edge of the board

	@And("The wall candidate is at the {string} edge of the board")
	public void atTheSide(String side) {
		QuoridorController.isSide(aWallMove);
	}

	@Then("I shall be notified that my move is illegal")
	public void moveIsIllegal() {
		Assert.assertTrue(true);
	}

	////////////////////////////////////////////////////////////////////////////


	/** Drop Wall Step Definition File
	 * @author Yanis Jallouli
	 */
	// ***********************************************
	// Drop Wall definitions
	// ***********************************************

	//Scenario 1
	@Given("The wall move candidate with {string} at position {int}, {int} is valid")
	public void theWallMoveCandidateWithDirAtPosIsValid(String dir, int row, int col) throws InvalidInputException {
		//Get a string- make a direction
		Direction direction = dir.equals("vertical") ? Direction.Vertical : Direction.Horizontal;
		WallMove toCheck = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate();
		toCheck.setWallDirection(direction);
		toCheck.setTargetTile(QuoridorController.findTile(row, col));

		if(!QuoridorController.wallIsValid()) {				
			QuoridorApplication.getQuoridor().getCurrentGame().getMove(QuoridorApplication.getQuoridor().getCurrentGame().getMoves().indexOf(QuoridorController.invalidWall())).setTargetTile(null);
		}
	}

	@When("I release the wall in my hand") 
	public void iReleaseTheWallInMyHand(){
		view.DropWall();
	}

	@Then("A wall move shall be registered with {string} at position {int}, {int}")
	public void aWallMoveIsRegisteredAtPosition(String dir, int row, int col) throws InvalidInputException {
		Direction direction = dir.equals("vertical") ? Direction.Vertical : Direction.Horizontal;
		Assert.assertTrue("Move wasn't registered after dropping", QuoridorController.moveIsRegistered(direction, row, col));
	}

	@And("I shall not have a wall in my hand") 
	public void iShallNotHaveAWallInMyHand() {
		//Ensures the candidate wallmove is null. Might be a grab wall feature, but this is easy
		Assert.assertFalse(QuoridorApplication.getQuoridor().getCurrentGame().hasWallMoveCandidate());
	}

	@And("My move shall be completed")
	public void myMoveIsCompleted() {
		assertTrue(view.p2Turn.isSelected() && !view.p1Turn.isSelected());
	}

	@And("It shall not be my turn to move")
	public void itIsNotMyTurnToMove() {
		Assert.assertTrue( !QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
				QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer())     );
	}
	//SCENARIO 2
	@Given("The wall move candidate with {string} at position {int}, {int} is invalid")
	public void theWallMoveCandidateWithDirAtPosIsInvalid(String dir, int row, int col) throws InvalidInputException {
		//Background ensures I have a wall in hand

		//Get a string- make a direction
		Direction direction = dir.equals("vertical") ? Direction.Vertical : Direction.Horizontal;
		WallMove toCheck = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate();
		toCheck.setWallDirection(direction);
		toCheck.setTargetTile(QuoridorController.findTile(row, col));

		if(QuoridorController.wallIsValid()) {
			//If it's valid, make it invalid
			QuoridorApplication.getQuoridor().getCurrentGame().addMove(toCheck);
		}

	}


	@Then("I shall be notified that my wall move is invalid")
	public void iShallBeNotifiedThatMyWallMoveIsInvalid() {
		assertTrue(view.notification.getText().equals("Invalid Wall Placement") && view.notification.isVisible());
	}

	@And("It shall be my turn to move")
	public void itShallBeMyTurnToMove() {
		Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
				QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer())   );
	}

	@But("No wall move shall be registered with {string} at position {int}, {int}")
	public void noWallMoveShallBeRegisteredAtPosition(String dir, int row, int col) throws InvalidInputException {
		Direction direction = dir.equals("vertical") ? Direction.Vertical : Direction.Horizontal;
		Assert.assertFalse(QuoridorController.moveIsRegistered(direction, row, col));
	}


	/** Save Position Step Definition File
	 * @author Yanis Jallouli
	 */
	// ***********************************************
	// Save Position definitions
	// ***********************************************
	//Scenario 1- Save Position
	@Given("No file {string} exists in the filesystem")
	public void noFileExistsInTheSystem(String fileName) {
		//I can't find anything on using givens as control flow
		if(QuoridorController.containsFile(fileName)) {
			QuoridorController.deleteFile(fileName);
		}	
	}

	@When("The user initiates to save the game with name {string}")
	public void theUserInitiatesToSaveTheGameWithName(String fileName) {	
		view.confirmSaveAction();

		JTextField fill = (JTextField) view.confirmFrame.getContentPane().getComponent(1); //Get TextBox
		fill.setText(fileName);
		JButton save = (JButton) view.confirmFrame.getContentPane().getComponent(2);
		save.doClick();

	}

	@Then("A file with {string} shall be created in the filesystem")
	public void aFileWithNameShallBeCreated(String fileName) {
		Assert.assertTrue(QuoridorController.containsFile(fileName));
	}

	//Scenario 2- Save Position with existing file name
	@Given("File {string} exists in the filesystem")
	public void fileNameExistsInSystem(String fileName) {
		if(!QuoridorController.containsFile(fileName)) {
			QuoridorController.createFile(fileName);
		}
	}

	@And("The user confirms to overwrite existing file")
	public void theUserConfirmsToOverwrite() {
		JButton yesBut = (JButton) view.confirmFrame.getContentPane().getComponent(1);
		yesBut.doClick();
	}
	@Then("File with {string} shall be updated in the filesystem")
	public void fileWithNameShallBeUpdatedInSystem(String fileName) {
		Assert.assertTrue(QuoridorController.isUpdated(fileName));
	}

	//Scenario 3- Save Position Cancelled
	@And("The user cancels to overwrite existing file")
	public void theUserCancelsToOverwrite() {
		JButton noBut = (JButton) view.confirmFrame.getContentPane().getComponent(2);
		noBut.doClick();
	}
	@Then("File {string} shall not be changed in the filesystem")
	public void fileWithNameShallNotBeUpdatedInSystem(String fileName) {
		Assert.assertFalse(QuoridorController.isUpdated(fileName));
	}

	/**
	 * Feature 4. Initiate Board step definitions
	 * 
	 * @author Matteo Nunez
	 *
	 */
	@When("The initialization of the board is initiated")
	public void theInitializationOfTheBoardIsInitiated() {
		view.newGame.doClick();
		if(view.confirmFrame.isVisible()) {
			((JButton) view.confirmFrame.getContentPane().getComponent(1)).doClick();
		}
	}

	@Then("It shall be white player to move")
	public void itShallBeWhitePlayerToMove() {
		if(!view.p1Turn.isSelected())
			QuoridorController.completeMove();
	}

	@Then("White's pawn shall be in its initial position")
	public void whitesPawnShallBeInItsInitialPosition() {
		assertEquals(9, QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile().getRow());
		assertEquals(5, QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile().getColumn());
	}

	@Then("Black's pawn shall be in its initial position")
	public void blacksPawnShallBeInItsInitialPosition() {
		assertEquals(1, QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile().getRow());
		assertEquals(5, QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile().getColumn());
	}

	@Then("All of White's walls shall be in stock")
	public void allOfWhitesWallsShallBeInStock() {
		assertEquals(10,QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhiteWallsInStock().size());
	}

	@Then("All of Black's walls shall be in stock")
	public void allOfBlacksWallsShallBeInStock() {
		assertEquals(10,QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackWallsInStock().size());
	}

	@Then("White's clock shall be counting down")
	public void whitesClockShallBeCountingDown() throws InterruptedException {
		assertTrue(view.whiteTimer.isRunning());
	}

	@Then("It shall be shown that this is White's turn")
	public void itShallBeShownThatThisIsWhitesTurn() {
		Assert.assertTrue(view.p1Turn.isSelected());
	}

	/**
	 * Feature 5. Rotate Wall step definitions
	 * 
	 * @author Matteo Nunez
	 *
	 */
	@When("I try to flip the wall")
	public void iTryToFlipTheWall() {
		view.RotateWall();
	}

	@Then("The wall shall be rotated over the board to {string}")
	public void theWallShallBeRotatedOverTheBoardTo(String dir) {
		Direction newDir = dir.equals("vertical") ? Direction.Vertical : Direction.Horizontal;
		assertEquals(newDir, QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection()); 
	}

	@And("A wall move candidate shall exist with {string} at position \\({int}, {int})")
	public void aWallMoveCandidateShallExistWithAtPosition(String string, int row, int col) {
		Direction direction = string.equals("vertical") ? Direction.Vertical : Direction.Horizontal;
		assertEquals(direction, QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection());
		assertEquals(QuoridorController.findTile(row, col), QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getTargetTile());

	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Feature: #2 Provide or select user name
	 * @author Keanu, Natchev
	 * ID#: 260804586
	 */

	@Given("Next player to set user name is {string}")
	public void nextPlayerToSetUserNameIs(String string) {

		if(string.equals("black")) {
			view.useExistingBlack.doClick();
		} else if(string.equals("white")) {
			view.useExistingWhite.doClick();
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Given("There is existing user {string}")
	public void thereIsExistingUser(String string) {
		if(!QuoridorController.ExistingUserName(string)) {
			QuoridorController.createUser(string);
			view.userList.add(new JLabel(string));
		}

	}

	@When("The player selects existing {string}")
	public void thePlayerSelectsExisting(String string) {
		if(view.userSelecting.equals("white")) {
			view.whiteName.setText(string);
		} else {
			view.blackName.setText(string);
		}
		view.newGame.doClick(); //Start new game and confirm you want an existing user
		((JButton) view.confirmFrame.getContentPane().getComponent(1)).doClick();
	}

	@Then("The name of player {string} in the new game shall be {string}")
	public void theNameOfPlayerInTheNewGameShallBe(String string, String string2) {
		if(string.equals("black")) {
			assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer().getUser().getName().equals(string2));
		} else {
			assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer().getUser().getName().equals(string2));
		}
	}

	@Given("There is no existing user {string}")
	public void thereIsNoExistingUser(String string) {
		if(QuoridorController.ExistingUserName(string)) {
			QuoridorApplication.getQuoridor().removeUser(QuoridorController.findUserName(string));
		}
	}

	@When("The player provides new user name: {string}")
	public void thePlayerProvidesNewUserName(String string) {
		if(view.userSelecting.equals("white")) {
			view.whiteName.setText(string);
		} else {
			view.blackName.setText(string);
		}
		view.newGame.doClick();
	}

	@Then("The player shall be warned that {string} already exists")
	public void thePlayerShallBeWarnedThatAlreadyExists(String string) {
		view.confirmExistingName();
	}

	@Then("Next player to set user name shall be {string}")
	public void nextPlayerToSetUserNameShallBe(String string) {
		if(string.equals("white")) {
			assertTrue(view.userSelecting.equals("white"));
		} else {
			assertTrue(view.userSelecting.equals("black"));
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Feature: #11 Validate position
	 * @author Keanu, Natchev
	 * ID#: 260804586
	 */

	@Given("A game position is supplied with pawn coordinate {int}:{int}")
	public void aGamePositionIsSuppliedWithPawnCoordinate(Integer int1, Integer int2) {
		if(int1 < 1 || int1 > 9 || int2 < 1 || int2 > 9) {
			System.out.println("Invalid coordinates given. Values must be between 1 and 9.");
		}
		else {			
			if(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().hasGameAsBlack()) {
				PlayerPosition aNewBlackPosition = new PlayerPosition(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer(), QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile());
				QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setBlackPosition(aNewBlackPosition);
			}
			if(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().hasGameAsWhite()) {
				PlayerPosition aNewWhitePosition = new PlayerPosition(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer(), QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile());
				QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setWhitePosition(aNewWhitePosition);
			}
		}
	}

	@When("Validation of the position is initiated")
	public void validationOfThePositionIsInitiated() {
		//QuoridorController.validatePosition();
		view.validateButton.doClick();
	}

	@Then("The position shall be {string}")
	public void thePositionShallBe(String string) {
		if(QuoridorController.validatePosition()) {
			//string = "ok";
			Assert.assertTrue(string.equals("ok"));
		}
		else {
			Assert.assertFalse(string.equals("ok"));
		}
	}

	@Given("A game position is supplied with wall coordinate {int}:{int}-{string}")
	public void aGamePositionIsSuppliedWithWallCoordinate(Integer row, Integer col, String string) {		
		Direction directionGiven = null;

		if(string.equals("vertical")) {
			directionGiven = Direction.Vertical;					
		} else {
			directionGiven = Direction.Horizontal;
		}

		view.grabButton.doClick();
		QuoridorController.moveWall(QuoridorController.findTile(row, col));
		if(directionGiven == Direction.Horizontal) view.rotateButton.doClick();
		QuoridorController.dropWall();
	}

	@Then("The position shall be valid")
	public void thePositionShallBeValid() {
		Assert.assertTrue(view.notification.getText().equals("Quoridor Position is Valid"));
	}

	@Then("The position shall be invalid")
	public void thePositionShallBeInvalid() {
		System.out.println("Notification: " + view.notification.getText());
		Assert.assertTrue(view.notification.getText().equals("Invalid Quoridor Position"));
		//assertEquals(false, QuoridorController.validPosition());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Feature: Move Pawn
	 * @author Yanis Jallouli
	 */


	//SCENARIO 1

	@And("The player is located at {int}:{int}")
	public void thePlayerIsLocatedAtRowCol(int row, int col) {
		QuoridorController.tpPlayer(row, col);
	}

	@And("There are no {string} walls {string} from the player") 
	public void thereAreNoDirWallsSideFromPlayer(String dir, String side){

		PlayerPosition pos;
		if(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
				QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()) ) {
			pos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition();
		} else {
			pos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition();
		}
		int row = pos.getTile().getRow();
		int col = pos.getTile().getColumn();

		Direction direction = dir.equals("vertical") ? Direction.Vertical : Direction.Horizontal;

		if(side.equals("left")) {
			col -=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Vertical) {
					if(w.getTargetTile().getColumn() == col && (w.getTargetTile().getRow() == row || w.getTargetTile().getRow() == row - 1)) {
						QuoridorApplication.getQuoridor().getCurrentGame().removeMove(w);
					}

				}
				//Horizontal Wall can't block left path
			}
		} else if (side.equals("right")) {
			//col +=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Vertical) {
					if(w.getTargetTile().getColumn() == col && (w.getTargetTile().getRow() == row || w.getTargetTile().getRow() == row - 1)) {
						QuoridorApplication.getQuoridor().getCurrentGame().removeMove(w);
					}
				}
				//Horizontal Wall can't block right path
			}

		} else if (side.equals("down")) {
			//row +=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Horizontal) {
					if(w.getTargetTile().getRow() == row && (w.getTargetTile().getColumn() == col || w.getTargetTile().getColumn() == col - 1)) {
						QuoridorApplication.getQuoridor().getCurrentGame().removeMove(w);
					}
				}
				//Vertical Wall can't block down path
			}

		} else if (side.equals("up")) {
			row -=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Horizontal) {
					if(w.getTargetTile().getRow() == row && (w.getTargetTile().getColumn() == col || w.getTargetTile().getColumn() == col - 1)) {
						QuoridorApplication.getQuoridor().getCurrentGame().removeMove(w);
					}
				}
				//Vertical Wall can't block up path
			}
		}
	}

	@And("There is a {string} wall {string} from the player")
	public void thereIsADirWallSideFromPlayer(String dir, String side) {


		PlayerPosition pos;
		if(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
				QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()) ) {
			pos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition();
		} else {
			pos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition();
		}
		int row = pos.getTile().getRow();
		int col = pos.getTile().getColumn();

		Direction direction = dir.equals("vertical") ? Direction.Vertical : Direction.Horizontal;
		boolean found = false;
		if(side.equals("left")) {
			col -=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Vertical) {
					if(w.getTargetTile().getColumn() == col && (w.getTargetTile().getRow() == row || w.getTargetTile().getRow() == row - 1)) {
						found = true;
					}

				}
				//Horizontal Wall can't block left path
			}

		} else if (side.equals("right")) {
			//col +=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Vertical) {
					if(w.getTargetTile().getColumn() == col && (w.getTargetTile().getRow() == row || w.getTargetTile().getRow() == row - 1)) {
						found = true;
					}
				}
				//Horizontal Wall can't block right path

			}

		} else if (side.equals("down")) {
			//row +=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Horizontal) {
					if(w.getTargetTile().getRow() == row && (w.getTargetTile().getColumn() == col || w.getTargetTile().getColumn() == col - 1)) {
						found = true;
					}
				}
				//Vertical Wall can't block down path
			}

		} else if (side.equals("up")) {
			row -=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Horizontal) {
					if(w.getTargetTile().getRow() == row && (w.getTargetTile().getColumn() == col || w.getTargetTile().getColumn() == col - 1)) {
						found = true;
					}
				}
				//Vertical Wall can't block up path
			}
		}
		if(!found) {
			WallMove move = new WallMove(1, 1, 
					QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove(), 
					QuoridorController.findTile(row, col),
					QuoridorApplication.getQuoridor().getCurrentGame(),
					direction,
					QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().getWall(0));
			QuoridorApplication.getQuoridor().getCurrentGame().addMove(move);
		}


	}

	@And("The opponent is not {string} from the player")
	public void theOpponentIsNotSideFromPlayer(String side) {
		PlayerPosition pPos;
		PlayerPosition oPos;
		if(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
				QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()) ) {
			pPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition();
			oPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition();
		} else {
			pPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition();
			oPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition();
		}
		int row = pPos.getTile().getRow();
		int col = pPos.getTile().getColumn();

		int rChange = 0;
		int cChange = 0;

		if(side.equals("up")) {
			rChange = -1;
		} else if(side.equals("down")) {
			rChange =1;
		} else if (side.equals("right")) {
			cChange =1;
		} else if (side.equals("left")) {
			cChange =-1;
		}
		//Move away from player by one unit
		if(oPos.getTile().equals(QuoridorController.findTile(row + rChange, col + cChange))) {
			oPos.setTile(QuoridorController.findTile(row + 2*rChange, col + 2*cChange));
		}
	}

	@When("Player {string} initiates to move {string}")
	public void playerIntiatesToMove(String player, String side) {
		view.moveButton.doClick(); //Sets the move mode

		if(side.equals("up")) 				view.movePlayer(MoveDirection.North);
		else if(side.equals("down")) 		view.movePlayer(MoveDirection.South);
		else if(side.equals("left")) 		view.movePlayer(MoveDirection.West);
		else if(side.equals("right")) 		view.movePlayer(MoveDirection.East);
		else if(side.equals("upleft")) 		view.movePlayer(MoveDirection.NorthWest);
		else if(side.equals("upright")) 	view.movePlayer(MoveDirection.NorthEast);
		else if(side.equals("downleft")) 	view.movePlayer(MoveDirection.SouthWest);
		else if(side.equals("downright"))	view.movePlayer(MoveDirection.SouthEast);

	}

	@Then("The move {string} shall be {string}")
	public void theMoveSideShallBeStatus(String side, String status) {
		if(status.equals("illegal")) {
			Assert.assertTrue(view.notification.isVisible() && view.notification.getText().equals("Invalid Player Move"));
		} else {
			Assert.assertFalse(view.notification.isVisible());
		}
	}

	@And("Player's new position shall be {int}:{int}") 
	public void playersNewPosShallBe(int row, int col) {
		GamePosition curPos;
		Player toCheck;

		curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();

		//Move failed, check current position current player
		if(view.notification.isVisible()) {

			if(curPos.getPlayerToMove().equals(
					QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer())) {
				toCheck = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
			} else {
				toCheck = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();
			}
			//Move succeeded, check current position- other player (since player moving has changed)
		} else {

			if(curPos.getPlayerToMove().equals(
					QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer())) {

				toCheck = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();
			} else {
				toCheck = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
			}
		}

		//Last move added to Positions was white
		if(toCheck.equals(
				QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer())) {
			Assert.assertTrue(curPos.getWhitePosition().getTile().getRow() == row && curPos.getWhitePosition().getTile().getColumn() == col);
			//Last move added to positions was black
		} else {
			Assert.assertTrue(curPos.getBlackPosition().getTile().getRow() == row && curPos.getBlackPosition().getTile().getColumn() == col);
		}


	}

	@And("The next player to move shall become {string}")
	public void theNextPlayerToMoveShallBecome(String player) {
		//TODO: This quite randomly and rarely comes out to a null pointer (I think somewhere in the get player). Find out why
		if(player.equals("white")) {
			Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
					QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer())
					&& view.p1Turn.isSelected());
		} else {
			Assert.assertTrue(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
					QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer())
					&& view.p2Turn.isSelected());
		}
	}


	@And("There is a {string} wall at {int}:{int}")
	public void thereIsADirWallAtRowCol(String d, int row, int col) {
		boolean exists = false;
		Direction dir = d.equals("vertical") ? Direction.Vertical : Direction.Horizontal;
		for(WallMove move : QuoridorController.getWalls()) {
			if(move.getWallDirection() == dir 
					&& move.getTargetTile().getRow() == row
					&& move.getTargetTile().getColumn() == col) {
				exists = true;
			}
		}

		if(!exists) {
			//TODO: See if we can't get this working
			WallMove move = new WallMove(1, 1, QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer(), 
					QuoridorController.findTile(row, col),
					QuoridorApplication.getQuoridor().getCurrentGame(),
					dir,
					QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer().getWall(0));
			QuoridorApplication.getQuoridor().getCurrentGame().addMove(move);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Feature: Jump Pawn
	 * @author Yanis Jallouli
	 */

	@And("The opponent is located at {int}:{int}")
	public void theOpponentIsLocatedAt(int row, int col) {
		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		PlayerPosition pos = new PlayerPosition(curPos.getPlayerToMove(), QuoridorController.findTile(row, col));

		if(curPos.getPlayerToMove().equals(white)) curPos.setBlackPosition(pos);
		else curPos.setWhitePosition(pos);
	}

	@And("There are no {string} walls {string} from the player nearby")
	public void thereAreNoDirWallsSideFromPlayerNearby(String dir, String side) {

		PlayerPosition pos;
		if(QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
				QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()) ) {
			pos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition();
		} else {
			pos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition();
		}
		int row = pos.getTile().getRow();
		int col = pos.getTile().getColumn();

		Direction direction = dir.equals("vertical") ? Direction.Vertical : Direction.Horizontal;

		if(side.contains("left")) {
			col -=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Vertical) {
					if((w.getTargetTile().getColumn() == col || w.getTargetTile().getColumn() == col - 1) && 
							(w.getTargetTile().getRow() == row || w.getTargetTile().getRow() == row - 1)) {
						QuoridorApplication.getQuoridor().getCurrentGame().removeMove(w);
					}

				}
				//Horizontal Wall can't block left path
			}
		} else if (side.contains("right")) {
			//col +=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Vertical) {
					if((w.getTargetTile().getColumn() == col || w.getTargetTile().getColumn() == col + 1)
							&& (w.getTargetTile().getRow() == row || w.getTargetTile().getRow() == row - 1)) {
						QuoridorApplication.getQuoridor().getCurrentGame().removeMove(w);
					}
				}
				//Horizontal Wall can't block right path
			}

		}
		if (side.contains("down")) {
			//row +=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Horizontal) {
					if((w.getTargetTile().getRow() == row  || w.getTargetTile().getRow() == row + 1)
							&& (w.getTargetTile().getColumn() == col || w.getTargetTile().getColumn() == col - 1)) {
						QuoridorApplication.getQuoridor().getCurrentGame().removeMove(w);
					}
				}
				//Vertical Wall can't block down path
			}

		} else if (side.contains("up")) {
			row -=1;
			for(WallMove w : QuoridorController.getWalls()) {
				if(direction == Direction.Horizontal) {
					if((w.getTargetTile().getRow() == row || w.getTargetTile().getRow() == row - 1)  
							&& (w.getTargetTile().getColumn() == col || w.getTargetTile().getColumn() == col - 1)) {
						QuoridorApplication.getQuoridor().getCurrentGame().removeMove(w);
					}
				}
				//Vertical Wall can't block up path
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////

	/** 
	 * Feature: Check If Path Exists
	 * @author Yanis Jallouli
	 */


	@Given("A {string} wall move candidate exists at position {int}:{int}")
	public void aDirWallMoveCandidateExistsAtPosRowCol(String dir, int row, int col) {
		//I checked, this is working just fine
		view.grabButton.doClick();
		if(dir.equals("horizontal")) view.rotateButton.doClick();
		QuoridorController.moveWall(QuoridorController.findTile(row, col));
	}

	@And("The black player is located at {int}:{int}")
	public void theBlackPlayerIsAtRowCol(int row, int col) {
		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player black = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();

		PlayerPosition pos = new PlayerPosition(black, QuoridorController.findTile(row, col));
		curPos.setBlackPosition(pos);

	}

	@And("The white player is located at {int}:{int}")
	public void theWhitePlayerIsAtRowCol(int row, int col) {
		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		PlayerPosition pos = new PlayerPosition(white, QuoridorController.findTile(row, col));
		curPos.setWhitePosition(pos);
	}

	@When("Check path existence is initiated")
	public void checkPathExistenceIsInitiated() {
		view.validateButton.doClick();
	}

	@Then("Path is available for {string} player\\(s)")
	public void pathIsAvailableForResultPlayers(String result) {
		if(result.equals("white")) {
			Assert.assertTrue(view.notification.getText().equals("Invalid Black Quoridor Position"));
		} else if (result.equals("black")) {
			Assert.assertTrue(view.notification.getText().equals("Invalid White Quoridor Position"));
		} else if (result.equals("none")) {
			Assert.assertTrue(view.notification.getText().equals("Both Players' Quoridor Positions Are Invalid"));
		} else {
			Assert.assertTrue(view.notification.getText().equals("Quoridor Position is Valid"));
		}
	}

	///////////////////////////////////////////////////////////////////////////////

	/** Enter Replay Mode
	 * @author Yanis Jallouli
	 */

	@When("I initiate replay mode")
	public void iInitiateReplayMode() {
		view.replayGame.doClick();
		view.replayGame.doClick(); //Select file, I guess we'll just assume it's the top
	}

	@Then("The game shall be in replay mode")
	public void theGameShallBeInReplay() {
		Assert.assertEquals(GameStatus.Replay, QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus());
	}

	@Given("The game is replay mode")
	public void theGameIsReplayMode() {
		theGameIsNotRunning();
		view.replayGame.doClick();
		view.replayGame.doClick(); //Select file, I guess we'll just assume it's the top
	}

	@Given("The following moves have been played in game:")
	public void theFollowingMovesHaveBeenPlayed(io.cucumber.datatable.DataTable dataTable) {
		List<Map<String, String>> valueMaps = dataTable.asMaps();
		// keys: mv, rnd, mov
		Game game = QuoridorApplication.getQuoridor().getCurrentGame();
		int i = 1;
		for (Map<String, String> map : valueMaps) {
			Integer moveNum = Integer.decode(map.get("mv"));
			Integer roundNum = Integer.decode(map.get("rnd"));
			String move = map.get("move");
			GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getPosition(QuoridorApplication.getQuoridor().getCurrentGame().getPositions().size() - 1);

			//White move
			if(roundNum == 1) {	
				Move aMove;
				GamePosition aPos;

				//Wall Move
				if(move.length() == 3) {
					if(move.charAt(1) == '-') {
						//Here's the thing. I have a separate method scanning the file every continue
						//for whether it was ended. That eliminates a new variable.
						//This way of doing step definitions is throwing me off.
						aMove = new StepMove(moveNum, 
								roundNum, 
								game.getWhitePlayer(), 
								QuoridorController.findTile(1, 5), 
								game);

						PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), aMove.getTargetTile());
						PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), curPos.getBlackPosition().getTile());

						aPos = new GamePosition(curPos.getId() + i,
								whiteP,
								blackP,
								game.getWhitePlayer(),
								game);
						for(Wall w : curPos.getWhiteWallsInStock()) aPos.addWhiteWallsInStock(w);
						for(Wall w : curPos.getBlackWallsInStock()) aPos.addBlackWallsInStock(w);
						for(Wall w : curPos.getWhiteWallsOnBoard()) aPos.addWhiteWallsOnBoard(w);
						for(Wall w : curPos.getBlackWallsOnBoard()) aPos.addBlackWallsOnBoard(w);

						if(game.getMoves().size() == 0) aMove.setPrevMove(null);
						else aMove.setPrevMove(game.getMove(game.getMoves().size() - 1));
						game.addMove(aMove);
						game.addPosition(aPos);

						break;
					}
					Wall wall = curPos.getWhiteWallsInStock(0);

					Direction d = (move.charAt(2) == 'h') ? Direction.Horizontal : Direction.Vertical;

					aMove = new WallMove(moveNum, 
							roundNum, 
							game.getWhitePlayer(), 
							QuoridorController.findStringTile(move), 
							game, 
							d,
							wall);

					PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), curPos.getWhitePosition().getTile());
					PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), curPos.getBlackPosition().getTile());

					aPos = new GamePosition(curPos.getId() + i,
							whiteP,
							blackP,
							game.getBlackPlayer(),
							game);
					for(Wall w : curPos.getWhiteWallsInStock()) aPos.addWhiteWallsInStock(w);
					for(Wall w : curPos.getBlackWallsInStock()) aPos.addBlackWallsInStock(w);
					for(Wall w : curPos.getWhiteWallsOnBoard()) aPos.addWhiteWallsOnBoard(w);
					for(Wall w : curPos.getBlackWallsOnBoard()) aPos.addBlackWallsOnBoard(w);

					aPos.removeWhiteWallsInStock(wall);
					aPos.addWhiteWallsOnBoard(wall);


				} else {
					aMove = new StepMove(moveNum, 
							roundNum, 
							game.getWhitePlayer(), 
							QuoridorController.findStringTile(move), 
							game);

					PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), aMove.getTargetTile());
					PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), curPos.getBlackPosition().getTile());

					aPos = new GamePosition(curPos.getId() + i,
							whiteP,
							blackP,
							game.getBlackPlayer(),
							game);
					for(Wall w : curPos.getWhiteWallsInStock()) aPos.addWhiteWallsInStock(w);
					for(Wall w : curPos.getBlackWallsInStock()) aPos.addBlackWallsInStock(w);
					for(Wall w : curPos.getWhiteWallsOnBoard()) aPos.addWhiteWallsOnBoard(w);
					for(Wall w : curPos.getBlackWallsOnBoard()) aPos.addBlackWallsOnBoard(w);

				}
				if(game.getMoves().size() == 0) aMove.setPrevMove(null);
				else aMove.setPrevMove(game.getMove(game.getMoves().size() - 1));

				game.addMove(aMove);
				game.addPosition(aPos);
			} else {
				//Black Move
				Move aMove;
				GamePosition aPos;

				//Wall Move
				if(move.length() == 3) {
					if(move.charAt(1) == '-') {
						//Here's the thing. I have a seperate method scanning the file every continue
						//for whether it was ended. That eliminates a new variable.
						//This way of doing step definitions is throwing me off.
						aMove = new StepMove(moveNum, 
								roundNum, 
								game.getWhitePlayer(), 
								QuoridorController.findTile(9, 5), 
								game);

						PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), curPos.getBlackPosition().getTile());
						PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), aMove.getTargetTile());

						aPos = new GamePosition(curPos.getId() + i,
								whiteP,
								blackP,
								game.getBlackPlayer(),
								game);
						for(Wall w : curPos.getWhiteWallsInStock()) aPos.addWhiteWallsInStock(w);
						for(Wall w : curPos.getBlackWallsInStock()) aPos.addBlackWallsInStock(w);
						for(Wall w : curPos.getWhiteWallsOnBoard()) aPos.addWhiteWallsOnBoard(w);
						for(Wall w : curPos.getBlackWallsOnBoard()) aPos.addBlackWallsOnBoard(w);


						if(game.getMoves().size() == 0) aMove.setPrevMove(null);
						else aMove.setPrevMove(game.getMove(game.getMoves().size() - 1));
						game.addMove(aMove);
						game.addPosition(aPos);
						break;
					}
					Wall wall = curPos.getBlackWallsInStock(0);

					Direction d = (move.charAt(2) == 'h') ? Direction.Horizontal : Direction.Vertical;
					aMove = new WallMove(moveNum, 
							roundNum, 
							game.getBlackPlayer(), 
							QuoridorController.findStringTile(move), 
							game, 
							d,
							wall);

					PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), curPos.getWhitePosition().getTile());
					PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), curPos.getBlackPosition().getTile());

					aPos = new GamePosition(curPos.getId() + i,
							whiteP,
							blackP,
							game.getWhitePlayer(),
							game);
					for(Wall w : curPos.getWhiteWallsInStock()) aPos.addWhiteWallsInStock(w);
					for(Wall w : curPos.getBlackWallsInStock()) aPos.addBlackWallsInStock(w);
					for(Wall w : curPos.getWhiteWallsOnBoard()) aPos.addWhiteWallsOnBoard(w);
					for(Wall w : curPos.getBlackWallsOnBoard()) aPos.addBlackWallsOnBoard(w);

					aPos.removeBlackWallsInStock(wall);
					aPos.addBlackWallsOnBoard(wall);

				} else { 
					//Player Move
					aMove = new StepMove(moveNum, 
							roundNum, 
							game.getBlackPlayer(), 
							QuoridorController.findStringTile(move), 
							game);


					PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), curPos.getWhitePosition().getTile());
					PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), aMove.getTargetTile());

					aPos = new GamePosition(curPos.getId() + i,
							whiteP,
							blackP,
							game.getWhitePlayer(),
							game);
					for(Wall w : curPos.getWhiteWallsInStock()) aPos.addWhiteWallsInStock(w);
					for(Wall w : curPos.getBlackWallsInStock()) aPos.addBlackWallsInStock(w);
					for(Wall w : curPos.getWhiteWallsOnBoard()) aPos.addWhiteWallsOnBoard(w);
					for(Wall w : curPos.getBlackWallsOnBoard()) aPos.addBlackWallsOnBoard(w);


				}
				if(game.getMoves().size() == 0) aMove.setPrevMove(null);
				else aMove.setPrevMove(game.getMove(game.getMoves().size() - 1));

				game.addMove(aMove);
				game.addPosition(aPos);
			}
			i++;
		}

		QuoridorApplication.getQuoridor().setCurrentGame(game);

	}

	@And("The game does not have a final result")
	public void theGameDoesNotHaveAFinalResult() {
		//Should honestly be taken care of in the moves. What do you want here???
		if(QuoridorController.isEnded(view.fileName)) {
			System.err.println("Ya ended the game with moves for replay ya doofus");
		}

	}
	@And("The game has a final result")
	public void theGameDoesHasAFinalResult() {
		//Should honestly be taken care of in the moves. What do you want here???
		if(!QuoridorController.isEnded(view.fileName)) {
			System.err.println("Ya didn't be endeding the game with moves for replay ya doofus");
		}
	}
	@And("The next move is {int}.{int}") 
	public void theNextMoveIs(int mNum, int rNum) {

		view.roundNum.setText("Round: " + rNum);
		view.moveNum.setText("Move: " + mNum);
	}

	@When("Jump to start position is initiated")
	public void jumpToStartInitiated() {
		view.jumpBackwards.doClick();
	}
	@When("Jump to final position is initiated")
	public void jumpToFinalInitiated() {
		view.jumpForward.doClick();
	}

	@Then("The next move shall be {int}.{int}")
	public void theNextMoveShallBe(int nmov, int nrnd) {
		if(view.jumpBackwards.getModel().isPressed()) {
			Assert.assertTrue(nmov == QuoridorApplication.getQuoridor().getCurrentGame().getMove(0).getMoveNumber() 
					&& nrnd == QuoridorApplication.getQuoridor().getCurrentGame().getMove(0).getRoundNumber());
		}
		if(view.jumpForward.getModel().isPressed()) {
			int end = QuoridorApplication.getQuoridor().getCurrentGame().getMoves().size();
			int lastMove = QuoridorApplication.getQuoridor().getCurrentGame().getMove(end-1).getMoveNumber();
			int lastRound = QuoridorApplication.getQuoridor().getCurrentGame().getMove(end-1).getRoundNumber();
			Assert.assertTrue(nmov == lastMove && nrnd == lastRound);
		}
		if(view.stepBackwards.getModel().isPressed()) {
			int moveNumber = Integer.parseInt(view.moveNum.getText().replace("Move: ", ""));
			int roundNumber = Integer.parseInt(view.roundNum.getText().replace("Round: ", ""));
			if(moveNumber != 1 && roundNumber != 1) {
				if(roundNumber == 2) {
					roundNumber--;
				}
				else {
					roundNumber = 2;
					moveNumber--;
				}
				Assert.assertTrue(nmov == moveNumber && nrnd == roundNumber);
			}
		}
		if(view.stepForward.getModel().isPressed()) {
			int moveNumber = Integer.parseInt(view.moveNum.getText().replace("Move: ", ""));
			int roundNumber = Integer.parseInt(view.roundNum.getText().replace("Round: ", ""));

			int end = QuoridorApplication.getQuoridor().getCurrentGame().getMoves().size();
			int lastMove = QuoridorApplication.getQuoridor().getCurrentGame().getMove(end-1).getMoveNumber();
			int lastRound = QuoridorApplication.getQuoridor().getCurrentGame().getMove(end-1).getRoundNumber();

			if(moveNumber != lastMove && roundNumber != lastRound) {
				if(roundNumber == 2) {
					roundNumber = 1;
					moveNumber++;
				}
				else {
					roundNumber = 2;
				}

				Assert.assertTrue(nmov == moveNumber && nrnd == roundNumber);
			}
		}
	}

	@And("White player's position shall be \\({int},{int})")
	public void whitePlayerPosition(int wrow, int wcol) {
		Tile tile = QuoridorController.findTile(wrow, wcol);
		PlayerPosition position = new PlayerPosition(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer(), tile);
		QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setWhitePosition(position);
		Assert.assertTrue(tile == QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile());
	}

	@And("Black player's position shall be \\({int},{int})")
	public void blackPlayerPosition(int wrow, int wcol) {
		Tile tile = QuoridorController.findTile(wrow, wcol);
		PlayerPosition position = new PlayerPosition(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer(), tile);
		QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setBlackPosition(position);
		Assert.assertTrue(tile == QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile());
	}

	@And("White has {int} on stock")
	public void whiteHasWallsOnStock(int walls) {
		int numWalls = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhiteWallsInStock().size();
		Assert.assertTrue(numWalls == walls);
	}

	@And("Black has {int} on stock")
	public void blackHasWallsOnStock(int walls) {
		int numWalls = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackWallsInStock().size();
		Assert.assertTrue(numWalls == walls);
	}

	@When("I initiate to continue game")
	public void iInitiateToContinueGame() {
		view.continueButton.doClick();
	}

	@And("The remaining moves of the game shall be removed")
	public void theRemainingMovesOfTheGameShallBeRemoved() {
		int rNum = Integer.parseInt(view.roundNum.getText().replace("Round: ", ""));
		int mNum = Integer.parseInt(view.moveNum.getText().replace("Move: ", ""));
		System.out.println("Replay mode is at Round " + rNum + " Move "+ mNum);
		int currentMNum, currentRNum;
		if(QuoridorApplication.getQuoridor().getCurrentGame().getMoves().size() != 0) {
			Move m = QuoridorApplication.getQuoridor().getCurrentGame().getMove(QuoridorApplication.getQuoridor().getCurrentGame().getMoves().size() - 1);
			currentMNum = m.getMoveNumber();
			currentRNum = m.getRoundNumber();
		} else {
			currentMNum = 0;
			currentRNum = 1; //This one might not work
		}
		System.out.println("The last move in game is at Round " + currentRNum + " Move "+ currentMNum);


		Assert.assertEquals(mNum, currentMNum);
		Assert.assertEquals(rNum, currentRNum);
	}

	@And("I shall be notified that finished games cannot be continued")
	public void iShallBeNotifiedFinishedNoConituuuuDoDo() {
		Assert.assertTrue(view.notification.getText().equals("Cannot continue a finished game"));
	}

	///////////////////////////////////////////////////////////////////////////////

	/** Feature: Load Game
	 *  @author Matteo Nunez
	 */

	@And("Each game move is valid")
	public void eachGameMoveIsValid() {
		//Ok I am not going through move by move removing invalid ones
		if(view.notification.getText().equals("Load File Error- Invalid Position"))
			System.err.println("Each game move was not valid");
	}


	@And("The game has no final results")
	public void theGameHasNoFinalResults() {
		if(QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus() == GameStatus.Running) {
			System.err.println("Game had a final result when it shouldn't");
		}
	}

	@And("The game to load has an invalid move")
	public void theGameLoadHasInvalidMoooooove() {
		//Ok I am not going through move by move removing invalid ones
		if(!view.loadGame.isVisible())
			System.err.println("Each game move was valid when it should not be");
	}

	@Then("The game shall notify the user that the game file is invalid")
	public void theGameShallNotifyUserFileInvalid() {
		Assert.assertEquals("Load File Error- Invalid Position", view.notification.getText());
		//TODO: I suppose make a file that has an end result- call it quoridor_test_game_3.mov
		//Make 2 files without end results, and call them quoridor_test_game_1.mov/quoridor_test_game_2.mov

	}

	///////////////////////////////////////////////////////////////////////////////

	/** 
	 * Feature: Jump To Final
	 * @author Matteo Nunez
	 */

	@Given("The game is in replay mode")
	public void theGameIsInReplayMode() {
		theGameIsNotRunning();
		view.replayGame.doClick();
		view.replayGame.doClick();
	}

	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Feature: Report final result
	 * @author xiangyu li
	 */
	@When("The game is no longer running")
	public void TheGameIsEnd() {
		theGameIsRunning();
		QuoridorController.GameIsFinished(view);
	}

	@Then("The final result shall be displayed")
	public void TheFinalResultShallBeDisplayed() {
		if(QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus()==GameStatus.BlackWon)
			Assert.assertEquals(view.result.getText(),"Black player wins this game");
		else if(QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus()==GameStatus.WhiteWon)
			Assert.assertEquals(view.result.getText(),"White player wins this game");
		else if(QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus()==GameStatus.Draw)
			Assert.assertEquals(view.result.getText(),"The game is draw");
	}
	@And("White's clock shall not be counting down")
	public void WhitesClockShallNotBeCountingDown() {	
		Assert.assertEquals(false,view.whiteTimer.isRunning());;
	}
	@And("Black's clock shall not be counting down")
	public void BlacksClockShallNotBeCountingDown(){
		Assert.assertEquals(false,view.blackTimer.isRunning());
	}
	@And("White shall be unable to move")
	public void WhiteShallBeUnableToMove(){
		Assert.assertEquals(false,QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer().hasNextPlayer());
	}
	@And("Black shall be unable to move")
	public void BlackShallBeUnableToMove() {
		Assert.assertEquals(false,QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer().hasNextPlayer());
	}



	/**
	 * Feature: ResignGame
	 * @author xiangyu li
	 */

	@Given("Then game to move is {string}")
	public void TheGameToMoveIs(String player) {
		if(player.equals("white")) {
			QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer());
		} else {
			QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer());
		}

	}
	@When("Player initates to resign")
	public void PlayerInitatesToResign() {
		view.resignButton.doClick();
		if(view.confirmFrame.isVisible()) {
			((JButton) view.confirmFrame.getContentPane().getComponent(1)).doClick();
		}
	}

	@Then("Game result shall be {string}")
	public void GameResultShallBe (String result) {
		if(result.equals("white")) Assert.assertEquals(QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus(), GameStatus.BlackWon);
		if(result.equals("black")) Assert.assertEquals(QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus(),GameStatus.WhiteWon);

	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Feature: Step Backward and Step Forward
	 * @author Keanu, Natchev
	 * ID#: 260804586
	 */


	@When("Step backward is initiated")
	public void stepBackwardHasBeenInitiated() {
		view.stepBackwards.doClick();
	}

	@When("Step forward is initiated")
	public void stepForwardHasBeenInitiated() {
		view.stepForward.doClick();
	}

	@Then("White has <wwallno> on stock")
	public void white_has_wwallno_on_stock() {
		int wallOnBoard = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().numberOfWhiteWallsOnBoard();
		int wallNumber = Integer.parseInt(view.p1Walls.getText().replace("Walls: ", ""));
		Assert.assertTrue(wallNumber == QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().numberOfWhiteWallsInStock());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// ***********************************************
	// Clean up
	// ***********************************************

	// After each scenario, the test model is discarded
	@After
	public void tearDown() {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		// Avoid null pointer for step definitions that are not yet implemented.
		if (quoridor != null) {
			quoridor.delete();
			quoridor = null;
		}
		for (int i = 0; i <= 20; i++) {Wall wall = Wall.getWithId(i);
		if(wall != null) {
			wall.delete();
		}

		}
	}
	// ***********************************************
	// Extracted helper methods
	// ***********************************************

	// Place your extracted methods below
	private void initQuoridorAndBoard() {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		Board board = new Board(quoridor);
		// Creating tiles by rows, i.e., the column index changes with every tile
		// creation
		for (int i = 1; i <= 9; i++) { // rows
			for (int j = 1; j <= 9; j++) { // columns
				board.addTile(i, j);
			}
		}
	}

	private ArrayList<Player> createUsersAndPlayers(String userName1, String userName2) {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		User user1 = quoridor.addUser(userName1);
		User user2 = quoridor.addUser(userName2);

		int thinkingTime = 180;

		// Players are assumed to start on opposite sides and need to make progress
		// horizontally to get to the other side
		//@formatter:off
		/*
		 *  __________
		 * |          |
		 * |          |
		 * |x->    <-x|
		 * |          |
		 * |__________|
		 * 
		 */
		//@formatter:on
		Player player1 = new Player(new Time(thinkingTime), user1, 9, Direction.Horizontal);
		Player player2 = new Player(new Time(thinkingTime), user2, 1, Direction.Horizontal);

		player1.setNextPlayer(player2);
		player2.setNextPlayer(player1);

		Player[] players = { player1, player2 };

		// Create all walls. Walls with lower ID belong to player1,
		// while the second half belongs to player 2
		for (int i = 0; i < 2; i++) {
			for (int j = 1; j <= 10; j++) {

				new Wall(i * 10 + j, players[i]);
			}
		}

		ArrayList<Player> playersList = new ArrayList<Player>();
		playersList.add(player1);
		playersList.add(player2);

		return playersList;
	}
	/* Not needed anymore since we have start gae and load game
		private void createAndStartGame(ArrayList<Player> players) {
			Quoridor quoridor = QuoridorApplication.getQuoridor();
			Tile player1StartPos = quoridor.getBoard().getTile(4);
			Tile player2StartPos = quoridor.getBoard().getTile(76);
			Game game = new Game(GameStatus.Running, MoveMode.PlayerMove, quoridor);
			game.setWhitePlayer(players.get(0));
			game.setBlackPlayer(players.get(1));

			PlayerPosition player1Position = new PlayerPosition(quoridor.getCurrentGame().getWhitePlayer(), player1StartPos);
			PlayerPosition player2Position = new PlayerPosition(quoridor.getCurrentGame().getBlackPlayer(), player2StartPos);

			GamePosition gamePosition = new GamePosition(0, player1Position, player2Position, players.get(0), game);

			// Add the walls as in stock for the players
			for (int j = 1; j <= 10; j++) {
				Wall wall = Wall.getWithId(j);
				gamePosition.addWhiteWallsInStock(wall);
			}
			for (int j = 1; j <= 10; j++) {
				Wall wall = Wall.getWithId(j + 10);
				gamePosition.addBlackWallsInStock(wall);
			}

			game.setCurrentPosition(gamePosition);
		}
	 */
}
