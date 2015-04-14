package nl.uva.heuristiek.view;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.model.Schedule;

import javax.swing.*;
import java.awt.*;

/**
 * Created by remco on 11/04/15.
 */
public class SchedulePanel extends JPanel {
    private final Schedule mSchedule;

    public SchedulePanel(Schedule schedule) {
        mSchedule = schedule;
        setLayout(new GridLayout(3,3, 20, 20));
        for (int room = 0; room < Constants.ROOM_COUNT; room++) {
            add(new RoomSchedulePanel(mSchedule.getActivities(), room));
        }
    }

}
