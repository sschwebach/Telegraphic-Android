package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import sam.wisc.edu.telegraphic.R;

public class ImageCanvasActivity extends Activity {
    boolean existing;
    String IID;
    String prev;
    int editTime;
    int hopsLeft;
    String recipient;
    String imageStr;
    ImageViewCanvas mCanvas;
    UserImage currImage;
    Button redButton;
    Button greenButton;
    Button blueButton;
    Button blackButton;
    Timer decTimer;
    TextView timeRemaining;
    int currTime;
    boolean isRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_canvas);
        mCanvas = new ImageViewCanvas(this);
        ((LinearLayout) findViewById(R.id.linear_canvas)).addView(mCanvas);
        this.currImage = DataHolder.currImage;
        Intent intent = getIntent();
        existing = intent.getBooleanExtra("existing", false);
        if (existing){
            IID = intent.getStringExtra("IID");
            prev = intent.getStringExtra("prev");
            editTime = intent.getIntExtra("editTime", 5);
            currTime = editTime + 1;
            hopsLeft = intent.getIntExtra("hopsRemaining", 5);
            imageStr = intent.getStringExtra("imageString");
            mCanvas.oldImage = Utilities.decodeBase64(imageStr);
        }
        doSetup();
    }

    public void doSetup(){
        timeRemaining = (TextView) findViewById(R.id.text_time_left);
        timeRemaining.setText("" + currTime);
        redButton = (Button) findViewById(R.id.button_red);
        greenButton = (Button) findViewById(R.id.button_green);
        blueButton = (Button) findViewById(R.id.button_blue);
        blackButton = (Button) findViewById(R.id.button_black);
        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Color", "Changed color");
                mCanvas.changeColor(Color.RED);
            }
        });
        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Color", "Changed color");
                mCanvas.changeColor(Color.GREEN);
            }
        });
        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Color", "Changed color");
                mCanvas.changeColor(Color.BLUE);
            }
        });
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Color", "Changed color");
                mCanvas.changeColor(Color.BLACK);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        isRunning = true;
        this.decTimer = new Timer();
        this.decTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 1000);

    }

    @Override
    protected void onPause(){
        super.onPause();
        isRunning = false;
        this.decTimer.cancel();
    }

    @Override
    protected void onStop(){
        super.onStop();
        isRunning = false;
        this.decTimer.cancel();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        isRunning = false;
        this.decTimer.cancel();
    }

    private void TimerMethod(){
        this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm.isScreenOn() && isRunning){
                    currTime -= 1;
                    timeRemaining.setText("" + currTime);
                }
                if (currTime == 0){
                    //stop
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_canvas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
