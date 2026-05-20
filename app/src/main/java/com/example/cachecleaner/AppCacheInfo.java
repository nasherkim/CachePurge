package com.example.cachecleaner;

import android.os.Parcel;
import android.os.Parcelable;

public class AppCacheInfo implements Parcelable {
    public String packageName;
    public long cacheSizeBytes;

    public AppCacheInfo() {}

    protected AppCacheInfo(Parcel in) {
        packageName = in.readString();
        cacheSizeBytes = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeLong(cacheSizeBytes);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<AppCacheInfo> CREATOR = new Creator<AppCacheInfo>() {
        @Override
        public AppCacheInfo createFromParcel(Parcel in) { return new AppCacheInfo(in); }
        @Override
        public AppCacheInfo[] newArray(int size) { return new AppCacheInfo[size]; }
    };
}