package com.tuc2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Nishant on 06-Jul-15.
 */
public class AItuc extends Activity {
    private Handler mHandler = new Handler();
    boolean turn = true;
    int b[][] = new int[6][6];
    TextView tv1, tv2;
    Button btn;

    class coordinate {
        public int xc, yc;

        coordinate() {
            xc = 0;
            yc = 0;
        }
    }

    coordinate xy[] = new coordinate[6];
    Thread th;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tuc_graphic);
        tv1 = (TextView) findViewById(R.id.tvwin);
        btn = (Button) findViewById(R.id.nbn);
        tv2 = (TextView) findViewById(R.id.gm);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame(v);
            }
        });
        for (int i = 0; i < 6; i++) {
            xy[i] = new coordinate();
        }
        resetButtons();
        setupOnClickListeners();

    }

    public void resetButtons() {
        TableLayout T = (TableLayout) findViewById(R.id.tblayout);

        for (int y = 0; y < T.getChildCount(); y++) {
            if (T.getChildAt(y) instanceof TableRow) {

                TableRow R = (TableRow) T.getChildAt(y);
                for (int x = 0; x < R.getChildCount(); x++) {
                    if (R.getChildAt(x) instanceof Button) {
                        Button B = (Button) R.getChildAt(x);
                        b[y][x] = -1;
                        B.setText("OX");
                        B.setEnabled(false);
                    }
                }
            }
        }
        if (T.getChildAt(5) instanceof TableRow) {
            TableRow R = (TableRow) T.getChildAt(5);
            int x = 0;
            for (x = 0; x < R.getChildCount(); x++) {
                if (R.getChildAt(x) instanceof Button) {
                    Button B = (Button) R.getChildAt(x);
                    B.setEnabled(true);
                    xy[x].xc = 5;
                    xy[x].yc = x;
                }

            }


        }
    }

    private void setupOnClickListeners() {
        TableLayout T = (TableLayout) findViewById(R.id.tblayout);
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
        b = new int[6][6];
        tv2.setText("New game");
        tv1.setText("Winner");
        resetButtons();
        Intent i = new Intent(this,selection.class);
        startActivity(i);
        onStop();
    }
    void winx()
    {
        Intent i = new Intent(AItuc.this,winner.class);
        Bundle b = new Bundle();
        b.putString("w","Player X win");
        i.putExtras(b);
        startActivity(i);
        finish();
    }
    void wino()
    {
        Intent i = new Intent(AItuc.this,winner.class);
        Bundle b = new Bundle();
        b.putString("w","Player O win");
        i.putExtras(b);
        startActivity(i);
        finish();
    }

    private class PlayOnClick implements View.OnClickListener {

        private int x = 0;
        private int y = 0;

        public PlayOnClick(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void onClick(View view) {
            if (view instanceof Button) {
                Button B = (Button) view;
                b[x][y] = 1;
                B.setText("X");
                B.setEnabled(false);
                TableLayout T = (TableLayout) findViewById(R.id.tblayout);
                if (x-1>=-1&&T.getChildAt(x - 1) instanceof TableRow) {
                    TableRow R = (TableRow) T.getChildAt(x - 1);
                    if (R.getChildAt(x - 1) instanceof Button) {
                        Button q = (Button) R.getChildAt(y);
                        q.setEnabled(true);

                    }

                }
                if (x - 1 >= -1)
                    xy[y].xc = x - 1;
                xy[y].yc = y;
               // turn = !turn;

                int w = ch(b, x, y);  //check for winner
                if (w == 1) {
                    tv1.setText("win X");
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            winx();
                        }
                    }, 3000);
                } else if (w == 0) {
                    tv1.setText("win O");
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            wino();
                        }
                    }, 3000);
                } else {
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            playcomputer();
                        }
                    }, 1500);

                }

            }
        }

    }

    public void playcomputer() {

        if (make3(b)) {
            Log.d("value of K","make3 execute");
            return;
        }
        if (block(b)) {
            Log.d("value of K","block execute");
            return;
        }
        if(fork(b))
        {
            Log.d("value of K","fork execute");
            return;
        }

        Random r = new Random();
        int k = r.nextInt(100) % 6;
        Log.d("value of K","random k="+k);
       while(xy[k].xc<0)
       {
           r = new Random();
           k = r.nextInt(100) % 6;
       }
        Log.d("value of K","Nothing"+k);
        domove(xy[k].xc,xy[k].yc);
        /*if (xy[k].xc - 1 > -1)
            xy[xy[k].yc].xc = xy[k].xc - 1;   //value xy update
        xy[xy[k].yc].yc = xy[k].yc;*/
        Log.d("value of K","Nothing");
      return;

    }

    private void domove(int px, int py) {

        TableLayout T = (TableLayout) findViewById(R.id.tblayout);
        if (T.getChildAt(px) instanceof TableRow) {
            TableRow R = (TableRow) T.getChildAt(px);
            if (R.getChildAt(px) instanceof Button) {
                Button B = (Button) R.getChildAt(py);
                B.setText("O");
                B.setEnabled(false);
                b[px][py] = 0; //value of b array update
                //turn = !turn;
                xy[py].xc=px-1;  //xy update
                xy[py].yc=py;
            }

        }
        if ((px - 1) > -1&&T.getChildAt(px - 1) instanceof TableRow) {
            TableRow R = (TableRow) T.getChildAt(px - 1);
            if (R.getChildAt(px - 1) instanceof Button) {
                Button q = (Button) R.getChildAt(py);
                q.setEnabled(true);
                //new add


            }
        }
        int w = ch(b, px, py);  //check for winner
        if (w == 1) {
            tv1.setText("win X");
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    winx();
                }
            }, 3000);
        } else if (w == 0) {
            tv1.setText("win O");
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    wino();
                }
            }, 3000);
        }

    }

    public boolean make3(int a[][]) {
        boolean got = false;
        for (int i = 0; i < 6; i++) {
            if(xy[i].xc!=-1) {
                a[xy[i].xc][xy[i].yc] = 0;
                if (ch(a, xy[i].xc, xy[i].yc) == 0) {
                    a[xy[i].xc][xy[i].yc] = -1;
                    domove(xy[i].xc, xy[i].yc);
                    got = true;
                    break;
                } else {
                    a[xy[i].xc][xy[i].yc] = -1;
                    got = false;
                }
            }
        }
        return got;

    }

    public boolean block(int a[][]) {

        boolean got = false;
        for (int i = 0; i < 6; i++) {
            if(xy[i].xc!=-1) {
                a[xy[i].xc][xy[i].yc] = 1;
                if (ch(a, xy[i].xc, xy[i].yc) == 1) {
                    a[xy[i].xc][xy[i].yc] = -1;
                    Log.d("value of K", "block execute" + xy[i].xc + " " + xy[i].yc);
                    domove(xy[i].xc, xy[i].yc);
                    got = true;
                    return got;
                    //  break;
                } else {
                    a[xy[i].xc][xy[i].yc] = -1;
                    got = false;
                }
            }
        }
        return got;
    }
    public boolean fork(int a[][])
    {
        boolean got = false;
        for(int i= 0;i<6;i++)
        {
            if(xy[i].xc!=-1) {
                a[xy[i].xc][xy[i].yc] = 1;
                if (ch3(a, xy[i].xc, xy[i].yc) > 0) {
                    a[xy[i].xc][xy[i].yc] = -1;
                    Log.d("value of K", "fork execute" + xy[i].xc + " " + xy[i].yc);
                    domove(xy[i].xc, xy[i].yc);
                    got = true;
                    return got;
                } else {
                    a[xy[i].xc][xy[i].yc] = -1;
                    got = false;
                }
            }
        }
        return got;

    }


    public void disablebuttons() {
        TableLayout T = (TableLayout) findViewById(R.id.tblayout);
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
        if(y>-1 && y+3<6 && b[x][y]==b[x][y+1]&&b[x][y+1]==b[x][y+2]&&b[x][y+2]==b[x][y+3]) // *RRR
            return b[x][y];
        if(y-1>-1 &&y+2<6 && b[x][y-1]==b[x][y]&&b[x][y]==b[x][y+1] &&b[x][y+1]==b[x][y+2])   // l*RR
            return b[x][y];
        if(y-2>-1 && y+1<6 && b[x][y-2]==b[x][y-1]&&b[x][y-1]==b[x][y]&&b[x][y]==b[x][y+1])   // ll*R
            return b[x][y];
        if(y-3>-1 && y<6 && b[x][y-3]==b[x][y-2]&&b[x][y-2]==b[x][y-1]&&b[x][y-1]==b[x][y])    // LLL*
            return b[x][y];
        if(x+3<6 && y+3<6 && b[x][y]==b[x+1][y+1]&&b[x+1][y+1]==b[x+2][y+2]&&b[x+2][y+2]==b[x+3][y+3])  // *LLL
            return b[x][y];
        if(x-1>-1&&y-1>-1&&x+2<6&&y+2<6 && b[x-1][y-1]==b[x][y]&&b[x][y]==b[x+1][y+1]&&b[x+1][y+1]==b[x+2][y+2])  // H*LL
            return b[x][y];
        if(x-2>-1&&y-2>-1&&x+1<6&&y+1<6 &&b[x-2][y-2]==b[x-1][y-1]&&b[x-1][y-1]==b[x][y]&&b[x][y]==b[x+1][y+1])  //HH*L
            return b[x][y];
        if(x-3>-1&&y-3>-1  &&b[x-3][y-3]==b[x-2][y-2]&&b[x-2][y-2]==b[x-1][y-1]&&b[x-1][y-1]==b[x][y])  //HHH*
            return b[x][y];
        if(x+3<6&&y-3>-1 &&b[x][y]==b[x+1][y-1]&&b[x+1][y-1]==b[x+2][y-2]&&b[x+2][y-2]==b[x+3][y-3])  //*DDD
            return b[x][y];
        if(x-1>-1&&y+1<6&&x+2<6&&y-2>-1 &&b[x-1][y+1]==b[x][y]&&b[x][y]==b[x+1][y-1]&&b[x+1][y-1]==b[x+2][y-2])  //U*DD
            return b[x][y];
        if(x-2>-1&&y+2<6&&x+1<6&&y-1>-1 &&b[x-2][y+2]==b[x-1][y+1]&&b[x-1][y+1]==b[x][y]&&b[x][y]==b[x+1][y-1]) //UU*D
            return b[x][y];
        if(x-3>-1&&y+3<6  &&b[x-3][y+3]==b[x-2][y+2]&&b[x-2][y+2]==b[x-1][y+1]&&b[x-1][y+1]==b[x][y])  //UUU*
            return b[x][y];
        if(x+3<6 && b[x][y]==b[x+1][y]&&b[x+1][y]==b[x+2][y]&&b[x+2][y]==b[x+3][y])  //VVVV
            return b[x][y];


        return -1;
    }
    int ch3(int b[][], int x, int y) {
        int k=0;
        if (y + 1 < 6 && y - 1 > -1 && (b[x][y + 1] == b[x][y]) && (b[x][y - 1] == b[x][y])) //left right 1 step
            k++;
        if (y + 1 < 6 && y + 2 < 6 && (b[x][y + 1] == b[x][y] && b[x][y + 2] == b[x][y]))   //right 2 step
            k++;
        if (y - 1 > -1 && y - 2 > -1 && (b[x][y - 1] == b[x][y] && b[x][y - 2] == b[x][y]))   //left 2 step
            k++;
        if (x + 1 < 6 && x + 2 < 6 && b[x + 1][y] == b[x][y] && b[x + 2][y] == b[x][y])    //down 2 step
            k++;
        if (x - 1 > -1 && y + 1 < 6 && x + 1 < 6 && y - 1 > -1 && b[x - 1][y + 1] == b[x][y] && b[x + 1][y - 1] == b[x][y])  //diag/ 1step up down
            k++;
        if (x - 1 > -1 && y - 1 > -1 && x + 1 < 6 && y + 1 < 6 && b[x - 1][y - 1] == b[x][y] && b[x + 1][y + 1] == b[x][y])  //diag\ 1step up down
            k++;
        if (x - 1 > -1 && y + 1 < 6 && x - 2 > -1 && y + 2 < 6 && b[x - 1][y + 1] == b[x][y] && b[x - 2][y + 2] == b[x][y])  //diag/ 2step up
            k++;
        if (x + 1 < 6 && y - 1 > -1 && x + 2 < 6 && y - 2 > -1 && b[x + 1][y - 1] == b[x][y] && b[x + 2][y - 2] == b[x][y])  //diag/ 2step down
            k++;
        if (x + 1 < 6 && y + 1 < 6 && x + 2 < 6 && y + 2 < 6 && b[x + 1][y + 1] == b[x][y] && b[x + 2][y + 2] == b[x][y])  //diag\ 2step down
            k++;
        if (x - 1 > -1 && y - 1 > -1 && x - 2 > -1 && y - 2 > -1 && b[x - 1][y - 1] == b[x][y] && b[x - 2][y - 2] == b[x][y])  //diag\ 2step
        k++;


        return k;
    }
}