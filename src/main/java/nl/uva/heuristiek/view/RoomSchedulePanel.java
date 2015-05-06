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

    public RoomSchedulePanel() {
        setLayout(new GridLayout(0, 5));
        for (int day = 0; day < Constants.DAY_COUNT; day++) {
            mPanels[day] = new RoomDaySchedulePanel();
            add(mPanels[day]);
        }
        setSize(400, 300);
    }

    public void addActivity(int timeSlot, Course.Activity activity) {
        mPanels[timeSlot/4].addActivity(timeSlot % 4, activity);
    }

    public void reset() {
        for (RoomDaySchedulePanel panel : mPanels) {
            panel.reset();
        }
    }

    public void removeActivity(int timeSlot) {
        mPanels[timeSlot/Constants.INDEX_COUNT].removeActivity(timeSlot % Constants.INDEX_COUNT);
    }
}
