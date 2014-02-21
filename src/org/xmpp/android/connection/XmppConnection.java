package org.xmpp.android.connection;

import android.util.Base64;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmpp.android.account.AccountHelper;
import org.xmpp.android.connection.resource.Bind;
import org.xmpp.android.connection.sasl.Auth;
import org.xmpp.android.connection.sasl.Mechanisms;
import org.xmpp.android.connection.sasl.Success;
import org.xmpp.android.connection.session.Session;
import org.xmpp.android.connection.stream.*;
import org.xmpp.android.connection.stream.Error;
import org.xmpp.android.connection.tls.Proceed;
import org.xmpp.android.connection.tls.StartTls;
import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.stanzas.IqStanza;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.TextStanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class XmppConnection implements Connection {
	private static final String TAG = "XMPP/Connection";
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private XmlPullParser xpp;
	private Stream stream;
	private Thread readingThread;
	private Jid jid;

	static {
		Auth.register();
		Mechanisms.register();
		Success.register();
		Stream.register();
		Features.register();
		Error.register();
		Proceed.register();
		StartTls.register();
		Bind.register();
		Session.register();
	}

	private XmppConnection(Socket socket) throws IOException {
		this.socket = socket;
		updateStreams();
	}

	public static Connection open(Jid jid, final ConnectionListener listener) throws IOException {
		// TODO: DNS for real connection details
		Log.d(TAG, "Opening Connection");
		final XmppConnection xmppConnection = new XmppConnection(new Socket(jid.getServer(), 5222));
		try {
			Log.d(TAG, "Initializing Connection");
			xmppConnection.init(jid);
		} catch (Exception e) {
			try {
				xmppConnection.close();
			} catch (Throwable ignored) {
			}
			throw new IOException(e);
		}

		xmppConnection.readingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "Started reading");
				while (!Thread.interrupted()) {
					try {
						xmppConnection.xpp.next();
					} catch (Exception e) {
						xmppConnection.handleException("while reading next xml", e);
					}
					Stanza stanza = xmppConnection.readElement();
					if (stanza != null) {
						listener.stanzaRead(stanza);
					}
				}
			}
		});
		xmppConnection.readingThread.start();
		return xmppConnection;
	}

	public static boolean testJid(Jid jid) {
		Connection connection;
		try {
			connection = open(jid, new ConnectionListener() {
				@Override
				public void stanzaRead(Stanza stanza) {
					//Ignore
				}
			});
			connection.close();
			return true;
		} catch (IOException e) {
			Log.w(TAG, "Test failed", e);
			return false;
		}
	}

	@Override
	public void close() {
		try {
			if (readingThread != null) {
				readingThread.interrupt();
			}
			readingThread = null;
			if (os != null) {
				stream.sendEndTag(this);
				os.close();
			}
			os = null;
			if (is != null) {
				is.close();
			}
			is = null;
		} catch (Throwable e) {
			handleException("while closing connection", e);
		}
	}

	@Override
	public void flush() {
		try {
			os.flush();
		} catch (Throwable e) {
			handleException("while flushing " + os, e);
		}
	}

	@Override
	public Jid getJid() {
		return jid;
	}

	@Override
	public Stream getStream() {
		return stream;
	}

	private void handleException(String info, Throwable throwable) {
		Log.w(TAG, String.format("Exception thrown %s", info), throwable);
	}

	private boolean handleLogin(Jid jid, Features streamFeatures)
			throws IOException, XmlPullParserException, UnrecoverableKeyException, NoSuchAlgorithmException,
				   KeyStoreException, KeyManagementException {
		for (Stanza xmppStanza : streamFeatures.getSubStanzas()) {
			if (xmppStanza instanceof Mechanisms) {
				if (Arrays.asList(((Mechanisms) xmppStanza).getMechanisms()).contains("PLAIN")) {
					new Auth("PLAIN",
							 Base64.encodeToString(("\0" + jid.getUser() + "\0" + jid.getPassword()).getBytes(), 0))
							.pushTag(this);

					xpp.next();

					Stanza success = readElement();
					if (success instanceof Success) {
						updateStreams();
						init(jid);
						return true;
					} else {
						throw new RuntimeException("Error while login: " + success.getStanzaType());
					}
				} else {
					throw new RuntimeException("WHAA, PLAIN not supported!!!");
				}
			}
		}
		return false;
	}

	private void handleResourceBind(Jid jid, Features streamFeatures) throws IOException, XmlPullParserException {
		for (Stanza xmppStanza : streamFeatures.getSubStanzas()) {
			if (xmppStanza instanceof Bind) {
				IqStanza iq = new IqStanza("set");
				Bind bind = new Bind();
				if (jid.getResource() != null) {
					XmppStanza element = new XmppStanza(null, AccountHelper.KEY_RESOURCE);
					element.addSubElement(new TextStanza(jid.getResource()));
					bind.addSubElement(element);
				}
				iq.asXmppStanza().addSubElement(bind);
				iq.asXmppStanza().pushTag(this);
				xpp.next();
				Stanza result = readElement();
				if (result instanceof IqStanza) {
					if ("result".equals(((IqStanza) result).getType())) {
						bind = (Bind) result.asXmppStanza().getSubStanzas().get(0);
						this.jid = Jid.of(bind.getJid());
						return;
					}
				}
				throw new RuntimeException("Error while bind: " + result);
			}
		}
	}

	private void handleSession(Features streamFeatures) throws IOException, XmlPullParserException {
		for (Stanza xmppStanza : streamFeatures.getSubStanzas()) {
			if (xmppStanza instanceof Session) {
				IqStanza iq = new IqStanza("set");
				iq.asXmppStanza().addSubElement(new Session());
				iq.asXmppStanza().pushTag(this);
				xpp.next();
				Stanza result = readElement();
				if (result instanceof IqStanza) {
					if ("result".equals(((IqStanza) result).getType())) {
						return;
					}
				}
				throw new RuntimeException("Error while session: " + result);
			}
		}
	}

	private boolean handleTls(Jid jid, Features streamFeatures)
			throws XmlPullParserException, IOException, KeyManagementException, NoSuchAlgorithmException,
				   KeyStoreException, UnrecoverableKeyException {
		for (Stanza xmppStanza : streamFeatures.getSubStanzas()) {
			if (xmppStanza instanceof StartTls) {
				new StartTls().pushTag(this);

				xpp.next();
				Stanza proceed = readElement();
				if (proceed instanceof Proceed) {
					SSLContext tls = setupTlsContext();
					socket = tls.getSocketFactory()
								.createSocket(socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
					System.out.println("TLS IS ENABLED!!!");
					updateStreams();
					init(jid);
					return true;
				} else {
					throw new RuntimeException("Error while tls");
				}
			}
		}
		return false;
	}

	private void init(Jid jid)
			throws IOException, XmlPullParserException, NoSuchAlgorithmException, KeyManagementException,
				   UnrecoverableKeyException, KeyStoreException {
		new Stream(jid.toString(), jid.getServer()).sendBeginTag(this);
		xpp.next();
		stream = (Stream) readElementBegin();
		xpp.next();
		Features streamFeatures = (Features) readElement();
		stream.addSubElement(streamFeatures);
		if (handleTls(jid, streamFeatures))
			return;
		if (handleLogin(jid, streamFeatures))
			return;
		handleResourceBind(jid, streamFeatures);
		handleSession(streamFeatures);
	}

	private Stanza readElement() {
		try {
			Stanza stanza = readElementBegin();
			int eventType = xpp.next();
			while ((eventType != XmlPullParser.END_DOCUMENT) && (eventType != XmlPullParser.END_TAG)) {
				if (eventType == XmlPullParser.TEXT) {
					stanza.asXmppStanza().addSubElement(new TextStanza(xpp.getText()));
				} else if (eventType == XmlPullParser.START_TAG) {
					stanza.asXmppStanza().addSubElement(readElement());
				}
				eventType = xpp.next();
			}
			return stanza;
		} catch (Throwable t) {
			handleException("while reading xml", t);
		}
		return null;
	}

	private Stanza readElementBegin() {
		try {
			int eventType = xpp.getEventType();
			if (eventType != XmlPullParser.START_TAG) {
				return null;
			}
			Map<String, String> attributes = new HashMap<String, String>();
			for (int i = 0; i < xpp.getAttributeCount(); ++i) {
				attributes.put(xpp.getAttributeName(i), xpp.getAttributeValue(i));
			}
			Stanza stanza = new XmppStanza(xpp.getNamespace(), xpp.getName(), attributes).encapsulate();
			//Log.d(TAG, stanza.toString());
			return stanza;
		} catch (Throwable t) {
			handleException("while reading xml", t);
		}
		return null;
	}

	@Override
	public void send(String string) {
		try {
			os.write(string.getBytes(Charset.forName("utf-8")));
		} catch (Throwable e) {
			handleException("while writing to " + os, e);
		}
	}

	private SSLContext setupTlsContext()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(null, null);
		KeyManager[] kms = kmf.getKeyManagers();

		SSLContext tls = SSLContext.getInstance("TLS");
		// TODO FIXME !!! This is not a SECURE socket !!!
		tls.init(kms, new javax.net.ssl.TrustManager[]{new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				//TODO: Implement
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				//TODO: Implement
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0]; //TODO: Implement
			}
		}}, new SecureRandom());
		return tls;
	}

	private void updateStreams() throws IOException {
		Log.d(TAG, (is == null) ? "Initialize in/out" : "Reset in/out");
		this.is = socket.getInputStream();
		this.os = socket.getOutputStream();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			xpp = factory.newPullParser();
			xpp.setInput(is, "UTF-8");
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		}
	}
}
