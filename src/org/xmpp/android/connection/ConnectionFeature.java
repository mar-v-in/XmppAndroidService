package org.xmpp.android.connection;

public interface ConnectionFeature {
	boolean isSupported();

	void start();
}
