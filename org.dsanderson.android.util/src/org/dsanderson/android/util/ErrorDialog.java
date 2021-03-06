/**
 * @author David S Anderson
 *
 *
 * Copyright (C) 2012 David S Anderson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dsanderson.android.util;

import org.dsanderson.util.IErrorDialog;

import android.app.AlertDialog;
import android.content.Context;

/**
 * 
 */
public class ErrorDialog implements IErrorDialog {
	AlertDialog dialog;

	public ErrorDialog(Context context, Exception e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(e.getLocalizedMessage()).setCancelable(false);
		dialog = builder.create();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dsanderson.xctrailreport.core.IErrorDialog#open()
	 */
	public void show() {
		dialog.show();
	}

}
