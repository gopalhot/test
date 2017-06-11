package org.secuso.privacyfriendlyludo.logic;

import android.os.Parcel;
import android.os.Parcelable;

import org.secuso.privacyfriendlyludo.Map.GameFieldPosition;
import org.secuso.privacyfriendlyludo.Map.StartGameFieldPosition;
import org.secuso.privacyfriendlyludo.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Julchen on 28.05.2017.
 */

public class BoardModel implements Parcelable, Serializable {

   private boolean dice = true;

    public boolean isGame_finished() {
        return game_finished;
    }

   private boolean game_finished = false;

    public ArrayList<Player> getPlayers() {
        return players;
    }

   private ArrayList<Player> players = new ArrayList<>();
   private int dice_number;

    public Player getRecent_player() {
        return recent_player;
    }

    // which player has the control
   private Player recent_player = new Player();

    public void setOpponent_player(Player opponent) {
        this.opponent_player = opponent;
    }

    public Player getOpponent_player() {
        return opponent_player;
    }

   private Player opponent_player = new Player();

    public GameFieldPosition getMy_game_field() {
        return my_game_field;
    }

    private GameFieldPosition my_game_field;

    public StartGameFieldPosition getStart_player_map() {
        return start_player_map;
    }

   private StartGameFieldPosition start_player_map;

    public BoardModel() {//ArrayList <Player> players) {
        players.add(new Player(1, R.color.red, ""));
        players.add(new Player(2, R.color.middleblue, "Mickey"));
         players.add(new Player(3, R.color.green, "Mini"));
         players.add(new Player(4, R.color.yellow, "Lisa"));
        recent_player = players.get(0);
        start_player_map = new StartGameFieldPosition(players);
        my_game_field = new GameFieldPosition(players);
        start_player_map.fill_with_players(this);
    }

    // get all Figures which can be moved according to the rules
    private ArrayList<Integer> checkMovableFigures() {
        int new_position;
        boolean movePossible = false;
        boolean moveOfFigureAllowed;
        ArrayList<Integer> figure_ids = new ArrayList<>();

        // make first field free if still players are in the house
        boolean freeHouse;
        boolean freeFirstField;
        freeFirstField = checkFirstFieldFree();
        freeHouse = checkHouseisFree();

        if (!freeHouse && !freeFirstField) {
            // move figure on free first field
            int position = 1 + (recent_player.getId() - 1) * 10;
            int figure_id = getMy_game_field().getMyGamefield().get(position - 1).getFigure_id();
            new_position = getNewPosition(figure_id, dice_number);
            if (isEmptyofSamePlayer(new_position)) {
                figure_ids.add(figure_id);
            } else {
                figure_id = getMy_game_field().getMyGamefield().get(new_position - 1).getFigure_id();
                figure_ids.add(figure_id);
            }

        } else {
            //check each figure
            for (int i = 0; i < recent_player.getFigures().size(); i++) {
                int figure_id = recent_player.getFigures().get(i).getId();

                moveOfFigureAllowed = figureIsAllowedToMove(dice_number, figure_id, freeHouse);
                if (moveOfFigureAllowed) {
                    new_position = getNewPosition(figure_id, dice_number);
                    movePossible = new_position != 0 && isEmptyofSamePlayer(new_position);
                }

                //move allowed and possible
                if (moveOfFigureAllowed && movePossible) {
                    figure_ids.add(figure_id);
                    // markfigures
                }

            }
        }

        return figure_ids;

    }

    private boolean checkFirstFieldFree() {
        boolean freeField;
        int position = 1 + (recent_player.getId() - 1) * 10;
        freeField = isEmptyofSamePlayer(position);
        return freeField;
    }

    private boolean checkHouseisFree() {
        boolean freeHouse = true;
        for (int i = 0; i < recent_player.getFigures().size(); i++) {
            if (Objects.equals(recent_player.getFigures().get(i).getState(), "start")) {
                freeHouse = false;
            }
        }
        return freeHouse;
    }

