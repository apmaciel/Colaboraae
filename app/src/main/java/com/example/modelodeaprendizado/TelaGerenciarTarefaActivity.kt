package com.example.modelodeaprendizado

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class TelaGerenciarTarefaActivity : AppCompatActivity() {

    private lateinit var etNomeTarefa: EditText
    private lateinit var spResponsavel: Spinner
    private lateinit var spStatus: Spinner
    private lateinit var spPrioridade: Spinner
    private lateinit var etPrazo: EditText
    private lateinit var etDescricao: EditText
    private lateinit var btnSalvarAlteracoes: Button
    private lateinit var btnExcluirTarefa: MaterialButton

    private var tarefaId: String = ""
    private var responsavelUidOriginal: String = ""
    private var dadosOriginais: Map<String, Any> = emptyMap()
    private var membros: List<Membro> = emptyList()
    private var prazoSelecionado: Calendar? = null

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

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        configurarMenuAdministrador(findViewById(R.id.ivMenu))

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

        etNomeTarefa = findViewById(R.id.etNomeTarefa)
        spResponsavel = findViewById(R.id.spResponsavel)
        spStatus = findViewById(R.id.spStatus)
        spPrioridade = findViewById(R.id.spPrioridade)
        etPrazo = findViewById(R.id.etPrazo)
        etDescricao = findViewById(R.id.etDescricao)
        btnSalvarAlteracoes = findViewById(R.id.btnSalvarAlteracoes)
        btnExcluirTarefa = findViewById(R.id.btnExcluirTarefa)

        tarefaId = intent.getStringExtra(Tarefas.EXTRA_TAREFA_ID).orEmpty()
        responsavelUidOriginal = intent.getStringExtra(Tarefas.EXTRA_RESPONSAVEL_UID).orEmpty()
        if (tarefaId.isEmpty() || responsavelUidOriginal.isEmpty()) {
            Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Só libera salvar/excluir depois que a tarefa foi carregada.
        btnSalvarAlteracoes.isEnabled = false
        btnExcluirTarefa.isEnabled = false

        carregarMembrosNoSpinner(spResponsavel) {
            membros = it
            selecionarResponsavel(responsavelUidOriginal)
        }
        carregarTarefa()

        etPrazo.setOnClickListener {
            val calendar = prazoSelecionado ?: Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    prazoSelecionado = Calendar.getInstance().apply {
                        clear()
                        set(year, month, dayOfMonth)
                    }
                    etPrazo.setText(formatarPrazo(year, month, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        findViewById<Button>(R.id.btnVoltar).setOnClickListener { finish() }
        btnSalvarAlteracoes.setOnClickListener { salvarAlteracoes() }
        btnExcluirTarefa.setOnClickListener { confirmarExclusao() }
    }

    private fun referenciaOriginal() =
        Tarefas.doUsuario(responsavelUidOriginal).document(tarefaId)

    private fun carregarTarefa() {
        referenciaOriginal().get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
                dadosOriginais = doc.data.orEmpty()

                etNomeTarefa.setText(doc.getString(Tarefas.FIELD_NOME).orEmpty())
                etDescricao.setText(doc.getString(Tarefas.FIELD_DESCRICAO).orEmpty())
                selecionarOpcao(spStatus, R.array.status_options, doc.getString(Tarefas.FIELD_STATUS))
                selecionarOpcao(spPrioridade, R.array.prioridade_options, doc.getString(Tarefas.FIELD_PRIORIDADE))

                doc.getTimestamp(Tarefas.FIELD_PRAZO)?.let { prazo ->
                    val calendar = Calendar.getInstance().apply { time = prazo.toDate() }
                    prazoSelecionado = calendar
                    etPrazo.setText(
                        formatarPrazo(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    )
                }

                selecionarResponsavel(responsavelUidOriginal)
                btnSalvarAlteracoes.isEnabled = true
                btnExcluirTarefa.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.tarefa_erro_carregar, Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    /** O spinner de responsável é preenchido por outra consulta; a seleção é refeita quando ela chega. */
    private fun selecionarResponsavel(uid: String) {
        val indice = membros.indexOfFirst { it.uid == uid }
        // Posição 0 é a opção vazia.
        if (indice >= 0) spResponsavel.setSelection(indice + 1)
    }

    private fun selecionarOpcao(spinner: Spinner, arrayRes: Int, valor: String?) {
        val indice = resources.getStringArray(arrayRes).indexOf(valor)
        if (indice >= 0) spinner.setSelection(indice)
    }

    private fun formatarPrazo(year: Int, month: Int, dayOfMonth: Int) =
        String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)

    private fun salvarAlteracoes() {
        val nome = etNomeTarefa.text.toString().trim()
        // Posição 0 é a opção vazia nos três spinners.
        val responsavel = membros.getOrNull(spResponsavel.selectedItemPosition - 1)
        val status = if (spStatus.selectedItemPosition > 0) spStatus.selectedItem.toString() else ""
        val prioridade = if (spPrioridade.selectedItemPosition > 0) spPrioridade.selectedItem.toString() else ""
        val prazo = prazoSelecionado

        if (nome.isEmpty() || responsavel == null || status.isEmpty() || prioridade.isEmpty() || prazo == null) {
            Toast.makeText(this, R.string.tarefa_gerenciar_campos_obrigatorios, Toast.LENGTH_SHORT).show()
            return
        }

        val alteracoes = mapOf(
            Tarefas.FIELD_NOME to nome,
            Tarefas.FIELD_DESCRICAO to etDescricao.text.toString().trim(),
            Tarefas.FIELD_RESPONSAVEL_UID to responsavel.uid,
            Tarefas.FIELD_RESPONSAVEL_NOME to responsavel.nome,
            Tarefas.FIELD_PRAZO to Timestamp(prazo.time),
            Tarefas.FIELD_PRIORIDADE to prioridade,
            Tarefas.FIELD_STATUS to status,
        )

        btnSalvarAlteracoes.isEnabled = false
        val tarefaAtualizada = if (responsavel.uid == responsavelUidOriginal) {
            referenciaOriginal().update(alteracoes)
        } else {
            // A tarefa vive na subcoleção do responsável: trocar de responsável move o documento.
            val db = FirebaseFirestore.getInstance()
            db.batch().apply {
                delete(referenciaOriginal())
                set(Tarefas.doUsuario(responsavel.uid).document(tarefaId), dadosOriginais + alteracoes)
            }.commit()
        }

        tarefaAtualizada
            .addOnSuccessListener {
                Toast.makeText(this, R.string.task_updated_message, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btnSalvarAlteracoes.isEnabled = true
                Toast.makeText(this, R.string.tarefa_erro_atualizar, Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle(R.string.excluir_tarefa_confirmar_title)
            .setMessage(R.string.excluir_tarefa_confirmar_message)
            .setPositiveButton(R.string.excluir_button) { _, _ -> excluirTarefa() }
            .setNegativeButton(R.string.cancelar_button, null)
            .show()
    }

    private fun excluirTarefa() {
        btnExcluirTarefa.isEnabled = false
        referenciaOriginal().delete()
            .addOnSuccessListener {
                Toast.makeText(this, R.string.task_deleted_message, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btnExcluirTarefa.isEnabled = true
                Toast.makeText(this, R.string.tarefa_erro_excluir, Toast.LENGTH_LONG).show()
            }
    }
}
