package m.google.ordenpedidosserver.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

import m.google.ordenpedidosserver.model.Requests;
import m.google.ordenpedidosserver.model.User;
import m.google.ordenpedidosserver.remote.ApiService;
import m.google.ordenpedidosserver.remote.IGeoCoordinates;
import m.google.ordenpedidosserver.remote.RetrofitClient;


public class Common {

    public static User currentUser;
    public static Requests currentRequest;

    public static String topicName= "News";

    public static String UPDATE= "Update";
    public static String DELETE= "Delete";
    public static final String USER_PHONE="userPhone";

    private static final String baseURL= " https://fcm.googleapis.com/";

    public static ApiService getFCMservice()
    {
        return RetrofitClient.getClient(baseURL).create(ApiService.class);
    }

    public static final int PICK_IMAGE_REQUEST = 71;


    public static String converCodeToStatus(String status){

        if(status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else if (status.equals("2"))
        return "Shipping";
        else
            return "Shipped";
    }


    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth,int newHeight){
        Bitmap scaleBitmap= Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);

        float scaleX= newWidth/(float)bitmap.getWidth();
        float scaleY= newHeight/(float)bitmap.getHeight();
        float pivotX= 0, pivoY= 0;

        Matrix scaleMatriz= new Matrix();
        scaleMatriz.setScale(scaleX,scaleY,pivotX,pivoY);

        Canvas canvas= new Canvas(scaleBitmap);
        canvas.setMatrix(scaleMatriz);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaleBitmap;
    }

    public static String getDate(long time)
    {
        Calendar calendar= Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder builder= new StringBuilder(DateFormat.format("dd-MM-yyy HH:mm",calendar).toString());
        return builder.toString();
    }



}
