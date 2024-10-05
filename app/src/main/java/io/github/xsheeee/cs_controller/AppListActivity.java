package io.github.xsheeee.cs_controller;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;

import io.github.xsheeee.cs_controller.Tools.AppInfo;
import io.github.xsheeee.cs_controller.Tools.Logger;

public class AppListActivity extends AppCompatActivity {
    private ListView listView; 
    private AppListAdapter adapter;
    private List<AppInfo> data; 
    private View loadingView; 
    private PackageManager packageManager; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        // 初始化视图组件
        listView = findViewById(R.id.list_view);
        loadingView = findViewById(R.id.loading_view);
        packageManager = getPackageManager();

        // 加载应用信息
        loadAppInfos();

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.backButton);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 列表项点击事件
        listView.setOnItemClickListener((parent, view, position, id) -> {
            AppInfo appInfo = data.get(position);
            Intent intent = new Intent(AppListActivity.this, AppConfigActivity.class);
            intent.putExtra("aName", appInfo.getAppName());
            intent.putExtra("pName", appInfo.getPackageName());
            startActivity(intent);
        });
    }

    // 加载应用信息
    private void loadAppInfos() {
        loadingView.setVisibility(View.VISIBLE);

        new Thread(() -> {
            data = getAllAppInfos();
            runOnUiThread(() -> {
                adapter = new AppListAdapter();
                listView.setAdapter(adapter);
                loadingView.setVisibility(View.GONE);
            });
        }).start();
    }

    // 获取所有应用信息
    protected List<AppInfo> getAllAppInfos() {
        List<AppInfo> list = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        // 遍历应用信息
        for (ResolveInfo ri : resolveInfos) {
            String packageName = ri.activityInfo.packageName;
            Drawable icon = ri.loadIcon(packageManager); 
            String appName = ri.loadLabel(packageManager).toString();
            list.add(new AppInfo(icon, appName, packageName));
        }

        return list;
    }

    // 自定义适配器类
    class AppListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(AppListActivity.this, R.layout.app_info_layout, null);
                holder = new ViewHolder();
                holder.imageView = convertView.findViewById(R.id.app_icon);
                holder.textView = convertView.findViewById(R.id.app_name);
                holder.packageNameView = convertView.findViewById(R.id.pck_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo appInfo = data.get(position);
            holder.imageView.setImageDrawable(appInfo.getIcon());
            holder.textView.setText(appInfo.getAppName());
            holder.packageNameView.setText(appInfo.getPackageName());

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView packageNameView; 
    }
}
