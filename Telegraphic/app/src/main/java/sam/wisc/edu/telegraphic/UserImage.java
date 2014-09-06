package sam.wisc.edu.telegraphic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by Sam on 9/6/2014.
 */
public class UserImage {
    String IID;
    String prev;
    int editTime;
    int hopsRemaining;
    String imageString;


    public UserImage(String IID, String prev, int edit, int hops, String image){
        this.IID = IID;
        this.prev = prev;
        this.editTime = edit;
        this.hopsRemaining = hops;
        this.imageString = image;

    }






}
