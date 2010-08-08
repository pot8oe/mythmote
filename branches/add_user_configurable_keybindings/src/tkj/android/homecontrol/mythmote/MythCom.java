package tkj.android.homecontrol.mythmote;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

/** Class that handles network communication with mythtvfrontend **/
public class MythCom {

	public interface StatusChangedEventListener extends EventListener {

		public void StatusChanged(String StatusMsg, int statusCode);
	}

	public static final int DEFAULT_MYTH_PORT = 6546;
	public static final int SOCKET_TIMEOUT = 2000;
	public static final int ENABLE_WIFI = 0;
	public static final int CANCEL = 1;
	public static final int STATUS_DISCONNECTED = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_CONNECTING = 3;
	public static final int STATUS_ERROR = 99;

	private static Timer _timer;
	private static Socket _socket;
	private static OutputStreamWriter _outputStream;
	private static InputStreamReader _inputStream;
	private static Activity _parent;
	private static ConnectivityManager _conMgr;
	private static String _status;
	private static String _tmpStatus;
	private static int _tmpStatusCode;
	private static StatusChangedEventListener _statusListener;
	private static FrontendLocation _frontend;

	private final Handler mHandler = new Handler();
	private final Runnable mSocketActionComplete = new Runnable() {
		public void run() {
			setStatus(_tmpStatus, _tmpStatusCode);
		}

	};

	/**
	 * TimerTask that probes the current connection for its mythtv screen.
	 **/
	private final TimerTask timerTaskCheckStatus = new TimerTask() {
		// Run at every timer tick
		public void run() {
			// only if socket is connected
			if (IsConnected()) {
				// set disconnected status if nothing is returned.
				if (queryMythScreen() == null) {
					// _timer.cancel();
					setStatus("Disconnected", STATUS_DISCONNECTED);
				} else {
					setStatus(_frontend.Name + " - Connected", STATUS_CONNECTED);
				}
			}
		}
	};

	/**
	 * Parent activity is used to get context
	 **/
	public MythCom(Activity parentActivity) {
		_parent = parentActivity;
		_conMgr = (ConnectivityManager) _parent
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		_timer = new Timer();
		_timer.schedule(timerTaskCheckStatus, 5000, 5000);
	}

