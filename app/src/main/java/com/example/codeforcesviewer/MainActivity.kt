package com.example.codeforcesviewer

import android.content.Context
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
        val sharedPreference =  getSharedPreferences("com.example.codeforcesviewer", Context.MODE_PRIVATE)
        val prevUsername = sharedPreference.getString("username", null)
        Log.i("Previous Username", prevUsername.toString())
        if(prevUsername != null){
            Toast.makeText(this, "Last selected username: $prevUsername\n Press back to reset username", Toast.LENGTH_LONG).show()
            val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("handle", prevUsername.toString())
            startActivity(intent)
        }
        binding.submit.setOnClickListener {
            val handle = binding.editText.text
            Log.d("MainActivity", handle.toString())
            if(handle != null && handle.toString() != "") {
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