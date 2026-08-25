package com.example.modelodeaprendizado

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Telasenha1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_esqueci_senha)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeaderFixed)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val etEmailReset = findViewById<EditText>(R.id.etEmailReset)
        val btnReceiveEmail = findViewById<Button>(R.id.btnReceiveEmail)
        val btnTopLogin = findViewById<TextView>(R.id.btnTopLogin)
        val ivLogo = findViewById<android.widget.ImageView>(R.id.ivLogo)

        ivLogo.setOnClickListener {
            val intent = Intent(this, TelaPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnReceiveEmail.setOnClickListener {
            val email = etEmailReset.text.toString()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "E-mail de recuperação enviado para: $email", Toast.LENGTH_LONG).show()
                // Simulação de navegação para a próxima tela de reset
                val intent = Intent(this, TelaResetarSenhaActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, digite seu e-mail", Toast.LENGTH_SHORT).show()
            }
        }

        btnTopLogin.setOnClickListener {
            val intent = Intent(this, Telalogin::class.java)
            startActivity(intent)
            finish()
        }
    }
}
