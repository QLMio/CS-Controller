package io.github.xsheeee.cs_controller;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.view.View;
import android.text.method.LinkMovementMethod;

public class AboutActivity extends AppCompatActivity {

    private static final String[] TEXTS = {
        "在 GitHub 查看源码",
        "查看开发者的 GitHub 主页",
        "在 GitHub 查看源码",
        "查看开发者的 GitHub 主页"
    };

    private static final String[] URLS = {
        "https://github.com/XSheeee/CS-Controller",
        "https://github.com/XSheeee",
        "https://github.com/MoWei-2077/CPU-speed-controller",
        "https://github.com/Mowei-2077"
    };

    private static final int[] TEXT_VIEW_IDS = {
        R.id.view_source_code,
        R.id.xshe_github,
        R.id.cs_source_code,
        R.id.mowei_github
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.backButton3);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish()); // 返回上一个活动

        // 添加可点击链接
        for (int i = 0; i < TEXT_VIEW_IDS.length; i++) {
            TextView textView = findViewById(TEXT_VIEW_IDS[i]);
            SpannableString spannableString = new SpannableString(TEXTS[i]);

            if (TEXTS[i].contains("GitHub")) {
                addClickablePart(spannableString, "GitHub", URLS[i]);
            }

            textView.setText(spannableString);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    // 添加点击链接
    private void addClickablePart(SpannableString spannableString, String clickableText, String url) {
        int start = spannableString.toString().indexOf(clickableText);
        int end = start + clickableText.length();

        if (start >= 0) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            };
            spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}