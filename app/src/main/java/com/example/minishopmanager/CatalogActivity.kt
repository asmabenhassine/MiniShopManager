package com.example.minishopmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val listView = findViewById<ListView>(R.id.listViewProduits)
        val products = resources.getStringArray(R.array.products)

        // Une image par produit, dans le même ordre que strings.xml
        val images = intArrayOf(
            R.drawable.ic_phone,
            R.drawable.outline_bluetooth_24,
            R.drawable.outline_aod_watch_24,
            R.drawable.outline_battery_charging_80_24,
            R.drawable.outline_add_shopping_cart_24,
            R.drawable.outline_align_justify_space_around_24
        )

        val adapter = object : ArrayAdapter<String>(this, R.layout.item_product, products) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView
                    ?: LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
                view.findViewById<TextView>(R.id.tvProductName).text = getItem(position)
                view.findViewById<ImageView>(R.id.imgProduct).setImageResource(images[position])
                return view
            }
        }
        listView.adapter = adapter

        listView.setOnItemClickListener { p, _, position, _ ->
            val item = p.getItemAtPosition(position).toString()
            Toast.makeText(this, "Produit sélectionné : $item", Toast.LENGTH_SHORT).show()
        }
    }
}