package droidlab.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    public static int width;
    public static int height;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Display size
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels;
        width = metrics.widthPixels;

        super.onCreate(savedInstanceState);
        startGame();
        dialogStart();


    }

    public void startGame() {
        setContentView(R.layout.activity_main);

        GameView view = (GameView) findViewById(R.id.game_view);
        GameLogic logic = new GameLogic(view, this);
        view.setLogic(logic);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.height = width;
        params.width = width;
        params.setMargins(0, (height - width) / 2, 0, 0);
        view.setLayoutParams(params);
         /*
        TextView text = (TextView) findViewById(R.id.try_again);
        text.setHeight((height - width) / 3);
        text.setWidth(width / 2);
        LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) text.getLayoutParams();
        int margleft = (width - width / 2) / 2;
        int margTop = ((height - width) / 2 - (height - width) / 3) / 2;
        int margRight =(width - width / 2) / 2;
        int margBot = width + width / 2 + ((height - width) / 2 - width / 2) / 2;
        textParams.setMargins(margleft ,margTop, margRight, margBot);
        */

    }

    public void dialogStart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        TextView text = (TextView) inflater.inflate(R.layout.start, null);
        builder.setView(text);

        final AlertDialog dia = builder.create();
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dia.cancel();
                startGame();
            }
        });
        dia.show();
    }

    public void dialogFinish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        TextView text = (TextView) inflater.inflate(R.layout.game_over, null);
        builder.setView(text);
        builder.setPositiveButton("TRY AGAIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startGame();
            }
        });
        builder.create().show();
    }
}