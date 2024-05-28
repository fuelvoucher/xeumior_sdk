package com.smart.st8050b_demo.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.smart.st8050b_demo.MainActivity;
import com.smart.st8050b_demo.R;

import java.util.List;

public class SetLauncherActivity extends AppCompatActivity implements View.OnClickListener {

    private Button setLauncher,cleanLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_launcher);
        setLauncher = (Button)findViewById(R.id.setLauncher);
        cleanLauncher = (Button)findViewById(R.id.cleanLauncher);
        setLauncher.setOnClickListener(this);
        cleanLauncher.setOnClickListener(this);
    }


    private void setDefualtLauncher()
    {
        PackageManager pm = getPackageManager();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MAIN");
        filter.addCategory("android.intent.category.HOME");
        filter.addCategory("android.intent.category.DEFAULT");
        Context context = getApplicationContext();
        ComponentName component = new ComponentName(context.getPackageName(), MainActivity.class.getName());
        ComponentName[] components = new ComponentName[] {new ComponentName("com.android.launcher", "com.android.launcher.Launcher"), component};
        pm.clearPackagePreferredActivities("com.android.launcher");
        pm.addPreferredActivity(filter, IntentFilter.MATCH_CATEGORY_EMPTY, components, component);
    }

    //存在多个以上的Launcher的时候
    private void setDefaultLauncher() {
        // remove this activity from the package manager.
        PackageManager pm = getPackageManager();
        Context context = getApplicationContext();
        String examplePackageName = context.getPackageName();//"com.jeejen.family"; //请修改为需要设置的 package name
        String exampleActivityName =  MainActivity.class.getName();//"com.jeejen.home.launcher.Launcher"; //请修改为需要设置的 launcher activity name

        ComponentName defaultLauncher = null;
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> resolveInfoList =
                pm.queryIntentActivities(intent, 0);
        if (resolveInfoList != null) {
            int size = resolveInfoList.size();
            for (int j = 0; j < size; ) {
                final ResolveInfo r = resolveInfoList.get(j);
                if (!r.activityInfo.packageName.equals(examplePackageName)) {
                    resolveInfoList.remove(j);
                    size -= 1;
                } else {
                    j++;
                }
            }
            ComponentName[] set = new ComponentName[size];
            defaultLauncher = new ComponentName(examplePackageName, exampleActivityName);
            int defaultMatch = 0;
            for (int i = 0; i < size; i++) {
                final ResolveInfo resolveInfo =
                        resolveInfoList.get(i);
                set[i] = new     ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                if (defaultLauncher.getClassName().equals(resolveInfo.activityInfo.name)) {
                    defaultMatch = resolveInfo.match;
                }
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MAIN);
            filter.addCategory(Intent.CATEGORY_HOME);
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            pm.clearPackagePreferredActivities(defaultLauncher.getPackageName());
            pm.addPreferredActivity(filter, defaultMatch, set, defaultLauncher);
        }
    }

    @Override
    public void onClick(View v) {

        if(v==setLauncher)
        {
            setDefaultLauncher();
        }else if(v==cleanLauncher)
        {

        }
    }
}