    public int getNewPosition(int figure_id, int dice_result) {
        // figure is finished

        if (recent_player.getFigures().get(figure_id - 1).isFinished()) {
            return 0;
        } else {
            String figureState = recent_player.getFigures().get(figure_id - 1).getState();
            //check if figureState is allowed to move
            int recent_index = recent_player.getFigures().get(figure_id - 1).getField_position_index();
            int new_index;
            switch (figureState) {
                case "start":
                    switch (recent_player.getId()) {
                        case 1:
                            return 1;
                        case 2:
                            return 11;
                        case 3:
                            return 21;
                        case 4:
                            return 31;
                        default:
                            return 0;
                    }
                case "inGame":
                    // check count steps
                    int count_steps = recent_player.getFigures().get(figure_id - 1).getCount_steps();
                    // because field 40 has to be included
                    if ((count_steps + dice_result) <= 40) {
                        new_index = (recent_index + dice_result) % 40;
                        if (new_index == 0) {
                            new_index = 40;
                        }
                    } else if (count_steps + dice_result > 44) {
                        // new position not possible because end of field
                        return 0;
                    } else {
                        new_index = 40 + (((count_steps + dice_result) - 40) + (4 * (recent_player.getId() - 1)));
                        return new_index;
                    }

                    return new_index;
                case "end":
                    new_index = (recent_index + dice_result);
                    if (new_index < 44 + (recent_player.getId() - 1) * 4) {
                        return new_index;
                    } else {
                        return 0;
                    }
                default:
                    return 0;
            }
        }
    }

    private boolean figureIsAllowedToMove(int dice_result, int figure_id, boolean freeHouse) {
        String figureState = recent_player.getFigures().get(figure_id - 1).getState();
        return dice_result == 6 && freeHouse || dice_result == 6 && Objects.equals(figureState, "start") || !(dice_result == 6 && !Objects.equals(figureState, "start")) && dice_result != 6 && !Objects.equals(figureState, "start");
    }

    //checks if already a figure of this player is there
    private boolean isEmptyofSamePlayer(int fieldindex) {

        // other GameField necessary
        return my_game_field.getMyGamefield().get(fieldindex - 1).getPlayer_id() != recent_player.getId();

    }

    //checks if already a figure is there
    public Boolean recent_player_on_field(int fieldindex) {
        return (my_game_field.getMyGamefield().get(fieldindex - 1).getPlayer_id() == 0);
    }

    private void updatePlayer(int player_id, int figure_id, int dice_result, int new_position, boolean kickedOut) {
        opponent_player = players.get(player_id - 1);
        Figure moved_figure = opponent_player.getFigures().get(figure_id - 1);
        int count_steps;
        if (kickedOut) {
            count_steps = 0;
        }
        // if figure comes out to the gamefield
        else if (Objects.equals(moved_figure.getState(), "start")) {
            count_steps = 1;
        } else {
            count_steps = (recent_player.getFigures().get(figure_id - 1).getCount_steps()) + dice_result;
        }
        moved_figure.setCount_steps(count_steps);
        moved_figure.setField_position_index(new_position);
        moved_figure.setState(moved_figure.getField_position_index());

    }

    private void updateBoard(int player_id, int figure_id, int old_position, int new_position) {
        GameField field_position_old;
        GameField field_position_new;
        opponent_player = players.get(player_id - 1);
        if (old_position >= 100) {
            field_position_old = getStart_player_map().getMyGamefield().get(old_position % 100);
        } else {
            field_position_old = getMy_game_field().getMyGamefield().get(old_position - 1);
        }
        field_position_old.setFigure_id(0);
        field_position_old.setPlayer_id(0);
        if (new_position >= 100) {
            field_position_new = getStart_player_map().getMyGamefield().get(new_position % 100);
        } else {
            field_position_new = getMy_game_field().getMyGamefield().get(new_position - 1);
        }
        field_position_new.setPlayer_id(opponent_player.getId());
        field_position_new.setFigure_id(figure_id);
    }

    public ArrayList<Integer> getMovable_figures() {
        return movable_figures;
    }

    private ArrayList<Integer> movable_figures;

    public ArrayList<Integer> processDiceResult() {

        movable_figures = new ArrayList<>();
        // check if Dice Roll is allowed
        //roll Dice
        Dicer dicer = new Dicer();
        dice = !dice;
        dice_number = dicer.rollDice(dice);
        //ask model what to do next --> for game rules
        // return all movable figures
        movable_figures = checkMovableFigures();
        movable_figures.add(0, dice_number);

        return movable_figures;
    }

