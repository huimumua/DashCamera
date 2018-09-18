package com.askey.dvr.cdr7010.dashcam.adas;

class Profiler {
    private static final String TAG = Profiler.class.getSimpleName();
    private int mIndex;
    private int[] mRecords;
    private final String NAME;
    private final int RECORD_SIZE;
    private int mUsedSize;
    private long mLogStartTime;
    private int mLogTimes;

    /* For calculation and toString() */
    private int mStatAvg, mStatMax, mStatMin;
    private int mMax, mMin;

    /**
     * @param name name of what the profiler is for
     * @param size the size record to statistics
     */
    public Profiler(String name, int size) {
        NAME = name;
        RECORD_SIZE = size;
        mUsedSize = 0;
        mRecords = new int[RECORD_SIZE];
        mLogTimes = 0;
        mMax = Integer.MIN_VALUE;
        mMin = Integer.MAX_VALUE;
    }

    public void logStart() {
        long timestamp = System.currentTimeMillis();
        mLogStartTime = timestamp;
    }

    public void logFinish() {
        int elapsed = (int) (System.currentTimeMillis() - mLogStartTime);
        mRecords[mIndex] = elapsed;
        if (mUsedSize < RECORD_SIZE) {
            mUsedSize++;
        }
        if (++mIndex >= RECORD_SIZE) {
            mIndex = 0;
        }
        if (elapsed > mMax) {
            mMax = elapsed;
        }
        if (elapsed < mMin) {
            mMin = elapsed;
        }
        mLogTimes++;
    }

    @Override
    public String toString() {
        if (mUsedSize == 0) {
            return "{" + TAG + "[" + NAME + "] " +
                    "Size=" + mUsedSize + "}";
        }
        updateStatistics();
        return "{" + TAG + "[" + NAME + "]" +
                "Size=" + mUsedSize +
                ", Stat(Avg/Max//Min)=(" + mStatAvg +
                "/" + mStatMax +
                "/" + mStatMin +
                ")" +
                ", Max=" + mMax +
                ", Min=" + mMin +
                ", LogTimes=" + mLogTimes +
                '}';
    }

    private void updateStatistics() {
        int sum = 0;
        mStatMax = Integer.MIN_VALUE;
        mStatMin = Integer.MAX_VALUE;
        synchronized (mRecords) {
            for (int i = 0; i < mUsedSize; i++) {
                int time = mRecords[i];
                if (time > mStatMax) {
                    mStatMax = time;
                }
                if (time < mStatMin) {
                    mStatMin = time;
                }
                sum += time;
            }
        }
        mStatAvg = sum / mUsedSize;
    }

}