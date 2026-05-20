package com.example.cachecleaner;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private static final int SHIZUKU_REQ_CODE = 7777;
    private ICacheService privilegedCacheService = null;
    
    private TextView txtStatus;
    private CheckBox checkSelectAll;
    private RecyclerView recyclerView;
    private Button btnRefresh, btnWipe, btnSearchToggle;
    private EditText editSearch;
    
    private AppAdapter adapter;
    private final List<AppInfo> downloadedApps = new ArrayList<>();
    private final List<AppInfo> displayedApps = new ArrayList<>();

    private final Shizuku.UserServiceArgs serviceArgs = new Shizuku.UserServiceArgs(
            new ComponentName("com.example.cachecleaner", CacheService.class.getName()))
            .version(1)
            .processNameSuffix("privileged")
            .debuggable(BuildConfig.DEBUG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUserInterface();
        Shizuku.addRequestPermissionResultListener(this::onPermissionResult);
        checkShizukuEnvironment();
    }

    private void setupUserInterface() {
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);

        txtStatus = new TextView(this);
        txtStatus.setText("Scanning device permissions...");
        txtStatus.setPadding(0, 0, 0, 16);
        mainLayout.addView(txtStatus);

        android.widget.LinearLayout controlRow = new android.widget.LinearLayout(this);
        controlRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        controlRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        checkSelectAll = new CheckBox(this);
        checkSelectAll.setText("Select All Apps");
        checkSelectAll.setEnabled(false);
        checkSelectAll.setOnCheckedChangeListener((btn, isChecked) -> toggleSelectAll(isChecked));
        controlRow.addView(checkSelectAll, new android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        btnRefresh = new Button(this);
        btnRefresh.setText("Refresh & Sort");
        btnRefresh.setOnClickListener(v -> refreshDownloadedAppsList());
        controlRow.addView(btnRefresh);
        mainLayout.addView(controlRow);

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter(displayedApps);
        recyclerView.setAdapter(adapter);
        mainLayout.addView(recyclerView, new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Bottom Action Bar Row
        android.widget.LinearLayout bottomActionBar = new android.widget.LinearLayout(this);
        bottomActionBar.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        bottomActionBar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        bottomActionBar.setPadding(0, 16, 0, 0);
        
        editSearch = new EditText(this);
        editSearch.setHint("Search apps...");
        editSearch.setVisibility(View.GONE);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterAppList(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
        android.widget.LinearLayout.LayoutParams searchParams = new android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchParams.setMarginEnd(16);
        bottomActionBar.addView(editSearch, searchParams);

        btnWipe = new Button(this);
        btnWipe.setText("Wipe Selected App Caches");
        btnWipe.setEnabled(false);
        btnWipe.setOnClickListener(v -> executeTargetedCacheWipe());
        android.widget.LinearLayout.LayoutParams wipeParams = new android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f);
        bottomActionBar.addView(btnWipe, wipeParams);

        btnSearchToggle = new Button(this);
        btnSearchToggle.setText("🔍");
        btnSearchToggle.setOnClickListener(v -> {
            if (editSearch.getVisibility() == View.GONE) {
                editSearch.setVisibility(View.VISIBLE);
                editSearch.requestFocus();
            } else {
                editSearch.setText("");
                editSearch.setVisibility(View.GONE);
            }
        });
        bottomActionBar.addView(btnSearchToggle, new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mainLayout.addView(bottomActionBar);
        setContentView(mainLayout);
    }

    private void filterAppList(String query) {
        List<AppInfo> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(downloadedApps);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (AppInfo app : downloadedApps) {
                if (app.getLabel().toLowerCase().contains(lowerCaseQuery) || 
                    app.getPackageName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(app);
                }
            }
        }
        adapter.updateList(filteredList);
    }

    private void checkShizukuEnvironment() {
        if (!Shizuku.pingBinder()) {
            txtStatus.setText("Shizuku framework is offline.");
            return;
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            txtStatus.setText("Connected to Shizuku securely.");
            btnWipe.setEnabled(true);
            checkSelectAll.setEnabled(true);
            Shizuku.bindUserService(serviceArgs, shizukuConnection);
        } else {
            Shizuku.requestPermission(SHIZUKU_REQ_CODE);
        }
    }

    private void onPermissionResult(int requestCode, int grantResult) {
        if (requestCode == SHIZUKU_REQ_CODE && grantResult == PackageManager.PERMISSION_GRANTED) {
            checkShizukuEnvironment();
        }
    }

    private void refreshDownloadedAppsList() {
        if (privilegedCacheService == null) return;
        txtStatus.setText("Analyzing storage data...");
        
        new Thread(() -> {
            try {
                PackageManager pm = getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                
                List<AppInfo> rawList = new ArrayList<>();
                List<String> packageNames = new ArrayList<>();

                for (ApplicationInfo app : apps) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        String label = app.loadLabel(pm).toString();
                        rawList.add(new AppInfo(label, app.packageName, app.loadIcon(pm)));
                        packageNames.add(app.packageName);
                    }
                }

                List<AppCacheInfo> sizingData = privilegedCacheService.getCacheSizes(packageNames);
                Map<String, Long> sizeMap = new HashMap<>();
                for (AppCacheInfo item : sizingData) {
                    sizeMap.put(item.packageName, item.cacheSizeBytes);
                }

                for (AppInfo info : rawList) {
                    Long size = sizeMap.get(info.getPackageName());
                    if (size != null) info.setCacheSizeBytes(size);
                }

                Collections.sort(rawList, (a, b) -> Long.compare(b.getCacheSizeBytes(), a.getCacheSizeBytes()));

                runOnUiThread(() -> {
                    downloadedApps.clear();
                    downloadedApps.addAll(rawList);
                    filterAppList(editSearch.getText().toString());
                    checkSelectAll.setChecked(false);
                    txtStatus.setText("Connected to Shizuku securely.");
                    Toast.makeText(MainActivity.this, "Sorted by size.", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void toggleSelectAll(boolean isChecked) {
        for (AppInfo app : displayedApps) {
            app.setChecked(isChecked);
        }
        adapter.notifyDataSetChanged();
    }

    private void executeTargetedCacheWipe() {
        if (privilegedCacheService == null) return;

        List<String> targetedPackages = new ArrayList<>();
        for (AppInfo app : downloadedApps) {
            if (app.isChecked()) targetedPackages.add(app.getPackageName());
        }

        if (targetedPackages.isEmpty()) {
            Toast.makeText(this, "Select apps to purge.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                privilegedCacheService.purgeSelectedCaches(targetedPackages);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Purge complete!", Toast.LENGTH_SHORT).show();
                    refreshDownloadedAppsList();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private final ServiceConnection shizukuConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            privilegedCacheService = ICacheService.Stub.asInterface(service);
            refreshDownloadedAppsList();
        }
        @Override public void onServiceDisconnected(ComponentName name) { privilegedCacheService = null; }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(this::onPermissionResult);
        if (privilegedCacheService != null) {
            Shizuku.unbindUserService(serviceArgs, shizukuConnection, true);
        }
    }
}