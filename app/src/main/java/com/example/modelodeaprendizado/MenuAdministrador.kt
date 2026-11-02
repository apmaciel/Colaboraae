package com.example.modelodeaprendizado

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth

/**
 * Abre o menu do administrador (Tarefas, Equipe, Criar Membro e Sair) ancorado em [ancora].
 * Escolher a tela em que o usuário já está não faz nada.
 */
fun Activity.configurarMenuAdministrador(ancora: View) {
    ancora.setOnClickListener {
        PopupMenu(this, ancora).apply {
            menuInflater.inflate(R.menu.menu_administrador, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_tarefas -> irPara(TelaAdministradorEquipeActivity::class.java)
                    R.id.menu_equipe -> irPara(TelaMinhaEquipeActivity::class.java)
                    R.id.menu_criar_membro -> irPara(TelaCriarMembroActivity::class.java)
                    R.id.menu_sair -> {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@configurarMenuAdministrador, TelaPrincipalActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                }
                true
            }
            show()
        }
    }
}

/**
 * Reaproveita a tela se ela já estiver aberta (fechando as que estão por cima dela),
 * para o menu não empilhar cópias da mesma tela.
 */
private fun Activity.irPara(tela: Class<*>) {
    if (this::class.java == tela) return
    val intent = Intent(this, tela)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    startActivity(intent)
    finish()
}
