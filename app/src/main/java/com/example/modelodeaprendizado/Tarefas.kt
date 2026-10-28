package com.example.modelodeaprendizado

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

/** Nomes da coleção e dos campos das tarefas no Firestore. */
object Tarefas {
    const val COLLECTION = "tarefas"
    const val FIELD_NOME = "nome"
    const val FIELD_DESCRICAO = "descricao"
    const val FIELD_RESPONSAVEL_UID = "responsavel_uid"
    const val FIELD_RESPONSAVEL_NOME = "responsavel_nome"
    const val FIELD_ADMIN_UID = "admin_uid"
    const val FIELD_PRAZO = "prazo"
    const val FIELD_PRIORIDADE = "prioridade"
    const val FIELD_STATUS = "status"
    const val FIELD_CRIADA_EM = "criada_em"

    const val EXTRA_TAREFA_ID = "tarefa_id"
}

data class Tarefa(
    val id: String,
    val nome: String,
    val responsavelNome: String,
    val prazo: Timestamp?,
    val prioridade: String,
    val status: String,
) {
    companion object {
        fun from(doc: DocumentSnapshot) = Tarefa(
            id = doc.id,
            nome = doc.getString(Tarefas.FIELD_NOME).orEmpty(),
            responsavelNome = doc.getString(Tarefas.FIELD_RESPONSAVEL_NOME).orEmpty(),
            prazo = doc.getTimestamp(Tarefas.FIELD_PRAZO),
            prioridade = doc.getString(Tarefas.FIELD_PRIORIDADE).orEmpty(),
            status = doc.getString(Tarefas.FIELD_STATUS).orEmpty(),
        )
    }
}

data class Membro(val uid: String, val nome: String)

/**
 * Busca os membros cadastrados pelo administrador logado e os coloca no [spinner].
 * A primeira opção é sempre vazia; sem membros, só ela aparece.
 * [onCarregado] recebe a lista na mesma ordem das opções (posição no spinner - 1).
 */
fun carregarMembrosNoSpinner(spinner: Spinner, onCarregado: (List<Membro>) -> Unit = {}) {
    val context = spinner.context
    val adminUid = FirebaseAuth.getInstance().currentUser?.uid

    fun preencher(membros: List<Membro>) {
        val opcoes = listOf("") + membros.map { it.nome }
        spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, opcoes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        onCarregado(membros)
    }

    preencher(emptyList())
    if (adminUid == null) return

    FirebaseFirestore.getInstance().collection(UserRoles.COLLECTION)
        .whereEqualTo(UserRoles.FIELD_ADMIN_UID, adminUid)
        .get()
        .addOnSuccessListener { result ->
            val membros = result.documents
                .filter { it.getString(UserRoles.FIELD_TIPO_USUARIO) == UserRoles.MEMBER }
                .map { Membro(it.id, it.getString(UserRoles.FIELD_NOME_MEMBRO).orEmpty()) }
                .sortedBy { it.nome.lowercase(Locale.getDefault()) }
            preencher(membros)
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao carregar membros.", Toast.LENGTH_SHORT).show()
        }
}

/** Alta primeiro, depois Média, depois Baixa; dentro da mesma prioridade, o prazo mais próximo primeiro. */
fun List<Tarefa>.ordenarPorPrioridadeEPrazo(context: Context): List<Tarefa> {
    val nivel = mapOf(
        context.getString(R.string.prioridade_alta) to 0,
        context.getString(R.string.prioridade_media) to 1,
        context.getString(R.string.prioridade_baixa) to 2,
    )
    return sortedWith(
        compareBy<Tarefa> { nivel[it.prioridade] ?: Int.MAX_VALUE }
            .thenBy { it.prazo?.toDate()?.time ?: Long.MAX_VALUE }
    )
}

/**
 * Preenche o contador, a mensagem de lista vazia e os cards de tarefa.
 * [acaoTexto]/[onAcao] configuram o segundo botão do card (Gerenciar ou Concluir);
 * se [mostrarAcao] devolver false, o botão fica oculto para aquela tarefa.
 */
fun mostrarTarefas(
    container: LinearLayout,
    tvContador: TextView,
    tvVazio: TextView,
    tarefas: List<Tarefa>,
    acaoTexto: Int,
    onDetalhes: (Tarefa) -> Unit,
    onAcao: (Tarefa) -> Unit,
    mostrarAcao: (Tarefa) -> Boolean = { true },
) {
    val context = container.context
    tvContador.text = context.resources.getQuantityString(
        R.plurals.tarefas_atribuidas_count, tarefas.size, tarefas.size
    )
    tvVazio.visibility = if (tarefas.isEmpty()) View.VISIBLE else View.GONE

    container.removeAllViews()
    val inflater = LayoutInflater.from(context)
    val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    for (tarefa in tarefas) {
        val card = inflater.inflate(R.layout.item_tarefa_card, container, false)
        card.findViewById<TextView>(R.id.tvTitulo).text = tarefa.nome
        card.findViewById<TextView>(R.id.tvResponsavel).text =
            context.getString(R.string.responsavel_card_format, tarefa.responsavelNome)
        card.findViewById<TextView>(R.id.tvData).text =
            tarefa.prazo?.toDate()?.let(formatoData::format).orEmpty()

        val concluida = tarefa.status == context.getString(R.string.status_concluida)
        card.findViewById<TextView>(R.id.tvStatus).apply {
            text = tarefa.status
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (concluida) R.color.status_success_green else R.color.status_warning_yellow
                )
            )
        }

        val corPrioridade = when (tarefa.prioridade) {
            context.getString(R.string.prioridade_alta) -> R.color.status_danger_pink
            context.getString(R.string.prioridade_media) -> R.color.status_warning_yellow
            else -> R.color.status_success_green
        }
        card.findViewById<TextView>(R.id.tvPrioridade).apply {
            text = tarefa.prioridade
            setTextColor(ContextCompat.getColor(context, corPrioridade))
        }

        card.findViewById<Button>(R.id.btnDetalhes).setOnClickListener { onDetalhes(tarefa) }
        card.findViewById<Button>(R.id.btnAcao).apply {
            setText(acaoTexto)
            visibility = if (mostrarAcao(tarefa)) View.VISIBLE else View.INVISIBLE
            setOnClickListener { onAcao(tarefa) }
        }

        container.addView(card)
    }
}
