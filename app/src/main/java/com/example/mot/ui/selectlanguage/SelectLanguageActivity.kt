package com.example.mot.ui.selectlanguage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.mot.R
import com.example.mot.data.MenuList
import com.example.mot.db.entity.Category
import com.example.mot.db.entity.Menu
import com.example.mot.unit.extension.TAG
import com.example.mot.viewmodel.CategoryViewModel
import com.example.mot.ui.menu.MenuActivity
import com.example.mot.viewmodel.MenuViewModel
import com.example.mot.ui.base.BaseActivity
import com.example.mot.unit.Language
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.activity_select_language.*
import java.util.concurrent.TimeUnit

class SelectLanguageActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val menuList =  mutableListOf<MenuList>()
    private val categoryVM: CategoryViewModel by lazy {
        ViewModelProvider(this, CategoryViewModel.Factory(application)).get(
            CategoryViewModel::class.java)
    }
    private val menuVM: MenuViewModel by lazy {
        ViewModelProvider(this, MenuViewModel.Factory(application)).get(
            MenuViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_language)

        init()
    }

    private fun init() {
        btnClick()
    }

    private fun btnClick() {
        btnGetFBData.clicks()
            .throttleFirst(3000, TimeUnit.MILLISECONDS)
            .doOnNext { showPB() }
            .subscribe (
                {
                    getCategory()
                    getMenuNames()
                    hidePB()
                },
                { hidePB() })
            .apply { disposables.add(this) }

        btnKor.clicks()
            .throttleFirst(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                Intent(this, MenuActivity::class.java).apply {
                    Language.setLangCode(0)
                    startActivity(this)
                }
            }
            .apply { disposables.add(this) }

        btnEng.clicks()
            .throttleFirst(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                Intent(this, MenuActivity::class.java).apply {
                    Language.setLangCode(1)
                    startActivity(this)
                }
            }
            .apply { disposables.add(this) }

        btnCh.clicks()
            .throttleFirst(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                Intent(this, MenuActivity::class.java).apply {
                    Language.setLangCode(2)
                    startActivity(this)
                }
            }
            .apply { disposables.add(this) }

        btnJp.clicks()
            .throttleFirst(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                Intent(this, MenuActivity::class.java).apply {
                    Language.setLangCode(3)
                    startActivity(this)
                }
            }
            .apply { disposables.add(this) }
    }

    private fun addCategory(id: Long, name: String) {
        categoryVM.insertCategory(Category(id, name))
    }

    private fun getCategory() {
        db.collection("/menu").document("category")
            .get()
            .addOnSuccessListener { document->
                if (document != null) {
                    val cat = document.data?.keys as MutableSet<String>

                    for(i in cat.indices) {
                        val id = document.get(cat.elementAt(i)) as Long
                        val name = cat.elementAt(i)
                        addCategory(id, name)
                    }
                } else {
                    Log.d("firebase", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("firebase", "get failed with ", exception)
            }
    }

    private fun getMenuNames() {
        db.collection("/menu")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                if(document.id != "category")  menuList.add(MenuList(document.id))
            }
            getMenu()
        }
        .addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }
    }

    private fun getMenu() {
        for(i in menuList.indices){
            db.collection("/menu").document(menuList[i].menuName)
                .get()
                .addOnSuccessListener { document->
                    if (document != null) {
                        val d = document.toObject(Menu::class.java)
                        d?.let { menuVM.insertMenu(it) }
                    } else { Log.d(TAG, "No such document") }
        }
        .addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }

        }

    }

    private fun showPB() {
        pbSelectLanguage.visibility = View.VISIBLE
    }

    private fun hidePB() {
        pbSelectLanguage.visibility = View.GONE
    }
}
