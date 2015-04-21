package nl.uva.heuristiek.view;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.model.Course;

import javax.swing.*;
import java.awt.*;

/**
 * Created by remco on 12/04/15.
 */
public class RoomSchedulePanel extends JPanel {

    RoomDaySchedulePanel[] mPanels = new RoomDaySchedulePanel[Constants.DAY_COUNT];

    public RoomSchedulePanel(Course.Activity[] activities, int room) {
        setLayout(new GridLayout(0, 5));
        for (int day = 0; day < Constants.DAY_COUNT; day++) {
            mPanels[day] = new RoomDaySchedulePanel(activities, room, day);
            add(mPanels[day]);
        }
        setSize(400, 300);
    }

    public void setActivities(Course.Activity[] activities) {
        for (RoomDaySchedulePanel panel : mPanels)
            panel.setActivities(activities);
    }

}
