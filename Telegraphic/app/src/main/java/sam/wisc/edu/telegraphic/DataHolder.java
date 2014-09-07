package sam.wisc.edu.telegraphic;

import android.graphics.Bitmap;

/**
 * Created by Sam on 9/6/2014.
 */
public class DataHolder {
    public static String username;
    public static String password;
    public static String accessToken;
    //public static Bitmap drawingBMP;
    public static String storageLoc = "TelegraphicPrefs";
    public static String baseURL = "http://kersten.io:8888";
    public static String registerURL = "/user/register";
    public static String loginURL = "/user/login";
    public static String userListURL = "/user/list";
    public static String imageCreateURL = "/image/create";
    public static String imageUpdateURL = "/image/update"; //still need id afterwards
    public static String imageQueryURL = "/image/query";
    public static String imageCleanupURL = "/image/seen";
    //public static UserImage currImage;
}
