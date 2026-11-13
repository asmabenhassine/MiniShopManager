package com.example.minishopmanager2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Produit(
    val nom: String,
    val categorie: String,
    val prix: Double,
    val iconeRes: Int,
    val description: String,
    val imageUrl: String
)

class CatalogueActivity : AppCompatActivity() {

    private lateinit var tousLesProduits: ArrayList<Produit>
    private lateinit var produitsAffiches: ArrayList<Produit>
    private lateinit var adapter: ArrayAdapter<Produit>
    private var categorieActive = ""
    private val imageCache = HashMap<String, Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogue)

        val gridView = findViewById<GridView>(R.id.gridViewProduits)
        val etRecherche = findViewById<EditText>(R.id.etRecherche)
        val dbHelper = DBHelper(this)

        findViewById<Button>(R.id.btnRetourCatalogue).setOnClickListener {
            finish()
        }

        tousLesProduits = lireProduitsDepuisJSON()
        if (tousLesProduits.isEmpty()) {
            tousLesProduits = getProduitsParDefaut()
        }
        produitsAffiches = ArrayList(tousLesProduits)

        adapter = object : ArrayAdapter<Produit>(this, R.layout.item_produit, produitsAffiches) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_produit, parent, false)
                val produit = getItem(position)!!

                view.findViewById<TextView>(R.id.tvNomProduit).text = produit.nom
                view.findViewById<TextView>(R.id.tvCategorie).text = produit.categorie
                view.findViewById<TextView>(R.id.tvDescCourte).text = produit.description
                view.findViewById<TextView>(R.id.tvPrixProduit).text = "${produit.prix} DT"

                val imgView = view.findViewById<ImageView>(R.id.imgProduit)
                chargerPhotoReelle(imgView, produit.imageUrl, produit.iconeRes)

                return view
            }
        }

        gridView.adapter = adapter

        etRecherche.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filtrerProduits(s.toString(), categorieActive)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<TextView>(R.id.chipTous).setOnClickListener {
            categorieActive = ""
            filtrerProduits(etRecherche.text.toString(), categorieActive)
        }
        findViewById<TextView>(R.id.chipTech).setOnClickListener {
            categorieActive = "Électronique"
            filtrerProduits(etRecherche.text.toString(), categorieActive)
        }
        findViewById<TextView>(R.id.chipAccessoires).setOnClickListener {
            categorieActive = "Accessoire"
            filtrerProduits(etRecherche.text.toString(), categorieActive)
        }
        findViewById<TextView>(R.id.chipBeaute).setOnClickListener {
            categorieActive = "Beauté"
            filtrerProduits(etRecherche.text.toString(), categorieActive)
        }

        // Clic sur un produit -> Affiche tous ses détails + bouton Ajouter au panier
        gridView.setOnItemClickListener { _, _, position, _ ->
            val produit = produitsAffiches[position]
            afficherDetailsProduit(produit, dbHelper)
        }

        findViewById<Button>(R.id.btnAllerPanier).setOnClickListener {
            startActivity(Intent(this, PanierActivity::class.java))
        }
    }

    private fun afficherDetailsProduit(produit: Produit, dbHelper: DBHelper) {
        val conteneur = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                420
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        chargerPhotoReelle(imageView, produit.imageUrl, produit.iconeRes)

        val tvInfos = TextView(this).apply {
            text = "\nCatégorie : ${produit.categorie}\nPrix : ${produit.prix} DT\n\nDescription :\n${produit.description}"
            textSize = 15f
            setTextColor(resources.getColor(R.color.text_primary, theme))
        }

        conteneur.addView(imageView)
        conteneur.addView(tvInfos)

        AlertDialog.Builder(this)
            .setTitle(produit.nom)
            .setView(conteneur)
            .setPositiveButton("Ajouter au panier") { _, _ ->
                val ajoute = dbHelper.ajouterAuPanier(produit.nom, produit.prix)
                if (ajoute) {
                    Toast.makeText(
                        this,
                        "${produit.nom} ajouté au panier !",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Fermer", null)
            .show()
    }

    private fun chargerPhotoReelle(imageView: ImageView, url: String, fallbackRes: Int) {
        imageView.tag = url
        val cached = imageCache[url]
        if (cached != null) {
            imageView.setImageBitmap(cached)
            return
        }

        imageView.setImageResource(fallbackRes)
        if (url.isEmpty()) return

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.instanceFollowRedirects = true
                connection.connect()

                val stream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()

                if (bitmap != null) {
                    imageCache[url] = bitmap
                    runOnUiThread {
                        if (imageView.tag == url) {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }
            } catch (_: Throwable) {
            }
        }.start()
    }

    private fun filtrerProduits(recherche: String, categorie: String) {
        produitsAffiches.clear()
        for (p in tousLesProduits) {
            val matchNom = p.nom.contains(recherche, ignoreCase = true)
            val matchCat = categorie.isEmpty() || p.categorie.contains(categorie, ignoreCase = true)
            if (matchNom && matchCat) {
                produitsAffiches.add(p)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun lireProduitsDepuisJSON(): ArrayList<Produit> {
        val liste = ArrayList<Produit>()
        try {
            val jsonString = assets.open("produits.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("produits")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val nom = obj.optString("nom", "Produit")
                val categorie = obj.optString("categorie", "Général")
                val prix = obj.optDouble("prix", 0.0)
                val iconeName = obj.optString("icone", "ic_phone")
                val description = obj.optString("description", "Article de qualité disponible en boutique.")
                val imageUrl = obj.optString("imageUrl", "")

                val resId = resources.getIdentifier(iconeName, "drawable", packageName)
                val finalIcon = if (resId != 0) resId else android.R.drawable.ic_menu_gallery

                liste.add(Produit(nom, categorie, prix, finalIcon, description, imageUrl))
            }
        } catch (_: Exception) {
        }
        return liste
    }

    private fun getProduitsParDefaut(): ArrayList<Produit> {
        return arrayListOf(
            Produit("Smartphone X Pro", "Électronique", 1250.0, android.R.drawable.ic_menu_gallery, "Écran AMOLED 6.7 pouces 120Hz, 256 Go, triple capteur photo 108 MP.", "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600"),
            Produit("Casque Bluetooth Pro", "Électronique", 180.0, android.R.drawable.ic_menu_gallery, "Réduction de bruit active (ANC) et autonomie de 35 heures.", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600"),
            Produit("Montre Connectée Sport", "Accessoires", 320.0, android.R.drawable.ic_menu_gallery, "Suivi cardiaque 24h/24, GPS intégré et étanchéité 5 ATM.", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600"),
            Produit("Chargeur Rapide USB-C", "Accessoires", 65.0, android.R.drawable.ic_menu_gallery, "Charge ultra-rapide 65W GaN compatible smartphone et PC.", "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=600"),
            Produit("Sac à dos Urbain", "Accessoires", 140.0, android.R.drawable.ic_menu_gallery, "Tissu déperlant haute résistance avec compartiment PC 15.6 pouces.", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600"),
            Produit("Coffret Soin Visage", "Beauté", 95.0, android.R.drawable.ic_menu_gallery, "Routine hydratante complète : sérum, crème jour et contour des yeux.", "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=600")
        )
    }
}