package com.example.focusstart5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val customView
        get() = findViewById<CustomView>(R.id.customView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scope = MainScope()

        gasButton.setOnClickListener {
            customView.updatePaintByClickGas()
        }

        gasButton.setOnLongClickListener {
            scope.launch {
                customView.updatePaintByPressGas()
                while (it.isPressed) {
                    delay(100)
                }
                customView.updatePaintByNaturalInhibition()
            }
            true
        }

        stopButton.setOnClickListener {
            customView.updatePaintByClickStop()
        }

        stopButton.setOnLongClickListener {
            scope.launch {
                customView.updatePaintByPressStop()
                while (it.isPressed) {
                    delay(100)
                }
                customView.updatePaintByNaturalInhibition()
            }
            true
        }
    }
}