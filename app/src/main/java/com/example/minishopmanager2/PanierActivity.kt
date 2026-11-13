package com.example.minishopmanager2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class PanierActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private lateinit var tvTotal: TextView
    private var totalArticles: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panier)

        dbHelper = DBHelper(this)
        listView = findViewById(R.id.listViewPanier)
        tvTotal = findViewById(R.id.tvTotalPanier)

        findViewById<Button>(R.id.btnRetourPanier).setOnClickListener {
            finish()
        }

        chargerPanier()

        findViewById<Button>(R.id.btnPayer).setOnClickListener {
            val articles = dbHelper.getArticlesPanier()
            if (articles.isEmpty()) {
                Toast.makeText(this, "Votre panier est vide !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ouvrirDetailsPaiement(articles.size, totalArticles)
        }

        findViewById<Button>(R.id.btnViderPanier).setOnClickListener {
            dbHelper.viderPanier()
            chargerPanier()
            Toast.makeText(this, "Panier vidé !", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ouvrirDetailsPaiement(nbArticles: Int, sousTotal: Double) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_paiement, null)

        val fraisLivraison = if (sousTotal >= 200.0) 0.0 else 7.0
        val totalTTC = sousTotal + fraisLivraison

        dialogView.findViewById<TextView>(R.id.tvDetailSousTotal).text =
            "Sous-total ($nbArticles article(s)) : $sousTotal DT"
        dialogView.findViewById<TextView>(R.id.tvDetailLivraison).text =
            if (fraisLivraison == 0.0) "Frais de livraison : Gratuite" else "Frais de livraison : $fraisLivraison DT"
        dialogView.findViewById<TextView>(R.id.tvDetailTotalFinal).text =
            "Total TTC à payer : $totalTTC DT"

        val etAdresse = dialogView.findViewById<EditText>(R.id.etAdresseLivraison)
        val rgMode = dialogView.findViewById<RadioGroup>(R.id.rgModePaiement)
        val rbCarte = dialogView.findViewById<RadioButton>(R.id.rbCarte)
        val layoutCarte = dialogView.findViewById<LinearLayout>(R.id.layoutCarteBancaire)
        val etNumCarte = dialogView.findViewById<EditText>(R.id.etNumCarte)

        rgMode.setOnCheckedChangeListener { _, checkedId ->
            layoutCarte.visibility = if (checkedId == R.id.rbCarte) View.VISIBLE else View.GONE
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Détails du Paiement")
            .setView(dialogView)
            .setPositiveButton("Confirmer le paiement", null)
            .setNegativeButton("Annuler", null)
            .create()

        dialog.setOnShowListener {
            val btnConfirmer = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnConfirmer.setOnClickListener {
                val adresse = etAdresse.text.toString().trim()
                if (adresse.isEmpty()) {
                    etAdresse.error = "Veuillez saisir une adresse"
                    return@setOnClickListener
                }

                val modeChoisi = if (rbCarte.isChecked) {
                    val num = etNumCarte.text.toString().trim()
                    if (num.length < 16) {
                        etNumCarte.error = "Numéro de carte invalide (16 chiffres)"
                        return@setOnClickListener
                    }
                    "Carte Bancaire (**** ${num.takeLast(4)})"
                } else {
                    "Paiement à la livraison (Espèces)"
                }

                dialog.dismiss()
                dbHelper.viderPanier()
                chargerPanier()
                afficherRecuEtRetour(totalTTC, modeChoisi, adresse)
            }
        }

        dialog.show()
    }

    private fun afficherRecuEtRetour(montantPaye: Double, modePaiement: String, adresse: String) {
        val refCommande = "#CMD-${Random.nextInt(10000, 99999)}"

        AlertDialog.Builder(this)
            .setTitle("Paiement Confirmé")
            .setMessage(
                "Votre commande a bien été enregistrée !\n\n" +
                        "• Référence : $refCommande\n" +
                        "• Montant réglé : $montantPaye DT\n" +
                        "• Mode : $modePaiement\n" +
                        "• Livraison : $adresse"
            )
            .setCancelable(false)
            .setPositiveButton("Retour à l'accueil") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun chargerPanier() {
        val articles = dbHelper.getArticlesPanier()
        val affichage = articles.map { "${it.nom}  —  ${it.prix} DT" }
        totalArticles = articles.sumOf { it.prix }

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, affichage)
        tvTotal.text = "Total : $totalArticles DT (${articles.size} articles)"
    }
}