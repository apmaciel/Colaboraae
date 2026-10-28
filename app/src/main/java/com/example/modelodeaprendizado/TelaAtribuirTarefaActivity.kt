package com.example.modelodeaprendizado

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class TelaAtribuirTarefaActivity : AppCompatActivity() {

    private var membros: List<Membro> = emptyList()
    private var prazoSelecionado: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireLoggedInUser()) return
        enableEdgeToEdge()
        setContentView(R.layout.tela_atribuir_tarefa)

        val mainLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        val etNomeTarefa = findViewById<EditText>(R.id.etNomeTarefa)
        val spResponsavel = findViewById<Spinner>(R.id.spResponsavel)
        val spPrioridade = findViewById<Spinner>(R.id.spPrioridade)
        val etDescricao = findViewById<EditText>(R.id.etDescricao)

        carregarMembrosNoSpinner(spResponsavel) { membros = it }

        val etPrazo = findViewById<EditText>(R.id.etPrazo)
        etPrazo.setOnClickListener {
            val calendar = prazoSelecionado ?: Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    prazoSelecionado = Calendar.getInstance().apply {
                        clear()
                        set(year, month, dayOfMonth)
                    }
                    etPrazo.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        btnCancelar.setOnClickListener { finish() }

        val btnCriar = findViewById<Button>(R.id.btnCriar)
        btnCriar.setOnClickListener {
            val nome = etNomeTarefa.text.toString().trim()
            // Posição 0 é a opção vazia nos dois spinners.
            val responsavel = membros.getOrNull(spResponsavel.selectedItemPosition - 1)
            val prioridade = if (spPrioridade.selectedItemPosition > 0) {
                spPrioridade.selectedItem.toString()
            } else ""
            val prazo = prazoSelecionado

            if (nome.isEmpty() || responsavel == null || prazo == null || prioridade.isEmpty()) {
                val mensagem = if (membros.isEmpty() && nome.isNotEmpty() && prazo != null && prioridade.isNotEmpty()) {
                    R.string.tarefa_sem_membros
                } else {
                    R.string.tarefa_campos_obrigatorios
                }
                Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val tarefa = hashMapOf(
                Tarefas.FIELD_NOME to nome,
                Tarefas.FIELD_DESCRICAO to etDescricao.text.toString().trim(),
                Tarefas.FIELD_RESPONSAVEL_UID to responsavel.uid,
                Tarefas.FIELD_RESPONSAVEL_NOME to responsavel.nome,
                Tarefas.FIELD_ADMIN_UID to adminUid,
                Tarefas.FIELD_PRAZO to Timestamp(prazo.time),
                Tarefas.FIELD_PRIORIDADE to prioridade,
                Tarefas.FIELD_STATUS to getString(R.string.status_em_andamento),
                Tarefas.FIELD_CRIADA_EM to FieldValue.serverTimestamp(),
            )

            btnCriar.isEnabled = false
            FirebaseFirestore.getInstance().collection(Tarefas.COLLECTION)
                .add(tarefa)
                .addOnSuccessListener {
                    Toast.makeText(this, R.string.task_created_message, Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    btnCriar.isEnabled = true
                    Toast.makeText(this, R.string.tarefa_erro_criar, Toast.LENGTH_LONG).show()
                }
        }
    }
}
