package com.petterp.floatingview

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val floatingView = FloatingView(this, R.layout.item_floating)
        contentView?.addView(floatingView)
        findViewById<View>(R.id.resetBtn).setOnClickListener {
            floatingView.reset()
        }
    }
}
