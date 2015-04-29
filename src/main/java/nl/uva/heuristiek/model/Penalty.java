package nl.uva.heuristiek.model;

/**
 * Created by remco on 29/04/15.
 */
public class Penalty {
    private int mCoursePenalty, mStudentPenalty;

    public Penalty(int coursePenalty, int studentPenalty) {
        mCoursePenalty = coursePenalty;
        mStudentPenalty = studentPenalty;
    }

    public int getCoursePenalty() {
        return mCoursePenalty;
    }

    public int getStudentPenalty() {
        return mStudentPenalty;
    }

    @Override
    public String toString() {
        return String.format("Total penalty: %d, Course penalty: %d, Student penalty: %d", mCoursePenalty+mStudentPenalty, mCoursePenalty, mStudentPenalty);
    }

    public int getTotal() {
        return mCoursePenalty+mStudentPenalty;
    }
}
