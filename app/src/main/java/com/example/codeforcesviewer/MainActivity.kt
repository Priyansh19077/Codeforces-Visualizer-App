package com.example.codeforcesviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.codeforcesviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.submit.setOnClickListener {
            val handle = binding.editText.text
            Log.d("MainActivity", handle.toString())
            if(handle != null) {
                val intent = Intent(this, Dashboard::class.java)
                intent.putExtra("handle", handle.toString())
                startActivity(intent)
            }else{
                Log.d("MainActivity", "No handle provided")
                Toast.makeText(applicationContext, "Handle cannot be empty!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}