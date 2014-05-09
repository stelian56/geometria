/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GRatioInputPane extends JPanel {

    private JTextField numeratorTextField;

    private JTextField denominatorTextField;

    public GRatioInputPane(String numerator, String denominator) {
        layoutComponents(numerator, denominator);
    }

    private void layoutComponents(String numerator, String denominator) {
        GGraphicsFactory.getInstance().createTitledBorder(" "
                + GDictionary.get("Ratio") + " ", this);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());
        numeratorTextField =
            GGraphicsFactory.getInstance().createNumberInput(numerator);
        add(numeratorTextField);
        add(GGraphicsFactory.getInstance().createSmallRigidArea());
        JLabel label = new JLabel(" : ");
        add(label);
        add(GGraphicsFactory.getInstance().createSmallRigidArea());
        denominatorTextField =
            GGraphicsFactory.getInstance().createNumberInput(denominator);
        add(denominatorTextField);
        add(Box.createHorizontalGlue());
    }

    public void prefill(String numeratorString, String denominatorString) {
        numeratorTextField.setText(numeratorString);
        denominatorTextField.setText(denominatorString);
    }

    public void setInputEnabled(boolean enableInput) {
        numeratorTextField.setEnabled(enableInput);
        denominatorTextField.setEnabled(enableInput);
    }

    public String getNumerator() {
        return numeratorTextField.getText().trim();
    }

    public String getDenominator() {
        return denominatorTextField.getText().trim();
    }

    private static final long serialVersionUID = 1L;
}