	/**
	 * Connects to the given address and port. Any existing connection will be
	 * broken first
	 **/
	public void Connect(FrontendLocation frontend) {
		// disconnect before we connect
		this.Disconnect();

		// set address and port
		_frontend = frontend;

		// create toast for all to eat and enjoy
		Toast toast = Toast.makeText(_parent.getApplicationContext(),
				R.string.attempting_to_connect_str, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		this.setStatus("Connecting", STATUS_CONNECTING);

		// create a socket connecting to the address on the requested port
		this.connectSocket();
	}

	/**
	 * Closes the socket if it exists and it is already connected
	 **/
	public void Disconnect() {
		try {
			// send exit if connected
			if (this.IsConnected())
				this.sendData("exit\n");

			// check if output stream exists
			if (_outputStream != null) {
				_outputStream.close();
				_outputStream = null;
			}

			// check if input stream exists
			if (_inputStream != null) {
				// close input stream
				_inputStream.close();
				_inputStream = null;
			}

			if (this.IsConnected())
				_socket.close();
			if (_socket != null)
				_socket = null;
		} catch (IOException ex) {
			this.setStatus("Disconnect I/O error", STATUS_ERROR);
		}
	}

	public void SendCommand(String jumpPoint) {
		// send command data
		this.sendData(String.format("%s\n", jumpPoint));
	}

	public void SendJumpCommand(String jumpPoint) {
		// send command data
		this.sendData(String.format("jump %s\n", jumpPoint));
	}

	public void SendKey(String key) {
		// send command data
		this.sendData(String.format("key %s\n", key));
	}

	public void SendKey(char key) {
		// send command data
		this.sendData(String.format("key %s\n", key));
	}

	public void SendPlaybackCmd(String cmd) {
		// send command data
		this.sendData(String.format("play %s\n", cmd));
	}

	public void SetOnStatusChangeHandler(StatusChangedEventListener listener) {
		_statusListener = listener;
	}

	public String GetStatusStr() {
		return _status;
	}

	public boolean IsNetworkReady() {
		if (_conMgr != null && _conMgr.getActiveNetworkInfo().isConnected())
			return true;
		return false;
	}

	public boolean IsConnected() {
		if (_socket == null)
			return false;

		return (!_socket.isClosed()) && _socket.isConnected();
	}

	/** Returns true if a route to the given InetAddress is available. **/
	private static boolean checkRouteToHost(InetAddress address)
			throws IOException {
		// TODO SDK is depreciated. If we target a newer frame work chage to
		// SDK_INT
		int version = Integer.parseInt(android.os.Build.VERSION.SDK);

		if (version <= 4)// androind 1.x
		{
			return _conMgr.requestRouteToHost(ConnectivityManager.TYPE_WIFI,
					address.hashCode())
					|| _conMgr
							.requestRouteToHost(
									ConnectivityManager.TYPE_MOBILE, address
											.hashCode());
		} else // android 2.x
		{
			return address.isReachable(30);
		}

	}

	/** Connects _socket to _frontend using a separate thread **/
	private void connectSocket() {
		// create socket
		_socket = new Socket();

		Thread thread = new Thread() {
			public void run() {
				try {
					InetAddress address = java.net.InetAddress
							.getByName(_frontend.Address);

					if (checkRouteToHost(address)) {
						_socket.connect(new InetSocketAddress(address,
								_frontend.Port), SOCKET_TIMEOUT);
						_outputStream = new OutputStreamWriter(_socket
								.getOutputStream());
						_inputStream = new InputStreamReader(_socket
								.getInputStream());

						// check if everything was connected OK
						if (!_socket.isConnected() || _outputStream == null) {
							_tmpStatus = "Unknown error getting output stream.";
							_tmpStatusCode = STATUS_ERROR;
						} else {
							_tmpStatus = _frontend.Name + " - Connected";
							_tmpStatusCode = STATUS_CONNECTED;
						}
					} else {
						_tmpStatus = "No route to host: " + _frontend.Address;
						_tmpStatusCode = STATUS_ERROR;
					}
				} catch (UnknownHostException e) {
					_tmpStatus = "Unknown host: " + _frontend.Address;
					_tmpStatusCode = STATUS_ERROR;
				} catch (IOException e) {
					_tmpStatus = e.getLocalizedMessage() + ": "
							+ _frontend.Address;
					_tmpStatusCode = STATUS_ERROR;
				}

				// post results
				mHandler.post(mSocketActionComplete);
			}
		};

		// run thread
		thread.start();
	}

	/**
	 * Sends data to the output stream of the socket. Attempts to reconnect
	 * socket if connection does not already exist.
	 **/
	private void sendData(String data) {
		if (this.IsConnected() && _outputStream != null) {
			try {
				if (!data.endsWith("\n"))
					data = String.format("%s\n", data);

				_outputStream.write(data);
				_outputStream.flush();
			} catch (IOException e) {
				this.setStatus("I/O Error data", STATUS_ERROR);
			}
		}
	}

	/**
	 * Reads data from the input stream of the socket. Returns null if no data
	 * in received
	 **/
	private char[] readData() {
		if (this.IsConnected() && _inputStream != null) {
			char[] buf = new char[100];
			try {
				if (_inputStream.ready()) {
					int len = _inputStream.read(buf);
					if (len > 0)
						return buf;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// return null if no data was received
		return null;
	}

	/** Sets _status and fires the StatusChanged event **/
	private void setStatus(final String StatusMsg, final int code) {
		_parent.runOnUiThread(new Runnable() {

			public void run() {
				_status = StatusMsg;
				if (_statusListener != null)
					_statusListener.StatusChanged(StatusMsg, code);
			}

		});
	}

	/**
	 * Returns the string representation of the current mythfrontend screen
	 * location. Returns null on error
	 **/
	private String queryMythScreen() {
		// send query location command
		this.sendData("query location");

		// read input stream
		char[] data = this.readData();

		if (data != null && data.length > 0) {
			return data.toString();
		} else {
			// we're not receiving data the other
			// end must have disconnected
			return null;
		}
	}

}
