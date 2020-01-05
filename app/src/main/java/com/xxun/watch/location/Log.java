package com.xxun.watch.location;


public class Log {
	public static final boolean DEBUG = false;

	public static void d(String tag, String msg) {
		if (DEBUG) {
			android.util.Log.d(tag, msg);
		}
	}
}

