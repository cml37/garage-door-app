/*
 * Created by JFormDesigner on Fri May 26 11:58:23 EDT 2017
 */

package com.lenderman.garage.jfd;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Christopher Lenderman
 */
@SuppressWarnings("serial")
public class GarageDoorInstance extends JPanel
{
    public GarageDoorInstance()
    {
        initComponents();
    }

    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY
        // //GEN-BEGIN:initComponents
        labelName = new JLabel();
        labelStatus = new JLabel();
        buttonChange = new JButton();
        labelDuration = new JLabel();

        // ======== this ========
        setLayout(null);

        // ---- labelName ----
        labelName.setText("Name");
        add(labelName);
        labelName.setBounds(10, 15, 95, labelName.getPreferredSize().height);

        // ---- labelStatus ----
        labelStatus.setText("Status");
        add(labelStatus);
        labelStatus.setBounds(125, 15, 115,
                labelStatus.getPreferredSize().height);

        // ---- buttonChange ----
        buttonChange.setText("Change");
        add(buttonChange);
        buttonChange.setBounds(395, 10, 85,
                buttonChange.getPreferredSize().height);

        // ---- labelDuration ----
        labelDuration.setText("Duration");
        add(labelDuration);
        labelDuration.setBounds(260, 15, 115,
                labelDuration.getPreferredSize().height);

        { // compute preferred size
            Dimension preferredSize = new Dimension();
            for (int i = 0; i < getComponentCount(); i++)
            {
                Rectangle bounds = getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width,
                        preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height,
                        preferredSize.height);
            }
            Insets insets = getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            setMinimumSize(preferredSize);
            setPreferredSize(preferredSize);
        }
        // //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY
    // //GEN-BEGIN:variables
    public JLabel labelName;
    public JLabel labelStatus;
    public JButton buttonChange;
    public JLabel labelDuration;
    // JFormDesigner - End of variables declaration //GEN-END:variables
}
