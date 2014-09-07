package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Sam on 9/6/2014.
 */
public class ImageListAdapter{
    ArrayList<UserImage> images;
    Context mContext;
    UserImage currImage;
    String IID;
    String prev;
    int editTime;
    Intent intent;
    int hopsLeft;
    String imageStr;

    public ImageListAdapter(ArrayList<UserImage> images, Context c){
        this.images = images;
        mContext = c;
    }

    public int getCount(){
        return images.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View toReturn;

        if (convertView != null){
            convertView.setOnClickListener(null);
            toReturn = convertView;
        }else{
            toReturn = ((Activity) mContext).getLayoutInflater().inflate(R.layout.list_image_view, null);
            //inflate a new view
        }
        toReturn.setOnClickListener(null);
        UserImage currImage = images.get(position);
        IID = currImage.IID;
        prev = currImage.prev;
        editTime = currImage.editTime;
        hopsLeft = currImage.hopsRemaining;
        imageStr = currImage.imageString;
        ((TextView) toReturn.findViewById(R.id.text_last_user)).setText("Last edited by " + currImage.prev);
        ((TextView) toReturn.findViewById(R.id.text_remaining_time)).setText(currImage.hopsRemaining + " edits remaining");
        intent = new Intent(mContext, ImageCanvasActivity.class);
        intent.putExtra("existing", true);
        intent.putExtra("IID", IID);
        intent.putExtra("prev", prev);
        intent.putExtra("editTime", editTime);
        intent.putExtra("hops", hopsLeft);
        intent.putExtra("image", imageStr);
//        toReturn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //start a new activity with this intent
//                //mContext.startActivity(intent);
//            }
//        });
        return toReturn;
    }
}
