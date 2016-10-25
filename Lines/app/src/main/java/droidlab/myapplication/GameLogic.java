package droidlab.myapplication;

/**
 * Created by Vladislav on 15.10.2016.
 */
import android.os.Handler;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameLogic {
    private final GameView view;
    private final MainActivity activity;
    private final int BOARD_WIDTH, BOARD_HEIGHT, NUMBER_CIRCLES;
    private final Cell[][] cells;

    private final int[] colors = {0xff1d76fc, 0xfffb1d76, 0xff76fb1d};//цвета атомов
    private final Handler mHandler;
    private final float BIG_RADIUS, SMALL_RADIUS;
    private final int NUMBER_OF_BALLS_TO_DEL;
    private int freeCells;
    private Ball[] smallBalls;
    private ArrayList<Ball> newBall;
    private boolean game;
    private boolean firstTapDone;
    private Pair from;

    Random rand;

    public GameLogic(GameView view, MainActivity activity) {
        this.view = view;
        this.activity = activity;
        mHandler = new Handler();
        //инициализируем игровые параметры (размер доски)
        this.firstTapDone = false;
        this.NUMBER_OF_BALLS_TO_DEL = 5;
        this.from = null;
        this.BOARD_HEIGHT = 9;
        this.game = true;
        this.BOARD_WIDTH = 9;
        this.NUMBER_CIRCLES = 3;
        this.BIG_RADIUS = 0.75f * MainActivity.width / (float) (BOARD_WIDTH + 1) / 2;
        this.SMALL_RADIUS = 0.2f * MainActivity.width / (float) (BOARD_WIDTH + 1) / 2;
        this.freeCells = BOARD_HEIGHT * BOARD_WIDTH;
        cells = new Cell[BOARD_WIDTH][BOARD_HEIGHT];
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int g = 0; g < BOARD_WIDTH; g++)
                cells[i][g] = new Cell(new Ball(0.f,0, i, g));
        }
        drawAll();
        this.rand = new Random();
        this.smallBalls = new Ball[NUMBER_CIRCLES];
        this.newBall = new ArrayList<Ball>();

        // появление больших шариков
        for (int j = 0; j < NUMBER_CIRCLES; j++) {
            int d = rand.nextInt(freeCells) + 1;
            int cur = 0;
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int g = 0; g < BOARD_WIDTH; g++) {
                    cur++;
                    if (cur == d) {
                        cells[i][g] = new Cell(new Ball(BIG_RADIUS, colors[j], i, g));
                        view.drawBall(i, g, BIG_RADIUS, colors[j]);
                        //view.eraseBall(i,g);
                    }

                }
            }
            freeCells--;
        }
        // появление маленьких шариков
        appearSmall();
    }

    public ArrayDeque<Pair> bfs (Pair v, Pair to){
        ArrayDeque<Pair> q = new ArrayDeque<Pair>();
        q.add(v);
        Pair[][] path = new Pair[BOARD_HEIGHT][BOARD_WIDTH];
        path[v.f][v.s] = new Pair(-1, -1);
        boolean[][] used = new boolean[BOARD_HEIGHT][BOARD_WIDTH];
        used[v.f][v.s] = true;
        while (!q.isEmpty() && !used[to.f][to.s]) {
            v = q.poll();
            Pair x;


            if (v.f > 0 && !used[v.f - 1][v.s] && check_to(v.f - 1, v.s)) {
                q.add(new Pair(v.f - 1, v.s));
                path[v.f - 1][v.s] = new Pair(v.f, v.s);
                used[v.f - 1][v.s] = true;


            }
            if (v.f < BOARD_HEIGHT - 1 && !used[v.f + 1][v.s] && check_to(v.f + 1, v.s)) {
                q.add(new Pair(v.f + 1, v.s));
                path[v.f + 1][v.s] = new Pair(v.f, v.s);
                used[v.f + 1][v.s] = true;

            }

            if (v.s > 0 && !used[v.f][v.s - 1] && check_to(v.f, v.s - 1)) {
                q.add(new Pair(v.f, v.s - 1));
                path[v.f][v.s - 1] = new Pair(v.f, v.s);
                used[v.f][v.s - 1] = true;


            }
            if (v.s < BOARD_HEIGHT - 1 && !used[v.f][v.s + 1] && check_to(v.f, v.s + 1)) {
                q.add(new Pair(v.f, v.s + 1));
                path[v.f][v.s + 1] = new Pair(v.f, v.s);
                used[v.f][v.s + 1] = true;
            }

        }

        ArrayDeque<Pair> way = new ArrayDeque<Pair>();
        if (!used[to.f][to.s]) {
            return way;
        } else {
            Pair x = to;
            do {
                way.addFirst(x);
                x = path[x.f][x.s];
            }
            while (x.f != -1);
        }
        return way;

    }
    private boolean check_to(int x, int y){
        return !cells[x][y].isFilledWithBigBall();
    }
    private boolean check_from(int x, int y){
        return !cells[x][y].isFilledWithBigBall();
    }
    void delete() {
        int bor = newBall.size();

        int left, right, up, down, l, v;
        int col = 0;
        for (int i = 0; i < bor; i++) {

            ArrayList<Pair> ballToDelHor = new ArrayList<Pair>();
            ArrayList<Pair> ballToDelVer = new ArrayList<Pair>();
            ArrayList<Pair> ballToDelCross1 = new ArrayList<Pair>();
            ArrayList<Pair> ballToDelCross2 = new ArrayList<Pair>();
            up = down = l = newBall.get(i).getX();
            left = right = v = newBall.get(i).getY();

            col = newBall.get(i).getColor();
            float radius = newBall.get(i).getRadius();

            int cLeft, cRight, cUp, cDown, hor, ver, cross1, cross2;
            int digLeftUp, digLeftDown, digRightUp, digRightDown;
            hor = ver = cross1 = cross2 = 0;
            while ((right < BOARD_WIDTH)&& (cells[l][right].getCell().getColor() == col) &&
                    (cells[l][right].getCell().getRadius() == radius)) {
                ballToDelHor.add(new Pair(l, right));
                right += 1;
                hor++;
            }
            left--;
            while (left >= 0 && cells[l][left].getCell().getRadius() == radius &&
            cells[l][left].getCell().getColor() == col) {
                ballToDelHor.add(new Pair(l, left));
                left--;
                hor++;
            }

            while (up >= 0 && cells[up][v].getCell().getColor() == col
                    && cells[up][v].getCell().getRadius() == radius) {
                ballToDelVer.add(new Pair(up, v));

                up--;
                ver++;
            }
            down++;
            while (down < BOARD_HEIGHT && cells[down][v].getCell().getRadius() == radius &&
                    cells[down][v].getCell().getColor() == col) {
                ballToDelVer.add(new Pair(down, v));
                down++;
                ver++;

            }
            // main diag
            up = down = l;
            left = right = v;
            while (up >= 0 && left >= 0 && cells[up][left].getCell().getRadius() == radius &&
                    cells[up][left].getCell().getColor() == col) {
                ballToDelCross1.add(new Pair(up, left));
                up--;
                left--;
                cross1++;
            }
            down++;
            right++;
            while (down < BOARD_HEIGHT && right < BOARD_WIDTH && cells[down][right].getCell().getRadius() == radius &&
                    cells[down][right].getCell().getColor() == col) {
                ballToDelCross1.add(new Pair(down, right));
                down++;
                right++;
                cross1++;
            }
            // pobochnaya diag
            up = down = l;
            left = right = v;
            while (up >= 0 && right < BOARD_WIDTH && cells[up][right].getCell().getRadius() == radius &&
                    cells[up][right].getCell().getColor() == col) {
                ballToDelCross2.add(new Pair(up, right));
                up--;
                right++;
                cross2++;
            }

            down++;
            left--;
            while (down < BOARD_HEIGHT && left >= 0 && cells[down][left].getCell().getRadius() == radius
            && cells[down][left].getCell().getColor() == col) {
                ballToDelCross2.add(new Pair(down, left));
                down++;
                left--;
                cross2++;
            }
            if (ballToDelCross1.size() >= NUMBER_OF_BALLS_TO_DEL)
                for (int u = 0; u < ballToDelCross1.size(); u++) {
                    if (cells[ballToDelCross1.get(u).f][ballToDelCross1.get(u).s].isFilled() )
                        freeCells++;
                    cells[ballToDelCross1.get(u).f][ballToDelCross1.get(u).s] = new Cell(new Ball(0.f,0,
                            ballToDelCross1.get(u).f,ballToDelCross1.get(u).s ));

                    view.eraseBall(ballToDelCross1.get(u).f,ballToDelCross1.get(u).s);
                }
            if (ballToDelCross2.size() >= NUMBER_OF_BALLS_TO_DEL)
                for (int u = 0; u < ballToDelCross2.size(); u++) {
                    if (cells[ballToDelCross2.get(u).f][ballToDelCross2.get(u).s].isFilled())
                        freeCells++;
                    cells[ballToDelCross2.get(u).f][ballToDelCross2.get(u).s] = new Cell(new Ball(0.f,0,
                            ballToDelCross2.get(u).f,ballToDelCross2.get(u).s ));
                    view.eraseBall(ballToDelCross2.get(u).f,ballToDelCross2.get(u).s);
                }
            if (ballToDelHor.size() >= NUMBER_OF_BALLS_TO_DEL)
                for (int u = 0; u < ballToDelHor.size(); u++) {
                    if (cells[ballToDelHor.get(u).f][ballToDelHor.get(u).s].isFilled())
                        freeCells++;
                    cells[ballToDelHor.get(u).f][ballToDelHor.get(u).s] = new Cell(new Ball(0.f,0,
                            ballToDelHor.get(u).f,ballToDelHor.get(u).s ));
                    view.eraseBall(ballToDelHor.get(u).f,ballToDelHor.get(u).s);
                }
            if (ballToDelVer.size() >= NUMBER_OF_BALLS_TO_DEL)
                for (int u = 0; u < ballToDelVer.size(); u++) {

                    if (cells[ballToDelVer.get(u).f][ballToDelVer.get(u).s].isFilled())
                        freeCells++;
                    cells[ballToDelVer.get(u).f][ballToDelVer.get(u).s] = new Cell(new Ball(0.f,0,
                            ballToDelVer.get(u).f,ballToDelVer.get(u).s ));

                    view.eraseBall(ballToDelVer.get(u).f,ballToDelVer.get(u).s);
                }


        }
    }
    void drawAll(){
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int g = 0; g < BOARD_WIDTH; g++) {
                if(cells[i][g].isFilled())
                    view.drawBall(i,g, cells[i][g].getCell().getRadius(),
                            cells[i][g].getCell().getColor());
                else view.eraseBall(i,g);
            }
        }
    }

    void afterBfs() {
        drawAll();

        changeToBig();
        drawAll();
        appearSmall();
        drawAll();
        delete();
        drawAll();


    }
    //int count = 0;
    void drawGoBfs(Pair from, final int color, final ArrayDeque <Pair> way) {
        final int s = way.size();
        cells[from.f][from.s] = new Cell(new Ball(0, 0, from.f, from.s));

        way.poll();
        final Timer timer = new Timer("bfs");
        final String TAG = "myTag";
        final Cell[] prevCell = {cells[from.f][from.s]};
        final Ball[] prevMoveBall = new Ball[1];
        prevMoveBall[0] = new Ball(prevCell[0].getCell().getRadius(), prevCell[0].getCell().getColor(),
                prevCell[0].getCell().getX(), prevCell[0].getCell().getY());
        view.eraseBall(prevCell[0].getCell().getX(), prevCell[0].getCell().getY());
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Pair nt = way.poll();
                        if (prevCell[0].isFillewWithSmallBall()) {
                           view.drawBall(prevCell[0].getCell().getX(), prevCell[0].getCell().getY(),
                                    prevCell[0].getCell().getRadius(), prevMoveBall[0].getColor());
                        } else {
                            view.eraseBall(prevCell[0].getCell().getX(), prevCell[0].getCell().getY());

                        }

                        view.drawBall(nt.f, nt.s, BIG_RADIUS, color);

                        if (way.isEmpty()) {
                            cells[nt.f][nt.s] = new Cell(new Ball(BIG_RADIUS, color, nt.f, nt.s));

                            afterBfs();
                            timer.cancel();
                        }
                        prevCell[0] = new Cell(new Ball(cells[nt.f][nt.s].getCell().getRadius(),
                                cells[nt.f][nt.s].getCell().getColor(), nt.f, nt.s));
                        prevMoveBall[0] = new Ball(prevCell[0].getCell().getRadius(), prevCell[0].getCell().getColor(),
                                prevCell[0].getCell().getX(), prevCell[0].getCell().getY());

                    }
                });
            }


        };
        timer.schedule(timerTask, 0, 300);


    }




     // появление маленьких шариков
    public void appearSmall() {
        if(freeCells < 3 ){
            activity.dialogFinish();
            return;
        }
        for (int j = 0; j < NUMBER_CIRCLES; j++) {
            int d = rand.nextInt(freeCells) + 1;
            int cur = 0;
            for (int i = 0; i < BOARD_HEIGHT && cur < d; i++) {
                for (int g = 0; g < BOARD_WIDTH && cur < d; g++) {
                    if (!cells[i][g].isFilled())
                        cur++;
                    if (cur == d) {
                        cells[i][g] = new Cell(new Ball(SMALL_RADIUS, colors[j], i, g));
                        smallBalls[j] = new Ball(SMALL_RADIUS, colors[j], i, g);
                        view.drawBall(i, g, SMALL_RADIUS, colors[j]);
                    }

                }
            }
            freeCells--;
        }
    }

    public void changeToBig() {
        for (int i = 0; i < NUMBER_CIRCLES; i++) {
           // if(true) throw new RuntimeException("message");
            if ((cells[smallBalls[i].getX()][smallBalls[i].getY()].isFillewWithSmallBall())) {
                cells[smallBalls[i].getX()][smallBalls[i].getY()] = new Cell(new Ball(BIG_RADIUS, smallBalls[i].getColor(), smallBalls[i].getX(), smallBalls[i].getY()));
                view.drawBall(smallBalls[i].getX(),smallBalls[i].getY(), BIG_RADIUS,smallBalls[i].getColor() );
                newBall.add(new Ball(BIG_RADIUS, smallBalls[i].getColor(), smallBalls[i].getX(), smallBalls[i].getY()));
            }
        }
    }


    //вызывается из вьюхи по одиночному тапу
    public void addCircle(int cellX, int cellY) {
        //получаем ячейку, в которую добавляем атом, если ее нет в массиве - выходим из функции.
        final Cell currentCell;
        try{
            currentCell=cells[cellX][cellY];
        }catch (IndexOutOfBoundsException ex){
            return;
        }
        final Pair to = new Pair(cellX, cellY);

        if(firstTapDone ){
            if(from.f == to.f && from.s == to.s) {
                from = null;
                firstTapDone = false;
                return;
            }
            firstTapDone = false;
        }
        else {
            if(!cells[cellX][cellY].isFilledWithBigBall() ){
                return;
            }
            from = new Pair(cellX, cellY);
            firstTapDone = true;
            return;
        }
        ArrayDeque <Pair> way;
        way = bfs(from, to);
        if(way.size() == 0){
            firstTapDone = false;
            from = null;
            return;
        }
        if(cells[to.f][to.s].isFillewWithSmallBall())
            freeCells++;
        newBall.clear();
        newBall.add(new Ball(BIG_RADIUS, cells[from.f][from.s].getCell().getColor(),to.f, to.s));

        drawGoBfs(from, cells[from.f][from.s].getCell().getColor(), way);

        }


    private class Ball {
        private float radius;
        private int color;
        private int x, y;

        Ball(float radius, int color, int x, int y) {
            this.radius = radius;
            this.color = color;
            this.x = x;
            this.y = y;
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    private class Cell {
        Ball ball;


        Cell(Ball ball) {
            this.ball = ball;
        }

        public void setCell(Ball ball) {
            this.ball = ball;
        }

        public boolean isFilled() {
            return ball.getRadius() > 0;
        }
        public boolean isFillewWithSmallBall(){
            return ball.getRadius() == SMALL_RADIUS;
        }

        public boolean isFilledWithBigBall() {
            return ball.getRadius() == BIG_RADIUS;
        }

        public Ball getCell() {
            return ball;
        }
    }

    private class Pair {
        int f, s;
        public Pair(int f, int s) {
            this.f = f;
            this.s = s;

        }
    }
}
