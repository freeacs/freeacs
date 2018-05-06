package com.owera.xaps.web.app.page.file;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.File;
import com.owera.xaps.dbi.FileType;
import com.owera.xaps.dbi.Files;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.Input;
import com.owera.xaps.web.app.input.InputDataIntegrity;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

/**
 * Handles add/delete and update of firmwares.
 * 
 * @author Morten
 * @author Jarl A.
 */
public class FilePage extends AbstractWebPage {

	/** The xaps. */
	private XAPS xaps;

	/** The unittype. */
	private Unittype unittype;

	/** The name. */
	private String name;

	/** The description. */
	private String description;

	/** The type*/
	private FileType fileTypeEnum;

	/** The version number. */
	private String versionNumber;

	private String targetName;

	/** The bytes. */
	private byte[] bytes;

	/** The content as string, if specified in textarea on Filepage */
	private String content;

	/** The formsubmit. */
	private String formsubmit;

	/** The delete list. */
	private List<String> deleteList = new ArrayList<String>();

	/** The input data. */
	private FileData inputData = new FileData();

	/** The file id */
	private Integer id;

	/** The update map. */
	//	private HashMap<String, Map<String, String>> updateMap = new HashMap<String, Map<String, String>>();

	/** The date. */
	private Date date;

	/**
	 * Action parse.
	 *
	 * @param req the req
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws SecurityException the security exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws NoSuchMethodException the no such method exception
	 */
	private void actionParse(ParameterParser req) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (formsubmit == null || !formsubmit.equals("Clear")) {
			id = inputData.getId().getInteger();
			FileItem file = req.getFileUpload("filename");
			if (file != null)
				bytes = file.get();
			name = inputData.getName().getStringWithoutTags();
			description = inputData.getDescription().getStringWithoutTagsAndContent();
			String fileTypeStr = inputData.getType().getString();
			if (fileTypeStr != null)
				fileTypeEnum = FileType.valueOf(fileTypeStr);
			date = inputData.getSoftwaredate().getDateOrDefault(new Date());
			versionNumber = inputData.getVersionNumber().getStringWithoutTags();
			targetName = inputData.getTargetName().getString();
			content = inputData.getContent().getString();
		}

