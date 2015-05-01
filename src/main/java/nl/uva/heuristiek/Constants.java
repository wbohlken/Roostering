package nl.uva.heuristiek;

/**
 * Created by remco on 08/04/15.
 */
public class Constants {
    public static final int ROOM_COUNT = 7;
    public static final int DAY_COUNT = 5;
    public static final int INDEX_COUNT = 4;
    public static final int TIMESLOT_COUNT = DAY_COUNT*INDEX_COUNT;
    public static final int ROOMSLOT_COUNT = ROOM_COUNT*TIMESLOT_COUNT;

    public static final int[] ROOM_CAPACATIES = {
            20,
            22,
            41,
            48,
            56,
            60,
            117
    };

    public static final String[] ROOM_NAMES = {
            "A1.08", //
            "A1.06",
            "A1.04",
        "A1.10",
        "B0.201",
        "C0.110",
        "C1.11"
    };
    public static final int MAX_POPULATION_COUNT = 20;
    public static final int MIN_POPULATION_COUNT = 10;
    public static final boolean ELITISM = true;
}
