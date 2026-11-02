package com.example.modelodeaprendizado

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

/**
 * Auth de um app secundário do Firebase, usado para criar/remover contas de membros
 * sem substituir a sessão do administrador logado.
 */
fun authSecundario(context: Context): FirebaseAuth {
    val app = try {
        FirebaseApp.getInstance("MemberCreation")
    } catch (e: IllegalStateException) {
        FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, "MemberCreation")
    }
    return FirebaseAuth.getInstance(app)
}

object UserRoles {
    const val COLLECTION = "usuarios"
    const val FIELD_TIPO_USUARIO = "tipo_usuario"
    const val FIELD_NOME_ADMIN = "nome_admin"
    const val FIELD_EMAIL_ADMIN = "email_admin"
    const val FIELD_NOME_MEMBRO = "nome_membro"
    const val FIELD_EMAIL_MEMBRO = "email_membro"
    /** UID do administrador que cadastrou o membro. */
    const val FIELD_ADMIN_UID = "admin_uid"
    const val ADMIN = "administrador"
    const val MEMBER = "membro"
}