		String item = null;
		Enumeration<?> names = req.getKeyEnumeration();
		while (names.hasMoreElements() && (item = (String) names.nextElement()) != null) {
			if (item.startsWith("delete::")) {
				deleteList.add(item.substring(8));
			}
			//					else if (item.startsWith("update::")) {
			//						String[] arr = item.split("::");
			//						if (arr.length == 3 && isNumber(arr[1])) {
			//							Map<String, String> details = updateMap.get(arr[1]);
			//							if (details == null) {
			//								updateMap.put(arr[1], new HashMap<String, String>());
			//								details = updateMap.get(arr[1]);
			//							}
			//							if (arr[2].equals("name")) {
			//								details.put("name", req.getParameter(item));
			//							} else if (arr[2].equals("type")) {
			//								details.put("type", req.getParameter(item));
			//							} else if (arr[2].equals("description")) {
			//								details.put("description", req.getParameter(item));
			//							} else if (arr[2].equals("softwaredate")) {
			//								details.put("softwaredate", req.getParameter(item));
			//							}
			//						}
			//					}
		}
	}

	/**
	 * Action add change delete.
	 *
	 * @throws Exception the exception
	 */
	private void actionAddChangeDelete() throws Exception {
		if (formsubmit != null) {
			Files files = unittype.getFiles();
			if (formsubmit.equals("Upload file")) {
				File compare = files.getByVersionType(versionNumber, fileTypeEnum);
				if (compare != null && !compare.getName().equals(name))
					throw new IllegalArgumentException("Cannot add/change file, beacuse version + file type is the same as for " + compare.getName());
				Date softwaredate = date;
				if (files.getByName(name) == null) {
					File file = new File(unittype, name, fileTypeEnum, description, versionNumber, softwaredate, targetName, xaps.getUser());
					if (bytes != null && bytes.length > 0) {
						file.setBytes(bytes);
					} else if (fileTypeEnum != FileType.SOFTWARE && content != null) {
						file.setBytes(content.getBytes());
					} else {
						throw new IllegalArgumentException("No file or content specified (content will be neglected if file type is SOFTWARE) - cannot create file");
					}
					files.addOrChangeFile(file, xaps);
					// We no longer want to automatically select the recently uploaded file,
					// instead we wish to leave all the fields cleared for another upload.
					// (requested by Jarl-Even 2013-03-07)
					//id = file.getId();
				} else {
					throw new IllegalArgumentException("The name " + name + " is already used for another file");
				}
			} 
			else if (formsubmit.equals("Update file") && id != null) {
				
				/* We don't allow changing a file to have the same version/filetype as an
				 * already existing file, except of course if they have the same ID. */
				File compare = files.getByVersionType(versionNumber, fileTypeEnum);
				if (compare != null && !compare.getId().equals(id))
					throw new IllegalArgumentException("Cannot add/change file, beacuse version" +
							" and file type is the same as for " + compare.getName());
				
				File file = files.getById(id);
				file.setName(name);
				file.setDescription(description);
				file.setTargetName(targetName);
				file.setTimestamp(date);
				file.setType(fileTypeEnum);
				file.setVersion(versionNumber);
								
				// If we've gotten a new file upload; great -- just replace the previous file contents.
				if (bytes != null && bytes.length > 0) {
					file.setBytes(bytes);
				}
				
				// Otherwise, for non-SOFTWARE filetypes, get the content from the input text field instead.
				else if (fileTypeEnum != FileType.SOFTWARE/* && content != null*/) {
					file.setBytes(content.getBytes());
				}
				
				// This means that SOFTWARE file contents often goes unchanged at this point.
				files.addOrChangeFile(file, xaps);
			} else if (formsubmit.equals("Delete selected files")) {
				for (String name : deleteList) {
					File firmware = files.getByName(name);
					files.deleteFile(firmware, xaps);
				}

			}
		}
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (FileData) InputDataRetriever.parseInto(new FileData(), params);

		xaps = XAPSLoader.getXAPS(params.getSession().getId());
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		if (inputData.getCmd().isValue("export")) {
			exportFirmware(outputHandler);
			return;
		}

		formsubmit = inputData.getFormSubmit().getString();

		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype());

		String utN = inputData.getUnittype().getString();
		if (utN != null)
			unittype = xaps.getUnittype(utN);

		DropDownSingleSelect<Unittype> unittypeSelect = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		Map<String, Object> rootMap = outputHandler.getTemplateMap();
		rootMap.put("unittypes", unittypeSelect);

		if (unittype != null) {
			actionParse(params);

			actionAddChangeDelete();

			FileType selectedFileType = null;
			if (id != null)
				selectedFileType = unittype.getFiles().getById(id).getType();
			DropDownSingleSelect<FileType> typeSelect = getTypeSelect(selectedFileType);

			Input fileTypeInput = inputData.getFileType();
			FileType fileType = null;
			List<File> list = null;
			if (fileTypeInput.getString() != null) {
				list = getFiles(FileType.valueOf(fileTypeInput.getString()));
				fileType = FileType.valueOf(fileTypeInput.getString());
			} else
				list = getFiles();

			rootMap.put("dummy", WebConstants.ALL_ITEMS_OR_DEFAULT);
			rootMap.put("num", list.size());
			rootMap.put("types", typeSelect);
			rootMap.put("filetypes", InputSelectionFactory.getDropDownSingleSelect(fileTypeInput, fileType, typeSelect.getItems()));
			rootMap.put("files", list);

			if (id != null && unittype != null) {
				File f = unittype.getFiles().getById(id);
				if (f != null) {
					rootMap.put("fileobj", f);
					if (f.getType() != FileType.SOFTWARE)
						rootMap.put("filecontent", new String(f.getContent()));
					else
						rootMap.put("filecontent", "Software is assumed to be binary content, not possible to view or change content. A file must be specified below in order to perform a change.");
				}
			}
		}
		outputHandler.setTemplatePathWithIndex("firmware");
	}

	protected List<File> getFiles() {
		return getFiles(null);
	}

	protected List<File> getFiles(FileType requiredType) {
		List<File> list = new ArrayList<File>();
		if (unittype != null) {
			list = Arrays.asList(unittype.getFiles().getFiles(requiredType));
			//			File[] firmwareArr = unittype.getFiles().getFiles();
			//			for(File f: firmwareArr){
			//				if(requiredType!=null){
			//					if(f.getType().equals(requiredType))
			//						list.add(f);
			//				}else
			//					list.add(f);
			//			}
			Collections.sort(list, new FileComparator(FileComparator.DATE));
		}
		return list;
	}

	/**
	 * Export firmware.
	 *
	 * @param outputHandler the outputHandler
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	private void exportFirmware(Output outputHandler) {
		try {
			String unittypeName = inputData.getUnittype().getString();
			Unittype unittype = xaps.getUnittype(unittypeName);
			File firmware = unittype.getFiles().getByName(inputData.getName().getString());
			outputHandler.setContentType("application/binary");
			outputHandler.setDownloadAttachment(stripSpacesReplaceWithUnderScore(firmware.getName()));
			outputHandler.writeBytesToResponse(firmware.getContent());
		} catch (Exception ex) {
		}
	}

	protected DropDownSingleSelect<FileType> getTypeSelect(FileType selectedFileType) {
		return getTypeSelect(selectedFileType, FileType.SOFTWARE, FileType.SHELL_SCRIPT, FileType.TR069_SCRIPT, FileType.TELNET_SCRIPT, FileType.UNITS, FileType.MISC);
	}

	/**
	 * Adds the group profile.
	 *
	 * @return the drop down single select
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws SecurityException the security exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws NoSuchMethodException the no such method exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	protected DropDownSingleSelect<FileType> getTypeSelect(FileType selectedFileType, FileType... types) {
		List<FileType> typeList = Arrays.asList(types);
		DropDownSingleSelect<FileType> typeDropdown = InputSelectionFactory.getDropDownSingleSelect(inputData.getType(), selectedFileType, typeList);
		return typeDropdown;
	}
}
