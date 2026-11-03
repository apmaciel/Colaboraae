package com.example.modelodeaprendizado

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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

        // Fica invisível até sabermos o tipo do usuário, para o ícone não "pular" de lado.
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.visibility = View.INVISIBLE
        ivBack.setOnClickListener { finish() }

        // O menu é só do administrador: fica oculto até confirmarmos o tipo do usuário.
        val ivMenu = findViewById<ImageView>(R.id.ivMenu)
        ivMenu.visibility = View.GONE

        val btnVoltar = findViewById<Button>(R.id.btnVoltar)
        btnVoltar.setOnClickListener { finish() }

        val tarefaId = intent.getStringExtra(Tarefas.EXTRA_TAREFA_ID).orEmpty()
        val responsavelUid = intent.getStringExtra(Tarefas.EXTRA_RESPONSAVEL_UID).orEmpty()
        val usuarioUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (tarefaId.isEmpty() || responsavelUid.isEmpty() || usuarioUid.isEmpty()) {
            Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseFirestore.getInstance().collection(UserRoles.COLLECTION).document(usuarioUid).get()
            .addOnSuccessListener { perfil ->
                when (perfil.getString(UserRoles.FIELD_TIPO_USUARIO)) {
                    UserRoles.ADMIN -> {
                        ivBack.visibility = View.VISIBLE
                        ivMenu.visibility = View.VISIBLE
                        configurarMenuAdministrador(ivMenu)
                        carregarTarefa(responsavelUid, tarefaId)
                    }
                    // Membro só vê a tarefa que foi atribuída a ele.
                    UserRoles.MEMBER ->
                        if (responsavelUid == usuarioUid) {
                            // O membro volta pelo botão "Voltar"; o ícone do topo fica oculto.
                            ivBack.visibility = View.GONE
                            carregarTarefa(responsavelUid, tarefaId)
                        } else erroEFechar()
                    else -> erroEFechar()
                }
            }
            .addOnFailureListener { erroEFechar() }
    }

    private fun carregarTarefa(responsavelUid: String, tarefaId: String) {
        Tarefas.doUsuario(responsavelUid).document(tarefaId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    erroEFechar()
                    return@addOnSuccessListener
                }
                mostrarDetalhes(doc)
            }
            .addOnFailureListener { erroEFechar() }
    }

    private fun erroEFechar() {
        Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
        finish()
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

        // Somente leitura: vazio até o membro concluir a tarefa (ou se o admin reabrir a tarefa).
        val concluida = status == getString(R.string.status_concluida)
        findViewById<TextView>(R.id.tvDetalheRealizada).text =
            if (concluida) {
                doc.getTimestamp(Tarefas.FIELD_DATA_REALIZACAO)?.toDate()
                    ?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }
                    .orEmpty()
            } else ""

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
