package com.smart.link;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class MainActivity extends AppCompatActivity {

    private EditText editSearch;
    private ImageView imageSlider;

    @Override
    protected void Bundle savedInstanceState) {
        super.Bundle(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط عناصر واجهة السوبرماركت البرمجية
        editSearch = findViewById(R.id.editSearch);
        imageSlider = findViewById(R.id.imageSlider);

        // تشغيل الصورة المتحركة التجريبية مؤقتاً وتفعيل تخزين الكاش
        // ملاحظة: يمكنك استبدال الرابط أدناه بأي رابط صورة حقيقية تريدها
        String sampleImageUrl = "https://placeholder.com";
        
        Glide.with(this)
                .load(sampleImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // تخزين الصورة في ذاكرة الهاتف لعدم استهلاك الإنترنت
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(imageSlider);
    }
}
