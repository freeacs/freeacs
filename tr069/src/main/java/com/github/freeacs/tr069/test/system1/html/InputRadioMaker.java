package com.github.freeacs.tr069.test.system1.html;

import java.util.ArrayList;
import java.util.List;

public class InputRadioMaker extends Element {

	class RadioButton {
		private String value;
		private String title;
		private boolean checked;

		public RadioButton() {
		}

		public RadioButton(String value, String title) {
			this.value = value;
			this.title = title;
		}

		public RadioButton(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public boolean isChecked() {
			return checked;
		}

		public void setChecked(boolean checked) {
			this.checked = checked;
		}

	}

	private String name;
	private String checkedValue;
	private List<RadioButton> buttons = new ArrayList<RadioButton>();

	public InputRadioMaker(String name) {
		this.name = name;
	}

	public void addButton(String value) {
		buttons.add(new RadioButton(value));
	}

	public void addButton(String value, String title) {
		buttons.add(new RadioButton(value, title));
	}

	public String getCheckedValue() {
		return checkedValue;
	}

	public void setCheckedValue(String checkedValue) {
		this.checkedValue = checkedValue;
	}

	public int getNumberOfButtons() {
		return buttons.size();
	}

	public String makeHtml(String tab) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buttons.size(); i++) {
			sb.append(makeHtml(tab, i));
		}
		return sb.toString();
	}

	public String makeHtml(String tab, int index) {
		Element anchor = new Element();
		RadioButton rb = buttons.get(index);
		if (rb.getTitle() != null)
			anchor.add(rb.getTitle());
		Element input = anchor.input(name, "radio", buttons.get(index).getValue());
		if (checkedValue.equals(rb.getValue()))
			input.attribute("checked");
		return anchor.toString(tab);

	}
	
	public String toString(String tab) {
		return makeHtml(tab);
	}

}
