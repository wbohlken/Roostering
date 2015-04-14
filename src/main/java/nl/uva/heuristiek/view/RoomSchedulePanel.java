package nl.uva.heuristiek.view;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.model.Course;

import javax.swing.*;
import java.awt.*;

/**
 * Created by remco on 12/04/15.
 */
public class RoomSchedulePanel extends JPanel {

    private final Course.Activity[] mActivities;

    public RoomSchedulePanel(Course.Activity[] activities, int room) {
        mActivities = activities;
        setLayout(new GridLayout(0, 5));
        for (int day = 0; day < Constants.DAY_COUNT; day++) {
            add(new RoomDaySchedulePanel(mActivities, room, day));
        }
        setSize(400, 300);
    }
}
