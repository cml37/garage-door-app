/*
 * Created by JFormDesigner on Fri May 26 10:33:44 EDT 2017
 */

package com.lenderman.garage.jfd;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Christopher Lenderman
 */
@SuppressWarnings("serial")
public class GarageDoorGuiFrame extends JFrame
{
    public GarageDoorGuiFrame()
    {
        initComponents();
    }

    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY
        // //GEN-BEGIN:initComponents
        buttonUpdateStatus = new JButton();
        panel2 = new JPanel();
        label1 = new JLabel();
        labelLastUpdatedDate = new JLabel();

        // ======== this ========
        setMinimumSize(new Dimension(510, 220));
        setTitle("Garage Door Controller");
        setIconImage(new ImageIcon(getClass().getResource(
                "/com/lenderman/garage/images/garage-icon-black.png"))
                        .getImage());
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout("center:default:grow",
                "fill:default, 2*($lgap, default)"));

        // ---- buttonUpdateStatus ----
        buttonUpdateStatus.setText("Update Status");
        contentPane.add(buttonUpdateStatus, CC.xy(1, 1));

        // ======== panel2 ========
        {
            panel2.setLayout(null);

            // ---- label1 ----
            label1.setText("Last Updated:");
            panel2.add(label1);
            label1.setBounds(
                    new Rectangle(new Point(0, 0), label1.getPreferredSize()));

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for (int i = 0; i < panel2.getComponentCount(); i++)
                {
                    Rectangle bounds = panel2.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width,
                            preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height,
                            preferredSize.height);
                }
                Insets insets = panel2.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel2.setMinimumSize(preferredSize);
                panel2.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(panel2, CC.xy(1, 3));

        // ---- labelLastUpdatedDate ----
        labelLastUpdatedDate.setText("Date");
        contentPane.add(labelLastUpdatedDate, CC.xy(1, 5));
        setSize(510, 220);
        setLocationRelativeTo(getOwner());
        // //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY
    // //GEN-BEGIN:variables
    protected JButton buttonUpdateStatus;
    private JPanel panel2;
    private JLabel label1;
    protected JLabel labelLastUpdatedDate;
    // JFormDesigner - End of variables declaration //GEN-END:variables
}
