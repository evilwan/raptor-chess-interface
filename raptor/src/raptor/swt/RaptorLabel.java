/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class RaptorLabel extends Composite {
	protected CLabel label;

	public RaptorLabel(Composite parent, int labelStyle) {
		this(parent, labelStyle, SWT.CENTER);
	}

	public RaptorLabel(Composite parent, int labelStyle, int verticalAlignment) {
		super(parent, SWT.NONE);
		setLayout(SWTUtils.createMarginlessGridLayout(1, true));
		label = new CLabel(this, labelStyle);
		label.setLayoutData(new GridData(SWT.FILL, verticalAlignment, true,
				true));
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		label.addMouseListener(listener);
	}

	public CLabel getLabel() {
		return label;
	}

	public String getText() {
		return label.getText();
	}

	@Override
	public void removeMouseListener(MouseListener listener) {
		label.removeMouseListener(listener);
	}

	public void setAlignment(int style) {
		label.setAlignment(style);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		label.setBackground(color);
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		label.setFont(font);
	}

	@Override
	public void setForeground(Color color) {
		super.setForeground(color);
		label.setForeground(color);
	}

	public void setImage(Image image) {
		label.setImage(image);
	}

	public void setLabel(CLabel label) {
		this.label = label;
	}

	public void setText(String text) {
		label.setText(text);
	}

}
