package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.tr069.TestCaseParameter.TestCaseParameterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCase {

	public enum TestCaseMethod {
		VALUE, ATTRIBUTE, FILE;
	}

	private Integer id;
	private Unittype unittype;
	private TestCaseMethod method;
	private String tags;
	private Boolean expectError;
	private List<TestCaseParameter> params = new ArrayList<TestCaseParameter>();
	private TestCaseFiles files;
	
	
//	private File inputFile;
//	private File outputFile;
	
	//	private List<TestCaseParameter> inputParams = new ArrayList<TestCaseParameter>();
	//	private List<TestCaseParameter> outputParams = new ArrayList<TestCaseParameter>();

	public TestCase() {

	}

	public TestCase(Unittype unittype, TestCaseMethod method, String tags, Boolean expectError) {
		super();
		this.unittype = unittype;
		this.method = method;
		this.tags = tags;
		this.expectError = expectError;
	}

	public Unittype getUnittype() {
		return unittype;
	}

	public void setUnittype(Unittype unittype) {
		this.unittype = unittype;
	}

	public TestCaseMethod getMethod() {
		return method;
	}

	public void setMethod(TestCaseMethod method) {
		this.method = method;
	}

	public String getTags() {
		return tags;
	}
	
	public String getIdFreeTags() {
		if (tags != null)
			return tags.replaceAll("[ID-" + id + "]", "");
		else
			return null;
	}
	
	public static List<String> getTagList(String tags) {
		List<String> tagList = new ArrayList<String>();
		if (tags == null)
			return tagList;
		if (tags.contains("[")) {
		while (true) {
			int startPos = tags.indexOf("[");
			if (startPos == -1)
				break;
			int endPos = tags.indexOf("]");
			if (endPos == -1 || endPos <= startPos + 1)
				break;
			tagList.add(tags.substring(startPos + 1, endPos));
			tags = tags.substring(endPos + 1);
		}
		} else {
			tagList = Arrays.asList(tags.split("\\s*\\,\\s*"));
		}
		return tagList;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Boolean getExpectError() {
		return expectError;
	}

	public void setExpectError(Boolean expectError) {
		this.expectError = expectError;
	}

	//	public List<TestCaseParameter> getInputParams() {
	//		return inputParams;
	//	}
	//
	//	public void setInputParams(List<TestCaseParameter> inputParams) {
	//		this.inputParams = inputParams;
	//	}
	//
	//	public List<TestCaseParameter> getOutputParams() {
	//		return outputParams;
	//	}
	//
	//	public void setOutputParams(List<TestCaseParameter> outputParams) {
	//		this.outputParams = outputParams;
	//	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (id != null) {
			sb.append("# The ID is auto-generated. In the event of importing this test case file back into Fusion, the following rules apply:\n");
			sb.append("#\tChanging the id : By changing it you may accidentally overwrite another test-case or create a new one.\n");
			sb.append("#\tDeleting the id : You will make a new test-case, possibly a duplicate of this test case.\n");
			sb.append(String.format("%-12s", "ID") + id + "\n\n");
		}
		sb.append("# The METHOD can be either VALUE, ATTRIBUTE or FILE. Make sure that the parameter definitions below match the method type\n");
		sb.append(String.format("%-12s", "METHOD") + method.toString() + "\n\n");
		if (tags != null) {
			sb.append("# The TAGS is a series of strings used to identify a set of test cases. Each string must be enclosed in square brackets [ ].\n");
			sb.append(String.format("%-12s", "TAGS") + tags.toString() + "\n\n");
		}
		if (expectError != null) {
			sb.append("# Set EXPECTERROR to true if the test case is designed to fail.\n");
			sb.append(String.format("%-12s", "EXPECTERROR") + expectError.toString() + "\n\n");
		}
		sb.append("\n");

		if (files != null && files.getInputFile() != null) {
			sb.append("# If VALUE is FILE, specifies an input file to send instead of SET/GET/FAC TR-069 parameters.\n");
			sb.append(String.format("%-12s", "INPUT") + files.getInputFile().getName() + "\n\n");
		}
		
		if (files != null && files.getOutputFile() != null) {
			sb.append("# If VALUE is FILE, specifies an output file to send instead of SET/GET/FAC TR-069 parameters.\n");
			sb.append(String.format("%-12s", "OUTPUT") + files.getOutputFile().getName() + "\n\n");
		}
				
		sb.append("# Specify one more SET parameters, to be used in either SetParameterValue or SetParameterAttribute method.\n");
		sb.append("# If METHOD is ATTRIBUTE, a number between 0 and 2 is expected, and will be used as the notification value\n");
		sb.append("# If METHOD is VALUE, a value/string is expected, and will be used as the parameter value\n");
		sb.append("#\tFormat/syntax : SET <ParameterName> <Value>|0|1|2\n");
		sb.append("#\tExample       : SET InternetGatewayDevice.DeviceInfo.ProvisioningCode HelloWorld\n");
		sb.append("#\tExample       : SET InternetGatewayDevice.DeviceInfo.ProvisioningCode 0\n");
		for (TestCaseParameter tcp : params) {
			if (tcp.getType() == TestCaseParameterType.SET) {
				if (method == TestCaseMethod.VALUE)
					sb.append(String.format("%1$-12s%2$-120s", tcp.getType().toString(), tcp.getUnittypeParameter().getName()) + " " + tcp.getValue() + "\n");
				else
					sb.append(String.format("%1$-12s%2$-120s", tcp.getType().toString(), tcp.getUnittypeParameter().getName()) + " " + tcp.getNotification() + "\n");
			}
		}
		sb.append("\n\n");

		sb.append("# Specify one more GET parameters, to be used in either SetParameterValue or SetParameterAttribute method.\n");
		sb.append("# If METHOD is ATTRIBUTE, a number between 0 and 2 is expected, and will be used as the notification value\n");
		sb.append("# If METHOD is VALUE, a value/string is expected, and will be used as the parameter value\n");
		sb.append("#\tFormat/syntax : GET <ParameterName> <Value>|0|1|2\n");
		sb.append("#\tExample       : GET InternetGatewayDevice.DeviceInfo.ProvisioningCode HelloWorld\n");
		sb.append("#\tExample       : GET InternetGatewayDevice.DeviceInfo.ProvisioningCode 0\n");
		sb.append("# The value are what we expect from the device if the SET parameters in this test case have already been run.\n");
		sb.append("# If a FACTORY RESET has been run after the SET method, this value is not used for comparison - check the\n");
		sb.append("# next section\n");
		sb.append("# It is also allowed to specify the following special values:\n");
		sb.append("#\t[STRING] - Any value is accepted - also blank/empty string\n");
		sb.append("#\t[EMPTY]  - A blank/emtpy string\n");
		sb.append("#\t[NUMBER] - Any integer is accepted\n");
		for (TestCaseParameter tcp : params) {
			if (tcp.getType() == TestCaseParameterType.GET) {
				if (method == TestCaseMethod.VALUE)
					sb.append(String.format("%1$-12s%2$-120s", tcp.getType().toString(), tcp.getUnittypeParameter().getName()) + " " + tcp.getValue() + "\n");
				else
					sb.append(String.format("%1$-12s%2$-120s", tcp.getType().toString(), tcp.getUnittypeParameter().getName()) + " " + tcp.getNotification() + "\n");
			}
		}
		sb.append("\n\n");

		sb.append("# Specify one more FAC parameters, to be used in either SetParameterValue or SetParameterAttribute method.\n");
		sb.append("# If METHOD is ATTRIBUTE, a number between 0 and 2 is expected, and will be used as the notification value\n");
		sb.append("# If METHOD is VALUE, a value/string is expected, and will be used as the parameter value\n");
		sb.append("#\tFormat/syntax : FAC <ParameterName> <Value>|0|1|2\n");
		sb.append("#\tExample       : FAC InternetGatewayDevice.DeviceInfo.ProvisioningCode HelloWorld\n");
		sb.append("#\tExample       : FAC InternetGatewayDevice.DeviceInfo.ProvisioningCode 0\n");
		sb.append("# The value will be used to compare with the results from the device, prior to a FactoryReset method.\n");
		sb.append("#\t[STRING] - Any value is accepted - also blank/empty string\n");
		sb.append("#\t[EMPTY]  - A blank/emtpy string\n");
		sb.append("#\t[NUMBER] - Any integer is accepted\n");
		for (TestCaseParameter tcp : params) {
			if (tcp.getType() == TestCaseParameterType.FAC) {
				if (method == TestCaseMethod.VALUE)
					sb.append(String.format("%1$-12s%2$-120s", tcp.getType().toString(), tcp.getUnittypeParameter().getName()) + " " + tcp.getValue() + "\n");
				else if (tcp.getNotification() == -1)
					sb.append(String.format("%1$-12s%2$-120s", tcp.getType().toString(), tcp.getUnittypeParameter().getName()) + " [NUMBER]\n");
				else
					sb.append(String.format("%1$-12s%2$-120s", tcp.getType().toString(), tcp.getUnittypeParameter().getName()) + " " + tcp.getNotification() + "\n");
			}
		}
		return sb.toString();
	}

	public boolean equals(Object o) {
		if (!(o instanceof TestCase))
			return false;
		TestCase tc = (TestCase) o;
		if (tc.getId() != null && this.getId() != null && tc.getId().intValue() == this.getId())
			return true;

		if (tc.getExpectError() == null && this.getExpectError() != null || tc.getExpectError() != null && this.getExpectError() == null)
			return false;
		if (tc.getExpectError() != null && tc.getExpectError().booleanValue() != this.getExpectError().booleanValue())
			return false;

		if (tc.getMethod() != this.getMethod())
			return false;

		if (tc.getTags() == null && this.getTags() != null || tc.getTags() != null && this.getTags() == null)
			return false;
		if (tc.getIdFreeTags() != null && !tc.getIdFreeTags().equals(this.getIdFreeTags()))
			return false;

		if (tc.getUnittype().getId().intValue() != this.getUnittype().getId())
			return false;

		if (tc.getParams().size() != this.getParams().size())
			return false;
		//		if (tc.getOutputParams().size() != this.getOutputParams().size())
		//			return false;

		int equalCount = 0;
		for (TestCaseParameter tcParam : tc.getParams()) {
			for (TestCaseParameter thisParam : this.getParams()) {
				if (tcParam.equals(thisParam))
					equalCount++;
			}
		}
		if (equalCount != this.getParams().size())
			return false;

		//		equalCount = 0;
		//		for (TestCaseParameter tcParam : tc.getOutputParams()) {
		//			for (TestCaseParameter thisParam : this.getOutputParams()) {
		//				if (tcParam.equals(thisParam))
		//					equalCount++;
		//			}
		//		}
		//		if (equalCount != this.getOutputParams().size())
		//			return false;

		return true;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<TestCaseParameter> getParams() {
		return params;
	}

	public void setParams(List<TestCaseParameter> params) {
		this.params = params;
	}

	public TestCaseFiles getFiles() {
		return files;
	}

	public void setFiles(TestCaseFiles files) {
		this.files = files;
	}

//	public File getInputFile() {
//		return inputFile;
//	}
//
//	public void setInputFile(File inputFile) {
//		this.inputFile = inputFile;
//	}
//
//	public File getOutputFile() {
//		return outputFile;
//	}
//
//	public void setOutputFile(File outputFile) {
//		this.outputFile = outputFile;
//	}

	
	
}
