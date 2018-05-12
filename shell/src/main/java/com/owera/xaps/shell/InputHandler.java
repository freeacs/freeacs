package com.owera.xaps.shell;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.File;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.shell.output.ListingReader;
import com.owera.xaps.shell.output.OutputHandler;
import com.owera.xaps.shell.util.StringUtil;

import java.io.*;
import java.sql.SQLException;

public class InputHandler {

	private BufferedReader br;

	public InputHandler(String filename, OutputHandler oh, Unittype unittype) throws FileNotFoundException, SQLException, NoAvailableConnectionException {
		if (oh != null && oh.getListing() != null) {
			ListingReader lr = new ListingReader(oh.getListing());
			br = new BufferedReader(lr);
		} else if (filename != null) {
			filename = filename.trim();
			if (unittype != null) {
				File f = unittype.getFiles().getByName(filename);
				if (f != null) {
					byte[] content = f.getContent();
					ByteArrayInputStream bais = new ByteArrayInputStream(content);
					InputStreamReader isr = new InputStreamReader(bais);
					br = new BufferedReader(isr);
				}
			}
			if (br == null) {
				FileReader fr = new FileReader(filename);
				br = new BufferedReader(fr);
			}
		}
	}

	public boolean isInput() {
		if (br != null)
			return true;
		else
			return false;
	}

	public String[] read() throws Exception {
		if (br != null) {
			String fileLine = br.readLine();
			if (fileLine != null) {
				if (fileLine.trim().equals(""))
					return read();
				if (fileLine.startsWith("#"))
					return read();
				return StringUtil.split(fileLine);
			}
			return null;
		}
		return null;
	}

	public void close() throws IOException {
		if (br != null)
			br.close();
	}

}
