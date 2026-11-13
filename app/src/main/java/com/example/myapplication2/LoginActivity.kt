package com.example.myapplication2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUserName = findViewById<EditText>(R.id.etUserName)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        prefs = getSharedPreferences("ShopPrefs", MODE_PRIVATE)

        val savedUser = prefs.getString("USERNAME", "")
        val isRemembered = prefs.getBoolean("REMEMBER", false)
        if (isRemembered) {
            etUserName.setText(savedUser)
            cbRemember.isChecked = true
        }

        btnLogin.setOnClickListener {
            val username = etUserName.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this, "Veuillez saisir votre nom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val editor = prefs.edit()
            if (cbRemember.isChecked) {
                editor.putString("USERNAME", username)
                editor.putBoolean("REMEMBER", true)
            } else {
                editor.clear()
                editor.putString("CURRENT_USER", username)
            }
            editor.apply()

            Toast.makeText(this, "Bonjour $username !", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USER_NAME", username)
            startActivity(intent)
            finish()
        }
    }
}