package com.smkapps.boardgame;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<Integer> listNumbers;  // use ArrayList to put it in Bundle at onSaveInstanceState method
    List<Integer> copyList;

    GridLayout gridLayout;
    Button newGameButton;
    RadioGroup radioGroup;
    TextView moves_tv;

    int moves;
    int boardSize = 4;
    int invisibleCellID;
    TextView[][] dicesArray; // use TextView instead of Button for shrinking cell width automatically

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = (GridLayout) findViewById(R.id.layoutGrid);
        newGameButton = (Button) findViewById(R.id.newGame);
        moves_tv = (TextView) findViewById(R.id.moves_tv);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.radio3:
                        boardSize = 3;
                        startGame();
                        break;
                    case R.id.radio4:
                        boardSize = 4;
                        startGame();
                        break;
                    case R.id.radio5:
                        boardSize = 5;
                        startGame();
                        break;
                    case R.id.radio6:
                        boardSize = 6;
                        startGame();
                        break;
                }
            }
        });

        startGame();
    }

    private void startGame() {
        moves = 0;
        moves_tv.setText(R.string.moves);
        fillData();
        fillBoard();
    }

    private void fillData() {
        dicesArray = new TextView[boardSize][boardSize];
        listNumbers = new ArrayList<>();
        for (int i = 1; i <= boardSize * boardSize; i++) {
            listNumbers.add(i);
        }
        copyList = new ArrayList<>(listNumbers);
        Collections.shuffle(listNumbers);
    }

    private void fillBoard() {
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(boardSize);
        setBoardMetrics();
        setDicesParams();
        fillWithInvisibleViews();

    }

    private void checkIsWinner() {
        if (listNumbers.equals(copyList)) {
            Toast.makeText(MainActivity.this, R.string.win_message, Toast.LENGTH_LONG).show();

            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    dicesArray[i][j].setEnabled(false);
                }
            }
        }
    }

    private int[] getEmptyCellCoordinates() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (Integer.parseInt(dicesArray[i][j].getText().toString()) == boardSize * boardSize) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{0, 0};
    }

    private int[] getCellCoordinates(TextView cell) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (cell.equals(dicesArray[i][j])) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{0, 0};
    }

    private void setDicesParams() {

        int counter = 0;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                final TextView dice = new TextView(this);
                dice.setText(listNumbers.get(counter) + "");
                dice.setBackgroundResource(R.drawable.shape_rectangle_stroke);
                dice.setGravity(Gravity.CENTER);
                dice.setTextSize(18);
                dice.setTypeface(Typeface.DEFAULT_BOLD);
                gridLayout.addView(dice);

                if (listNumbers.get(counter) == boardSize * boardSize) {
                    dice.setVisibility(View.INVISIBLE);
                    dice.setId(View.generateViewId());
                    invisibleCellID = dice.getId();

                }
                dicesArray[i][j] = dice;
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.columnSpec = GridLayout.spec(j, 1f);
                params.rowSpec = GridLayout.spec(i, 1f);
                params.setMargins(2, 2, 2, 2);

                dice.setLayoutParams(params);
                dice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int[] emptyCoordinates = getEmptyCellCoordinates();
                        int[] pressedCoordinates = getCellCoordinates(dice);
                        TextView emptyCell = dicesArray[emptyCoordinates[0]][emptyCoordinates[1]];

                        GridLayout.LayoutParams emptyParams = (GridLayout.LayoutParams) emptyCell.getLayoutParams();
                        GridLayout.LayoutParams pressedParams = (GridLayout.LayoutParams) dice.getLayoutParams();
                        if ((Math.abs(pressedCoordinates[1] - emptyCoordinates[1]) == 1 && pressedCoordinates[0] == emptyCoordinates[0]) ||
                                (Math.abs(pressedCoordinates[0] - emptyCoordinates[0]) == 1) && pressedCoordinates[1] == emptyCoordinates[1]) {
                            dice.setLayoutParams(emptyParams);
                            emptyCell.setLayoutParams(pressedParams);
                            dicesArray[pressedCoordinates[0]][pressedCoordinates[1]] = emptyCell;
                            dicesArray[emptyCoordinates[0]][emptyCoordinates[1]] = dice;
                            int tempIndexEmpty = listNumbers.indexOf(boardSize * boardSize);
                            int valueDice = Integer.parseInt(dice.getText().toString());
                            int tempIndexPressed = listNumbers.indexOf(valueDice);
                            listNumbers.set(tempIndexEmpty, valueDice);
                            listNumbers.set(tempIndexPressed, boardSize * boardSize);
                            moves++;
                            moves_tv.setText(getString(R.string.moves) + " " + moves);
                            checkIsWinner();
                        }
                    }
                });
                counter++;
            }
        }

    }

    private void setBoardMetrics() {
        int pix = getResources().getDimensionPixelSize(R.dimen.customPadding);


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int height = getWindowManager().getDefaultDisplay().getWidth() - pix * 2;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height, Gravity.CENTER);

            gridLayout.setLayoutParams(layoutParams);
        } else {
            int width = getWindowManager().getDefaultDisplay().getHeight() - pix * 2;
            FrameLayout.LayoutParams layoutParamsLand = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
            gridLayout.setLayoutParams(layoutParamsLand);
        }
    }

    /* this method fills first row with invisible textviews with value 12 to prevent shrinking
        column with dices which all have value consisting of one digit*/
    private void fillWithInvisibleViews() {
        for (int i = 0; i < boardSize; i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            TextView temp = new TextView(this);
            params.rowSpec = GridLayout.spec(0, 0f);
            params.columnSpec = GridLayout.spec(i, 1f);
            temp.setText("12");
            params.setMargins(2, 2, 2, 2);
            temp.setTextSize(18);
            temp.setLayoutParams(params);
            temp.setVisibility(View.INVISIBLE);
            gridLayout.addView(temp);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList("currentList", listNumbers);
        outState.putInt("moves", moves);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listNumbers = savedInstanceState.getIntegerArrayList("currentList");
        moves = savedInstanceState.getInt("moves");
        fillBoard();
        moves_tv.setText(getString(R.string.moves) + " " + moves);
        checkIsWinner();
    }

}

