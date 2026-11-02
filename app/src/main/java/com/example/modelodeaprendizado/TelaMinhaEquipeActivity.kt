package com.example.modelodeaprendizado

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class TelaMinhaEquipeActivity : AppCompatActivity() {

    private var membrosListener: ListenerRegistration? = null

    private data class MembroEquipe(val uid: String, val nome: String, val email: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireLoggedInUser()) return
        enableEdgeToEdge()
        setContentView(R.layout.tela_minha_equipe)

        val mainLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tarefas e Equipe se substituem na navegação, então "voltar" daqui leva às Tarefas.
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            startActivity(Intent(this, TelaAdministradorEquipeActivity::class.java))
            finish()
        }

        configurarMenuAdministrador(findViewById(R.id.ivMenu))

        val ivTasksNav = findViewById<ImageView>(R.id.ivTasksNav)
        ivTasksNav.setOnClickListener {
            val intent = Intent(this, TelaAdministradorEquipeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val fabAddMember = findViewById<ImageView>(R.id.fabAddMember)
        fabAddMember.setOnClickListener {
            val intent = Intent(this, TelaCriarMembroActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Escuta em tempo real: membros novos ou excluídos aparecem sem recarregar a tela.
        membrosListener = FirebaseFirestore.getInstance().collection(UserRoles.COLLECTION)
            .whereEqualTo(UserRoles.FIELD_ADMIN_UID, adminUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Toast.makeText(this, R.string.membro_erro_carregar, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val membros = snapshot.documents
                    .filter { it.getString(UserRoles.FIELD_TIPO_USUARIO) == UserRoles.MEMBER }
                    .map {
                        MembroEquipe(
                            uid = it.id,
                            nome = it.getString(UserRoles.FIELD_NOME_MEMBRO).orEmpty(),
                            email = it.getString(UserRoles.FIELD_EMAIL_MEMBRO).orEmpty(),
                        )
                    }
                    .sortedBy { it.nome.lowercase(Locale.getDefault()) }
                mostrarMembros(membros)
            }
    }

    override fun onStop() {
        super.onStop()
        membrosListener?.remove()
        membrosListener = null
    }

    private fun mostrarMembros(membros: List<MembroEquipe>) {
        findViewById<TextView>(R.id.tvMembrosCount).text =
            resources.getQuantityString(R.plurals.membros_count, membros.size, membros.size)
        findViewById<TextView>(R.id.tvSemMembros).visibility =
            if (membros.isEmpty()) View.VISIBLE else View.GONE

        val container = findViewById<LinearLayout>(R.id.llMembros)
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)
        for (membro in membros) {
            val linha = inflater.inflate(R.layout.item_membro_row, container, false)
            linha.findViewById<TextView>(R.id.tvMembroNome).text = membro.nome
            linha.findViewById<TextView>(R.id.tvMembroEmail).text = membro.email
            linha.findViewById<View>(R.id.btnExcluirMembro).setOnClickListener { confirmarExclusao(membro) }
            container.addView(linha)
        }
    }

    private fun confirmarExclusao(membro: MembroEquipe) {
        AlertDialog.Builder(this)
            .setTitle(R.string.excluir_membro_confirmar_title)
            .setMessage(getString(R.string.excluir_membro_confirmar_message, membro.nome))
            .setPositiveButton(R.string.excluir_button) { _, _ -> excluirMembro(membro) }
            .setNegativeButton(R.string.cancelar_button, null)
            .show()
    }

    /**
     * Remove o perfil do membro e as tarefas dele (a subcoleção não some junto com o documento).
     * Sem o perfil, o login do membro é recusado e ele sai das listas do administrador.
     */
    private fun excluirMembro(membro: MembroEquipe) {
        val db = FirebaseFirestore.getInstance()
        Tarefas.doUsuario(membro.uid).get()
            .addOnSuccessListener { tarefas ->
                val batch = db.batch()
                tarefas.documents.forEach { batch.delete(it.reference) }
                batch.delete(db.collection(UserRoles.COLLECTION).document(membro.uid))
                batch.commit()
                    .addOnSuccessListener {
                        removerContaDeLogin(membro)
                        Toast.makeText(this, R.string.membro_excluido_message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, R.string.membro_erro_excluir, Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.membro_erro_excluir, Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Tenta apagar também a conta de login, entrando com a senha padrão num app secundário
     * (o cliente não pode apagar contas de outros usuários). Se o membro já trocou a senha, a conta
     * de login permanece, mas sem perfil ela não consegue mais entrar no app.
     */
    private fun removerContaDeLogin(membro: MembroEquipe) {
        if (membro.email.isEmpty()) return
        val auth = authSecundario(this)
        auth.signInWithEmailAndPassword(membro.email, getString(R.string.senha_padrao_value))
            .addOnSuccessListener { resultado ->
                resultado.user?.delete()?.addOnCompleteListener { auth.signOut() } ?: auth.signOut()
            }
    }
}
