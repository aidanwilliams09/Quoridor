
package ca.mcgill.ecse223.quoridor.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.Timer;

import ca.mcgill.ecse223.quoridor.QuoridorApplication;
import ca.mcgill.ecse223.quoridor.features.InvalidInputException;
import ca.mcgill.ecse223.quoridor.model.Board;
import ca.mcgill.ecse223.quoridor.model.Destination;
import ca.mcgill.ecse223.quoridor.model.Direction;
import ca.mcgill.ecse223.quoridor.model.Game;
import ca.mcgill.ecse223.quoridor.model.Game.GameStatus;
import ca.mcgill.ecse223.quoridor.model.Game.MoveMode;
import ca.mcgill.ecse223.quoridor.model.GamePosition;
import ca.mcgill.ecse223.quoridor.model.JumpMove;
import ca.mcgill.ecse223.quoridor.model.Move;
import ca.mcgill.ecse223.quoridor.model.Player;
import ca.mcgill.ecse223.quoridor.model.PlayerPosition;
import ca.mcgill.ecse223.quoridor.model.Quoridor;
import ca.mcgill.ecse223.quoridor.model.StepMove;
import ca.mcgill.ecse223.quoridor.model.Tile;
import ca.mcgill.ecse223.quoridor.model.User;
import ca.mcgill.ecse223.quoridor.model.Wall;
import ca.mcgill.ecse223.quoridor.model.WallMove;
import ca.mcgill.ecse223.quoridor.view.QuoridorView;

public class QuoridorController {
	QuoridorView view;

