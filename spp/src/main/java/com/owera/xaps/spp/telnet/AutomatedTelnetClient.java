package com.owera.xaps.spp.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

import com.owera.common.log.Logger;

public class AutomatedTelnetClient {
	private static Logger conversationLog = new Logger("ConversationTelnet");
	private static Logger logger = new Logger();
	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	private char[] lastCharsInPrompt = new char[] { '>', '#', ':' };
	private StringBuffer conversation = new StringBuffer();

	public AutomatedTelnetClient(String server, String user, String password, int port) throws TelnetClientException {
		try {
			// make sure 15 seconds without any activity is maximum
			//			telnet.setConnectTimeout(15000);
			//			telnet.setSoTimeout(15000);
			//			telnet.setDefaultTimeout(15000);
			// Connect to the specified server
			telnet.connect(server, port);

			TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
			//			EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
			//			SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

			try {
				telnet.addOptionHandler(ttopt);
				//				//				telnet.addOptionHandler(echoopt);
				//				telnet.addOptionHandler(gaopt);
			} catch (InvalidTelnetOptionException e) {
				System.err.println("Error registering option handlers: " + e.getMessage());
			}

			// Get input and output stream references
			in = telnet.getInputStream();
			out = new PrintStream(telnet.getOutputStream());
			login(user, password);
		} catch (TelnetClientException atce) {
			throw atce;
		} catch (Exception e) {
			throw new TelnetClientException("Exception occured in Telnet-session: " + e.getMessage());
		}
	}

	/* This loging procedure must handle the following cases:
	 * 1. The device does not prompt for any login-information, thus bypassing the entire login
	 * 2. The device only prompts for username, no password
	 * 3. The device only prompts for password, no username
	 * 4. The device prompts for both username and password
	 * 5. The device prompts for username, but no username is found - throw ex
	 * 6. The device prompts for password, but no password is found - throw ex
	 * 7. The device prompts for username twice, indicating wrong username - throw ex
	 * 8. The device prompts for password twice, indicating wrong password - throw ex
	 * 9. The device prompts for password first, then fails and ask for username/password
	 */
	private void login(String user, String password) throws TelnetClientException {
		boolean userWritten = false;
		boolean passWritten = false;
		while (true) {
			String str = read();
			str = str.toLowerCase();
			if (str.indexOf("login") > -1 || str.indexOf("username") > -1 || str.indexOf("password") > 0 - 1) {
				if (str.indexOf("login") > -1 || str.indexOf("username") > -1) {
					if (user != null) {
						if (userWritten) {
							throw new TelnetClientException("The telnet session failed in login, was prompted twice for username"); // 7.
						} else {
							write(user);
							userWritten = true;
							passWritten = false; // If username was asked after password - then we will probably retry password
						}
					} else {
						throw new TelnetClientException("The telnet session requires a username, but no username is found for this unit"); // 5.
					}
				}
				if (str.indexOf("password") > -1) {
					if (password != null) {
						if (passWritten) {
							throw new TelnetClientException("The telnet session failed in login, was prompted twice for password"); // 8.
						} else {
							write(password);
							passWritten = true;
						}
					} else {
						throw new TelnetClientException("The telnet session requires a password, but no password is found for this unit"); // 6.
					}
				}
			} else {
				return; // 1., 2., 3.
			}
		}
	}

	private static long TIMEOUT_MS = 3000;

	public String read() throws TelnetClientException {
		try {
			StringBuffer sb = new StringBuffer();
			long start = System.currentTimeMillis();
			while (in.available() == 0 && (System.currentTimeMillis() - start) < TIMEOUT_MS) {
				Thread.sleep(10);
			}
			if (System.currentTimeMillis() - start >= TIMEOUT_MS) {
				logger.warn("No response from client in  " + TIMEOUT_MS + " ms - will return empty string");
				return "";
			}
			char ch = (char) in.read();
			boolean possibleEOS = false;
			while (true) {
				sb.append(ch);
				conversation.append(ch);
				if (possibleEOS) {
					if (ch == ' ' || ch == '\t') {
						if (in.available() == 0) {
							return sb.toString();
						}
					} else {
						possibleEOS = false;
					}
				}
				for (char lastChar : lastCharsInPrompt) {
					if (ch == lastChar && in.available() == 0) {
						return sb.toString();
					} else if (ch == lastChar) {
						possibleEOS = true;
					}
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			throw new TelnetClientException("An error occured in read(): " + e.getMessage());
		}
	}

	public void write(String value) {
		try {
			out.println(value);
			out.flush();
			conversation.append(value + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String sendCommand(String command) {
		try {
			write(command);
			if (command != null) {
				command = command.trim().toLowerCase();
				if (command.equals("exit") || command.equals("quit"))
					return null;
			}
			return read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() throws IOException {
		if (conversationLog.isInfoEnabled())
			conversationLog.info(conversation.toString());
		telnet.disconnect();
	}

	public static void main(String[] args) {
		try {
			AutomatedTelnetClient telnet = new AutomatedTelnetClient("192.168.176.1", "admin", "p1ngc0m", 23);
			telnet.sendCommand("sys");
			telnet.sendCommand("mem");
			telnet.sendCommand("mem");
			//			telnet.sendCommand("quit ");
			telnet.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}