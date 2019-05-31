package com.clevertap.pushtemplates;

import android.util.Log;

final class PTLog {

    static void debug(String message){
            Log.d(Constants.LOG_TAG,message);
    }

    static void info(String message){
        Log.i(Constants.LOG_TAG,message);
    }

    static void verbose(String message){
        Log.v(Constants.LOG_TAG,message);
    }

    static void error(String message){
        Log.e(Constants.LOG_TAG,message);
    }

    static void debug(String message, Throwable t){
        Log.d(Constants.LOG_TAG,message, t);
    }

    static void info(String message, Throwable t){
        Log.i(Constants.LOG_TAG,message, t);
    }

    static void verbose(String message, Throwable t){
        Log.v(Constants.LOG_TAG,message, t);
    }

    static void error(String message, Throwable t){
        Log.e(Constants.LOG_TAG,message, t);
    }

}
