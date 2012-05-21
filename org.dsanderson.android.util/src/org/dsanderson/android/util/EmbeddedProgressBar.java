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

import org.dsanderson.util.IProgressBar;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 
 */
public class EmbeddedProgressBar implements IProgressBar {
	final TextView label;
	final ProgressBar progressBar;
	
	/**
	 * 
	 */
	public EmbeddedProgressBar(TextView label, ProgressBar progressBar) {
		this.label = label;
		this.progressBar = progressBar;
	}

	/* (non-Javadoc)
	 * @see org.dsanderson.util.IProgressBar#show()
	 */
	public void show() {
		label.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);

	}

	/* (non-Javadoc)
	 * @see org.dsanderson.util.IProgressBar#close()
	 */
	public void close() {
		label.setVisibility(View.GONE);
		label.setVisibility(View.GONE);

	}

	/* (non-Javadoc)
	 * @see org.dsanderson.util.IProgressBar#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		label.setText(message);
	}

	/* (non-Javadoc)
	 * @see org.dsanderson.util.IProgressBar#setProgress(int)
	 */
	public void setProgress(int progress) {
		progressBar.setProgress(progress);
	}

	/* (non-Javadoc)
	 * @see org.dsanderson.util.IProgressBar#incrementProgress()
	 */
	public void incrementProgress() {
		progressBar.incrementProgressBy(1);
	}

}
