package com.github.freeacs.web.app.page.file;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Files;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.WebConstants;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.fileupload.FileItem;

/**
 * Handles add/delete and update of firmwares.
 *
 * @author Morten
 * @author Jarl A.
 */
public class FilePage extends AbstractWebPage {
  /** The xaps. */
  private ACS acs;

  /** The unittype. */
  private Unittype unittype;

  /** The name. */
  private String name;

  /** The description. */
  private String description;

  /** The type. */
  private FileType fileTypeEnum;

  /** The version number. */
  private String versionNumber;

  private String targetName;

  /** The bytes. */
  private byte[] bytes;

  /** The content as string, if specified in textarea on Filepage. */
  private String content;

  /** The formsubmit. */
  private String formsubmit;

  /** The delete list. */
  private List<String> deleteList = new ArrayList<>();

  /** The input data. */
  private FileData inputData = new FileData();

  /** The file id. */
  private Integer id;

  /** The update map. */
  //	private HashMap<String, Map<String, String>> updateMap = new HashMap<String, Map<String,
  // String>>();

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
  private void actionParse(ParameterParser req) {
    if (!"Clear".equals(formsubmit)) {
      id = inputData.getId().getInteger();
      FileItem file = req.getFileUpload("filename");
      if (file != null) {
        bytes = file.get();
      }
      name = inputData.getName().getStringWithoutTags();
      description = inputData.getDescription().getStringWithoutTagsAndContent();
      String fileTypeStr = inputData.getType().getString();
      if (fileTypeStr != null) {
        fileTypeEnum = FileType.valueOf(fileTypeStr);
      }
      date = inputData.getSoftwaredate().getDateOrDefault(new Date());
      versionNumber = inputData.getVersionNumber().getStringWithoutTags();
      targetName = inputData.getTargetName().getString();
      content = inputData.getContent().getString();
    }

    String item;
    Enumeration<?> names = req.getKeyEnumeration();
    while (names.hasMoreElements() && (item = (String) names.nextElement()) != null) {
      if (item.startsWith("delete::")) {
        deleteList.add(item.substring(8));
      }
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
      if ("Upload file".equals(formsubmit)) {
        File compare = files.getByVersionType(versionNumber, fileTypeEnum);
        if (compare != null && !compare.getName().equals(name)) {
          throw new IllegalArgumentException(
              "Cannot add/change file, beacuse version + file type is the same as for "
                  + compare.getName());
        }
        Date softwaredate = date;
        if (files.getByName(name) == null) {
          File file =
              new File(
                  unittype,
                  name,
                  fileTypeEnum,
                  description,
                  versionNumber,
                  softwaredate,
                  targetName,
                  acs.getUser());
          if (bytes != null && bytes.length > 0) {
            file.setBytes(bytes);
          } else if (fileTypeEnum != FileType.SOFTWARE && content != null) {
            file.setBytes(content.getBytes());
          } else {
            throw new IllegalArgumentException(
                "No file or content specified (content will be neglected if file type is SOFTWARE) - cannot create file");
          }
          files.addOrChangeFile(file, acs);
          // We no longer want to automatically select the recently uploaded file,
          // instead we wish to leave all the fields cleared for another upload.
          // (requested by Jarl-Even 2013-03-07)
          // id = file.getId();
        } else {
          throw new IllegalArgumentException(
              "The name " + name + " is already used for another file");
        }
      } else if ("Update file".equals(formsubmit) && id != null) {
        /* We don't allow changing a file to have the same version/filetype as an
         * already existing file, except of course if they have the same ID. */
        File compare = files.getByVersionType(versionNumber, fileTypeEnum);
        if (compare != null && !compare.getId().equals(id)) {
          throw new IllegalArgumentException(
              "Cannot add/change file, beacuse version"
                  + " and file type is the same as for "
                  + compare.getName());
        }

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
        else if (fileTypeEnum != FileType.SOFTWARE /* && content != null */) {
          file.setBytes(content.getBytes());
        }

        // This means that SOFTWARE file contents often goes unchanged at this point.
        files.addOrChangeFile(file, acs);
      } else if ("Delete selected files".equals(formsubmit)) {
        for (String name : deleteList) {
          File firmware = files.getByName(name);
          files.deleteFile(firmware, acs);
        }
      }
    }
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    inputData = (FileData) InputDataRetriever.parseInto(new FileData(), params);

    acs = ACSLoader.getXAPS(params.getSession().getId(), xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    if (inputData.getCmd().isValue("export")) {
      exportFirmware(outputHandler);
      return;
    }

    formsubmit = inputData.getFormSubmit().getString();

    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype());

    String utN = inputData.getUnittype().getString();
    if (utN != null) {
      unittype = acs.getUnittype(utN);
    }

    DropDownSingleSelect<Unittype> unittypeSelect =
        InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    Map<String, Object> rootMap = outputHandler.getTemplateMap();
    rootMap.put("unittypes", unittypeSelect);

    if (unittype != null) {
      actionParse(params);

      actionAddChangeDelete();

      FileType selectedFileType = null;
      if (id != null) {
        selectedFileType = unittype.getFiles().getById(id).getType();
      }
      DropDownSingleSelect<FileType> typeSelect = getTypeSelect(selectedFileType);

      Input fileTypeInput = inputData.getFileType();
      FileType fileType = null;
      List<File> list;
      if (fileTypeInput.getString() != null) {
        list = getFiles(FileType.valueOf(fileTypeInput.getString()));
        fileType = FileType.valueOf(fileTypeInput.getString());
      } else {
        list = getFiles();
      }

      rootMap.put("dummy", WebConstants.ALL_ITEMS_OR_DEFAULT);
      rootMap.put("num", list.size());
      rootMap.put("types", typeSelect);
      rootMap.put(
          "filetypes",
          InputSelectionFactory.getDropDownSingleSelect(
              fileTypeInput, fileType, typeSelect.getItems()));
      rootMap.put("files", list);

      if (id != null && unittype != null) {
        File f = unittype.getFiles().getById(id);
        if (f != null) {
          rootMap.put("fileobj", f);
          if (f.getType() != FileType.SOFTWARE) {
            rootMap.put("filecontent", new String(f.getContent()));
          } else {
            rootMap.put(
                "filecontent",
                "Software is assumed to be binary content, not possible to view or change content. A file must be specified below in order to perform a change.");
          }
        }
      }
    }
    outputHandler.setTemplatePathWithIndex("firmware");
  }

