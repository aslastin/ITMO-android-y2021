package ru.aslastin.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate

const val BUNDLE_IS_LIGHT_THEME = "BUNDLE_IS_LIGHT_THEME"

class MainActivity : AppCompatActivity() {

    var isLightTheme: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
            isLightTheme = false
        } else {
            setTheme(R.style.LightTheme)
            isLightTheme = true
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.themeButton).setOnClickListener {
            if (isLightTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                setTheme(R.style.LightTheme)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setTheme(R.style.DarkTheme)
            }
            isLightTheme = !isLightTheme
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BUNDLE_IS_LIGHT_THEME, isLightTheme)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isLightTheme = savedInstanceState.getBoolean(BUNDLE_IS_LIGHT_THEME)
    }
}