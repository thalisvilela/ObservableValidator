package com.umobi.observablevalidatorsample.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.umobi.observablevalidatorsample.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        binding.lifecycleOwner = this

        setContentView(binding.root)

        registerViewModel = ViewModelProviders.of(this)
            .get(RegisterViewModel::class.java)


        binding.viewModel = registerViewModel

        register.setOnClickListener {
            if(registerViewModel.validator.validateAll()){
                Snackbar.make(binding.root, "All good! Proceeding to register!", Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(binding.root, "Validation failed!", Snackbar.LENGTH_LONG).show()
            }
        }


    }


}