    public boolean playerChanged(int count_Calls) {

        boolean notfinished = false;
        for (int i = 0; i < recent_player.getFigures().size(); i++) {
            String state = recent_player.getFigures().get(i).getState();
            boolean isfinished = recent_player.getFigures().get(i).isFinished();
            if (Objects.equals(state, "end") && !isfinished) {
                notfinished = true;
            }
        }
        // no movable figures, 3 times roll dice done, move is finished
        if (dice_number != 6 && count_Calls >= 3 && movable_figures.size() == 1) {
            recent_player = players.get((recent_player.getId()) % 4);
            return true;
        }
        // figure was moved, move is finished or not finished but in house
        else if ((dice_number != 6 && movable_figures.size() > 1)) {
            recent_player = players.get((recent_player.getId()) % 4);
            return true;
        } else if (dice_number != 6 && notfinished && count_Calls >= 1) {
            recent_player = players.get((recent_player.getId()) % 4);
            return true;
        } else //same player is again
        {
            // update information on recent player
            recent_player = players.get(recent_player.getId()-1);
            return false;
        }
    }

    public int moveFigure(int figure_index, boolean kicked_out) {
        int figure_id;
        int player_id;
        if (figure_index >= 100) {
            // figure is on startfields
            figure_id = start_player_map.getMyGamefield().get(figure_index % 100).getFigure_id();
            player_id = start_player_map.getMyGamefield().get(figure_index % 100).getPlayer_id();
        } else {
            figure_id = my_game_field.getMyGamefield().get(figure_index - 1).getFigure_id();
            player_id = my_game_field.getMyGamefield().get(figure_index - 1).getPlayer_id();
        }
        int new_position;
        if (kicked_out) {
            //setFigureBackToStart
            new_position = start_player_map.getMyGamefield().get((figure_id - 1) + 4 * (player_id - 1)).getIndex();
        } else {
            new_position = getNewPosition(figure_id, dice_number);
        }
        updatePlayer(player_id, figure_id, dice_number, new_position, kicked_out);
        updateBoard(player_id, figure_id, figure_index, new_position);
        updateFinishFlag(player_id, figure_id, new_position);

        return new_position;
    }

    private void updateFinishFlag(int player_id, int figure_id, int new_position) {
        if (players.get(player_id - 1).getFigures().get(figure_id - 1).getCount_steps() > 40) {
            int max_possible_fieldindex = 44 + 4 * (player_id - 1);
            int count_not_empty_fields = 0;
            int count_fields = max_possible_fieldindex - new_position;
            for (int i = 1; i < (count_fields + 1); i++) {
                if (!isEmptyofSamePlayer(new_position + i)) {
                    count_not_empty_fields = count_not_empty_fields + 1;
                }
            }
            // no fields are empty, figure is finished
            if (count_not_empty_fields == 0) {
                players.get(player_id - 1).getFigures().get(figure_id - 1).setFinished(true);
                int count_figures_finished = 0;
                for (int i = 0; i < players.size(); i++) {
                    for (int j = 0; j < players.get(player_id - 1).getFigures().size(); j++) {
                        if (players.get(i).getFigures().get(j).isFinished()) {
                            count_figures_finished = count_figures_finished + 1;
                        }
                    }
                }

                if (count_figures_finished == 16) {
                    // all figures are in the house
                    game_finished = true;
                }

            }
        }
    }


    private BoardModel(Parcel in) {
        dice = in.readByte() != 0x00;
        game_finished = in.readByte() != 0x00;
        if (in.readByte() == 0x01) {
            players = new ArrayList<>();
            in.readList(players, Player.class.getClassLoader());
        } else {
            players = null;
        }
        dice_number = in.readInt();
        recent_player = (Player) in.readValue(Player.class.getClassLoader());
        opponent_player = (Player) in.readValue(Player.class.getClassLoader());
        my_game_field = (GameFieldPosition) in.readValue(GameFieldPosition.class.getClassLoader());
        start_player_map = (StartGameFieldPosition) in.readValue(StartGameFieldPosition.class.getClassLoader());
        if (in.readByte() == 0x01) {
            movable_figures = new ArrayList<>();
            in.readList(movable_figures, Integer.class.getClassLoader());
        } else {
            movable_figures = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (dice ? 0x01 : 0x00));
        dest.writeByte((byte) (game_finished ? 0x01 : 0x00));
        if (players == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(players);
        }
        dest.writeInt(dice_number);
        dest.writeValue(recent_player);
        dest.writeValue(opponent_player);
        dest.writeValue(my_game_field);
        dest.writeValue(start_player_map);
        if (movable_figures == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(movable_figures);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BoardModel> CREATOR = new Parcelable.Creator<BoardModel>() {
        @Override
        public BoardModel createFromParcel(Parcel in) {
            return new BoardModel(in);
        }

        @Override
        public BoardModel[] newArray(int size) {
            return new BoardModel[size];
        }
    };

}

