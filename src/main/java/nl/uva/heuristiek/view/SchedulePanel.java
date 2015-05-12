package nl.uva.heuristiek.view;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.model.Course;
import nl.uva.heuristiek.model.Penalty;
import nl.uva.heuristiek.model.Schedule;

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
            RoomSchedulePanel comp = new RoomSchedulePanel();
            mPanels[room] = comp;
            add(mPanels[room]);
        }

        mControlPanel = new ControlPanel(controlInterface);
        add(mControlPanel);
    }

    public void setPenalty(Penalty penalty) {
        mControlPanel.setPenalty(penalty);
    }

    public void setComplete(boolean complete) {
        mControlPanel.setComplete(complete);
    }

    public void reset() {
        for (RoomSchedulePanel panel : mPanels)
            panel.reset();
        mControlPanel.setComplete(false);
    }

    public void setSchedule(Schedule schedule) {
        Course.Activity[] activities = new Course.Activity[Constants.ROOMSLOT_COUNT];
        final int count = schedule.getContext().getActivities().size();
        for (int activityIndex = 0; activityIndex < count; activityIndex++) {
            int roomSlot = schedule.getRoomSlot(activityIndex);
            if (roomSlot != -1)
                activities[roomSlot] = schedule.getContext().getActivities().get(activityIndex);
        }
        setPenalty(schedule.getPenalty());
        mControlPanel.setComplete(schedule.isComplete());
    }

    public void addActivity(int roomSlot, Course.Activity activity) {
        mPanels[roomSlot/20].addActivity(roomSlot % 20, activity);
    }

    public void removeActivity(int roomSlot) {
        mPanels[roomSlot/20].removeActivity(roomSlot % 20);
    }
}
