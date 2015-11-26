package com.tuc2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by Nishant on 18-Oct-15.
 */
public class btmplogic extends Activity {
    TableLayout layout;
    int bordsize=8;
    boolean turn = false;
    int b[][] = new int[bordsize][bordsize];
    TextView tv1,tv2;
    Button btn;
    int score1=0;
    int score2=0;
    int movecounter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dynamictwoplayer);
        final LinearLayout lm = (LinearLayout) findViewById(R.id.dnm2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        layout = new TableLayout (this);
        layout.setLayoutParams( new TableLayout.LayoutParams(params) );

        layout.setPadding(1,1,1,1);

        for (int f=0; f<=7; f++) {
            TableRow tr = new TableRow(this);
            for (int c=0; c<=7; c++) {
                //ImageButton b = new ImageButton(this);
                Button b = new Button(this);
                // b.setText(""+f+c);
                b.setTextSize(15.0f);
                b.setTextColor(Color.rgb(100, 300, 300));
                b.setHeight(50);
                b.setWidth(50);
                // b.setOnClickListener(this);
                tr.addView(b,60,60);
                // b.setImageResource(R.drawable.button_bg);
            } // for
            layout.addView(tr);
        } // for
        lm.setBackgroundResource(R.drawable.steel_bg);
        lm.addView(layout);
        tv1 = (TextView) findViewById(R.id.scr1);

        tv2 = (TextView) findViewById(R.id.scr2);

        resetButtons();
        setupOnClickListeners();


    }

    public void resetButtons() {
        TableLayout T = layout;
        for (int y = 0; y < T.getChildCount(); y++) {
            if (T.getChildAt(y) instanceof TableRow) {
                TableRow R = (TableRow) T.getChildAt(y);
                for (int x = 0; x < R.getChildCount(); x++) {
                    if (R.getChildAt(x) instanceof Button) {
                        Button B = (Button) R.getChildAt(x);
                        b[y][x] = -1;
                        // B.setText("OX");
                        B.setEnabled(false);
                        score1=0;
                        score2=0;
                        movecounter=0;
                        tv1.setText("X: "+score1);
                        tv2.setText("O:"+score2);
                    }
                }
            }
        }
        if (T.getChildAt(7) instanceof TableRow) {
            TableRow R = (TableRow) T.getChildAt(7);
            for (int x = 0; x < R.getChildCount(); x++) {
                if (R.getChildAt(x) instanceof Button) {
                    Button B = (Button) R.getChildAt(x);
                    // B.setText("OX");
                    B.setEnabled(true);
                }

            }


        }
    }

    private void setupOnClickListeners() {
        TableLayout T =layout;
        for (int y = 0; y < T.getChildCount(); y++) {
            if (T.getChildAt(y) instanceof TableRow) {
                TableRow R = (TableRow) T.getChildAt(y);
                for (int x = 0; x < R.getChildCount(); x++) {
                    View V = R.getChildAt(x); // In our case this will be each button on the grid
                    V.setOnClickListener(new PlayOnClick(y, x));
                }
            }
        }
    }
    public void newGame(View view) {
        turn = false;
        b = new int[bordsize][bordsize];
        tv2.setText("New game");
        tv1.setText("Winner");
        resetButtons();
        Intent i = new Intent(this,selection.class);
        startActivity(i);
        finish();

    }

    @Override
    protected void onPause() {
        super.onPause();
        onDestroy();
    }

    private class PlayOnClick implements View.OnClickListener {

        private int x = 0;
        private int y = 0;

        public PlayOnClick(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void onClick(View view){
            if (view instanceof Button) {
                Button B = (Button) view;
                b[x][y] = turn ? 0 : 1;
                movecounter++;
                B.setText(turn ? "O" : "X");
                B.setEnabled(false);
                if(turn)
                {
                    score2+=ch3(b,x,y);
                    tv2.setText("O: "+score2);

                }
                else
                {
                    score1+=ch3(b,x,y);
                    tv1.setText("X: "+score1);
                }
                TableLayout T = layout;
                if (T.getChildAt(x-1) instanceof TableRow) {
                    TableRow R = (TableRow) T.getChildAt(x-1);
                    if (R.getChildAt(x-1) instanceof Button) {
                        Button q = (Button) R.getChildAt(y);
                        q.setEnabled(true);
                    }
                }

                turn = !turn;

                int w = ch(b,x,y);
                if(w==1)
                {
                    Intent i = new Intent(btmplogic.this,winner.class);
                    Bundle b = new Bundle();
                    b.putString("w","Player X win");
                    i.putExtras(b);
                    startActivity(i);
                    finish();
                }
                else
                if(w==0)
                {
                    Intent i = new Intent(btmplogic.this,winner.class);
                    Bundle b = new Bundle();
                    b.putString("w","Player O win");
                    i.putExtras(b);
                    startActivity(i);
                    finish();
                }
                else
                {
                    if(movecounter==64)
                    {
                        Intent i = new Intent(btmplogic.this,winner.class);
                        Bundle b = new Bundle();
                        String s=null;
                        if(score1>score2) {
                            s = "Player X won"+Integer.toString(score1);
                        }
                        else
                        if(score1<score2) {
                            s = "Player O won: " + Integer.toString(score2);
                        }
                        else
                            s="Draw";
                        b.putString("w",s);
                        i.putExtras(b);
                        startActivity(i);
                        finish();
                    }
                }
            }
        }

    }

    private void disablebuttons() {
        TableLayout T = layout;
        for (int y = 0; y < T.getChildCount(); y++) {
            if (T.getChildAt(y) instanceof TableRow) {
                TableRow R = (TableRow) T.getChildAt(y);
                for (int x = 0; x < R.getChildCount(); x++) {
                    if (R.getChildAt(x) instanceof Button) {
                        Button B = (Button) R.getChildAt(x);

                        B.setEnabled(false);
                        tv2.setText("GAme over");
                    }
                }
            }

        }
    }
    int ch(int b[][],int x,int y)
    {
        if(y>-1 && y+3<bordsize && b[x][y]==b[x][y+1]&&b[x][y+1]==b[x][y+2]&&b[x][y+2]==b[x][y+3]) // *RRR
            return b[x][y];
        if(y-1>-1 &&y+2<bordsize && b[x][y-1]==b[x][y]&&b[x][y]==b[x][y+1] &&b[x][y+1]==b[x][y+2])   // l*RR
            return b[x][y];
        if(y-2>-1 && y+1<bordsize && b[x][y-2]==b[x][y-1]&&b[x][y-1]==b[x][y]&&b[x][y]==b[x][y+1])   // ll*R
            return b[x][y];
        if(y-3>-1 && y<bordsize && b[x][y-3]==b[x][y-2]&&b[x][y-2]==b[x][y-1]&&b[x][y-1]==b[x][y])    // LLL*
            return b[x][y];
        if(x+3<bordsize && y+3<bordsize && b[x][y]==b[x+1][y+1]&&b[x+1][y+1]==b[x+2][y+2]&&b[x+2][y+2]==b[x+3][y+3])  // *LLL
            return b[x][y];
        if(x-1>-1&&y-1>-1&&x+2<bordsize&&y+2<bordsize && b[x-1][y-1]==b[x][y]&&b[x][y]==b[x+1][y+1]&&b[x+1][y+1]==b[x+2][y+2])  // H*LL
            return b[x][y];
        if(x-2>-1&&y-2>-1&&x+1<bordsize&&y+1<bordsize &&b[x-2][y-2]==b[x-1][y-1]&&b[x-1][y-1]==b[x][y]&&b[x][y]==b[x+1][y+1])  //HH*L
            return b[x][y];
        if(x-3>-1&&y-3>-1  &&b[x-3][y-3]==b[x-2][y-2]&&b[x-2][y-2]==b[x-1][y-1]&&b[x-1][y-1]==b[x][y])  //HHH*
            return b[x][y];
        if(x+3<bordsize&&y-3>-1 &&b[x][y]==b[x+1][y-1]&&b[x+1][y-1]==b[x+2][y-2]&&b[x+2][y-2]==b[x+3][y-3])  //*DDD
            return b[x][y];
        if(x-1>-1&&y+1<bordsize&&x+2<bordsize&&y-2>-1 &&b[x-1][y+1]==b[x][y]&&b[x][y]==b[x+1][y-1]&&b[x+1][y-1]==b[x+2][y-2])  //U*DD
            return b[x][y];
        if(x-2>-1&&y+2<bordsize&&x+1<bordsize&&y-1>-1 &&b[x-2][y+2]==b[x-1][y+1]&&b[x-1][y+1]==b[x][y]&&b[x][y]==b[x+1][y-1]) //UU*D
            return b[x][y];
        if(x-3>-1&&y+3<bordsize  &&b[x-3][y+3]==b[x-2][y+2]&&b[x-2][y+2]==b[x-1][y+1]&&b[x-1][y+1]==b[x][y])  //UUU*
            return b[x][y];
        if(x+3<bordsize && b[x][y]==b[x+1][y]&&b[x+1][y]==b[x+2][y]&&b[x+2][y]==b[x+3][y])  //VVVV
            return b[x][y];


        return -1;
    }
    int ch3(int b[][], int x, int y) {
        int k=0;
        if (y + 1 < bordsize && y - 1 > -1 && (b[x][y + 1] == b[x][y]) && (b[x][y - 1] == b[x][y])) //left right 1 step
            k++;
        if (y + 1 < bordsize && y + 2 < bordsize && (b[x][y + 1] == b[x][y] && b[x][y + 2] == b[x][y]))   //right 2 step
            k++;
        if (y - 1 > -1 && y - 2 > -1 && (b[x][y - 1] == b[x][y] && b[x][y - 2] == b[x][y]))   //left 2 step
            k++;
        if (x + 1 < bordsize && x + 2 < bordsize && b[x + 1][y] == b[x][y] && b[x + 2][y] == b[x][y])    //down 2 step
            k++;
        if (x - 1 > -1 && y + 1 < bordsize && x + 1 < bordsize && y - 1 > -1 && b[x - 1][y + 1] == b[x][y] && b[x + 1][y - 1] == b[x][y])  //diag/ 1step up down
            k++;
        if (x - 1 > -1 && y - 1 > -1 && x + 1 < bordsize && y + 1 < bordsize && b[x - 1][y - 1] == b[x][y] && b[x + 1][y + 1] == b[x][y])  //diag\ 1step up down
            k++;
        if (x - 1 > -1 && y + 1 < bordsize && x - 2 > -1 && y + 2 < bordsize && b[x - 1][y + 1] == b[x][y] && b[x - 2][y + 2] == b[x][y])  //diag/ 2step up
            k++;
        if (x + 1 < bordsize && y - 1 > -1 && x + 2 < bordsize && y - 2 > -1 && b[x + 1][y - 1] == b[x][y] && b[x + 2][y - 2] == b[x][y])  //diag/ 2step down
            k++;
        if (x + 1 < bordsize && y + 1 < bordsize && x + 2 < bordsize && y + 2 < bordsize && b[x + 1][y + 1] == b[x][y] && b[x + 2][y + 2] == b[x][y])  //diag\ 2step down
            k++;
        if (x - 1 > -1 && y - 1 > -1 && x - 2 > -1 && y - 2 > -1 && b[x - 1][y - 1] == b[x][y] && b[x - 2][y - 2] == b[x][y])  //diag\ 2step
            k++;


        return k;
    }

}
