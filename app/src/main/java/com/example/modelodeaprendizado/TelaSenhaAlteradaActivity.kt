package com.example.modelodeaprendizado

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TelaSenhaAlteradaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_senha_alterada)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeaderFixed)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)
        val btnTopLogin = findViewById<TextView>(R.id.btnTopLogin)
        val ivLogo = findViewById<android.widget.ImageView>(R.id.ivLogo)

        ivLogo.setOnClickListener {
            val intent = Intent(this, TelaPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }

        val navigateToLogin = {
            val intent = Intent(this, Telalogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnBackToLogin.setOnClickListener { navigateToLogin() }
        btnTopLogin.setOnClickListener { navigateToLogin() }
    }
}
