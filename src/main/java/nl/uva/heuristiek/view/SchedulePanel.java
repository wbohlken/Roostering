package nl.uva.heuristiek.view;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Penalty;

import javax.swing.*;
import java.awt.*;

/**
 * Created by remco on 11/04/15.
 */
public class SchedulePanel extends JPanel {

    RoomSchedulePanel[] mPanels = new RoomSchedulePanel[Constants.ROOM_COUNT];
    private final ControlPanel mControlPanel;

    public SchedulePanel(ControlPanel.ControlInterface controlInterface) {
        setLayout(new GridLayout(3,3, 20, 20));
        for (int room = 0; room < Constants.ROOM_COUNT; room++) {
            RoomSchedulePanel comp = new RoomSchedulePanel(room);
            mPanels[room] = comp;
            add(mPanels[room]);
        }

        mControlPanel = new ControlPanel(controlInterface);
        add(mControlPanel);
    }

    public void setActivities(Course.Activity[] activities, Penalty penalty) {
        for (RoomSchedulePanel panel : mPanels)
            panel.setActivities(activities);
        mControlPanel.setPenalty(penalty);


    }

    public void setComplete(boolean complete) {
        mControlPanel.setComplete(complete);
    }
}
