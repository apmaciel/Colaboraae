package com.example.modelodeaprendizado

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

class TelaVerDetalhesTarefaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireLoggedInUser()) return
        enableEdgeToEdge()
        setContentView(R.layout.tela_ver_detalhes_tarefa)

        val mainLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        configurarMenuAdministrador(findViewById(R.id.ivMenu))

        val btnVoltar = findViewById<Button>(R.id.btnVoltar)
        btnVoltar.setOnClickListener { finish() }

        val tarefaId = intent.getStringExtra(Tarefas.EXTRA_TAREFA_ID).orEmpty()
        val responsavelUid = intent.getStringExtra(Tarefas.EXTRA_RESPONSAVEL_UID).orEmpty()
        if (tarefaId.isEmpty() || responsavelUid.isEmpty()) {
            Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Tarefas.doUsuario(responsavelUid).document(tarefaId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
                mostrarDetalhes(doc)
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun mostrarDetalhes(doc: DocumentSnapshot) {
        val status = doc.getString(Tarefas.FIELD_STATUS).orEmpty()
        val prioridade = doc.getString(Tarefas.FIELD_PRIORIDADE).orEmpty()

        findViewById<TextView>(R.id.tvDetalheTitulo).text = doc.getString(Tarefas.FIELD_NOME).orEmpty()
        findViewById<TextView>(R.id.tvDetalheResponsavel).text =
            doc.getString(Tarefas.FIELD_RESPONSAVEL_NOME).orEmpty()
        findViewById<TextView>(R.id.tvDetalheDescricao).text =
            doc.getString(Tarefas.FIELD_DESCRICAO).orEmpty()
        findViewById<TextView>(R.id.tvDetalhePrazo).text =
            doc.getTimestamp(Tarefas.FIELD_PRAZO)?.toDate()
                ?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }
                .orEmpty()

        // Mesmas cores dos cards da lista.
        findViewById<TextView>(R.id.tvDetalheStatus).apply {
            text = status
            setTextColor(
                ContextCompat.getColor(
                    this@TelaVerDetalhesTarefaActivity,
                    if (status == getString(R.string.status_concluida)) R.color.status_success_green
                    else R.color.status_warning_yellow
                )
            )
        }
        findViewById<TextView>(R.id.tvDetalhePrioridade).apply {
            text = prioridade
            setTextColor(
                ContextCompat.getColor(
                    this@TelaVerDetalhesTarefaActivity,
                    when (prioridade) {
                        getString(R.string.prioridade_alta) -> R.color.status_danger_pink
                        getString(R.string.prioridade_media) -> R.color.status_warning_yellow
                        else -> R.color.status_success_green
                    }
                )
            )
        }
    }
}
