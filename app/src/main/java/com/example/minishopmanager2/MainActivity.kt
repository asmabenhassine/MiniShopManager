package com.example.minishopmanager2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var tvResumePanier: TextView
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)
        tvResumePanier = findViewById(R.id.tvResumePanier)

        val prefs = getSharedPreferences("ShopPrefs", MODE_PRIVATE)
        val userName = intent.getStringExtra("USER_NAME")
            ?: prefs.getString("USERNAME", prefs.getString("CURRENT_USER", "Client VIP"))

        val heure = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val salutation = if (heure < 18) "Bonjour" else "Bonsoir"

        findViewById<TextView>(R.id.tvWelcome).text = "$salutation, $userName !"

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

    override fun onResume() {
        super.onResume()
        val articles = dbHelper.getArticlesPanier()
        val total = articles.sumOf { it.prix }
        tvResumePanier.text = "${articles.size} article(s) — $total DT"
    }
}