package com.example.modelodeaprendizado

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.Locale

class TelaGerenciarTarefaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireLoggedInUser()) return
        enableEdgeToEdge()
        setContentView(R.layout.tela_gerenciar_tarefa)

        val mainLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ivLogout = findViewById<ImageView>(R.id.ivLogout)
        ivLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Telalogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        val ivTeamNav = findViewById<ImageView>(R.id.ivTeamNav)
        ivTeamNav.setOnClickListener {
            val intent = Intent(this, TelaMinhaEquipeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val ivTasksNav = findViewById<ImageView>(R.id.ivTasksNav)
        ivTasksNav.setOnClickListener {
            val intent = Intent(this, TelaAdministradorEquipeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val etPrazo = findViewById<EditText>(R.id.etPrazo)
        etPrazo.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    etPrazo.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val btnVoltar = findViewById<Button>(R.id.btnVoltar)
        btnVoltar.setOnClickListener { finish() }

        val btnSalvarAlteracoes = findViewById<Button>(R.id.btnSalvarAlteracoes)
        btnSalvarAlteracoes.setOnClickListener {
            Toast.makeText(this, R.string.task_updated_message, Toast.LENGTH_SHORT).show()
            finish()
        }

        val btnExcluirTarefa = findViewById<MaterialButton>(R.id.btnExcluirTarefa)
        btnExcluirTarefa.setOnClickListener {
            Toast.makeText(this, R.string.task_deleted_message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
