package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Sam on 9/7/2014.
 */
public class ImagesFragment extends ListFragment {
    ArrayList<String> images = new ArrayList<String>();
    ArrayList<UserImage> imageList = new ArrayList<UserImage>();
    int mCurCheckPosition = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle){
        View view = inflater.inflate(R.layout.activity_image_list, container, false);
        ArrayAdapter<String> imageAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                images);
        setListAdapter(imageAdapter);

        return super.onCreateView(inflater, container, icicle);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((CombinedImageActivity) this.getActivity()).prepare(imageList.get(position));
    }


}
