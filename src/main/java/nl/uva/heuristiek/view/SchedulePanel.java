package nl.uva.heuristiek.view;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Schedule;

import javax.swing.*;
import java.awt.*;

/**
 * Created by remco on 11/04/15.
 */
public class SchedulePanel extends JPanel {

    RoomSchedulePanel[] mPanels = new RoomSchedulePanel[Constants.ROOM_COUNT];

    public SchedulePanel(Course.Activity[] activities) {
        setLayout(new GridLayout(3,3, 20, 20));
        for (int room = 0; room < Constants.ROOM_COUNT; room++) {
            RoomSchedulePanel comp = new RoomSchedulePanel(activities, room);
            mPanels[room] = comp;
            add(mPanels[room]);
        }
    }

    public void setActivities(Course.Activity[] activities) {
        for (RoomSchedulePanel panel : mPanels)
            panel.setActivities(activities);
    }

}
