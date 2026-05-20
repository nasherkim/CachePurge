package com.example.cachecleaner;

import android.graphics.drawable.Drawable;
import java.util.Locale;

public class AppInfo {
    private final String label;
    private final String packageName;
    private final Drawable icon;
    private long cacheSizeBytes;
    private boolean isChecked;

    public AppInfo(String label, String packageName, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
        this.cacheSizeBytes = 0;
        this.isChecked = false;
    }

    public String getLabel() { return label; }
    public String getPackageName() { return packageName; }
    public Drawable getIcon() { return icon; }
    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
    public long getCacheSizeBytes() { return cacheSizeBytes; }
    public void setCacheSizeBytes(long bytes) { this.cacheSizeBytes = bytes; }

    public String getFormattedCacheSize() {
        if (cacheSizeBytes <= 0) return "0.00 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(cacheSizeBytes) / Math.log10(1024));
        return String.format(Locale.US, "%.2f %s", cacheSizeBytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}