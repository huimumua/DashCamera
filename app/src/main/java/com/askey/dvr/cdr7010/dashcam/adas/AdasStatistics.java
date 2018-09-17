package com.askey.dvr.cdr7010.dashcam.adas;

import java.util.HashMap;
import java.util.Map;

class AdasStatistics {
    private Map<ProfileItem, Profiler> mProfilers;
    private Map<TimesItem, Integer> mLogTimes;
    private static final int PROFILE_TIME_NUM = 30;

    public void log(TimesItem timesItem) {
        mLogTimes.put(timesItem, mLogTimes.get(timesItem) + 1);
    }

    public enum ProfileItem {
        Init, Process, Stop, Finish, Reinitialize
    }

    public enum TimesItem {
        NewImageReader
    }

    public AdasStatistics() {
        mProfilers = new HashMap<>();
        addProfiler(ProfileItem.Init);
        addProfiler(ProfileItem.Process);
        addProfiler(ProfileItem.Stop);
        addProfiler(ProfileItem.Finish);
        addProfiler(ProfileItem.Reinitialize);
        mLogTimes = new HashMap<>();
        addTimesItem(TimesItem.NewImageReader);
    }

    private void addProfiler(ProfileItem profile) {
        mProfilers.put(profile, new Profiler(profile.name(), PROFILE_TIME_NUM));
    }

    private void addTimesItem(TimesItem timesItem) {
        mLogTimes.put(timesItem, 0);
    }

    public void logStart(ProfileItem profile) {
        mProfilers.get(profile).logStart();
    }

    public void logFinish(ProfileItem profile) {
        mProfilers.get(profile).logFinish();
    }

    @Override
    public String toString() {
        return "AdasStatistics{" +
                mProfilers.get(ProfileItem.Init) +
                ", " + mProfilers.get(ProfileItem.Process) +
                ", " + mProfilers.get(ProfileItem.Finish) +
                ", " + mProfilers.get(ProfileItem.Stop) +
                ", " + mProfilers.get(ProfileItem.Reinitialize) +
                ", " + TimesItem.NewImageReader.name() + "=" + mLogTimes.get(TimesItem.NewImageReader) +
                '}';
    }
}
