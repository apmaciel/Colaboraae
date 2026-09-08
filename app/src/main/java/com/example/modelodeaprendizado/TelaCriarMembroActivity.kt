package com.example.modelodeaprendizado

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class TelaCriarMembroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireLoggedInUser()) return
        enableEdgeToEdge()
        setContentView(R.layout.tela_criar_membro)

        val mainLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        btnCancelar.setOnClickListener { finish() }

        val etNome = findViewById<EditText>(R.id.etNome)
        val etEmail = findViewById<EditText>(R.id.etEmail)

        val btnCriar = findViewById<Button>(R.id.btnCriar)
        btnCriar.setOnClickListener {
            val name = etNome.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCriar.isEnabled = false
            val defaultPassword = getString(R.string.senha_padrao_value)

            // Usa um app secundário do Firebase só para criar a conta, assim a
            // sessão do administrador logado não é substituída pela do novo membro.
            val secondaryApp = try {
                FirebaseApp.getInstance("MemberCreation")
            } catch (e: IllegalStateException) {
                FirebaseApp.initializeApp(this, FirebaseApp.getInstance().options, "MemberCreation")
            }
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

            secondaryAuth.createUserWithEmailAndPassword(email, defaultPassword)
                .addOnSuccessListener { authResult ->
                    val newUid = authResult.user?.uid
                    secondaryAuth.signOut()

                    if (newUid == null) {
                        btnCriar.isEnabled = true
                        Toast.makeText(this, "Erro ao criar membro. Tente novamente.", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    val memberData = hashMapOf(
                        UserRoles.FIELD_NOME_MEMBRO to name,
                        UserRoles.FIELD_EMAIL_MEMBRO to email,
                        UserRoles.FIELD_TIPO_USUARIO to UserRoles.MEMBER
                    )
                    FirebaseFirestore.getInstance().collection(UserRoles.COLLECTION)
                        .document(newUid)
                        .set(memberData)
                        .addOnSuccessListener {
                            Toast.makeText(this, R.string.member_created_message, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            btnCriar.isEnabled = true
                            Toast.makeText(this, "Membro criado, mas houve erro ao salvar os dados.", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { exception ->
                    btnCriar.isEnabled = true
                    val message = when (exception) {
                        is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado."
                        is FirebaseAuthInvalidCredentialsException -> "E-mail inválido."
                        is FirebaseAuthWeakPasswordException -> "Erro interno de senha padrão."
                        else -> "Erro ao criar membro: ${exception.localizedMessage}"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
        }
    }
}
