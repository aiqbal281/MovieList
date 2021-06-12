package com.adil.movielist.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adil.movielist.R
import com.adil.movielist.fragment.MovieListFragment

class MainActivity : AppCompatActivity() {
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        supportFragmentManager.beginTransaction().replace(
            R.id.container,
            MovieListFragment()
        ).commit()

    }

    override fun onBackPressed() {
        when {
            supportFragmentManager.backStackEntryCount > 0 -> {
                supportFragmentManager.popBackStack()
            }
            else -> {
                finish()
            }
        }

    }
}