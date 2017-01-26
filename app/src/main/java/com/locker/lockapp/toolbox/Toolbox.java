package com.locker.lockapp.toolbox;

import android.content.Intent;
import android.os.Bundle;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by sredorta on 1/16/2017.
 */
public class Toolbox {

    //Dump all extras of an intent
    public static void dumpIntent(Intent i){
        Logs.i("------- Dumping Intent start", Toolbox.class);
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                Logs.i("[" + key + "=" + bundle.get(key)+"]", Toolbox.class);
            }
        }
        Logs.i("------- Dumping Intent end", Toolbox.class);
    }
}
