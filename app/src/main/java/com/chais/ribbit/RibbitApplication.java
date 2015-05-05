package com.chais.ribbit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Enrique on 04/05/2015.
 */
public class RibbitApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.enableLocalDatastore(this);
		Parse.initialize(this, "Ek6938i9YHbfWGxEHVPHhQaWZ1ylN0LeocKbm9oE", "ZlsXacw3sATIdAtl8inY7xI3TPXSDR6DW7ZHiq4A");
	}
}
