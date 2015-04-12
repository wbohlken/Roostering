package nl.uva.heuristiek.view;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.model.Course;

import javax.swing.*;
import java.awt.*;

/**
 * Created by remco on 12/04/15.
 */
public class RoomDaySchedulePanel extends JPanel {

    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    private final Course.Activity[] mActivities;

    public RoomDaySchedulePanel(Course.Activity[] activities) {
        mActivities = activities;

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Dimension size = getSize();
        int cellHeight = (int) (size.getHeight() / Constants.INDEX_COUNT);
        for (int i = 0; i < Constants.INDEX_COUNT; i++) {
            if (mActivities[i] == null)
                g2d.setColor(GREEN);
            else {
                g2d.setColor(RED);
            }
            g2d.fillRect(0, cellHeight * i, (int) size.getWidth(), cellHeight);
            if (mActivities[i] != null) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(null, 0, 8));
                g2d.drawString(mActivities[i].getName()+"\n"+mActivities[i].getStudents().size()+" studenten", 10, cellHeight * i + 3);
            }
        }
    }
}
