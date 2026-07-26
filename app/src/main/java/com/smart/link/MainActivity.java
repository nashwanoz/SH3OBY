package com.smart.link; // الحزمة القديمة كما هي دون تغيير

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editSearch;
    private ImageView imageSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // قراءة واجهة السوبرماركت الجديدة

        // ربط العناصر الأساسية الموجودة فعلياً في التصميم لمنع الانهيار
        editSearch = findViewById(R.id.editSearch);
        imageSlider = findViewById(R.id.imageSlider);
    }
}
