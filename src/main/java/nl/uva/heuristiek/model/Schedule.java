package nl.uva.heuristiek.model;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import nl.uva.heuristiek.Constants;

import java.security.SecureRandom;
import java.util.*;

/**
 * Created by remco on 08/04/15.
 */
public class Schedule {

    public static final int FLAG_ACTIVITY_SORT_STUDENTS_DESC = 0x0;
    public static final int FLAG_ACTIVITY_SORT_STUDENTS_ASC = 0x1;
    public static final int FLAG_ACTIVITY_SORT_RANDOM = 0x2;

    public static final int FLAG_PLAN_METHOD_CONSTRUCTIVE = 0x0;
    public static final int FLAG_PLAN_METHOD_RANDOM = 0x1 << 2;

    private static final int MASK_ACTIVITY_SORT = 0x3;
    private static final int MASK_PLAN_METHOD = 0x1 << 2;
    private final int mFlags;

    private ScheduleStateListener mListener;
    private Course.Activity[] mSchedule;
    private ArrayList<Student> mStudents;
    private Collection<Course> mCourses;
    private ArrayList<Course.Activity> mActivities;
    private int mPenalty = -1;
    private Integer[] mTimeslots;
    private Set<Course.Activity> mPenaltyActivities = new HashSet<Course.Activity>();

    int mActivitiesPlanned = 0;

    public Schedule(Collection<Course> courses, ArrayList<Student> students, int flags) {
        this(courses, students, flags, null);
    }

    public Schedule(@NotNull Collection<Course> courses, @NotNull ArrayList<Student> students, int flags, @Nullable Integer[] timeslots) {
        mCourses = courses;
        mStudents = students;
        mFlags = flags;
        mActivities = new ArrayList<>();
        for (Course course : courses) {
            mActivities.addAll(course.getActivities());
        }
        Collections.sort(mActivities, getActivityComparator(flags & MASK_ACTIVITY_SORT));
        mSchedule = new Course.Activity[Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT];
        if (timeslots == null) {
            mTimeslots = new Integer[20];
            for (int i = 0; i < mTimeslots.length; i++) {
                mTimeslots[i] = i;
            }
            shuffleArray(mTimeslots);
        } else
            mTimeslots = timeslots;

    }

