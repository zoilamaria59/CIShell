package org.cishell.templates.wizards.pagepanels;

import org.cishell.templates.staticexecutable.providers.InputDataProvider;
import org.cishell.templates.staticexecutable.providers.InputParameterProvider;
import org.cishell.templates.wizards.staticexecutable.InputDataItem;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.service.metatype.AttributeDefinition;

/*
 */
public class SpecifyTemplateStringPanel extends Composite
		implements SelectionListener {
	public static final String PLACEHOLDER_LABEL =
		"Template String Placeholders";
	public static final String TEMPLATE_STRING_LABEL = "Template String";
	public static final String INSERT_PLACEHOLDER_BUTTON_LABEL = "Insert";
	
	public static final String PLACEHOLDER_LABEL_COLUMN_LABEL = "Item";
	public static final String PLACEHOLDER_COLUMN_LABEL =
		"Placeholder String for Item";
	
	public static final int COLUMN_WIDTH = 85;
	public static final int TABLE_HEIGHT = 300;
	
	private Table placeholderTable;
	private Button insertPlaceholderButton;
	private TemplateOption templateStringOption;
	
	public SpecifyTemplateStringPanel(Composite parent,
									  int style,
									  TemplateOption templateStringOption) {
		super(parent, style);
		
		this.templateStringOption = templateStringOption;
		
		setLayout(createLayoutForThis());
		
		createHeader();
		this.placeholderTable = createPlaceholderTable();
		this.insertPlaceholderButton = createInsertPlaceholderButton();
		createTemplateStringText();
	}
	
	public void updateControls(InputParameterProvider inputParameterProvider,
							   InputDataProvider inputDataProvider) {
		this.placeholderTable.deselectAll();
		this.placeholderTable.removeAll();
		
		AttributeDefinition[] inputParameters =
			inputParameterProvider.getInputParameters();
		
		for (int ii = 0; ii < inputParameters.length; ii++) {
			String label = inputParameters[ii].getName() +
						   " (" +
						   inputParameters[ii].getDescription() +
						   ")";
			String value = "\"${" + inputParameters[ii].getID() + "}\"";
			addTableItem(label, value);
		}
		
		InputDataItem[] inputDataItems =
			inputDataProvider.getInputDataItems();
		
		for (int ii = 0; ii < inputDataItems.length; ii++) {
			String label = inputDataItems[ii].getMimeType() + " File";
			String value = "\"${inFile[" + ii + "]}\"";
			addTableItem(label, value);
		}
	}
	
	public void widgetDefaultSelected(SelectionEvent selectionEvent) {
		insertButtonSelected(selectionEvent);
	}

	public void widgetSelected(SelectionEvent selectionEvent) {
		insertButtonSelected(selectionEvent);
	}
	
	private Layout createLayoutForThis() {
		GridLayout layout = new GridLayout(4, true);
		layout.makeColumnsEqualWidth = false;
		
		return layout;
	}
	
	private void createHeader() {
		Label placeholderTableLabel = new Label(this, SWT.NONE);
		placeholderTableLabel.setLayoutData(
			createPlaceholderTableLabelLayoutData());
		placeholderTableLabel.setText(PLACEHOLDER_LABEL);
		
		// This is to keep the layout manager happy and make everything line up
		// the way it should.
		Label dummyLabel = new Label(this, SWT.NONE);
		dummyLabel.setLayoutData(
			createInsertPlaceholderButtonLayoutData());
		dummyLabel.setText(INSERT_PLACEHOLDER_BUTTON_LABEL);
		dummyLabel.setVisible(false);
		
		// To accomodate for the option label control.
		new Label(this, SWT.NONE).setLayoutData(new GridData());
		
		Label templateStringLabel = new Label(this, SWT.NONE);
		templateStringLabel.setLayoutData(
			createTemplateStringTextLayoutData());
		templateStringLabel.setText(TEMPLATE_STRING_LABEL);
	}
	
	private Table createPlaceholderTable() {
		Table placeholderTable = new Table(
			this, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		placeholderTable.setLayoutData(createPlaceholderTableLayoutData());
		placeholderTable.setLinesVisible(true);
		placeholderTable.setHeaderVisible(true);
		
		TableColumn placeholderLabelColumn =
			new TableColumn(placeholderTable, SWT.NONE);
		placeholderLabelColumn.pack();
		placeholderLabelColumn.setWidth(COLUMN_WIDTH);
		placeholderLabelColumn.setText(PLACEHOLDER_LABEL_COLUMN_LABEL);
		
		TableColumn placeholderColumn =
			new TableColumn(placeholderTable, SWT.NONE);
		placeholderColumn.pack();
		placeholderColumn.setWidth(COLUMN_WIDTH);
		placeholderColumn.setText(PLACEHOLDER_COLUMN_LABEL);
		
		return placeholderTable;
	}
	
	private Button createInsertPlaceholderButton() {
		Button insertPlaceholderButton = new Button(this, SWT.PUSH);
		insertPlaceholderButton.setLayoutData(
			createInsertPlaceholderButtonLayoutData());
		insertPlaceholderButton.setText(INSERT_PLACEHOLDER_BUTTON_LABEL);
		insertPlaceholderButton.addSelectionListener(this);
		
		return insertPlaceholderButton;
	}
	
	private void createTemplateStringText() {
		this.templateStringOption.createControl(this, 2);
	}
	
	private Object createPlaceholderTableLabelLayoutData() {
		GridData data = new GridData();
		data.horizontalAlignment = SWT.CENTER;
		
		return data;
	}
	
	private Object createPlaceholderTableLayoutData() {
		GridData data = new GridData();
		data.horizontalAlignment = SWT.CENTER;
		data.heightHint = TABLE_HEIGHT;
		
		return data;
	}
	
	private Object createInsertPlaceholderButtonLayoutData() {
		GridData data = new GridData();
		
		return data;
	}
	
	private Object createTemplateStringTextLayoutData() {
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		
		return data;
	}
	
	private void addTableItem(String label, String value) {
		String[] tableItemText = new String[] { label, value };
		TableItem tableItem = new TableItem(this.placeholderTable, SWT.NONE);
		tableItem.setText(tableItemText);
	}
	
	private void insertButtonSelected(SelectionEvent selectionEvent) {
		int selectedTableItemIndex = this.placeholderTable.getSelectionIndex();
		
		if (selectedTableItemIndex != -1) {
			TableItem selectedTableItem =
				this.placeholderTable.getItem(selectedTableItemIndex);
			String placeholder = selectedTableItem.getText(1);
			
			insertPlaceholder(placeholder);
		}
	}
	
	private void insertPlaceholder(String placeholder) {
		/*if (!this.templateStringOption.getSelectionText().equals("")) {
			// If there is a selection, replace the selection with the
			// placeholder.
			this.templateStringText.insert(placeholder);
		} else {
			// Otherwise, insert the placeholder at the fixedCaretPosition.
			int caretPosition = this.templateStringText.getCaretPosition();
			int fixedCaretPosition = fixCaretPosition(caretPosition);
			this.templateStringText.setSelection(fixedCaretPosition);
			this.templateStringText.insert(" " + placeholder + " ");
		}*/
	
		this.templateStringOption.setValue(
			this.templateStringOption.getValue().toString() +
			" " +
			placeholder);
	}
	
	/*private int fixCaretPosition(int caretPosition) {
		int fixedCaretPosition = caretPosition;
		String templateStringText = this.templateStringText.getText();
		
		if (fixedCaretPosition == 0 || templateStringText.length() == 0) {
			return fixedCaretPosition;
		} else {
			char currentCharacter = templateStringText.charAt(fixedCaretPosition);
			
			while (!Character.isWhitespace(currentCharacter) &&
				   fixedCaretPosition < templateStringText.length()) {
				fixedCaretPosition++;
				currentCharacter =
					templateStringText.charAt(fixedCaretPosition);
			}
		}
		
		return fixedCaretPosition;
	}*/
}