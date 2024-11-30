package com.example.mobilelab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fifthmobilelab.AppPreferences
import com.example.mobilelab.fragments.OnboardFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface FontSizeApplier {
    fun applyFontSize(fontSize: String)
}

class MainActivity : AppCompatActivity(), FontSizeApplier {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Применение масштаба шрифта перед установкой разметки
        lifecycleScope.launch {
            val fontSize = AppPreferences.getFontSize(this@MainActivity).first()
            applyFontSize(fontSize)
        }

        setContentView(R.layout.activity_main)

        // Установка обработчика системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun applyFontSize(fontSize: String) {
        val scale = when (fontSize) {
            "Small" -> 0.85f
            "Medium" -> 1.0f
            "Large" -> 1.15f
            else -> 1.0f
        }

        val currentScale = resources.configuration.fontScale
        if (currentScale != scale) {
            val newConfig = resources.configuration
            newConfig.fontScale = scale
            resources.updateConfiguration(newConfig, resources.displayMetrics)
            recreate()
        }
    }
}


