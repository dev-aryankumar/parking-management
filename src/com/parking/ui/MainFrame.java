package com.parking.ui;

import com.parking.model.Employee;
import com.parking.model.User;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

public class MainFrame extends JFrame {

    public static final String PORTAL_SELECTOR = "PORTAL_SELECTOR";
    public static final String USER_AUTH = "USER_AUTH";
    public static final String USER_DASHBOARD = "USER_DASHBOARD";
    public static final String EMPLOYEE_LOGIN = "EMPLOYEE_LOGIN";
    public static final String EMPLOYEE_DASHBOARD = "EMPLOYEE_DASHBOARD";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final UserDashboardPanel userDashboardPanel;
    private final EmployeeDashboardPanel employeeDashboardPanel;

    public MainFrame() {
        setTitle("Smart Parking Management System");
        applyResponsiveSize();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        userDashboardPanel = new UserDashboardPanel(this);
        employeeDashboardPanel = new EmployeeDashboardPanel(this);
        UserAuthPanel userAuthPanel = new UserAuthPanel(this);

        cards.add(new PortalSelectorPanel(this), PORTAL_SELECTOR);
        cards.add(userAuthPanel, USER_AUTH);
        cards.add(userDashboardPanel, USER_DASHBOARD);
        cards.add(new EmployeeLoginPanel(this), EMPLOYEE_LOGIN);
        cards.add(employeeDashboardPanel, EMPLOYEE_DASHBOARD);

        setContentPane(cards);
        showCard(PORTAL_SELECTOR);
    }

    /**
     * Sizes the window to the usable screen area. Swing sizes are logical
     * units, so the physical screen size is divided by the display scale
     * (Windows 125% / 150% would otherwise push the window off-screen).
     */
    private void applyResponsiveSize() {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        double scale = gc.getDefaultTransform().getScaleX();
        if (scale <= 0) {
            scale = 1.0;
        }
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int logicalWidth = (int) Math.floor(screen.width / scale);
        int logicalHeight = (int) Math.floor(screen.height / scale);

        int width = Math.min(1000, logicalWidth - 40);
        int height = Math.min(700, logicalHeight - 30);
        width = Math.max(760, width);
        height = Math.max(470, height);

        setSize(width, height);
        setMinimumSize(new Dimension(720, 450));
        cards.setBackground(Theme.BG);
    }

    public void showCard(String cardName) {
        cardLayout.show(cards, cardName);
    }

    public void navigateToUserDashboard(User user) {
        userDashboardPanel.setUser(user);
        showCard(USER_DASHBOARD);
    }

    public void navigateToEmployeeDashboard(Employee employee) {
        employeeDashboardPanel.setEmployee(employee);
        showCard(EMPLOYEE_DASHBOARD);
    }

    public void logout() {
        showCard(PORTAL_SELECTOR);
    }
}
