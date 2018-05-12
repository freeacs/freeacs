package com.owera.tr069client.messages;

import com.owera.tr069client.Arguments;
import com.owera.tr069client.HttpHandler;
import com.owera.tr069client.monitor.Status;

import java.io.IOException;

public class Empty {

	public static String execute(Arguments args, HttpHandler httpHandler, Status status) throws IOException {
		String req = null;
		return httpHandler.send(req, args, status, "EM");
	}
}
