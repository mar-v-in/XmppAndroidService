package org.xmpp.android;

import org.junit.Assert;
import org.junit.Test;
import org.xmpp.android.connection.Connection;
import org.xmpp.android.connection.ConnectionListener;
import org.xmpp.android.connection.XmppConnection;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.util.SysoutConnection;

public class XmppConnectionTest {
	@Test
	public void testOpenClose() throws Exception {
		Connection connection = XmppConnection
				.open(LoginData.TEST_LOGIN_JID, new ConnectionListener() {
					@Override
					public void stanzaRead(Stanza stanza) {
						stanza.asXmppStanza().pushTag(new SysoutConnection());
					}
				});
		Assert.assertNotNull(connection);
		connection.getStream().pushTag(new SysoutConnection());
		connection.send("<presence><show>away</show><status>Test</status><priority>51</priority></presence>");
		connection.flush();
		Thread.sleep(1000000);
		connection.close();
	}
}
