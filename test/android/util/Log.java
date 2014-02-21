package android.util;

public class Log {
	public static int w(String tag, String msg, Throwable t) {
		System.err.println("W/"+tag+": "+msg);
		t.printStackTrace();
		return 0;
	}

	public static int w(String tag, String msg) {
		System.err.println("W/"+tag+": "+msg);
		return 0;
	}

	public static int d(String TAG, String msg) {
		System.err.println("D/"+TAG+": "+msg);
		return 0;
	}
}