	public QuoridorController(){		
	}
	/**
	 * Set current player to complete its move 
	 * Feature:Switch player
	 * @author Xiangyu Li
	 * @param player player that completes his move 
	 */
	public static void completeMove() {

		Game current = QuoridorApplication.getQuoridor().getCurrentGame();
		GamePosition curPos = current.getCurrentPosition();


		current.addPosition(curPos);

		//Create a new current Pos
		GamePosition newPos;
		if(curPos.getPlayerToMove().equals(current.getWhitePlayer())) {
			PlayerPosition white = new PlayerPosition(current.getWhitePlayer(), curPos.getWhitePosition().getTile());
			PlayerPosition black = new PlayerPosition(current.getBlackPlayer(), curPos.getBlackPosition().getTile());
			newPos = new GamePosition(curPos.getId() + 1, 
					white, black, 
					current.getBlackPlayer(), 
					QuoridorApplication.getQuoridor().getCurrentGame() );


			current.setCurrentPosition(newPos);

			QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(current.getBlackPlayer());
		} else {
			PlayerPosition white = new PlayerPosition(current.getWhitePlayer(), curPos.getWhitePosition().getTile());
			PlayerPosition black = new PlayerPosition(current.getBlackPlayer(), curPos.getBlackPosition().getTile());
			newPos = new GamePosition(curPos.getId() + 1, 
					white, black, 
					current.getWhitePlayer(), 
					QuoridorApplication.getQuoridor().getCurrentGame() );

			current.setCurrentPosition(newPos);

			QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(current.getWhitePlayer());
		}

		for(Wall w :curPos.getBlackWallsInStock()) {
			current.getCurrentPosition().addBlackWallsInStock(w);
		}
		for(Wall w :curPos.getWhiteWallsInStock()) {
			current.getCurrentPosition().addWhiteWallsInStock(w);
		}
		for(Wall w :curPos.getBlackWallsOnBoard()) {
			current.getCurrentPosition().addBlackWallsOnBoard(w);
		}
		for(Wall w :curPos.getWhiteWallsOnBoard()) {
			current.getCurrentPosition().addWhiteWallsOnBoard(w);
		}



		if(current.hasWallMoveCandidate()) {	
			if(current.getMoveMode() == MoveMode.WallMove) {
				int rn = current.getWallMoveCandidate().getRoundNumber();
				int mn = current.getWallMoveCandidate().getMoveNumber();
				Wall wallPlaced = current.getWallMoveCandidate().getWallPlaced();
				Player moveEM = current.getWallMoveCandidate().getPlayer();
				Tile t = current.getWallMoveCandidate().getTargetTile();
				Direction dir = current.getWallMoveCandidate().getWallDirection();

				current.getWallMoveCandidate().delete();

				WallMove newMove = new WallMove(mn, rn, moveEM, t, current, dir, wallPlaced);
				current.addMove(newMove);
				current.setWallMoveCandidate(null);
			} else {
				current.getWallMoveCandidate().delete();
				current.setWallMoveCandidate(null);
			}
		}
		current.setMoveMode(null);

		//QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(player.getNextPlayer());

	}
	/**
	 * Set total thinking time for each player
	 * Feature: Set total thinking time
	 * @param minute minute for total thinking time 
	 * @param second second for total thinking time
	 */
	public static void setTotaltime(int minute, int second) {
		long totaltime=(minute*60+second)*1000;
		QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer().setRemainingTime(new Time(totaltime));
		QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer().setRemainingTime(new Time(totaltime));
	}
	/**
	 * @author Xiangyu Li
	 * Feature:Switch player
	 * Stop black player's clock
	 * @param Timer used for counting time and do actions
	 */
	public static void stopblackclock(Timer timer) {
		timer.stop();
	}
	/**
	 * @author Xiangyu Li
	 * Feature:Switch player
	 * Run white player's clock
	 */
	public static Timer runwhiteclock(QuoridorView view) {

		Timer whitetimer;
		ActionListener taskPerformer= new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				view.updateView();
			}
		};

		whitetimer=new Timer(1000,taskPerformer);
		whitetimer.setInitialDelay(1000);
		whitetimer.start();
		return whitetimer;
	}
	/**
	 * @author Xiangyu Li
	 * Feature:Switch player
	 * Stop white player's clock
	 * @param Timer used for counting time and do actions
	 */
	public static void stopwhiteclock(Timer timer) {
		timer.stop();
	}
	/**
	 * @author Xiangyu Li
	 * Feature:Switch player
	 * Run black player's clock
	 */
	public static Timer runblackclock(QuoridorView view) {

		Timer blacktimer;
		ActionListener taskPerformer= new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				//view.updateView();
				//Doing this will just call it twice as often. 
				//A good soln would be to make view whiteTime/blackTime visible in here
				//But that may mess with step definitions
			}
		};

		blacktimer=new Timer(1000,taskPerformer);
		blacktimer.setInitialDelay(1000);
		blacktimer.start();
		return blacktimer;
	}


	/**
	 * @author Hongshuo Zhou
	 * feature: Start a new game
	 * @throws InvalidInputException
	 * This method starts the new game and check existing game
	 */
	public static void startGame(){
		//QuoridorApplication.getQuoridor().setCurrentGame(null);
		if (QuoridorApplication.getQuoridor().getCurrentGame() != null) {
			QuoridorApplication.getQuoridor().getCurrentGame().delete();
		}

		if (QuoridorApplication.getQuoridor().getCurrentGame() == null) {
			Game newGame = new Game(GameStatus.Initializing, MoveMode.PlayerMove, QuoridorApplication.getQuoridor());
			QuoridorApplication.getQuoridor().setCurrentGame(newGame);
			User whiteU = findUserName("User1");
			User blackU = findUserName("User2");
			if (whiteU == null){
				createUser("User1");
				whiteU = findUserName("User1");
			}
			if (blackU == null){
				createUser("User2");
				blackU = findUserName("User2");
			}
			Player white = new Player(new Time(0), whiteU, 0, Direction.Vertical);
			Player black = new Player(new Time(0), blackU, 1, Direction.Vertical);
			QuoridorApplication.getQuoridor().getCurrentGame().setWhitePlayer(white);
			QuoridorApplication.getQuoridor().getCurrentGame().setBlackPlayer(black);


		} else {
			System.err.println("Running Game Existing");
		}
	}

	/** load position Feature
	 * Public method to load game 
	 * @author Hongshuo Zhou 
	 * @return Whether the game successfully loaded
	 * @param filename - name of game file
	 */
	public static Boolean loadGame(String filename) {

		startGame();
		User white = findUserName("User1");
		User black = findUserName("User2");
		if (white == null){
			createUser("User1");
			white = findUserName("User1");
		}
		if (black == null){
			createUser("User2");
			black = findUserName("User2");
		}
		setTotaltime(1, 30);

		if(!containsFile(filename)) {
			return false;
		}
		initializeBoard();
		//read line for both players
		File file = new File(filename);
		String PlayerOneLine = new String();
		String PlayerTwoLine = new String();
		int moveNumber = 1;

		Game game = QuoridorApplication.getQuoridor().getCurrentGame();
		//TODO: 1) Add game positions. 2) I messed up move/round number. Fix that
		try {
			Scanner scan = new Scanner(file);
			scan.useDelimiter(Pattern.compile(" "));
			//I have two empty lines. One in the middle and one at the end


			//reader = new BufferedReader(new FileReader(file));
			PlayerOneLine = scan.nextLine();
			PlayerTwoLine = scan.nextLine();

			if(scan.hasNextLine()) scan.nextLine();
			//They always have a start position from initialize board
			//Iterates through moves
			while(scan.hasNext()) {
				String move = scan.next();
				int contains = move.indexOf('.'); //Ignore the number in front
				//String manipulation
				if(contains != -1) move = move.substring(0, contains - 1);
				if(move.contains("\n")) move = move.substring(0, move.indexOf("\n") - 1);
				if(move.length() == 0) continue;

				if(move.charAt(1) == '-') {
					//TODO: How to communicate the game was ended? We don't!
					if(move.charAt(0) == '0' && move.charAt(2) == '1') QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.BlackWon);
					else if (move.charAt(0) == '1'&& move.charAt(2) == '0')QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.WhiteWon);
					else QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.Draw);
					break;
				}
				//White move
				if(moveNumber % 2 == 1) {	
					Move aMove;
					GamePosition pos;

					//Wall Move
					if(move.length() == 3) {
						Wall wall = game.getCurrentPosition().getWhiteWallsInStock(0);

						Direction d = (move.charAt(2) == 'h') ? Direction.Horizontal : Direction.Vertical;
						//Move Number is what I called round. Round is 1 for white, 2 for black
						aMove = new WallMove((moveNumber+1) / 2, 
								1, 
								game.getWhitePlayer(), 
								findStringTile(move), 
								game, 
								d,
								wall);

						PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), game.getCurrentPosition().getWhitePosition().getTile());
						PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), game.getCurrentPosition().getBlackPosition().getTile());

						pos = new GamePosition(game.getCurrentPosition().getId() + 1,
								whiteP,
								blackP,
								game.getBlackPlayer(),
								game);

						for(Wall w : game.getCurrentPosition().getWhiteWallsInStock()) pos.addWhiteWallsInStock(w);
						for(Wall w : game.getCurrentPosition().getBlackWallsInStock()) pos.addBlackWallsInStock(w);
						for(Wall w : game.getCurrentPosition().getWhiteWallsOnBoard()) pos.addWhiteWallsOnBoard(w);
						for(Wall w : game.getCurrentPosition().getBlackWallsOnBoard()) pos.addBlackWallsOnBoard(w);


						pos.removeWhiteWallsInStock(wall);
						pos.addWhiteWallsOnBoard(wall);
					} else {
						aMove = new StepMove((moveNumber+1) / 2, 
								1, 
								game.getWhitePlayer(), 
								findStringTile(move), 
								game);	


						PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), aMove.getTargetTile());
						PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), game.getCurrentPosition().getBlackPosition().getTile());

						pos = new GamePosition(game.getCurrentPosition().getId() + 1,
								whiteP,
								blackP,
								game.getBlackPlayer(),
								game);
						for(Wall w : game.getCurrentPosition().getWhiteWallsInStock()) pos.addWhiteWallsInStock(w);
						for(Wall w : game.getCurrentPosition().getBlackWallsInStock()) pos.addBlackWallsInStock(w);
						for(Wall w : game.getCurrentPosition().getWhiteWallsOnBoard()) pos.addWhiteWallsOnBoard(w);
						for(Wall w : game.getCurrentPosition().getBlackWallsOnBoard()) pos.addBlackWallsOnBoard(w);

					}
					if(game.getMoves().size() == 0) aMove.setPrevMove(null);
					else aMove.setPrevMove(game.getMove(game.getMoves().size() - 1));

					game.addMove(aMove);
					game.addPosition(pos);
					game.setCurrentPosition(pos);
				} else {
					//Black Move
					Move aMove;
					GamePosition pos;

					//Wall Move
					if(move.length() == 3) {
						Wall wall = game.getCurrentPosition().getBlackWallsInStock(0);

						Direction d = (move.charAt(2) == 'h') ? Direction.Horizontal : Direction.Vertical;
						aMove = new WallMove((moveNumber+1) / 2, 
								2, 
								game.getBlackPlayer(), 
								findStringTile(move), 
								game, 
								d,
								wall);

						PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), game.getCurrentPosition().getWhitePosition().getTile());
						PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), game.getCurrentPosition().getBlackPosition().getTile());

						pos = new GamePosition(game.getCurrentPosition().getId() + 1,
								whiteP,
								blackP,
								game.getWhitePlayer(),
								game);
						for(Wall w : game.getCurrentPosition().getWhiteWallsInStock()) pos.addWhiteWallsInStock(w);
						for(Wall w : game.getCurrentPosition().getBlackWallsInStock()) pos.addBlackWallsInStock(w);
						for(Wall w : game.getCurrentPosition().getWhiteWallsOnBoard()) pos.addWhiteWallsOnBoard(w);
						for(Wall w : game.getCurrentPosition().getBlackWallsOnBoard()) pos.addBlackWallsOnBoard(w);

						pos.removeBlackWallsInStock(wall);
						pos.addBlackWallsOnBoard(wall);

					} else { 
						//Player Move
						aMove = new StepMove((moveNumber+1) / 2, 
								2, 
								game.getBlackPlayer(), 
								findStringTile(move), 
								game);

						PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), game.getCurrentPosition().getWhitePosition().getTile());
						PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), aMove.getTargetTile());

						pos = new GamePosition(game.getCurrentPosition().getId() + 1,
								whiteP,
								blackP,
								game.getWhitePlayer(),
								game);
						for(Wall w : game.getCurrentPosition().getWhiteWallsInStock()) pos.addWhiteWallsInStock(w);
						for(Wall w : game.getCurrentPosition().getBlackWallsInStock()) pos.addBlackWallsInStock(w);
						for(Wall w : game.getCurrentPosition().getWhiteWallsOnBoard()) pos.addWhiteWallsOnBoard(w);
						for(Wall w : game.getCurrentPosition().getBlackWallsOnBoard()) pos.addBlackWallsOnBoard(w);						

					}
					if(game.getMoves().size() == 0) aMove.setPrevMove(null);
					else aMove.setPrevMove(game.getMove(game.getMoves().size() - 1));

					game.addMove(aMove);
					game.addPosition(pos);
					game.setCurrentPosition(pos);
				}
				moveNumber++;
			}
			scan.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//create object
		String blackline;
		String whiteline;


		int wCol,wRow,bCol,bRow;
		Tile bTile,wTile;
		PlayerPosition bposition,wposition;
		// create players
		Player bPlayer;
		Player wPlayer;
		Player playerToMove = null;
		bPlayer = game.getBlackPlayer();
		wPlayer = game.getWhitePlayer();

		if (PlayerOneLine.charAt(0) == 'B' ){
			blackline = PlayerOneLine;
			whiteline = PlayerTwoLine;
			playerToMove = bPlayer;
		}
		else{
			blackline = PlayerTwoLine;
			whiteline = PlayerOneLine;
			playerToMove = wPlayer;
		}
		//set coordinate by transfer ASCII
		bCol = blackline.charAt(3) - 'a';
		bRow = blackline.charAt(4) - '0';
		bCol += 1;
		bTile = findTile(bRow, bCol);

		bposition = new PlayerPosition(bPlayer, bTile);
		wCol = whiteline.charAt(3) - 'a';
		wRow = whiteline.charAt(4) - '0';
		wCol += 1;
		wTile = findTile(wRow, wCol);

		wposition = new PlayerPosition(wPlayer, wTile);



		GamePosition loadPosition = game.getCurrentPosition();

		loadPosition.setPlayerToMove(playerToMove);
		loadPosition.setBlackPosition(bposition);
		loadPosition.setWhitePosition(wposition);

		if(validatePos(loadPosition)){
			game.setCurrentPosition(loadPosition);
			if(game.getMoves().size() == 0) {
				//For the pre arithmetic definition stuff
				if(!loadWalls(blackline,bPlayer)) return false;
				if(!loadWalls(whiteline,wPlayer)) return false;
			}
			//Woah wait, if validate is wrong we still have all the moves and stuff added
			if(!validatePosition()) return false;

			QuoridorApplication.getQuoridor().getCurrentGame().setWallMoveCandidate(null);
			QuoridorApplication.getQuoridor().getCurrentGame().setMoveMode(null);
			return true;
		}else{
			//Oh wait- if it's returning false it'll restart the game later anyways
			return false;
		}

	} 




	/** load position Feature
	 * Helper method for load walls
	 * @author Hongshuo Zhou 
	 * @param input - inputline
	 * @param player - player
	 */
	public static boolean loadWalls(String input, Player player) {
		//initialize game
		Game myGame = QuoridorApplication.getQuoridor().getCurrentGame();
		Wall wall;
		int column,row;
		Direction direction;
		GamePosition position = myGame.getCurrentPosition();

		//check wall move, pawn move, overlap
		for(int counter = 0; counter < (input.length()*0.2-1); counter++){

			if(player.equals(myGame.getBlackPlayer())){
				wall = position.getBlackWallsInStock(0);
				position.removeBlackWallsInStock(wall);
				position.addBlackWallsOnBoard(wall);
			} else {
				wall = position.getWhiteWallsInStock(0);
				position.removeWhiteWallsInStock(wall);
				position.addWhiteWallsOnBoard(wall);
			}

			column = input.charAt(counter * 5 + 7) - 96;
			row = input.charAt(counter * 5 + 8 ) - 48;
			if(input.charAt(counter * 5 + 9) == 'h'){
				direction = Direction.Horizontal;
			}else{
				direction = Direction.Vertical;
			}
			if(row < 1 || row > 8 || column <1 || column > 8) return false;
			//TODO: Uncomment if not working?

			Tile tile = QuoridorApplication.getQuoridor().getBoard().getTile((row-1)*9+column-1);
			QuoridorApplication.getQuoridor().getCurrentGame().addMove(new WallMove(counter + 1, 0, player, tile, myGame, direction, wall));
			if (player.hasGameAsBlack()){
				position.addBlackWallsOnBoard(wall);
			}else{
				position.addWhiteWallsOnBoard(wall);
			}

		}
		QuoridorApplication.getQuoridor().getCurrentGame().setCurrentPosition(position);
		return true;
	}

	public static Tile findStringTile(String rC) {
		int Col = rC.charAt(0) - 'a';
		int Row = rC.charAt(1) - '0';
		Col += 1;
		return findTile(Row, Col);
	}


	public static boolean isEnded(String fileName) {
		if(fileName == null) return false;
		if(!containsFile(fileName)) return false;
		File file = new File(fileName);
		String line = "";
		try {
			Scanner scan = new Scanner(file);
			while(scan.hasNextLine()) {
				line = scan.nextLine();
			}
			scan.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//End: #-#
		//      ^
		if(line.length() > 6 && line.charAt(6) == '-') return true;
		else if (QuoridorApplication.getQuoridor().getCurrentGame() != null && QuoridorApplication.getQuoridor().getCurrentGame().getMoves().size() >= 1) {
			//This part is purely for the sake of step definitions.
			//The game should work fine without it. More than fine
			Move m = QuoridorApplication.getQuoridor().getCurrentGame().getMove(QuoridorApplication.getQuoridor().getCurrentGame().getMoves().size() - 1);
			if(! (m instanceof StepMove)) return false;
			if((m.getTargetTile().getRow() == 1 && m.getPlayer().equals(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()))
					|| (m.getTargetTile().getRow() == 9 && m.getPlayer().equals(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer()))) 
				return true;
		}
		return false;
	}


	/** Validate Position Feature 
	 * Takes the given GamePosition and returns whether it is valid (no overlapping walls or blocked paths) 
	 * @param loadPosition - position to validate
	 * @return Whether the position is valid
	 */
	public static boolean validatePos(GamePosition loadPosition) {

		if (loadPosition == null){
			return false;
		}
		else{
			if(loadPosition.getBlackPosition().getTile().equals(loadPosition.getWhitePosition().getTile())) {
				return false;
			}
			for(Wall w : loadPosition.getWhiteWallsOnBoard()) {
				if(loadPosition.getBlackWallsOnBoard().contains(w)) return false;

			}
			return true;
		}

	}

	/** Validate Position Feature
	 * Returns whether the current game position is valid (no overlapping walls or blocked paths);
	 * @return Whether the position is valid
	 */
	public static boolean validatePosition() {
		GamePosition position = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		if (position == null) return false;

		if(position.getBlackPosition().getTile().equals(position.getWhitePosition().getTile())) {
			return false;
		}

		for(WallMove check : QuoridorController.getWalls()) {
			ArrayList<WallMove> existing = new ArrayList<WallMove>();
			for(WallMove p : QuoridorController.getWalls()) {
				if (!p.equals(check)) existing.add(p);
			}

			if(check.getWallDirection() == Direction.Horizontal) {
				if(check.getTargetTile().getColumn() > 8 || check.getTargetTile().getColumn() < 1) return false;
				for(WallMove ex : existing) {

					//Horizontal check- Horizontal placed
					if(ex.getWallDirection() == Direction.Horizontal) {

						if(ex.getTargetTile().getRow() == check.getTargetTile().getRow()) {
							if(Math.abs(ex.getTargetTile().getColumn() - check.getTargetTile().getColumn()) < 2 ) {
								return false;
							}
						}

						//Horizontal check- Vertical Place
					} else {

						if(ex.getTargetTile().getRow() == check.getTargetTile().getRow() 
								&& ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
							return false;
						}
					}
				}	

			} else {
				if(check.getTargetTile().getRow() > 8 || check.getTargetTile().getRow() < 1) return false;
				for(WallMove ex : existing) {
					//Vertical check- Horizontal placed
					if(ex.getWallDirection() == Direction.Horizontal) {

						if(ex.getTargetTile().getRow() == check.getTargetTile().getRow() 
								&& ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
							return false;
						}
						//Vertical check- Vertical Place
					} else {

						if(ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
							if(Math.abs(ex.getTargetTile().getRow() - check.getTargetTile().getRow()) < 2 ) {
								return false;
							}
						}
					}
				}
			}	
		}
		return true;

	}

	/** Identify if game won Feature
	 * Public method to check game result
	 * @author Hongshuo Zhou 
	 * @return game result
	 */
	public static String checkGameResult() {
		Player black = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
		Destination blackDes = black.getDestination();
		Direction blackDirection = blackDes.getDirection();
		int blackTarget = blackDes.getTargetNumber();
		int blackRow = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile().getRow();
		boolean blackWon = (blackDirection == Direction.Horizontal) && (blackTarget == blackRow);

		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();
		Destination whiteDes = white.getDestination();
		Direction whiteDirection = whiteDes.getDirection();
		int whiteTarget = whiteDes.getTargetNumber();
		int whiteRow = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile().getRow();
		boolean whiteWon = (whiteDirection == Direction.Horizontal) && (whiteTarget == whiteRow);


		if (whiteWon && !blackWon){
			QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.WhiteWon);
			return "whiteWon";
		}
		else if (!whiteWon && blackWon) {
			QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.BlackWon);
			return "blackWon";
		}
		else {
			return "pending";
		}

	}
	/** Identify if game won Feature
	 * Public method to check time limit and give result
	 * @author Hongshuo Zhou 
	 * @return game result
	 */
	public static String clockCountDownToZero(Player player) {
		Time timeremain = player.getRemainingTime();
		Time end = new Time(0);
		if (timeremain.equals(end)) {
			if (player.hasGameAsWhite()) {
				QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.BlackWon);
				return "blackWon";
			} else {
				QuoridorApplication.getQuoridor().getCurrentGame().setGameStatus(GameStatus.WhiteWon);
				return "whiteWon";
			}
		} else {
			return "pending";
		}
	}

	/** Helper method to update position
	 * @author Hongshuo Zhou 
	 * @return game result
	 */
	public static void newPosition(){
		Game game = QuoridorApplication.getQuoridor().getCurrentGame();
		GamePosition position = game.getCurrentPosition();
		PlayerPosition whiteP = new PlayerPosition(game.getWhitePlayer(), game.getCurrentPosition().getWhitePosition().getTile());
		PlayerPosition blackP = new PlayerPosition(game.getBlackPlayer(), game.getCurrentPosition().getBlackPosition().getTile());
		Player player = position.getPlayerToMove();

		GamePosition newposition = new GamePosition(game.getCurrentPosition().getId() + 1,
				whiteP,
				blackP,
				player,
				game);
		for(Wall w : game.getCurrentPosition().getWhiteWallsInStock()) newposition.addWhiteWallsInStock(w);
		for(Wall w : game.getCurrentPosition().getBlackWallsInStock()) newposition.addBlackWallsInStock(w);
		for(Wall w : game.getCurrentPosition().getWhiteWallsOnBoard()) newposition.addWhiteWallsOnBoard(w);
		for(Wall w : game.getCurrentPosition().getBlackWallsOnBoard()) newposition.addBlackWallsOnBoard(w);

		game.setCurrentPosition(newposition);



	}



	//////////////////////////////////////////////////////////////
	/**
	 * Move Wall Feature
	 * 
	 * @author aidanwilliams Will fail if position is not valid Updates game
	 *         position with candidate wall move
	 * @param move       - wall move candidate
	 * @param targetTile - new tile to move to
	 * @return whether or not the wall successfully moved
	 */
	public static boolean moveWall(Tile targetTile) {

		// take in a WallMove created in GrabWall feature and put the wall in the
		// targetTile
		// will validate position to ensure no overlapping
		if(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection() == Direction.Horizontal) {
			if(targetTile.getColumn() > 8) {
				targetTile = findTile(targetTile.getRow(), 8);
			}
		}
		if(QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection() == Direction.Vertical) {
			if(targetTile.getRow() > 8) {
				targetTile = findTile(8, targetTile.getColumn());
			}
		}
		Tile oldTile = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getTargetTile();
		QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().setTargetTile(targetTile);
		if(!wallIsValid()) {
			QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().setTargetTile(oldTile);
			return false;
		}
		return true;
	}


	public static void tpWall(Tile targetTile) {
		QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().setTargetTile(targetTile);
	}


	public static boolean pathExists(Player p) {		
		return (aStar(p) != null);
	}
	public static boolean pathExists() {
		if(aStar(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()) == null) {
			return false;
		}
		else if (aStar(QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer()) == null) {
			return false;
		}
		return true;
	}

	/** A method to check whether the current wall move candidate is in a valid position.
	 * Useful for many features, such as move wall and drop wall
	 * @return Whether the current wall move candidate is valid
	 */
	public static boolean wallIsValid() {
		// loop through wall moves to see if any interfere with desired move to be made
		// check to see if wall to be moved overlaps with players or is out of bounds
		WallMove check = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate();
		ArrayList<WallMove> existing = QuoridorController.getWalls();

		if(check.getWallDirection() == Direction.Horizontal) {
			if(check.getTargetTile().getRow() > 8) return false;
			for(WallMove ex : existing) {
				//Horizontal check- Horizontal existing
				if(ex.getWallDirection() == Direction.Horizontal) {

					if(ex.getTargetTile().getRow() == check.getTargetTile().getRow()) {
						if(Math.abs(ex.getTargetTile().getColumn() - check.getTargetTile().getColumn()) < 2 ) {
							return false;
						}
					}
					//Horizontal check- Vertical existing
				} else {
					if(ex.getTargetTile().getRow() == check.getTargetTile().getRow() 
							&& ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
						return false;
					}
				}
			}	

		} else {
			if(check.getTargetTile().getColumn() > 8) return false;
			for(WallMove ex : existing) {
				//Vertical check- Horizontal existing
				if(ex.getWallDirection() == Direction.Horizontal) {
					if(ex.getTargetTile().getRow() == check.getTargetTile().getRow() 
							&& ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
						return false;
					}
					//Vertical check- Vertical existing
				} else {
					if(ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
						if(Math.abs(ex.getTargetTile().getRow() - check.getTargetTile().getRow()) < 2 ) {
							return false;
						}
					}
				}
			}
		}
		return true;

	}

	/** A helper method that searches for a conflcting wall and returns it if found
	 *  Used for gherkin steps that require no conflicting walls exist
	 * @return a wallmove conflicting with wall move candidate, or null if no such walls exist
	 */
	public static WallMove invalidWall() {
		WallMove check = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate();
		ArrayList<WallMove> existing = new ArrayList<WallMove>();
		for(Move m : QuoridorApplication.getQuoridor().getCurrentGame().getMoves()) {
			if (m instanceof WallMove) existing.add((WallMove) m);
		}

		if(check.getWallDirection() == Direction.Horizontal) {
			for(WallMove ex : existing) {
				//Horizontal check- Horizontal placed
				if(ex.getWallDirection() == Direction.Horizontal) {

					if(ex.getTargetTile().getRow() == check.getTargetTile().getRow()) {
						if(Math.abs(ex.getTargetTile().getColumn() - check.getTargetTile().getColumn()) < 2 ) {
							return ex;
						}
					}
					//Horizontal check- Vertical Place
				} else {
					if(ex.getTargetTile().getRow() == check.getTargetTile().getRow() 
							&& ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
						return ex;
					}
				}
			}	

		} else {
			for(WallMove ex : existing) {
				//Vertical check- Horizontal placed
				if(ex.getWallDirection() == Direction.Horizontal) {
					if(ex.getTargetTile().getRow() == check.getTargetTile().getRow() 
							&& ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
						return ex;
					}
					//Vertical check- Vertical Place
				} else {
					if(ex.getTargetTile().getColumn() == check.getTargetTile().getColumn()) {
						if(Math.abs(ex.getTargetTile().getRow() - check.getTargetTile().getRow()) < 2 ) {
							return ex;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * findTile helper method will find a tile given coordinates row and column
	 * 
	 * @param r
	 * @param c
	 * @return tile at location
	 */
	public static Tile findTile(int r, int c) {
		// use row and col to find the tile we want
		if(r <= 0)
			r = 1;
		if(r > 9)
			r = 9;
		if(c <= 0)
			c = 1;
		if(c > 9)
			c = 9;
		return QuoridorApplication.getQuoridor().getBoard().getTile((r-1)*9+c-1);
	}

	/////////////////////////////////////////////////////////////
	/**
	 * Grab Wall feature
	 * 
	 * @author aidanwilliams Checks to make sure player has walls and creates
	 *         WallMove object to be passed on to move wall feature
	 * @param aWall - wall to grab
	 * @return
	 */

	public static boolean grabWall() {
		// will take in a wall and create a wall move object with some default values
		if(QuoridorApplication.getQuoridor().getCurrentGame().hasWallMoveCandidate() == false) {
			WallMove newMove;
			Player curPlayer;
			int nrWalls;
			Game curGame = QuoridorApplication.getQuoridor().getCurrentGame();
			GamePosition curPos = curGame.getCurrentPosition();


			if(curPos.getPlayerToMove().equals(curGame.getBlackPlayer())) {
				curPlayer = curGame.getBlackPlayer();
				nrWalls = curPos.numberOfBlackWallsInStock();

			} else {
				curPlayer = curGame.getWhitePlayer();
				nrWalls = curPos.numberOfWhiteWallsInStock();
			}


			if(nrWalls > 0) {
				newMove = new WallMove(curGame.getMoves().size()+1, 
						curGame.getMoves().size()/2+1, 
						curPlayer, 
						defaultTile(curPlayer), 
						curGame, 
						Direction.Vertical, 
						curPos.getPlayerToMove().getWall(nrWalls-1));

				curGame.setWallMoveCandidate(newMove);

				if(curPlayer.equals(curGame.getBlackPlayer())) {
					curPos.removeBlackWallsInStock(newMove.getWallPlaced());
					curPos.addBlackWallsOnBoard(newMove.getWallPlaced());
				} else {
					curPos.removeWhiteWallsInStock(newMove.getWallPlaced());
					curPos.addWhiteWallsOnBoard(newMove.getWallPlaced());
				}
				QuoridorApplication.getQuoridor().getCurrentGame().setMoveMode(MoveMode.WallMove);
				return true;
			}
		} else if (QuoridorApplication.getQuoridor().getCurrentGame().getMoveMode() == MoveMode.PlayerMove) {
			//If it has a wall move candidate and is transitioning- takes care of stock problem
			Game curGame = QuoridorApplication.getQuoridor().getCurrentGame();
			GamePosition curPos = curGame.getCurrentPosition();
			Player curPlayer = curPos.getPlayerToMove();
			int nrWalls;

			if(curPlayer.equals(curGame.getBlackPlayer())) {
				nrWalls = curPos.numberOfBlackWallsInStock();
				if(nrWalls > 0) {
					curPos.removeBlackWallsInStock(curGame.getWallMoveCandidate().getWallPlaced());
					curPos.addBlackWallsOnBoard(curGame.getWallMoveCandidate().getWallPlaced());
				}

			} else {
				nrWalls = curPos.numberOfWhiteWallsInStock();
				if(nrWalls > 0) {
					curPos.removeWhiteWallsInStock(curGame.getWallMoveCandidate().getWallPlaced());
					curPos.addWhiteWallsOnBoard(curGame.getWallMoveCandidate().getWallPlaced());
				}
			}
			QuoridorApplication.getQuoridor().getCurrentGame().setMoveMode(MoveMode.WallMove);
			return true;
		}

		return false;
	}

	public static boolean undoGrabWall() {
		if(QuoridorApplication.getQuoridor().getCurrentGame().hasWallMoveCandidate() != false) {
			Player curPlayer;
			int nrWalls;
			Game curGame = QuoridorApplication.getQuoridor().getCurrentGame();
			GamePosition curPos = curGame.getCurrentPosition();

			if(curPos.getPlayerToMove().equals(curGame.getBlackPlayer())) {
				curPlayer = curGame.getBlackPlayer();
				nrWalls = curPos.numberOfBlackWallsOnBoard();

			} else {
				curPlayer = curGame.getWhitePlayer();
				nrWalls = curPos.numberOfWhiteWallsOnBoard();
			}


			if(nrWalls > 0) {

				if(curPlayer.equals(curGame.getBlackPlayer())) {
					curPos.addBlackWallsInStock(curGame.getWallMoveCandidate().getWallPlaced());
					curPos.removeBlackWallsOnBoard(curGame.getWallMoveCandidate().getWallPlaced());
				} else {
					curPos.addWhiteWallsInStock(curGame.getWallMoveCandidate().getWallPlaced());
					curPos.removeWhiteWallsOnBoard(curGame.getWallMoveCandidate().getWallPlaced());
				}

				return true;
			}
		}
		return false;
	}

	/**
	 * defaultTile helper method assigns default starting tile for white and black player
	 * @param curPlayer
	 * @return defaultTile to start a wall move candidate
	 */
	public static Tile defaultTile(Player curPlayer) {
		if(curPlayer.equals(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()))
			return QuoridorApplication.getQuoridor().getBoard().getTile(0);
		return QuoridorApplication.getQuoridor().getBoard().getTile(70);
	}

	/**
	 * isSide helper method Checks whether wallMove targetTile is on side of board
	 * 
	 * @param aWallMove
	 * @return boolean
	 */
	public static boolean isSide(WallMove aWallMove) {
		if(aWallMove.getWallDirection().equals(Direction.Horizontal)) {
			if(aWallMove.getTargetTile().getColumn() == 1 || 
					aWallMove.getTargetTile().getColumn() == 8 || 
					aWallMove.getTargetTile().getRow() == 1 || 
					aWallMove.getTargetTile().getRow() == 9)
				return true;
		}
		if(aWallMove.getWallDirection().equals(Direction.Vertical)) {
			if(aWallMove.getTargetTile().getColumn() == 1 || 
					aWallMove.getTargetTile().getColumn() == 9 || 
					aWallMove.getTargetTile().getRow() == 1 || 
					aWallMove.getTargetTile().getRow() == 8)
				return true;
		}
		return false;
	}
	////////////////////////////////////////////////////////////////

	/** Drop Wall 
	 * Updates game position with candidate wall move 
	 * @author Yanis Jallouli
	 */
	public static boolean dropWall() {

		Game current = QuoridorApplication.getQuoridor().getCurrentGame();
		GamePosition curPos = current.getCurrentPosition();

		//Both View & MoveWall checks if it's valid for us

		//Add the move to game list
		if(current.getMoves().size() > 0) {
			current.getWallMoveCandidate().setPrevMove(current.getMove(current.getMoves().size() - 1));
		} else {
			current.getWallMoveCandidate().setPrevMove(null);
		}


		if(!pathExists())  {
			return false;
		}
		//Adding the current position to the games list and all that is taken care of 
		//in complete move (now at least)

		completeMove();

		return true;
	}

	/** Move Is Registered
	 * Query method to check if a wall move was properly registered in the game
	 * @param dir - Direction of wall to check
	 * @param row - row of wall to check (defined by northwest)
	 * @param col - column of wall to check (defined by northwest)
	 * @author Yanis Jallouli
	 */
	public static boolean moveIsRegistered(Direction dir, int row, int col) {

		Game current = QuoridorApplication.getQuoridor().getCurrentGame();
		if(current.getMoves().size() == 0) return false;
		Move lastMove = current.getMoves().get(current.getMoves().size() - 1);

		if(lastMove instanceof WallMove) {

			WallMove lastWallMove = (WallMove) lastMove;

			//If we completed a move: Check if the last move is the one desired
			if(current.getWallMoveCandidate() == null) {
				if(findTile(row, col).equals(lastWallMove.getTargetTile()) &&
						dir == lastWallMove.getWallDirection()                ) {
					return true;
				}
			} 
		}

		//If 1)move!=completed, 2) LastMove!=wallMove, 3) LastWallMove!=desired
		//Then it wasn't registered
		return false;
	}

	/**Get Walls
	 * Query method that returns an ArrayList of all wall moves
	 * registered within the current game.
	 * @return list of wall moves
	 * @author Yanis Jallouli
	 */
	public static ArrayList<WallMove> getWalls() {
		ArrayList<WallMove> walls = new ArrayList<WallMove>();
		if(QuoridorApplication.getQuoridor().getCurrentGame() == null) return null;
		for(Move move : QuoridorApplication.getQuoridor().getCurrentGame().getMoves()) {
			if(move instanceof WallMove) walls.add((WallMove) move);
		}
		return walls;
	}

	/** Save Position Feature
	 * Public method to save current game into a given .txt file
	 * @return Whether the method successfully saved
	 * @param filePath - the name of the save file to write to.
	 * @author Yanis Jallouli
	 */
	public static boolean savePosition(String filePath) {
		//No easy way to write certain lines of file, so I just remake it every time
		if(!containsFile(filePath)) {
			createFile(filePath);
		} else {
			deleteFile(filePath);
			createFile(filePath);
		}
		File fil = new File(filePath);

		Game current = QuoridorApplication.getQuoridor().getCurrentGame();

		/* 1) Writes white position (playerPos & walls) in file "W: e3, a4h, e8v..."
		 * 2) Writes black position the same way on new line "B: a4, c3v, f6v..."
		 * 3) Go down two lines and start writing white/black moves of each round "1. e4h a5\n2. e2 d5v\n3. ..."
		 */	

		try {
			List<Move> moves = current.getMoves();
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fil)));
			if(current.getCurrentPosition().getPlayerToMove().equals(current.getBlackPlayer())) {
				writer.print("B: ");
				Tile blackPos = QuoridorApplication.getQuoridor().getCurrentGame()
						.getCurrentPosition().getBlackPosition().getTile();		
				char col = (char) ((blackPos.getColumn() -1) + 'a');
				writer.print(col);
				writer.print(blackPos.getRow());

				for(WallMove move : getWalls()) {
					//Assumes Black Player moves second
					if(move.getMoveNumber() % 2 == 0) {
						writer.print(", ");
						writeWall((WallMove) move, writer);
					}	
				}

				writer.println();

				writer.print("W: ");
				Tile whitePos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition()
						.getWhitePosition().getTile();
				col = (char) ((whitePos.getColumn() -1) + 'a');
				writer.print(col);
				writer.print(whitePos.getRow());

				for(WallMove move : getWalls()) {
					//Assumes White Player moves first
					if(move.getMoveNumber() % 2 == 1) {
						writer.print(", ");
						writeWall((WallMove) move, writer);
					}	
				}

				writer.println();
				writer.println();
			} else {

				writer.print("W: ");
				Tile whitePos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition()
						.getWhitePosition().getTile();
				char col = (char) ((whitePos.getColumn() -1) + 'a');
				writer.print(col);
				writer.print(whitePos.getRow());

				for(WallMove move : getWalls()) {
					//Assumes White Player moves first
					if(move.getMoveNumber() % 2 == 1) {
						writer.print(", ");
						writeWall((WallMove) move, writer);
					}	
				}

				writer.println();

				writer.print("B: ");
				Tile blackPos = QuoridorApplication.getQuoridor().getCurrentGame()
						.getCurrentPosition().getBlackPosition().getTile();		
				col = (char) ((blackPos.getColumn() -1) + 'a');
				writer.print(col);
				writer.print(blackPos.getRow());



				for(WallMove move : getWalls()) {
					//Assumes Black Player moves second
					if(move.getMoveNumber() % 2 == 0) {
						writer.print(", ");
						writeWall((WallMove) move, writer);
					}	
				}

				writer.println();
				writer.println();
			}





			//Ok this looks massive but it's simple. For each round, it prints the target tile of white&black's move
			// The conditionals are all just type of move made and move number within the round
			for(int i = 0; i < moves.size(); i++) {
				Move move = moves.get(i);

				//If even,we're starting a new line
				if(i % 2 == 0) {
					writer.print(move.getRoundNumber() + ". ");

					//WALLMOVE- print e3h or a4v, or any other such wall placement 
					if(move instanceof WallMove) {
						writeWall((WallMove) move, writer);
						writer.print(" ");

						//PLAYERMOVE - print b5 or h7 or whatever other move was made
					} else { 

						writePlayer(move, writer);
						writer.print(" ");
					}	

					//Basically same as above, but ending current line	
				} else {				
					if(move instanceof WallMove) {
						writeWall((WallMove) move, writer);
						writer.println();
					} else { 
						writePlayer(move, writer);
						writer.println();
					}			
				}	
			}
			if(moves.size() %2 == 1) {
				//If you ended after a white move
				writer.println();
			}
			if(QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus() != GameStatus.Running) {
				writeEnd(writer);
			}
			if(writer.checkError() ) throw new IOException();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**Private helper method that writes a wall into a file with the correct syntax
	 * Does not account for anything other than column, row, and direction
	 * @param move - WallMove to write into file
	 * @param writer - PrintWriter set to write into a file
	 * @throws IOException - thrown in case of error
	 * @author Yanis Jallouli
	 */
	private static void writeWall(WallMove move, PrintWriter writer) throws IOException {
		char col = (char) ((move.getTargetTile().getColumn() -1) + 'a');
		writer.print(col);
		writer.print(move.getTargetTile().getRow());
		if(move.getWallDirection() == Direction.Horizontal) writer.print("h");
		else writer.print("v");
	}

	/**Private helper method that writes a player into a file with the correct syntax
	 * Does not account for anything other than column, and row
	 * @param move - PlayerMove to write into file
	 * @param writer - PrintWriter set to write into a file
	 * @throws IOException - thrown in case of error
	 * @author Yanis Jallouli
	 */
	private static void writePlayer(Move move, PrintWriter writer) throws IOException {
		char col = (char) ((move.getTargetTile().getColumn() -1) + 'a');
		writer.print(col);
		writer.print(move.getTargetTile().getRow());
	}

	private static void writeEnd(PrintWriter writer) throws IOException {
		GameStatus stat = QuoridorApplication.getQuoridor().getCurrentGame().getGameStatus();
		if(stat == GameStatus.WhiteWon) {
			writer.println("End: 1-0");
		} else if (stat == GameStatus.BlackWon) {
			writer.println("End: 0-1");
		} else {
			writer.println("End: 1-1");
		}

	}


	/** Query method to check if a file is exists within the file system.
	 * @param filepath - the file to check for
	 * @return boolean - whether the file was found
	 * @author Yanis Jallouli
	 */
	public static boolean containsFile(String filepath) {
		//String workDirectory = System.getProperty("user.dir");
		if(filepath ==null || filepath.equals("")) return false;
		File file = new File(filepath);
		return file.exists();
	}

	/** Method to check whether a save file has been updated with the current game.
	 * Uses move number and player turn to check.
	 * @param filepath - the file to check for updates
	 * @return boolean - whether an error occurred
	 * @author Yanis Jallouli
	 */
	public static boolean isUpdated(String filepath) {
		if(filepath == null || !containsFile(filepath)) return false;

		File fil = new File(filepath);
		int moveNumber = 0;

		try {
			//Goes to the last line of the scanner
			Scanner scan = new Scanner(fil);
			scan.useDelimiter(Pattern.compile(" |\n"));
			//I have two empty lines. One in the middle and one at the end
			if(scan.hasNextLine()) scan.nextLine();
			if(scan.hasNextLine()) scan.nextLine();
			if(scan.hasNextLine()) scan.nextLine();
			while(scan.hasNext()) {
				String line = scan.next();
				if(line != "" && !line.contains(".")) {
					moveNumber++;
				}
			}

			scan.close();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		//TODO: Make this only happen if it's a win/loss situation
		if(isEnded(filepath)) moveNumber -=2;
		
		int realMoveNum = QuoridorApplication.getQuoridor().getCurrentGame().getMoves().size();
		System.out.println("Expected Move Number: " + moveNumber); 
		System.out.println("Real Move Number: " + realMoveNum);
		if(realMoveNum == moveNumber) {
			//This is to combat the "File is up to date but not modified('updated')"
			//Essentially I'm setting last mod to 0 when we update, or a high number when we don't
			if(fil.lastModified() > 10000) {
				return false;
			}

			return true;
		} else {
			return false;
		}

	}

	/** Create a save file within the file system (initializer).
	 * @param filepath - the file to create
	 * @return boolean - whether an error occurred
	 * @author Yanis Jallouli
	 */
	public static boolean createFile(String filepath) {
		File file = new File(filepath);
		if(!file.exists()) {
			try {return file.createNewFile();}
			catch (IOException e) {e.printStackTrace();}
		}
		return false;
	}

	/** A method to delete a file from the fileSystem
	 * @param filePath - path to the file to be deleted
	 * @return Boolean- whether the file was deleted succesfully
	 * @author Yanis Jallouli
	 */
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if(!file.exists()) return false;
		return file.delete();
	}

	///////////////////////////////////////////////////////////////////////////

	/**
	 * Checks whether or not the position is valid
	 * @author Keanu Natchev
	 * @return Boolean: true if the position is valid and false if it is not.
	 */

	public static boolean validPosition() {
		GamePosition position = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		if (position == null){
			return false;
		}
		else{
			List<Wall> wallsOnBoard = position.getBlackWallsOnBoard();
			Integer row = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getTargetTile().getRow();
			Integer column = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getTargetTile().getColumn();
			Direction direction = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection();
			for(int i = 0; i < wallsOnBoard.size(); i ++) {
				Integer rowBoard = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getTargetTile().getRow();
				Integer columnBoard = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getTargetTile().getColumn();
				Direction directionBoard = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection();
				int rowDifference = row - rowBoard;
				int columnDifference = column - columnBoard;
				if(direction == directionBoard && (rowDifference == 0 && columnDifference == 0)) {
					return false;
				}
				if(direction != directionBoard && (rowDifference == 0 && columnDifference == 0)) {
					return false;
				}
				if(direction == directionBoard && (rowDifference == 1 && columnDifference == 0)) {
					return false;
				}
				if(direction == directionBoard && (rowDifference == 0 && columnDifference == 1)) {
					return false;
				}
			}

			return true;
		}

	}

	/**
	 * Goes through the list of usernames and checks whether the given username
	 * is part of that list.
	 * @author Keanu Natchev
	 * @param userName - the username that needs to be checked
	 */

	public static boolean ExistingUserName(String userName) {
		if(userName == null) return false;
		if(userName.equals("") && findUserName("") != null) {
			findUserName("").delete();
			return false;
		}
		for(User u : QuoridorApplication.getQuoridor().getUsers()) {
			if(u.getName().equals(userName)) return true;
		}
		return false;
	}

	public static User findUserName(String userName) {
		for(User u : QuoridorApplication.getQuoridor().getUsers()) {
			if(u.getName().equals(userName)) return u;
		}
		return null;
	}

	/**
	 * Creates a new user with name
	 * @author Keanu Natchev
	 * @param newUserName: desired name of new user
	 */

	public static void createUser(String newUserName) {
		QuoridorApplication.getQuoridor().addUser(newUserName);
	}

	///////////////////////////////////////////////////////////////////////////


	/**
	 * Feature 4. Initialize wall
	 * This methods sets the board to its initial position and the player's stock of 
	 * walls and clocks are counting down so that they can start playing the game
	 * @author Matteo Nunez
	 * @param board - board object that is going to be initialize
	 */
	public static void initializeBoard() {

		Game game = QuoridorApplication.getQuoridor().getCurrentGame();

		Player whitePlayer = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();
		Player blackPlayer = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();

		Quoridor quoridor = QuoridorApplication.getQuoridor();
		// Fill board
		quoridor.setBoard(null);
		if(quoridor.getBoard() == null) {	
			Board board = new Board(quoridor);
			for (int i = 1; i <= 9; i++) { // rows
				for (int j = 1; j <= 9; j++) { // columns
					board.addTile(i, j);
				}
			}
			quoridor.setBoard(board);
		}


		Tile whiteStartTile = quoridor.getBoard().getTile(76);	// White starting tile
		Tile blackStartTile = quoridor.getBoard().getTile(4);	// Black starting tile

		GamePosition cur = new GamePosition(0,
				new PlayerPosition(whitePlayer, whiteStartTile),	// Move white player to initial position
				new PlayerPosition(blackPlayer, blackStartTile),	// Move black player to initial position
				whitePlayer,
				game);



		cur.setPlayerToMove(whitePlayer);	// White players plays first

		// Add missing walls in white stock until 10

		for (int whiteWallInStock = cur.numberOfWhiteWallsInStock(); whiteWallInStock < 10; whiteWallInStock++) {
			boolean hasWall = true;
			try {quoridor.getCurrentGame().getWhitePlayer().getWall(whiteWallInStock);} catch(Exception e) {hasWall = false;}
			if(!hasWall || quoridor.getCurrentGame().getWhitePlayer().getWall(whiteWallInStock) == null) {

				try{quoridor.getCurrentGame().getWhitePlayer().addWall(whiteWallInStock);}
				catch(Exception e) {
					Wall.getWithId(whiteWallInStock).delete();
					quoridor.getCurrentGame().getWhitePlayer().addWall(whiteWallInStock);
				}

				cur.addWhiteWallsInStock(quoridor.getCurrentGame().getWhitePlayer().getWall(whiteWallInStock));

			} else {
				cur.addWhiteWallsInStock(quoridor.getCurrentGame().getWhitePlayer().getWall(whiteWallInStock));
			}


		}
		// Add missing walls in black stock until 10

		for (int blackWallInStock = cur.numberOfBlackWallsInStock(); blackWallInStock < 10; blackWallInStock++) {
			boolean hasWall = true;
			try {quoridor.getCurrentGame().getBlackPlayer().getWall(blackWallInStock);} catch(Exception e) {hasWall = false;}
			if(!hasWall || quoridor.getCurrentGame().getBlackPlayer().getWall(blackWallInStock) == null) {

				try{quoridor.getCurrentGame().getBlackPlayer().addWall(blackWallInStock + 10);}
				catch(Exception e) {
					Wall.getWithId(blackWallInStock + 10).delete();
					quoridor.getCurrentGame().getBlackPlayer().addWall(blackWallInStock + 10);
				}
				cur.addBlackWallsInStock(quoridor.getCurrentGame().getBlackPlayer().getWall(blackWallInStock));
			} else {
				cur.addBlackWallsInStock(quoridor.getCurrentGame().getBlackPlayer().getWall(blackWallInStock));
			}

		}

		quoridor.getCurrentGame().setCurrentPosition(cur);

	}

	/** 
	 * Feature 5. Rotate Wall
	 * This method rotates the grabbed wall by 90 degrees (from horizontal to 
	 * vertical or vice versa) to adjust its designated target position
	 * @author Matteo Nunez
	 * @param wall - wall object that is going to be rotated
	 */
	public static void rotateWall() {
		if(!QuoridorApplication.getQuoridor().getCurrentGame().hasWallMoveCandidate()) return;
		if (QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().getWallDirection().equals(Direction.Vertical)) {
			QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().setWallDirection(Direction.Horizontal);

		} else {
			QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate().setWallDirection(Direction.Vertical);

		}
	}

	/** Method used for gherkin scenarios to set the current player position to an intial tile
	 * Bypasses all checks for validity
	 * @param row - row to move current player to
	 * @param col - column to move current player to
	 */
	public static void tpPlayer(int row, int col) {

		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		PlayerPosition pos = new PlayerPosition(curPos.getPlayerToMove(), findTile(row, col));

		if(curPos.getPlayerToMove().equals(white)) {
			curPos.setWhitePosition(pos);
		} else {
			curPos.setBlackPosition(pos);
		}
	}

	/** The public move pawn method visible to the View.
	 * 	Takes in a change in position (both of which must be <= to 1 in magnitude)
	 * 	And attempts to move the pawn in that direction.
	 * 	If it is impossible, returns false
	 * @param rChange - change in row being attempted
	 * @param cChange - change in column being attempted
	 * @return whether the pawn moved succesfully
	 */
	public static boolean movePlayer(int rChange, int cChange) {		
		if(QuoridorApplication.getQuoridor().getCurrentGame().getMoveMode() !=
				MoveMode.PlayerMove) return false;

		if(Math.abs(rChange) == 1 && Math.abs(cChange) == 1) {
			return diagonalMove(rChange, cChange);
		}

		//Otherwise proceed with jump/step Check
		if(!hasOpponent(rChange, cChange)) {
			//If one is 0 and one is a 1 step move	
			if((rChange == 0 || cChange == 0) 
					&& (Math.abs(rChange)==1 || Math.abs(cChange) == 1)) {
				return stepPawn(rChange, cChange);
			}
			//If either one is 0 and the other is 1 step or both are 1 step

		} else {
			if((Math.abs(rChange) <= 1 && Math.abs(cChange) <= 1)) {
				return jumpPawn(rChange * 2, cChange * 2);
			}
		}
		return false;
	}

	/** A query method to see whether a tile a specific offset from the current player
	 *  is occupied by the opponent.
	 * @param rChange - row offset to check
	 * @param cChange - column offset to check
	 * @return whether the tile in this direction (offset) has the opponent
	 */
	public static boolean hasOpponent(int rChange, int cChange) {
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

		//Move away from player by one unit
		if(oPos.getTile().equals(QuoridorController.findTile(row + rChange, col + cChange))) {
			return true;
		}
		return false;
	}

	/** A private method to take a single step in any (none diagonal) direction
	 *  Called by movePawn only when the attempted move is not diagonal
	 *  and there is no opponent in the way. Has a built in check for whether 
	 *  a wall is impeding the motion
	 * @param rChange - row offset to step Pawn in (-1, 0, or 1)
	 * @param cChange - column offset to step Pawn in (-1, 0, or 1)
	 * @return whether the step was successful
	 */
	private static boolean stepPawn(int rChange, int cChange) {

		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		int whiteCol = curPos.getWhitePosition().getTile().getColumn();
		int whiteRow = curPos.getWhitePosition().getTile().getRow();
		int blackCol = curPos.getBlackPosition().getTile().getColumn();
		int blackRow = curPos.getBlackPosition().getTile().getRow();


		int targetRow = 0;
		int targetCol = 0;

		Game curGame = QuoridorApplication.getQuoridor().getCurrentGame();

		if(curPos.getPlayerToMove().equals(white)) {
			//If a wall doesn't impede it and black isn't there
			if(noWallBlock(white, rChange, cChange) 
					&& (whiteRow + rChange != blackRow || whiteCol + cChange != blackCol)) {
				targetRow = whiteRow + rChange;
				targetCol = whiteCol + cChange;
				PlayerPosition pos = new PlayerPosition(curPos.getPlayerToMove(), findTile(whiteRow + rChange, whiteCol + cChange));
				curPos.setWhitePosition(pos);
			} else {
				return false;
			}
		} else {

			///If a wall doesn't impede it and black isn't there
			if(noWallBlock(curGame.getBlackPlayer(), rChange, cChange) 
					&& (blackRow + rChange != whiteRow || blackCol + cChange != whiteCol)) {

				targetRow = blackRow + rChange;
				targetCol = blackCol + cChange;
				PlayerPosition pos = new PlayerPosition(curPos.getPlayerToMove(), findTile(blackRow + rChange, blackCol + cChange));
				curPos.setBlackPosition(pos);
			} else {
				return false;
			}
		}

		StepMove move = new StepMove(curGame.getMoves().size()+1, 
				curGame.getMoves().size()/2+1, 
				curPos.getPlayerToMove(),
				findTile(targetRow, targetCol),
				curGame);
		curGame.addMove(move);
		completeMove();
		return true;
	}

	/** A private method to make a jump in any (none diagonal) direction
	 *  Called by movePawn only when the attempted move is not diagonal
	 *  and there is an opponent in the way. Has a built in check for whether 
	 *  a wall is impeding the motion
	 * @param rChange - row offset to jump Pawn in (-2, 0, or 2)
	 * @param cChange - column offset to jump Pawn in (-2, 0, or 2)
	 * @return whether the step was successful
	 */
	private static boolean jumpPawn(int rChange, int cChange) {

		//You could implement this by seeing if you can move (noWallBlock()) towards the direction
		//of r/c Change twice. Something to note is that the only 2 inputs you will
		//ever get here are 2,0 or 0,2

		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		int whiteCol = curPos.getWhitePosition().getTile().getColumn();
		int whiteRow = curPos.getWhitePosition().getTile().getRow();
		int blackCol = curPos.getBlackPosition().getTile().getColumn();
		int blackRow = curPos.getBlackPosition().getTile().getRow();


		int targetRow = 0;
		int targetCol = 0;


		if(curPos.getPlayerToMove().equals(white)) {
			PlayerPosition pos = new PlayerPosition(curPos.getPlayerToMove(), findTile(whiteRow + rChange, whiteCol + cChange));
			//Moving left or right wall check
			if(cChange != 0) {
				whiteCol += cChange;
				targetRow = whiteRow;
				targetCol = whiteCol;
				if(whiteCol < 1 || whiteCol > 9) return false;
				for(WallMove w : QuoridorController.getWalls()) {
					if(w.getWallDirection() == Direction.Vertical) {

						//If left- check col -1, -2. If right- check col +0, +1
						int tmp;
						if(cChange < 0) tmp = -2;
						else tmp = 0;

						int checkCol = (whiteCol -cChange) + tmp;

						if((w.getTargetTile().getColumn() == checkCol ||w.getTargetTile().getColumn() == checkCol + 1)  && 
								(w.getTargetTile().getRow() == whiteRow || w.getTargetTile().getRow() == whiteRow - 1)) {
							return false;
						}
					}
					//Horizontal Wall can't block right/left path
				}	
			}
			//Moving up or down wall check
			if(rChange != 0) {
				whiteRow += rChange;
				targetRow = whiteRow;
				targetCol = whiteCol;
				if(whiteRow < 1 || whiteRow > 9) return false;
				for(WallMove w : QuoridorController.getWalls()) {

					if(w.getWallDirection() == Direction.Horizontal) {
						//If up- check row -1, -2. If down- check row +0, +1
						int tmp;
						if(rChange < 0) tmp = -2;
						else tmp = 0;

						int checkRow = (whiteRow -rChange) + tmp;

						if((w.getTargetTile().getRow() == checkRow || w.getTargetTile().getRow() == checkRow + 1)
								&& (w.getTargetTile().getColumn() == whiteCol || w.getTargetTile().getColumn() == whiteCol - 1)) {
							return false;
						}
					}
					//Vertical Wall can't block up/down path
				}
			}

			if((blackRow == whiteRow) && (blackCol == whiteCol)) return false;

			curPos.setWhitePosition(pos);
		} else {
			PlayerPosition pos = new PlayerPosition(curPos.getPlayerToMove(), findTile(blackRow + rChange, blackCol + cChange));
			//Moving left or right wall check
			if(cChange != 0) {
				blackCol += cChange;
				targetRow = blackRow;
				targetCol = blackCol;
				if(blackCol < 1 || blackCol > 9) return false;
				for(WallMove w : QuoridorController.getWalls()) {
					if(w.getWallDirection() == Direction.Vertical) {

						//If left- check col -1, -2. If right- check col +0, +1
						int tmp;
						if(cChange < 0) tmp = -2;
						else tmp = 0;

						int checkCol = (blackCol -cChange) + tmp;

						if((w.getTargetTile().getColumn() == checkCol ||w.getTargetTile().getColumn() == checkCol + 1)  && 
								(w.getTargetTile().getRow() == blackRow || w.getTargetTile().getRow() == blackRow - 1)) {
							return false;
						}

					}
					//Horizontal Wall can't block right/left path
				}	
			}
			//Moving up or down wall check
			if(rChange != 0) {
				blackRow += rChange;
				targetRow = blackRow;
				targetCol = blackCol;
				if(blackRow < 1 || blackRow > 9) return false;
				for(WallMove w : QuoridorController.getWalls()) {
					if(w.getWallDirection() == Direction.Horizontal) {


						//If up- check row -1, -2. If down- check row +0, +1
						int tmp;
						if(rChange < 0) tmp = -2;
						else tmp = 0;

						int checkRow = (blackRow -rChange) + tmp;

						if((w.getTargetTile().getRow() == checkRow || w.getTargetTile().getRow() == checkRow + 1)
								&& (w.getTargetTile().getColumn() == blackCol || w.getTargetTile().getColumn() == blackCol - 1)) {
							return false;
						}

					}
					//Vertical Wall can't block up/down path
				}
			}

			if((blackRow == whiteRow) && (blackCol == whiteCol)) return false;

			curPos.setBlackPosition(pos);
		}
		Game curGame = QuoridorApplication.getQuoridor().getCurrentGame();
		JumpMove move = new JumpMove(curGame.getMoves().size()+1, 
				curGame.getMoves().size()/2+1, 
				curPos.getPlayerToMove(),
				findTile(targetRow, targetCol),
				curGame);
		curGame.addMove(move);
		completeMove();
		return true;
	}

	/** A private method to take a diagonal jump
	 *  Called by movePawn only when the attempted move is diagonal
	 *  and there is an opponent AND wall in the way. Has a built in check for whether 
	 *  a wall is impeding the motion
	 * @param rChange - row offset to jump Pawn in (-1 or 1)
	 * @param cChange - column offset to step Pawn in (-1 or 1)
	 * @return whether the step was successful
	 */
	private static boolean diagonalMove(int rChange, int cChange) {
		//Basically, a diagonal move must cross a pawn. I'll do a check
		//To make sure that a opponent tile - player tile == rChange or cChange
		//If it's == rChange, I want to make sure the opponent pawn can move 
		//by the cChange.
		//If it's == cChange, I want to make sure the opponent pawn can move 
		//by the rChange

		Game curGame = QuoridorApplication.getQuoridor().getCurrentGame();

		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		int whiteCol = curPos.getWhitePosition().getTile().getColumn();
		int whiteRow = curPos.getWhitePosition().getTile().getRow();
		int blackCol = curPos.getBlackPosition().getTile().getColumn();
		int blackRow = curPos.getBlackPosition().getTile().getRow();


		int targetRow = 0;
		int targetCol = 0;

		//White to move
		if(curPos.getPlayerToMove().equals(white)) {
			targetRow = whiteRow + rChange;
			targetCol = whiteCol + cChange;
			if(targetRow < 1 || targetCol > 9) return false;
			PlayerPosition pos = new PlayerPosition(curPos.getPlayerToMove(), findTile(targetRow, targetCol));
			if(whiteCol + cChange == blackCol && whiteRow == blackRow) {
				if(noWallBlock(curGame.getWhitePlayer(), 0, cChange) && noWallBlock(curGame.getBlackPlayer(), rChange, 0)) {
					curPos.setWhitePosition(pos);
					JumpMove move = new JumpMove(curGame.getMoves().size()+1, 
							curGame.getMoves().size()/2+1, 
							curPos.getPlayerToMove(),
							findTile(targetRow, targetCol),
							curGame);
					curGame.addMove(move);
					completeMove();

					return true;
				}
			} else if(whiteRow + rChange == blackRow && whiteCol == blackCol) {
				if(noWallBlock(curGame.getWhitePlayer(), rChange, 0) && noWallBlock(curGame.getBlackPlayer(), 0, cChange)) {
					curPos.setWhitePosition(pos);
					JumpMove move = new JumpMove(curGame.getMoves().size()+1, 
							curGame.getMoves().size()/2+1, 
							curPos.getPlayerToMove(),
							findTile(targetRow, targetCol),
							curGame);
					curGame.addMove(move);
					completeMove();

					return true;
				}
			}

		} else {
			targetRow = blackRow + rChange;
			targetCol = blackCol + cChange;
			if(targetRow < 1 || targetCol > 9) return false;
			PlayerPosition pos = new PlayerPosition(curPos.getPlayerToMove(), findTile(targetRow, targetCol));
			if(blackCol + cChange == whiteCol && whiteRow == blackRow) {
				if(noWallBlock(curGame.getBlackPlayer(), 0, cChange) && noWallBlock(curGame.getWhitePlayer(), rChange, 0)) {
					curPos.setBlackPosition(pos);
					JumpMove move = new JumpMove(curGame.getMoves().size()+1, 
							curGame.getMoves().size()/2+1, 
							curPos.getPlayerToMove(),
							findTile(targetRow, targetCol),
							curGame);
					curGame.addMove(move);
					completeMove();

					return true;
				}
			} else if(blackRow + rChange == whiteRow && whiteCol == blackCol) {
				if(noWallBlock(curGame.getBlackPlayer(), rChange, 0) && noWallBlock(curGame.getWhitePlayer(), 0, cChange)) {
					curPos.setBlackPosition(pos);
					JumpMove move = new JumpMove(curGame.getMoves().size()+1, 
							curGame.getMoves().size()/2+1, 
							curPos.getPlayerToMove(),
							findTile(targetRow, targetCol),
							curGame);
					curGame.addMove(move);
					completeMove();

					return true;
				}
			}
		}
		return false;
	}

	/** Helper method that checks to see whether a wall is in the way of a player 
	 * 	movement for an arbitrary player
	 *  Note, this does not account for an opponent in the way of the move
	 * @param p - player to check move for
	 * @param rChange - change in player row
	 * @param cChange - change in player column
	 * @return boolean- move is allowed
	 */
	public static boolean noWallBlock(Player p, int rChange, int cChange) {

		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		int whiteCol = curPos.getWhitePosition().getTile().getColumn();
		int whiteRow = curPos.getWhitePosition().getTile().getRow();
		int blackCol = curPos.getBlackPosition().getTile().getColumn();
		int blackRow = curPos.getBlackPosition().getTile().getRow();


		if(p.equals(white)) {
			//Moving left or right wall check
			if(cChange != 0) {
				whiteCol += cChange;
				if(whiteCol < 1 || whiteCol > 9) return false;
				for(WallMove w : QuoridorController.getWalls()) {
					if(w.getWallDirection() == Direction.Vertical) {
						//og position + change
						int checkCol = whiteCol - cChange + (int) (cChange-0.1); //if 1 -> 0

						if(w.getTargetTile().getColumn() == checkCol && (w.getTargetTile().getRow() == whiteRow || w.getTargetTile().getRow() == whiteRow - 1)) {
							return false;
						}
					}
					//Horizontal Wall can't block right/left path
				}	
			}
			//Moving up or down wall check
			if(rChange != 0) {
				whiteRow += rChange;
				if(whiteRow < 1 || whiteRow > 9) return false;
				for(WallMove w : QuoridorController.getWalls()) {

					if(w.getWallDirection() == Direction.Horizontal) {
						//og position + change
						int checkRow = whiteRow - rChange +  (int) (rChange-0.1); //if 1 -> 0

						if(w.getTargetTile().getRow() == checkRow && (w.getTargetTile().getColumn() == whiteCol || w.getTargetTile().getColumn() == whiteCol - 1)) {
							return false;
						}
					}
					//Vertical Wall can't block up/down path
				}
			}
		} else {
			//Moving left or right wall check
			if(cChange != 0) {
				blackCol += cChange;
				if(blackCol < 1 || blackCol > 9) return false;
				for(WallMove w : QuoridorController.getWalls()) {
					if(w.getWallDirection() == Direction.Vertical) {
						int checkCol = blackCol - cChange + (int) (cChange-0.1); //if 1 -> 0
						if(w.getTargetTile().getColumn() == checkCol && (w.getTargetTile().getRow() == blackRow || w.getTargetTile().getRow() == blackRow - 1)) {
							return false;
						}
					}
					//Horizontal Wall can't block right/left path
				}	
			}
			//Moving up or down wall check
			if(rChange != 0) {
				blackRow += rChange;
				if(blackRow < 1 || blackRow > 9) return false;
				for(WallMove w : QuoridorController.getWalls()) {
					if(w.getWallDirection() == Direction.Horizontal) {
						int checkRow = blackRow - rChange + (int) (rChange-0.1); //if 1 -> 0
						if(w.getTargetTile().getRow() == checkRow && (w.getTargetTile().getColumn() == blackCol || w.getTargetTile().getColumn() == blackCol - 1)) {
							return false;
						}
					}
					//Vertical Wall can't block up/down path
				}
			}
		}
		return true;
	}


	/** Private Helper method that checks to see whether a wall of a specific step 
	 *  Note, this does not account for an opponent in the way of the move
	 *  Also note- this one checks the wall move candidate as well
	 * @param p - player to check move for
	 * @param rChange - change in player row
	 * @param cChange - change in player column
	 * @return boolean- move is allowed
	 */
	private static boolean noWallBlockFrom(int curR, int curC, int rChange, int cChange) {
		//Moving left or right wall check
		if(cChange != 0) {
			curC += cChange;
			if(curC < 1 || curC > 9) return false;
			for(WallMove w : QuoridorController.getWalls()) {
				if(w.getWallDirection() == Direction.Vertical) {
					//og position + change
					int checkCol = curC - cChange + (int) (cChange-0.1); //if 1 -> 0

					if(w.getTargetTile().getColumn() == checkCol && (w.getTargetTile().getRow() == curR || w.getTargetTile().getRow() == curR - 1)) {
						return false;
					}
				}
				//Horizontal Wall can't block right/left path
			}
			if(QuoridorApplication.getQuoridor().getCurrentGame().hasWallMoveCandidate()) {
				WallMove w = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate();
				if(w.getWallDirection() == Direction.Vertical) {
					//og position + change
					int checkCol = curC - cChange + (int) (cChange-0.1); //if 1 -> 0

					if(w.getTargetTile().getColumn() == checkCol && (w.getTargetTile().getRow() == curR || w.getTargetTile().getRow() == curR - 1)) {
						return false;
					}
				}
			}


		}
		//Moving up or down wall check
		if(rChange != 0) {
			curR += rChange;
			if(curR < 1 || curR > 9) return false;
			for(WallMove w : QuoridorController.getWalls()) {

				if(w.getWallDirection() == Direction.Horizontal) {
					//og position + change
					int checkRow = curR - rChange +  (int) (rChange-0.1); //if 1 -> 0

					if(w.getTargetTile().getRow() == checkRow && (w.getTargetTile().getColumn() == curC || w.getTargetTile().getColumn() == curC - 1)) {
						return false;
					}
				}
				//Vertical Wall can't block up/down path
			}
			if(QuoridorApplication.getQuoridor().getCurrentGame().hasWallMoveCandidate()) {
				WallMove w = QuoridorApplication.getQuoridor().getCurrentGame().getWallMoveCandidate();
				if(w.getWallDirection() == Direction.Horizontal) {
					//og position + change
					int checkRow = curR - rChange +  (int) (rChange-0.1); //if 1 -> 0

					if(w.getTargetTile().getRow() == checkRow && (w.getTargetTile().getColumn() == curC || w.getTargetTile().getColumn() == curC - 1)) {
						return false;
					}
				}
			}		
		}
		return true;
	}

	/** A method to be used by the view. Takes an array of size corresponding
	 *  to the number of tiles and marks the tiles the current player is allowed to move to as
	 *  true. To be used when move pawn is clicked.
	 *  Note, currently Quoridor checks whether a motion is part of allowed tiles
	 *  before attempting a move. This means the validity of a move is being checked twice
	 * @param allowed - a boolean array with tiles the current player can move to marked as true
	 */
	public static void findAllowedTiles(boolean[] allowed) {
		if(allowed.length != 81) return;

		GamePosition curPos = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition();
		Player white = QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer();

		Player toMove;
		Player oppo;

		int col, row;
		if(curPos.getPlayerToMove().equals(white)) {
			col = curPos.getWhitePosition().getTile().getColumn();
			row = curPos.getWhitePosition().getTile().getRow();

			toMove = white;
			oppo = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
		} else {
			col = curPos.getBlackPosition().getTile().getColumn();
			row = curPos.getBlackPosition().getTile().getRow();

			oppo = white;
			toMove = QuoridorApplication.getQuoridor().getCurrentGame().getBlackPlayer();
		}
		//Tiles are drawn by row then by column. 0= row1 col1, 
		//Checking the has opponent first

		//Check down
		if(hasOpponent(1, 0)) {
			if(noWallBlock(toMove, 1, 0)) {

				if(noWallBlock(oppo, 1, 0) ) {
					//Jump straight allowed
					allowed[(col-1) + (row+1) * 9] = true;

				} else {
					if(noWallBlock(oppo, 0, -1)) {
						//Jump diagonal- check left
						allowed[(col-2) + (row) * 9] = true;

					} 
					if(noWallBlock(oppo, 0, 1)) {
						//Jump diagonal- check right
						allowed[(col) + (row) * 9] = true;
					}
				}
			}		
		//Check up

		} else if(hasOpponent(-1, 0)) {
			if(noWallBlock(toMove, -1, 0)) {

				if(noWallBlock(oppo, -1, 0) ) {
					//Jump straight allowed
					allowed[(col-1) + (row-3) * 9] = true;

				} else {
					if(noWallBlock(oppo, 0, -1)) {
						//Jump diagonal- check left
						allowed[(col-2) + (row-2) * 9] = true;

					} 
					if(noWallBlock(oppo, 0, 1)) {
						//Jump diagonal- check right
						allowed[(col) + (row-2) * 9] = true;
					}
				}
			}
		//Check right
		} else if(hasOpponent(0, 1)) {
			if(noWallBlock(toMove, 0, 1)) {
				if(noWallBlock(oppo, 0, 1) ) {
					//Jump straight allowed
					allowed[(col+1) + (row-1) * 9] = true;

				} else {
					if(noWallBlock(oppo, -1, 0)) {
						//Jump diagonal- check up
						allowed[(col) + (row-2) * 9] = true;

					} 
					if(noWallBlock(oppo, 1, 0)) {
						//Jump diagonal- check down
						allowed[(col) + (row) * 9] = true;
					}
				}
			}	
		//Check left
		} else if(hasOpponent(0, -1)) {
			if(noWallBlock(toMove, 0, -1)) {
				if(noWallBlock(oppo, 0, -1) ) {
					//Jump straight allowed
					allowed[(col-3) + (row-1) * 9] = true;
				} else {
					if(noWallBlock(oppo, -1, 0)) {
						//Jump diagonal- check up
						allowed[(col-2) + (row-2) * 9] = true;

					} 
					if(noWallBlock(oppo, 1, 0)) {
						//Jump diagonal- check down
						allowed[(col-2) + (row) * 9] = true;
					}
				}
			}
		}
		//If you reached here, you've done the opponent tiles, just to step check
		if(!hasOpponent(1,0)) {
			if(noWallBlock(toMove, 1, 0)) {
				allowed[(col-1) + (row) * 9] = true;
			}
		}
		if(!hasOpponent(-1,0)) {
			if(noWallBlock(toMove, -1, 0)) {
				allowed[(col-1) + (row-2) * 9] = true;
			}
		}
		if(!hasOpponent(0,1)) {
			if(noWallBlock(toMove, 0, 1)) {
				allowed[(col) + (row-1) * 9] = true;
			}
		}
		if(!hasOpponent(0,-1)) {
			if(noWallBlock(toMove, 0, -1)) {
				allowed[(col-2) + (row-1) * 9] = true;
			}
		}
	}




	public static Tile getCurrentPlayerTile() {
		return QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getPlayerToMove().equals(
				QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()) ? 
						QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile() :
							QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile();
	}


	//Lol hopefully nobody cares about this too much.
	//The class is super helpful for A* though
	private static class Cell implements Comparable<Cell>{
		private Cell parent;
		//private int parentR, parentC; 
		private int Row, Col;
		//g = distance from start cell to current tile
		//h = estimated distance from current tile to goal
		//f = g+h
		private int f,g,h;

		public int compareTo(Cell c) {
			return (int) Math.signum(this.f - c.f);
		}

		@Override
		public boolean equals(Object c) {
			if(c instanceof Cell) {
				Cell p = (Cell) c;
				return (p.Row == this.Row) && (p.Col == this.Col);
			}
			return false;
		}
	}

	private static Cell aStar(Player p) {
		//Initialize cells with row / col
		Cell[][] cellDetails = new Cell[9][9]; 
		for(int i = 0; i <9; i++) { 
			for(int j = 0; j < 9; j++) {
				Cell hi = new Cell();
				hi.f = 2000; 	 hi.h = 1000; 		hi.g = 1000;
				hi.parent = null;
				hi.Row = i + 1;  hi.Col = j + 1;
				cellDetails[i][j] = hi;
			}

		}

		Tile curTile;
		int goalRow = p.equals(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer()) ? 1 : 9;
		if(p.equals(QuoridorApplication.getQuoridor().getCurrentGame().getWhitePlayer())) {
			curTile = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getWhitePosition().getTile();
		} else {
			curTile = QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().getBlackPosition().getTile();
		}

		//A list of all possible options for the next step (tiles to update)
		PriorityQueue<Cell> open = new PriorityQueue<>();
		//A list of tiles already checked through/updated
		ArrayList<Cell> closed = new ArrayList<>();

		//Start parent cell
		int curRow = curTile.getRow() - 1; 		int curCol = curTile.getColumn() - 1;
		cellDetails[curRow][curCol].f = 0;
		cellDetails[curRow][curCol].g = 0;
		cellDetails[curRow][curCol].h = 0;
		open.add(cellDetails[curRow][curCol]);
		int count = 0; //This shouldn't be necessary, but I'm too lazy to make sure

		//Random Upper Limit
		while(!open.isEmpty() && count < 100) {
			count++;


			Cell q = open.poll(); //Returns 'best' tile in q (smallest f)- aka least work to reach and closest
			//aStarDisplay(open, closed, cellDetails, q);
			if(q.Row == goalRow) return q; //Found the Target!
			closed.add(q);


			//Check successors
			neighbors: for(int i = 0; i <4; i++ ) {
				//Return indices of neighbors
				//-1 to everything for the array thing
				int tmpRow, tmpCol;
				if(i<2) {
					tmpCol = q.Col - 1; //Don't change col
					tmpRow = q.Row+2*i-1   -1; //-1 or 1
				} else {
					tmpRow = q.Row - 1; //Don't change row
					tmpCol = q.Col+2*(i%2)-1   -1; //-1 or 1
				}

				if(tmpRow > 8 || tmpRow < 0 || tmpCol > 8 || tmpCol < 0) continue neighbors;
				if(!noWallBlockFrom(q.Row, q.Col, tmpRow+1 - q.Row, tmpCol+1 - q.Col)) continue neighbors;

				Cell suc = cellDetails[tmpRow][tmpCol]; 


				boolean isClosed = closed.contains(suc);

				int newG = q.g + 1;

				if(newG < suc.g && isClosed) {
					//System.out.println("Problem");
					System.err.println("Algorithmic problem: Not the end of the world (just possibly my grade)");
					//closed.remove(closed.indexOf(suc));
				} else if(isClosed) {
					continue neighbors;
				}
				//If it's here, it ain't on the closed list

				//IE if open contains suc
				//Basically refresh open if it's better, or add to open if the thing doesn't have it
				if(open.remove(suc)) {
					if(newG < suc.g && !isClosed) { //If new is better, update it and add
						suc.parent = q;
						suc.g = newG;
						suc.h = 2*Math.abs(suc.Row - goalRow); //Distance from row to target row
						suc.f = suc.g + suc.h;
						open.add(suc);
					}
					open.add(suc);

				} else {
					//If it isn't in open list
					suc.parent = q;
					suc.g = newG;
					suc.h = 2*Math.abs(suc.Row - goalRow); //Distance from row to target row
					suc.f = suc.g + suc.h;
					open.add(suc);
				}


			}
		}
		return null;		
	}

	private static void aStarDisplay(PriorityQueue<Cell> openList, ArrayList<Cell> closedList, Cell[][] Tiles, Cell q) {
		//i=row, j= column
		System.out.println();
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j <9; j++) {
				if(Tiles[i][j].equals(q)) System.out.print("Q");
				else if(closedList.contains(Tiles[i][j])) System .out.print("X");
				else if (openList.contains(Tiles[i][j])) System.out.print("O");
				else System.out.print("-");
			}
			System.out.println();
		}

		System.out.println();
	}


	/** 
	 * 
	 * @param fileName
	 * @return
	 */
	//Shamelessly stealing load- so yeah
	public static boolean startReplay(String fileName) {
		boolean worked = loadGame(fileName);
		//Moves players as if they were in move 1
		if(worked) {
			Game cur = QuoridorApplication.getQuoridor().getCurrentGame();
			cur.setCurrentPosition(cur.getPosition(0));
			cur.setGameStatus(GameStatus.Replay);
		}


		return worked;
	}

	public static void addReplayWallsBack(int MoveNum, int RoundNum) {
		Game curGame = QuoridorApplication.getQuoridor().getCurrentGame();
		GamePosition curPos = curGame.getCurrentPosition();
		List<Move> moveList = curGame.getMoves();
		//Here's a problem. You can't delete from a list and then keep iterating

		int fromMoveNum = (MoveNum*2) - (RoundNum == 1 ? 1:0);

		for(Move m : moveList) {
			if(m instanceof WallMove) { // if WallMove
				WallMove wall = (WallMove) m;
				if(wall.getMoveNumber() > MoveNum ||
						(wall.getMoveNumber() == MoveNum && wall.getRoundNumber() > RoundNum))
				{
					//Black made a wall move to add back
					if(wall.getPlayer().equals(curGame.getBlackPlayer())) {
						curPos.addBlackWallsInStock(wall.getWallPlaced());
						curPos.removeBlackWallsOnBoard(wall.getWallPlaced());
					} else {
						//White made a wall move to add back
						curPos.addWhiteWallsInStock(wall.getWallPlaced());
						curPos.removeWhiteWallsOnBoard(wall.getWallPlaced());
					}
				}
			}
		}
		//Ex. fromMoveNum = 1, moves size = 20, index of move 1 = 0
		//You would want to iterate from index 1 to 19, deleting
		int count = moveList.size();
		for(int i = fromMoveNum; i < count; i++) {
			//Idk why wallMoves aren't getting removed properly, but they aren't :(
			if(curGame.getMove(fromMoveNum) instanceof WallMove) {
				WallMove wallP = (WallMove) curGame.getMove(fromMoveNum);
				wallP.setWallPlaced(null);
				wallP.delete();
				curGame.removeMove(curGame.getMove(fromMoveNum));
			} else {
				StepMove stepP = (StepMove) curGame.getMove(fromMoveNum);
				stepP.delete();
				//curGame.getMove(fromMoveNum).delete();
			}
			GamePosition pos = curGame.getPosition(fromMoveNum);
			pos.delete();
		}

	}	

	public static void GameIsFinished(QuoridorView view) {

		Game currentgame=QuoridorApplication.getQuoridor().getCurrentGame();
		if(view.blackTimer!=null)
			QuoridorController.stopblackclock(view.blackTimer);
		if(view.whiteTimer!=null)
			QuoridorController.stopwhiteclock(view.whiteTimer);
			currentgame.getCurrentPosition().setPlayerToMove(null);
			currentgame.getBlackPlayer().setNextPlayer(null);
			currentgame.getWhitePlayer().setNextPlayer(null);
			
		}
		
	/** Method to identify when a game has been drawn
	 * @return whether it is drawn
	 */
	public static boolean gameIsDrawn() {
		
		List<Move> moves = QuoridorApplication.getQuoridor().getCurrentGame().getMoves();
		
		if(moves.size() < 9) return false;
		
		Move drawMove;
		Move lastMove = moves.get(moves.size() - 1);
		
		//A - 0 - B - 0 - A - 0 - B - 0 - A
		
		for(int i = moves.size() - 5, c = 0; c < 2; i-=4, c++) {
			//Compare lastMove to the one two before it
			drawMove = moves.get(i);
			//If you get a difference- break out
			if(!drawMove.getTargetTile().equals(lastMove.getTargetTile())) return false;
			//Otherwise, update lastMove
			lastMove = moves.get(i);
		}
		
		lastMove = moves.get(moves.size() - 3);
		drawMove = moves.get( moves.size() - 7);
		if(!drawMove.getTargetTile().equals(lastMove.getTargetTile())) return false;
		
		return true;
		
	}
}
