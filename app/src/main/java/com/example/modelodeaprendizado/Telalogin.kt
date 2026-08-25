package com.example.modelodeaprendizado

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Telalogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeaderFixed)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvFooterLink = findViewById<TextView>(R.id.tvFooterLink)
        val ivLogo = findViewById<android.widget.ImageView>(R.id.ivLogo)
        val btnTopLogin = findViewById<TextView>(R.id.btnTopLogin)

        ivLogo.setOnClickListener {
            val intent = Intent(this, TelaPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnTopLogin.setOnClickListener {
            // Apenas recarrega ou mantém na tela como solicitado
            val intent = Intent(this, Telalogin::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            // Redireciona para a própria tela de login conforme solicitado
            val intent = Intent(this, Telalogin::class.java)
            startActivity(intent)
            finish()
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, Telasenha1::class.java)
            startActivity(intent)
        }

        tvFooterLink.setOnClickListener {
            val intent = Intent(this, Telacadastro::class.java)
            startActivity(intent)
        }
    }
}