package com.example.modelodeaprendizado

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TelaMembroEquipeActivity : AppCompatActivity() {

    private var tarefasListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireLoggedInUser()) return
        enableEdgeToEdge()
        setContentView(R.layout.tela_membro_equipe)

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
    }

    override fun onStart() {
        super.onStart()
        val membroUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val concluida = getString(R.string.status_concluida)

        tarefasListener = FirebaseFirestore.getInstance().collection(Tarefas.COLLECTION)
            .whereEqualTo(Tarefas.FIELD_RESPONSAVEL_UID, membroUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val tarefas = snapshot.documents.map(Tarefa::from).ordenarPorPrioridadeEPrazo(this)
                val container = findViewById<LinearLayout>(R.id.llTarefas)
                mostrarTarefas(
                    container = container,
                    tvContador = findViewById<TextView>(R.id.tvTarefasAtribuidas),
                    tvVazio = findViewById<TextView>(R.id.tvSemTarefas),
                    tarefas = tarefas,
                    acaoTexto = R.string.concluir_button,
                    onDetalhes = { abrirDetalhes(it) },
                    onAcao = { concluirTarefa(it) },
                    mostrarAcao = { it.status != concluida },
                )
                // No card do membro o botão "Concluir" é verde, como no layout original.
                val verde = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.btn_green))
                for (i in 0 until container.childCount) {
                    container.getChildAt(i).findViewById<Button>(R.id.btnAcao).backgroundTintList = verde
                }
            }
    }

    override fun onStop() {
        super.onStop()
        tarefasListener?.remove()
        tarefasListener = null
    }

    private fun abrirDetalhes(tarefa: Tarefa) {
        val intent = Intent(this, TelaVerDetalhesTarefaActivity::class.java)
        intent.putExtra(Tarefas.EXTRA_TAREFA_ID, tarefa.id)
        startActivity(intent)
    }

    private fun concluirTarefa(tarefa: Tarefa) {
        FirebaseFirestore.getInstance().collection(Tarefas.COLLECTION)
            .document(tarefa.id)
            .update(Tarefas.FIELD_STATUS, getString(R.string.status_concluida))
            .addOnSuccessListener {
                Toast.makeText(this, R.string.tarefa_concluida_message, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao concluir tarefa.", Toast.LENGTH_SHORT).show()
            }
    }
}
