package com.example.modelodeaprendizado

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TelaAdministradorEquipeActivity : AppCompatActivity() {

    private var tarefasListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireLoggedInUser()) return
        enableEdgeToEdge()
        setContentView(R.layout.tela_administrador_equipe)

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

        val fabAddTask = findViewById<ImageView>(R.id.fabAddTask)
        fabAddTask.setOnClickListener {
            val intent = Intent(this, TelaAtribuirTarefaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Escuta em tempo real: tarefas novas ou alteradas aparecem sem recarregar a tela.
        tarefasListener = FirebaseFirestore.getInstance().collection(Tarefas.COLLECTION)
            .whereEqualTo(Tarefas.FIELD_ADMIN_UID, adminUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val tarefas = snapshot.documents.map(Tarefa::from).ordenarPorPrioridadeEPrazo(this)
                mostrarTarefas(
                    container = findViewById<LinearLayout>(R.id.llTarefas),
                    tvContador = findViewById<TextView>(R.id.tvTarefasAtribuidas),
                    tvVazio = findViewById<TextView>(R.id.tvSemTarefas),
                    tarefas = tarefas,
                    acaoTexto = R.string.gerenciar_button,
                    onDetalhes = { abrirTela(TelaVerDetalhesTarefaActivity::class.java, it) },
                    onAcao = { abrirTela(TelaGerenciarTarefaActivity::class.java, it) },
                )
            }
    }

    override fun onStop() {
        super.onStop()
        tarefasListener?.remove()
        tarefasListener = null
    }

    private fun abrirTela(tela: Class<*>, tarefa: Tarefa) {
        val intent = Intent(this, tela)
        intent.putExtra(Tarefas.EXTRA_TAREFA_ID, tarefa.id)
        startActivity(intent)
    }
}
