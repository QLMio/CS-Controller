package io.github.xsheeee.cs_controller;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.appcompat.widget.Toolbar;
import android.widget.Spinner;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ipc.RootService;

import java.util.List;

import io.github.xsheeee.cs_controller.Tools.Values;

public class AppConfigActivity extends AppCompatActivity {
    private ArrayAdapter<CharSequence> adapter;
    private Spinner spinner;
    private int defaultPosition = -1; // 初始化为 -1 表示未找到

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.backButton2);
        setSupportActionBar(toolbar);
        
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一个活动
            }
        });

        // 获取应用名称并设置到 Toolbar 的副标题
        Intent intent = getIntent();
        String aName = intent.getStringExtra("aName");
        toolbar.setSubtitle(aName); // 设置副标题为应用名称

        // 初始化 Spinner
        spinner = findViewById(R.id.spinner);

        String pName = intent.getStringExtra("pName");

        // 创建 ArrayAdapter
        adapter = ArrayAdapter.createFromResource(
                this,
                R.array.modes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 设置 Adapter 到 Spinner
        spinner.setAdapter(adapter);

        // 更新列表
        Values.updateLists();

        // 查找默认项
        findDefaultPosition(pName);

        // 设置默认的 Spinner 项
        setDefaultSpinnerItem(defaultPosition);

        // 设置 Spinner 的 OnItemSelectedListener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 如果已经在当前列表中，则直接返回
                if (position == defaultPosition) {
                    return;
                }

                // 从旧列表移除并添加到新列表
                removeAndAddToNewList(pName, position);
                Values.toUpdateLists();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 什么都不做
            }
        });
    }

    private void findDefaultPosition(String pName) {
        for (int i = 0; i < Values.lists.size(); i++) {
            if (Values.lists.get(i).contains(pName)) {
                defaultPosition = i;
                break;
            }
        }
        if (defaultPosition == -1) {
            defaultPosition = 1; // 如果没有找到，设置为 1
        }
    }

    private void setDefaultSpinnerItem(int defaultItem) {
        spinner.setSelection(defaultItem); // 设置默认的 Spinner 项
    }

    private void removeAndAddToNewList(String pName, int newPosition) {
        // 从旧列表移除
        for (int i = 0; i < Values.lists.size(); i++) {
            if (Values.lists.get(i).contains(pName)) {
                Values.lists.get(i).remove(pName);
                break;
            }
        }

        // 添加到新列表
        Values.lists.get(newPosition).add(pName);
    }
}
