package nl.uva.heuristiek.model;

/**
 * Created by remco on 29/04/15.
 */
public class Penalty {
    private int mCoursePenalty, mStudentPenalty, mSmallRoomPenalty, mRoomSlotPenalty;

    public Penalty(int coursePenalty, int studentPenalty, int smallRoomPenalty, int roomSlotPenalty) {
        mCoursePenalty = coursePenalty;
        mStudentPenalty = studentPenalty;
        mSmallRoomPenalty = smallRoomPenalty;
        mRoomSlotPenalty = roomSlotPenalty;
    }

    public int getCoursePenalty() {
        return mCoursePenalty;
    }

    public int getStudentPenalty() {
        return mStudentPenalty;
    }

    @Override
    public String toString() {
        return String.format("Total penalty: %d\nCourse penalty: %d\nStudent penalty: %d\nSmallRoom penalty: %d", getTotal(), mCoursePenalty, mStudentPenalty, mSmallRoomPenalty);
    }

    public int getTotal() {
        return mCoursePenalty+mStudentPenalty+mSmallRoomPenalty+mRoomSlotPenalty;
    }
}
