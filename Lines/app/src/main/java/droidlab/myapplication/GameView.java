package droidlab.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by Vladislav on 11.10.2016.
 */
public class GameView extends View {
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint paint, mBitmapPaint;
    private float canvasSize;
    private final int horizontalCountOfCells, verticalCountOfCells;
    private final GestureDetector detector;

    private final ScaleGestureDetector scaleGestureDetector;
    private int viewSize;
    private float mScaleFactor;
    private final float BIG_RADIUS, SMALL_RADIUS, ERASE_RADIUS;

    private GameLogic logic;
    private final MainActivity context;





    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = (MainActivity) context;
        //размер игрового поля
        horizontalCountOfCells = 9;
        verticalCountOfCells = 9;
        mScaleFactor = 1f;//значение зума по умолчанию
        viewSize = MainActivity.width;
        BIG_RADIUS = 0.75f * viewSize / (float) (horizontalCountOfCells + 1) / 2;
        SMALL_RADIUS = 0.2f * viewSize / (float) (horizontalCountOfCells + 1) / 2;
        ERASE_RADIUS = 0.85f * viewSize / (float) (horizontalCountOfCells + 1) / 2;

        canvasSize = (int)(viewSize * mScaleFactor);//определяем размер канваса
        mBitmap = Bitmap.createBitmap((int) canvasSize, (int) canvasSize, Bitmap.Config.ARGB_8888);

        mCanvas = new Canvas(mBitmap);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        //определяем параметры кисти, которой будем рисовать сетку и атомы
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xff00ffff);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.FILL);

        //рисуем сетку
        for(int x = 0;x <= horizontalCountOfCells; x++)
            mCanvas.drawLine((float)x * canvasSize / horizontalCountOfCells, 0, (float)x * canvasSize / horizontalCountOfCells, canvasSize, paint);
        for(int y = 0;y <= verticalCountOfCells; y++)
            mCanvas.drawLine(0, (float)y * canvasSize / verticalCountOfCells, canvasSize, (float)y * canvasSize / verticalCountOfCells, paint);

        scaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureListener());
        detector=new GestureDetector(context, new MyGestureListener());

    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor); // зумируем канвас
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }
    void eraseBall(int cellX, int cellY) {
        //считаем координаты центра ячейки
        float x0 = ((1f / (2 * horizontalCountOfCells)) * viewSize + (1f / horizontalCountOfCells) * cellX * viewSize);
        float y0 = ((1f / (2 * verticalCountOfCells)) * viewSize + (1f / verticalCountOfCells) * cellY * viewSize);
       // paint.setColor(0xff00ffff);
        //устанавливаем кисти режим стирания
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //заполнение обойденного участка
        paint.setColor(context.getResources().getColor(R.color.colorCell));
        paint.setStyle(Paint.Style.FILL);

       //paint.setColor(0xff00ffff);
        //рисуем большой круг, на месте которого ничего не останется
        mCanvas.drawCircle(x0, y0, ERASE_RADIUS, paint);
        //возвращаем исходные параметры
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

                invalidate();

        //перерисовываем канвас
    }

    void drawBall(final int cellX, final int cellY, final float radius, final int color){
        //считаем координаты центра ячейки
        final float x0 = ((1f / (2 * horizontalCountOfCells)) * viewSize + (1f/ horizontalCountOfCells) * cellX * viewSize);
        final float y0 = ((1f / (2 * verticalCountOfCells)) * viewSize + (1f/ verticalCountOfCells) * cellY * viewSize);

                eraseBall(cellX, cellY);

                paint.setColor(color);
                mCanvas.drawCircle(x0, y0, radius, paint);// рисуем;

               // Log.d("myTag", "run: i am here " + cellX + " " + cellY);
                invalidate();


        //перерисовываем канвас
    }


    //в случае касания пальем передаем управление MyScaleGestureListener
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    //унаследовались от ScaleGestureDetector.SimpleOnScaleGestureListener, чтобы не писать пустую реализацию ненужных методов интерфейса OnScaleGestureListener
    private class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        //обрабатываем "щипок" пальцами
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float scaleFactor= scaleGestureDetector.getScaleFactor();// получаем значение зума относительно предыдущего состояния
            // получаем координаты фокальной точки - точки между пальцами
            float focusX = scaleGestureDetector.getFocusX();
            float focusY = scaleGestureDetector.getFocusY();
            //следим чтобы канвас не уменьшили меньше исходного размера и не допускаем увеличения больше чем в 2 раза
            //следим чтобы канвас не уменьшили меньше исходного размера и не допускаем увеличения больше чем в 2 раза
            if(mScaleFactor * scaleFactor > 1 && mScaleFactor * scaleFactor < 2){
                mScaleFactor *= scaleGestureDetector.getScaleFactor();
                canvasSize = viewSize* mScaleFactor;//изменяем хранимое в памяти значение размера канваса
                //используется при расчетах
                //по умолчанию после зума канвас отскролит в левый верхний угол. Скролим канвас так, чтобы на экране оставалась обасть канваса, над которой был
                //жест зума
                int scrollX = (int)((getScrollX() + focusX) * scaleFactor - focusX);
                scrollX = Math.min( Math.max(scrollX, 0), (int) canvasSize - viewSize);
                int scrollY = (int)((getScrollY()+focusY)*scaleFactor-focusY);
                scrollY = Math.min( Math.max(scrollY, 0), (int) canvasSize - viewSize);
                scrollTo(scrollX, scrollY);
            }
            //вызываем перерисовку принудительно
            invalidate();
            return true;

        }
    }
    //унаследовались от GestureDetector.SimpleOnGestureListener
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        //обрабатываем скролл (перемещение пальца по экрану)
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //не даем канвасу показать края по горизонтали
            if (getScrollX() + distanceX < canvasSize - viewSize && getScrollX() + distanceX > 0) {
                scrollBy((int) distanceX, 0);
            }
            //не даем канвасу показать края по вертикали
            if (getScrollY() + distanceY < canvasSize - viewSize && getScrollY() + distanceY > 0) {
                scrollBy(0, (int) distanceY);
            }
            return true;
        }

        //обрабатываем одиночный тап
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            //получаем координаты ячейки, по которой тапнули
            int cellX = (int) ((event.getX() + getScrollX()) / mScaleFactor);
            int cellY = (int) ((event.getY() + getScrollY()) / mScaleFactor);
            logic.addCircle((int)(horizontalCountOfCells * cellX / (float) viewSize), (int)(verticalCountOfCells * cellY / (float) viewSize));
            return true;
        }

        //обрабатываем двойной тап
        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            //зумируем канвас к первоначальному виду
            mScaleFactor = 1f;
            canvasSize = viewSize;
            scrollTo(0, 0);


            invalidate();
            return true;
        }
    }
        public void setTableSize(int size){

        }
        public void setLogic(GameLogic logic) {
            this.logic = logic;
        }
    }




