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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class Telacadastro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_cadastro)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeaderFixed)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etRepeatPassword = findViewById<EditText>(R.id.etRepeatPassword)
        val btnConfirmSignup = findViewById<Button>(R.id.btnConfirmSignup)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)
        val btnTopLogin = findViewById<TextView>(R.id.btnTopLogin)
        val ivLogo = findViewById<android.widget.ImageView>(R.id.ivLogo)
        val auth = FirebaseAuth.getInstance()

        ivLogo.setOnClickListener {
            val intent = Intent(this, TelaPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }

        val navigateToLogin = {
            val intent = Intent(this, Telalogin::class.java)
            startActivity(intent)
            finish()
        }

        btnConfirmSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val repeatPassword = etRepeatPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repeatPassword) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnConfirmSignup.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    authResult.user?.updateProfile(profileUpdate)

                    val uid = authResult.user?.uid
                    val userData = hashMapOf(
                        UserRoles.FIELD_NOME_ADMIN to name,
                        UserRoles.FIELD_EMAIL_ADMIN to email,
                        UserRoles.FIELD_TIPO_USUARIO to UserRoles.ADMIN
                    )
                    if (uid != null) {
                        FirebaseFirestore.getInstance().collection(UserRoles.COLLECTION)
                            .document(uid)
                            .set(userData)
                            .addOnCompleteListener {
                                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                            }
                    } else {
                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                }
                .addOnFailureListener { exception ->
                    btnConfirmSignup.isEnabled = true
                    val message = when (exception) {
                        is FirebaseAuthWeakPasswordException -> "A senha é muito fraca. Use pelo menos 6 caracteres."
                        is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado."
                        is FirebaseAuthInvalidCredentialsException -> "E-mail inválido."
                        else -> "Erro ao cadastrar: ${exception.localizedMessage}"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
        }

        tvLoginLink.setOnClickListener { navigateToLogin() }
        btnTopLogin.setOnClickListener { navigateToLogin() }
    }
}