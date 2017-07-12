package com.asix.miruchat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This is a general purpose class created by Asix Jin
 * So he doesn't have to write the same code over and
 * over when doing simple shit....
 */
public class AsixUtils {

    //region General
    public static Toast makeToast(Context context, String message){
        return Toast.makeText(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static Dialog createDialog(Activity activity, int layoutID){
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(layoutID);
        return dialog;
    }

    public static SharedPreferences getSharedPrefs(Activity activity){
        return activity.getSharedPreferences(activity.getString(R.string.prefName), Context.MODE_PRIVATE);
    }
    //endregion

    //region View Methods
    /*
         These methods are used for simple silly shit
         like getting text from EditText views or setting
         the text of a TextView

     */
    public static boolean setVisibility(View view, boolean isVisible){
        if(isVisible){
            view.setVisibility(View.VISIBLE);
        }else{
            view.setVisibility(View.GONE);
        }

        return isVisible;
    }

    public static String getEditText_Text(View editText){
        return ((EditText)editText).getText().toString();
    }

    public static int getEditText_Int(View editText, int defaultInt){
        try{
            int number = Integer.valueOf(((EditText)editText).getText().toString());
            return number;
        }catch (Exception e){
            e.printStackTrace();
            return defaultInt;
        }
    }
    //endregion

    //region String Manipulation

    public static boolean doesStringExist(String string){
        boolean yes = true;

        if(string == null){
            yes = false;
        }else{
            if(string.equalsIgnoreCase("")){
                yes = false;
            }
        }

        return yes;
    }
    //endregion

}

