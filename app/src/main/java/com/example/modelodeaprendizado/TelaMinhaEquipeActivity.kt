package com.example.modelodeaprendizado

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class TelaMinhaEquipeActivity : AppCompatActivity() {
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

        val ivLogout = findViewById<ImageView>(R.id.ivLogout)
        ivLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Telalogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

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
}
