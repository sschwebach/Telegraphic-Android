package sam.wisc.edu.telegraphic;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Sam on 9/7/2014.
 */
public class CanvasFragment extends Fragment {
    ImageViewCanvas mCanvas;
    String recipient;
    UserImage image;
    TextView timer;
    int time;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View toReturn = getActivity().getLayoutInflater().inflate(R.layout.activity_image_canvas, null);
        LinearLayout layout = (LinearLayout) toReturn.findViewById(R.id.linear_canvas);
        mCanvas = new ImageViewCanvas(this.getActivity());
        mCanvas.oldImage = ((CombinedImageActivity) getActivity()).currImage;
        layout.addView(mCanvas);
        timer = (TextView) toReturn.findViewById(R.id.text_time_left);
        if (image != null) {
            time = image.editTime;
        }else{
            time = 10;
        }
        timer.setText("" + time);
        Button redButton = (Button) toReturn.findViewById(R.id.button_red);
        Button greenButton = (Button) toReturn.findViewById(R.id.button_green);
        Button blueButton = (Button) toReturn.findViewById(R.id.button_blue);
        Button blackButton = (Button) toReturn.findViewById(R.id.button_black);
        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeColor(Color.RED);
            }
        });
        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeColor(Color.GREEN);
            }
        });
        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeColor(Color.BLUE);
            }
        });
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeColor(Color.BLACK);
            }
        });

        return toReturn;
    }

}
