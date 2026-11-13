package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("LIFECYCLE", "MainActivity: onCreate appelé")

        val prefs = getSharedPreferences("ShopPrefs", MODE_PRIVATE)
        val userName = intent.getStringExtra("USER_NAME")
            ?: prefs.getString("USERNAME", prefs.getString("CURRENT_USER", "GTIC L3"))

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Bienvenue $userName !\nMiniShopManager"

        findViewById<Button>(R.id.btnCatalogue).setOnClickListener {
            startActivity(Intent(this, CatalogueActivity::class.java))
        }

        findViewById<Button>(R.id.btnPanier).setOnClickListener {
            startActivity(Intent(this, PanierActivity::class.java))
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("LIFECYCLE", "MainActivity: onStart appelé")
    }

    override fun onResume() {
        super.onResume()
        Log.d("LIFECYCLE", "MainActivity: onResume appelé")
    }
}