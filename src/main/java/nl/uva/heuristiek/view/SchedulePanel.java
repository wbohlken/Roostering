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
            add(new RoomSchedulePanel(mSchedule.forRoom(room)));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        Dimension size = getSize();
        g2d.setColor(new Color(125, 167, 116));
        g2d.fillRect(0, 0, (int) size.getWidth(), (int) size.getHeight());

    }


}
