/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.rdb.core.dialog.export.sqltoapplication;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.hangum.tadpole.ace.editor.core.define.EditorDefine;
import com.hangum.tadpole.commons.google.analytics.AnalyticCaller;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.commons.util.GlobalImageUtils;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.rdb.core.Messages;
import com.hangum.tadpole.rdb.core.dialog.export.sqltoapplication.application.SQLToJavaConvert;

/**
 * sql to application string 
 * 
 * @author hangum
 *
 */
public class SQLToStringDialog extends Dialog {
	private static final Logger logger = Logger.getLogger(SQLToStringDialog.class);
	private UserDBDAO userDB;
	private Combo comboLanguageType;
	private String languageType = ""; //$NON-NLS-1$
	private String sql = ""; //$NON-NLS-1$
	
	private Text textConvert;
	private Text textVariable;

	/**
	 * Create the dialog.
	 * @param parentShell
	 * @param languageType 디폴트 변화 언어
	 * @param sql sql
	 */
	public SQLToStringDialog(Shell parentShell, UserDBDAO userDB, String languageType, String sql) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		
		this.userDB = userDB;
		this.languageType = languageType;
		this.sql = sql;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.get().SQLToStringDialog_2);
		newShell.setImage(GlobalImageUtils.getTadpoleIcon());
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.verticalSpacing = 4;
		gridLayout.horizontalSpacing = 4;
		gridLayout.marginHeight = 4;
		gridLayout.marginWidth = 4;
		
		Composite compositeBody = new Composite(container, SWT.NONE);
		compositeBody.setLayout(new GridLayout(1, false));
		compositeBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeTitle = new Composite(compositeBody, SWT.NONE);
		compositeTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeTitle.setLayout(new GridLayout(3, false));
		
		Label lblType = new Label(compositeTitle, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText(Messages.get().Language);
		
		comboLanguageType = new Combo(compositeTitle, SWT.READ_ONLY);
		comboLanguageType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				textVariable.setText("");
				sqlToStr();
			}
		});
		comboLanguageType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for(EditorDefine.SQL_TO_APPLICATION app : EditorDefine.SQL_TO_APPLICATION.values()) {
			comboLanguageType.add(app.toString());
			comboLanguageType.setData(app.toString(), app);
		}
		comboLanguageType.setText(this.languageType);
		
		Button btnOriginalText = new Button(compositeTitle, SWT.NONE);
		btnOriginalText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				textConvert.setText(sql);
			}
		});
		btnOriginalText.setText(Messages.get().SQLToStringDialog_4);
		
		Label lblVariable = new Label(compositeTitle, SWT.NONE);
		lblVariable.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVariable.setText(Messages.get().Variable);
		
		textVariable = new Text(compositeTitle, SWT.BORDER);
		textVariable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textVariable.setText(SQLToJavaConvert.DEFAULT_VARIABLE);
		
		Button btnConvertSQL = new Button(compositeTitle, SWT.NONE);
		btnConvertSQL.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sqlToStr();
			}
		});
		btnConvertSQL.setText(Messages.get().SQLToStringDialog_btnNewButton_text);
		
		textConvert = new Text(compositeBody, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		textConvert.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				sql = textConvert.getText();
			}
		});
		textConvert.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		textConvert.setText(sql);
		
		sqlToStr();
		
		// google analytic
		AnalyticCaller.track(this.getClass().getName());

		return container;
	}
	
	private void sqlToStr() {
		
		StringBuffer sbStr = new StringBuffer();
		String[] sqls = parseSQL();
		
		SQLToLanguageConvert slt = new SQLToLanguageConvert(userDB, (EditorDefine.SQL_TO_APPLICATION)comboLanguageType.getData(comboLanguageType.getText()) );

		String variable = textVariable.getText();
		if(StringUtils.isEmpty(variable)){ 
			variable = slt.getDefaultVariable();
			textVariable.setText(variable);
		}
		
		for(int i=0; i < sqls.length; i++) {
			if("".equals(StringUtils.trimToEmpty(sqls[i]))) continue; //$NON-NLS-1$
			
			if(i ==0) sbStr.append( slt.sqlToString(variable, sqls[i]) );
			else sbStr.append( slt.sqlToString(variable + i, sqls[i]) );
			
			// 쿼리가 여러개일 경우 하나씩 한개를 준다.
			sbStr.append("\r\n"); //$NON-NLS-1$
		}
		
		textConvert.setText(sbStr.toString());
	}
	
	private String[] parseSQL() {
		String[] arry = sql.split(PublicTadpoleDefine.SQL_DELIMITER); //$NON-NLS-1$
		 if( arry.length == 1) {
			 String ars[] = { sql };
			 return ars;
		 }
		 return arry;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.get().Close, true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(562, 481);
	}

}
