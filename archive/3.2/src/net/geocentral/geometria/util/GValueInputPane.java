/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GValueInputPane extends JPanel {

    private JTextField valueTextField;

    public GValueInputPane(String value, String title) {
        layoutComponents(value, title);
    }

    private void layoutComponents(String value, String title) {
        GGraphicsFactory.getInstance().createTitledBorder(
                " " + title + " ", this);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());
        valueTextField =
            GGraphicsFactory.getInstance().createVariableInput(value);
        add(valueTextField);
        add(Box.createHorizontalGlue());
    }

    public String getInput() {
        return valueTextField.getText().trim();
    }

    public void setInput(String value) {
        valueTextField.setText(value);
    }
    
    private static final long serialVersionUID = 1L;
}
