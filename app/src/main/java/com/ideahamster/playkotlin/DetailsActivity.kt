package com.ideahamster.playkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ideahamster.playkotlin.ui.main.DetailsFragment

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DetailsFragment.newInstance())
                .commitNow()
        }
        println("SCOPE: Details activity launched")
    }
}