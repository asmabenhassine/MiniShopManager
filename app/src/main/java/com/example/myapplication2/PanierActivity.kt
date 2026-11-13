package com.example.myapplication2

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PanierActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private lateinit var tvTotal: TextView
    private var totalActuel: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panier)

        dbHelper = DBHelper(this)
        listView = findViewById(R.id.listViewPanier)
        tvTotal = findViewById(R.id.tvTotalPanier)

        chargerPanier()

        // Bouton Payer avec boîte de dialogue de confirmation
        findViewById<Button>(R.id.btnPayer).setOnClickListener {
            val articles = dbHelper.getArticlesPanier()
            if (articles.isEmpty()) {
                Toast.makeText(this, "Votre panier est vide !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Confirmation de paiement")
                .setMessage("Montant total : $totalActuel DT (${articles.size} article(s))\n\nSouhaitez-vous valider et payer votre commande ?")
                .setPositiveButton("Confirmer le paiement") { _, _ ->
                    val montantPaye = totalActuel
                    dbHelper.viderPanier()
                    chargerPanier()
                    Toast.makeText(
                        this,
                        "Paiement de $montantPaye DT effectué avec succès !",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // Bouton Vider le panier
        findViewById<Button>(R.id.btnViderPanier).setOnClickListener {
            dbHelper.viderPanier()
            chargerPanier()
            Toast.makeText(this, "Panier vidé !", Toast.LENGTH_SHORT).show()
        }
    }

    private fun chargerPanier() {
        val articles = dbHelper.getArticlesPanier()
        val affichage = articles.map { "${it.nom}  —  ${it.prix} DT" }
        totalActuel = articles.sumOf { it.prix }

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, affichage)
        tvTotal.text = "Total à payer : $totalActuel DT (${articles.size} articles)"
    }
}