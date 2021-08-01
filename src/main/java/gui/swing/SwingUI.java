package gui.swing;

import javax.swing.*;
import java.awt.event.*;

public class SwingUI extends JDialog {

    private JPanel contentPane;
    private JTabbedPane tabbedPane1;
    private JEditorPane editorPane1;
    private JButton GOButton;
    private JTabbedPane tabbedPane2;
    private JEditorPane editorPane2;
    private JEditorPane editorPane3;
    private JEditorPane editorPane4;

    public SwingUI() {
        setContentPane(contentPane);
        setModal(true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void main(String[] args) {
        SwingUI dialog = new SwingUI();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
