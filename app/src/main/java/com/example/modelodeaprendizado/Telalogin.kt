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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

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
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvFooterLink = findViewById<TextView>(R.id.tvFooterLink)
        val ivLogo = findViewById<android.widget.ImageView>(R.id.ivLogo)
        val btnTopLogin = findViewById<TextView>(R.id.btnTopLogin)
        val auth = FirebaseAuth.getInstance()

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
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid == null) {
                        btnLogin.isEnabled = true
                        Toast.makeText(this, "Erro ao entrar. Tente novamente.", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }
                    FirebaseFirestore.getInstance().collection(UserRoles.COLLECTION)
                        .document(uid)
                        .get()
                        .addOnSuccessListener { document ->
                            val destination = when (document.getString(UserRoles.FIELD_TIPO_USUARIO)) {
                                UserRoles.ADMIN -> TelaAdministradorEquipeActivity::class.java
                                UserRoles.MEMBER -> TelaMembroEquipeActivity::class.java
                                else -> null
                            }
                            if (destination == null) {
                                btnLogin.isEnabled = true
                                auth.signOut()
                                Toast.makeText(this, "Perfil de usuário não encontrado.", Toast.LENGTH_LONG).show()
                                return@addOnSuccessListener
                            }
                            val intent = Intent(this, destination)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            btnLogin.isEnabled = true
                            Toast.makeText(this, "Erro ao carregar perfil do usuário.", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { exception ->
                    btnLogin.isEnabled = true
                    val message = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
                        is FirebaseAuthInvalidUserException -> "Usuário não encontrado."
                        else -> "Erro ao entrar: ${exception.localizedMessage}"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
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