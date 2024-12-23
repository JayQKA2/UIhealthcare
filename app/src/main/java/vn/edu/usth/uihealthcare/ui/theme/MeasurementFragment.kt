package vn.edu.usth.uihealthcare.ui.theme


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import vn.edu.usth.uihealthcare.R

class MeasurementFragment : Fragment() {

    private lateinit var weightTextView: TextView
    private lateinit var heightTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var weightSpinner: Spinner
    private lateinit var heightSpinner: Spinner
    private lateinit var distanceSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_measurement, container, false)

        weightTextView = view.findViewById(R.id.cv2t1)
        heightTextView = view.findViewById(R.id.cv2t3)
        distanceTextView = view.findViewById(R.id.cv2t5)

        weightSpinner = view.findViewById(R.id.cv2t11)
        heightSpinner = view.findViewById(R.id.cv2t31)
        distanceSpinner = view.findViewById(R.id.cv2t51)

        // Setting adapters for each Spinner
        val weightAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.weight_units,
            android.R.layout.simple_spinner_item
        )
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weightSpinner.adapter = weightAdapter

        val heightAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.height_units,
            android.R.layout.simple_spinner_item
        )
        heightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        heightSpinner.adapter = heightAdapter

        val distanceAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.distance_units,
            android.R.layout.simple_spinner_item
        )
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        distanceSpinner.adapter = distanceAdapter

        // Handle clicks for each TextView
        weightTextView.setOnClickListener {
            weightTextView.visibility = View.VISIBLE
            weightSpinner.visibility = View.VISIBLE
        }

        heightTextView.setOnClickListener {
            heightTextView.visibility = View.VISIBLE
            heightSpinner.visibility = View.VISIBLE
        }

        distanceTextView.setOnClickListener {
            distanceTextView.visibility = View.VISIBLE
            distanceSpinner.visibility = View.VISIBLE
        }

        weightSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                weightSpinner.visibility = View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        heightSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                heightSpinner.visibility = View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        distanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                distanceSpinner.visibility = View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return view
    }
}
