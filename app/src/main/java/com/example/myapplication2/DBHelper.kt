package com.example.myapplication2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class ArticlePanier(val id: Int, val nom: String, val prix: Double)

class DBHelper(context: Context) : SQLiteOpenHelper(context, "minishop.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE panier (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT,
                prix REAL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS panier")
        onCreate(db)
    }

    fun ajouterAuPanier(nom: String, prix: Double): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nom", nom)
            put("prix", prix)
        }
        val result = db.insert("panier", null, values)
        db.close()
        return result != -1L
    }

    fun getArticlesPanier(): ArrayList<ArticlePanier> {
        val liste = ArrayList<ArticlePanier>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM panier", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"))
                val prix = cursor.getDouble(cursor.getColumnIndexOrThrow("prix"))
                liste.add(ArticlePanier(id, nom, prix))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return liste
    }

    fun viderPanier() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM panier")
        db.close()
    }
}