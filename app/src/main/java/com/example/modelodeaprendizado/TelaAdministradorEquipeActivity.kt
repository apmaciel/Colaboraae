package com.example.modelodeaprendizado

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class TelaAdministradorEquipeActivity : AppCompatActivity() {
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

        val openGerenciarTarefa = {
            val intent = Intent(this, TelaGerenciarTarefaActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnTarefa1Gerenciar).setOnClickListener { openGerenciarTarefa() }
        findViewById<Button>(R.id.btnTarefa2Gerenciar).setOnClickListener { openGerenciarTarefa() }
        findViewById<Button>(R.id.btnTarefa3Gerenciar).setOnClickListener { openGerenciarTarefa() }

        val openVerDetalhesTarefa = {
            val intent = Intent(this, TelaVerDetalhesTarefaActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnTarefa1Detalhes).setOnClickListener { openVerDetalhesTarefa() }
        findViewById<Button>(R.id.btnTarefa2Detalhes).setOnClickListener { openVerDetalhesTarefa() }
        findViewById<Button>(R.id.btnTarefa3Detalhes).setOnClickListener { openVerDetalhesTarefa() }
    }
}
