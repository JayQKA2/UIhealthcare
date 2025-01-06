package vn.edu.usth.uihealthcare.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import vn.edu.usth.uihealthcare.R

class MyProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        val weightInputEditText: EditText = view.findViewById(R.id.weight_input_edit_text)
        val saveWeightButton: Button = view.findViewById(R.id.save_weight_button)

        saveWeightButton.setOnClickListener {
            val weight = weightInputEditText.text.toString().toFloatOrNull()
            if (weight != null) {
                Toast.makeText(requireContext(), "Weight saved: $weight kg", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid weight.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
