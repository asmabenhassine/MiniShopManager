package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

data class Produit(val nom: String, val categorie: String, val prix: Double, val iconeRes: Int)

class CatalogueActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogue)

        val listView = findViewById<ListView>(R.id.listViewProduits)
        val dbHelper = DBHelper(this)
        val produits = lireProduitsDepuisJSON()

        val adapter = object : ArrayAdapter<Produit>(this, R.layout.item_produit, produits) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_produit, parent, false)
                val produit = getItem(position)!!

                view.findViewById<TextView>(R.id.tvNomProduit).text = produit.nom
                view.findViewById<TextView>(R.id.tvCategorie).text = produit.categorie
                view.findViewById<TextView>(R.id.tvPrixProduit).text = "${produit.prix} DT"
                view.findViewById<ImageView>(R.id.imgProduit).setImageResource(produit.iconeRes)

                return view
            }
        }

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val produitSelectionne = produits[position]
            val ajoute = dbHelper.ajouterAuPanier(produitSelectionne.nom, produitSelectionne.prix)
            if (ajoute) {
                Toast.makeText(
                    this,
                    "Produit sélectionné : ${produitSelectionne.nom} (Ajouté au panier !)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.btnAllerPanier).setOnClickListener {
            startActivity(Intent(this, PanierActivity::class.java))
        }
    }

    private fun lireProduitsDepuisJSON(): ArrayList<Produit> {
        val liste = ArrayList<Produit>()
        val jsonString = assets.open("produits.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("produits")

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val nom = obj.getString("nom")
            val categorie = obj.getString("categorie")
            val prix = obj.getDouble("prix")
            val iconeName = obj.getString("icone")

            val resId = resources.getIdentifier(iconeName, "drawable", packageName)
            val finalIcon = if (resId != 0) resId else android.R.drawable.ic_menu_compass

            liste.add(Produit(nom, categorie, prix, finalIcon))
        }
        return liste
    }
}