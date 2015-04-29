package nl.uva.heuristiek.model;

import nl.uva.heuristiek.Context;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by remco on 29/04/15.
 */
public class BaseModel {
    private Context mContext;

    public BaseModel(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    protected final ArrayList<Course.Activity> getActivities() {
        return mContext.getActivities();
    }

    protected final Map<String, Course> getCourseMap() {
        return mContext.getCourseMap();
    }

    protected final ArrayList<Student> getStudents() {
        return mContext.getStudents();
    }

    public int getActivityDay(int activityIndex) {
        return mContext.getActivitySlots()[activityIndex] / 20;
    }
}
