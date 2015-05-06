package nl.uva.heuristiek.view;

import nl.uva.heuristiek.Constants;
import nl.uva.heuristiek.Context;
import nl.uva.heuristiek.model.Course;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Created by remco on 12/04/15.
 */
public class RoomDaySchedulePanel extends JPanel {

    private Context mContext;
    private Course.Activity[] mDayActivities = new Course.Activity[Constants.INDEX_COUNT];

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Dimension size = getSize();
        int cellHeight = (int) (size.getHeight() / Constants.INDEX_COUNT);
        for (int i = 0; i < mDayActivities.length; i++) {
            if (mDayActivities[i] == null)
                g2d.setColor(Color.GREEN);
            else {
                g2d.setColor(mDayActivities[i].getColor());
            }
            g2d.fillRect(0, cellHeight * i, (int) size.getWidth(), cellHeight);
            if (mDayActivities[i] != null) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(null, 0, 12));
                g2d.drawString(mDayActivities[i].getCourse().getCourseId() + "\n" + mDayActivities[i].getStudents().size() + " studenten", 10, cellHeight * i + 20);
                g2d.drawString(String.valueOf(mDayActivities[i].getId()), 20, cellHeight * i + 50);
            }
        }
    }

    public void setContext(Context context) {
//        mActivities = context.getActivities();
        mContext = context;
    }

    public void addActivity(int time, Course.Activity activity) {
        mDayActivities[time] = activity;
    }

    public void reset() {
        Arrays.fill(mDayActivities, null);
    }

    public void removeActivity(int time) {
        mDayActivities[time] = null;
    }
}
