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
    private final int mOffset;

    public RoomDaySchedulePanel(Course.Activity[] activities, int room, int day) {
        mActivities = activities;
        mOffset = room * 20 + day * 4;

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Dimension size = getSize();
        int cellHeight = (int) (size.getHeight() / Constants.INDEX_COUNT);
        for (int i = 0; i < Constants.INDEX_COUNT; i++) {
            if (mActivities[mOffset + i] == null)
                g2d.setColor(Color.GREEN);
            else {
                g2d.setColor(mActivities[mOffset+i].getColor());
            }
            g2d.fillRect(0, cellHeight * i, (int) size.getWidth(), cellHeight);
            if (mActivities[mOffset + i] != null) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(null, 0, 12));
                g2d.drawString(mActivities[mOffset + i].getCourse().getCourseId() + "\n" + mActivities[mOffset + i].getStudents().size() + " studenten", 10, cellHeight * i + 20);
                g2d.drawString(String.valueOf(mActivities[mOffset + i].getId()), 20, cellHeight * i + 50);
            }
        }
    }
}
