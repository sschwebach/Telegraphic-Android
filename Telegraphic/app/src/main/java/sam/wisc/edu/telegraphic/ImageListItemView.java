package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Sam on 9/6/2014.
 */
public class ImageListItemView implements AdapterView.OnClickListener{
    Context mContext;
    View mView;
    UserImage mImage;

    public ImageListItemView(Context c, UserImage i){
        mContext = c;
        this.mImage = i;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(mContext, ImageCanvasActivity.class);
        intent.putExtra("existing", true);
        intent.putExtra("IID", mImage.IID);
        intent.putExtra("prev", mImage.prev);
        intent.putExtra("editTime", mImage.editTime);
        intent.putExtra("hopsRemaining", mImage.hopsRemaining);
        intent.putExtra("imageString", mImage.imageString);
        ((Activity) mContext).startActivity(intent);
    }

    public View getView(){
        View newView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.list_image_view, null);
        ((ImageView) newView.findViewById(R.id.image_thumb)).setImageBitmap(Utilities.decodeBase64(mImage.imageString));
        ((TextView) newView.findViewById(R.id.text_last_user)).setText("Last edited by " + mImage.prev);
        ((TextView) newView.findViewById(R.id.text_remaining_time)).setText(mImage.hopsRemaining + " edits remaining");
        newView.setOnClickListener(this);
        return newView;

    }
}
