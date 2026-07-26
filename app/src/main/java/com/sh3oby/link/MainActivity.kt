package com.sh3oby.link

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ربط ملف البرمجة بواجهة التصميم activity_main
        setContentView(R.layout.activity_main)
    }
}