    private Comparator<Course.Activity> getActivityComparator(int activitySortFlag) {

        switch (activitySortFlag) {
            case FLAG_ACTIVITY_SORT_RANDOM:
                final Random random = new Random();
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return random.nextInt(2) - 1;
                    }
                };
            case FLAG_ACTIVITY_SORT_STUDENTS_ASC:
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return Integer.compare(o1.getStudents().size(), o2.getStudents().size());
                    }
                };
            default:
                return new Comparator<Course.Activity>() {
                    @Override
                    public int compare(Course.Activity o1, Course.Activity o2) {
                        return Integer.compare(o2.getStudents().size(), o1.getStudents().size());
                    }
                };
        }

    }

    public Collection<Course> getmCourses() {
        return mCourses;
    }

    public void setListener(ScheduleStateListener listener) {
        mListener = listener;
    }

    public Integer[] getTimeslots() {
        return mTimeslots;
    }

    private void planCourses() {
        final int count = mActivities.size();
        for (int i = 0; i < count; i++) {
            planActivity(mActivities.get(i), i);
            mListener.onStateChanged(mSchedule);
        }
//                System.out.println(String.format("Total activities to plan: %d", activities.size()));
//
//                System.out.println(String.format("Activities planned: %d", mActivitiesPlanned));
//                System.out.println(String.format("Total penalty: %d", mPenalty));
        mListener.onScheduleComplete(mSchedule);

    }

    public void plan() {
        switch (mFlags & MASK_PLAN_METHOD) {
            case FLAG_PLAN_METHOD_RANDOM:
                planRandom();
                break;
            default:
                planCourses();

        }
    }

    private void planRandom() {
        SecureRandom random = new SecureRandom();
        for (Course.Activity activity : mActivities) {
            int i = random.nextInt(mSchedule.length);
            while (mSchedule[i] != null)
                i = random.nextInt(mSchedule.length);
            mSchedule[i] = activity;
            activity.plan(i % 20);
        }
    }

    static void shuffleArray(Integer[] ar)
    {
        SecureRandom rnd = new SecureRandom();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private int bestFitRoom(final int students) {
        for (int i = 0; i < Constants.ROOM_COUNT; i++) {
            if (students < Constants.ROOM_CAPACATIES[i]) return i;
        }
        throw new RuntimeException("No suitable room found");
    }

    private void planActivity(Course.Activity activity, int activityIndex) {
        int room = bestFitRoom(activity.getStudents().size()), timeslotIndex = 0;
        float treshhold = 1;
        while (checkContraints(activity, room, mTimeslots[timeslotIndex]) < treshhold) {
            if (++timeslotIndex == Constants.TIMESLOT_COUNT) {
                if (room + 1 == Constants.ROOM_COUNT) {
                    treshhold -= 0.1f;
                    if (treshhold <= 0)
                        return;
                    room = bestFitRoom(activity.getStudents().size());
                } else {
//                    shuffleArray(randomTimeslots);
                    room++;

                }
                timeslotIndex = 0;
            }
        }
        mSchedule[room*20+mTimeslots[timeslotIndex]] = activity;
        activity.plan(mTimeslots[timeslotIndex]);
        mActivitiesPlanned++;
    }

    public Set<Course.Activity> getPenaltyActivities() {
        return mPenaltyActivities;
    }

    private double checkContraints(Course.Activity activity, int room, int timeslot) {
        double value = 1;
        if (mSchedule[room*20+timeslot] == null) {
            if (activity.isDayUsed(timeslot / 4)) value -= 0.5;
            final double studentNegativeValue = value / (double)activity.getStudents().size();
            for (Student student : activity.getStudents()) {
                if (!student.isAvailable(timeslot)) {
                    value -= studentNegativeValue;
                }
            }
            return value;
        }
        return 0;
    }

    public int getPenalty() {
        if (mPenalty == -1) {
            mPenalty = 0;
            for (Course course : mCourses) {
                mPenalty += course.getPenalty();
            }
            for (Student student : mStudents) {
                mPenalty += student.getPenalty();
            }
        }
        return mPenalty;
    }

    public int[] getRoomOccupation() {
        int[] roomOccupation = new int[Constants.ROOM_COUNT];

        for (int i = 0; i < (Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT); i++) {
            if (mSchedule[i] != null) {
                roomOccupation[i / 20]++;
            }
        }

        int[] roomOccupationPercentage = new int[Constants.ROOM_COUNT];

        for (int i = 0; i < Constants.ROOM_COUNT; i++) {
            roomOccupationPercentage[i] = roomOccupation[i] * 100 / 20;
        }

        return roomOccupationPercentage;
    }

    public int[] getSeatOccupationPerRoom() {
        int[] totalSeatOccupation = new int[Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT];
        int[] seatOccupation = new int[Constants.ROOM_COUNT];

        for (int i = 0; i < (Constants.ROOM_COUNT*Constants.TIMESLOT_COUNT); i++) {
            totalSeatOccupation[i] = 0;
            int roomCapacity = Constants.ROOM_CAPACATIES[i/20];
            if (mSchedule[i] != null) {
                totalSeatOccupation[i] = 100 * mSchedule[i].getStudents().size() / roomCapacity;
            }
        }

        for (int i = 0; i < totalSeatOccupation.length; i++) {
            seatOccupation[i/20] += totalSeatOccupation[i];
        }

        for (int i = 0; i < seatOccupation.length; i++) {
            seatOccupation[i] = seatOccupation[i] / 20;
        }

        return seatOccupation;
    }

    public Course.Activity[] getSchedule() {
        return mSchedule;
    }

    public interface ScheduleStateListener {
        void onStateChanged(Course.Activity[] schedule);
        void onScheduleComplete(Course.Activity[] activities);
    }

}