  protected List<File> getFiles() {
    return getFiles(null);
  }

  protected List<File> getFiles(FileType requiredType) {
    List<File> list = new ArrayList<>();
    if (unittype != null) {
      list = Arrays.asList(unittype.getFiles().getFiles(requiredType));
      list.sort(new FileComparator(FileComparator.DATE));
    }
    return list;
  }

  /**
   * Export firmware.
   *
   * @param outputHandler the outputHandler
   */
  private void exportFirmware(Output outputHandler) {
    try {
      String unittypeName = inputData.getUnittype().getString();
      Unittype unittype = acs.getUnittype(unittypeName);
      File firmware = unittype.getFiles().getByName(inputData.getName().getString());
      outputHandler.setContentType("application/binary");
      outputHandler.setDownloadAttachment(stripSpacesReplaceWithUnderScore(firmware.getName()));
      outputHandler.writeBytesToResponse(firmware.getContent());
    } catch (Exception ignored) {
    }
  }

  protected DropDownSingleSelect<FileType> getTypeSelect(FileType selectedFileType) {
    return getTypeSelect(
        selectedFileType,
        FileType.SOFTWARE,
        FileType.SHELL_SCRIPT,
        FileType.TR069_SCRIPT,
        FileType.TELNET_SCRIPT,
        FileType.UNITS,
        FileType.MISC);
  }

  /**
   * Adds the group profile.
   *
   * @return the drop down single select
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException the security exception exception
   */
  protected DropDownSingleSelect<FileType> getTypeSelect(
      FileType selectedFileType, FileType... types) {
    List<FileType> typeList = Arrays.asList(types);
    return InputSelectionFactory.getDropDownSingleSelect(
        inputData.getType(), selectedFileType, typeList);
  }
}
