package com.example.criminal_intent

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.util.*
import android.R.attr.bitmap
import android.graphics.Matrix

import android.media.ExifInterface




private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "date_dialog"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val REQUEST_CAMERA_PERMISSION = 3
private const val DATE_FORMAT = "EEE, MM, dd"

class CrimeFragment : Fragment(), DatePickerFragment.Callback {

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val crimeVM: CrimeVM by lazy {
        ViewModelProviders.of(this)[CrimeVM::class.java]
    }

    private lateinit var etTitle: EditText
    private lateinit var btnDate: Button
    private lateinit var cbSolved: CheckBox
    private lateinit var btnSendReport: Button
    private lateinit var btnChooseSuspect: Button
    private lateinit var ibtnTakePhoto: ImageButton
    private lateinit var ivCrimePhoto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeVM.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        etTitle = view.findViewById(R.id.et_title)
        btnDate = view.findViewById(R.id.btn_date)
        cbSolved = view.findViewById(R.id.cb_solved)
        btnSendReport = view.findViewById(R.id.btn_report)
        btnChooseSuspect = view.findViewById(R.id.btn_suspect)
        ibtnTakePhoto = view.findViewById(R.id.ibtn_take_photo)
        ivCrimePhoto = view.findViewById(R.id.iv_crime_photo)
        return view
    }

    override fun onStart() {
        super.onStart()
        val titleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etTitle.addTextChangedListener(titleTextWatcher)

        cbSolved.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        btnDate.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }
        btnSendReport.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also {
                val chooserIntent = Intent.createChooser(it, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
        btnChooseSuspect.apply {
            val picContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener { startActivityForResult(picContactIntent, REQUEST_CONTACT) }
            val packageManager = requireActivity().packageManager
            val resolveActivity = packageManager.resolveActivity(picContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            isEnabled = resolveActivity != null
        }

        ibtnTakePhoto.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                getCameraPermission {
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    val cameraActivities: List<ResolveInfo> =
                        packageManager.queryIntentActivities(captureImage,
                            PackageManager.MATCH_DEFAULT_ONLY)
                    for (cameraActivity in cameraActivities) {
                        requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                    startActivityForResult(captureImage, REQUEST_PHOTO)
                }
            }
        }
    }

    private fun getCameraPermission(onPermissionAllowed: () -> Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            onPermissionAllowed.invoke()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf<String>(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode == REQUEST_CAMERA_PERMISSION) {
            true -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ibtnTakePhoto.callOnClick()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri = data.data
                //Указать какие поля нужно вытащить из запроса
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = requireActivity().contentResolver
                    .query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    if (it.count == 0) return
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeVM.updateCrime(crime)
                    updateUI()
                }
            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeVM.crimeLiveData.observe(viewLifecycleOwner, {
            crime = it
            photoFile = crimeVM.getPhotoFile(it)
            photoUri = FileProvider.getUriForFile(requireActivity(), "com.example.criminal_intent.fileprovider", photoFile)
            updateUI()
            updatePhotoView()
        })
    }

    override fun onStop() {
        super.onStop()
        crimeVM.updateCrime(crime)
    }

    private fun updateUI() {
        etTitle.setText(crime.title)
        btnDate.text = crime.date.toString()
        cbSolved.isChecked = crime.isSolved
        cbSolved.jumpDrawablesToCurrentState()
        btnChooseSuspect.text = if (crime.suspect.isNotEmpty()) crime.suspect else getString(R.string.crime_suspect_text)
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getRotatedBitmap(getScaledBitmap(photoFile.path, requireActivity()))
            ivCrimePhoto.setOnClickListener { ImageDialogFragment(bitmap).show(requireFragmentManager(), null) }
            ivCrimePhoto.setImageBitmap(bitmap)
        } else {
            ivCrimePhoto.setImageDrawable(null)
        }
    }

    private fun getRotatedBitmap(bitmap: Bitmap): Bitmap {
        val ei = ExifInterface(photoFile.path)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        var rotatedBitmap: Bitmap? = null
        rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }
        return rotatedBitmap!!
    }

    private fun rotateImage(source: Bitmap?, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source!!, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        }else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
        // Чтение размеров изображения на диске
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val srcWidth = options.outWidth.toFloat()
        val srcHeight = options.outHeight.toFloat()
        // Выясняем, на сколько нужно уменьшить
        var inSampleSize = 1
        if (srcHeight > destHeight || srcWidth > destWidth) {
            val heightScale = srcHeight / destHeight
            val widthScale = srcWidth / destWidth
            val sampleScale = if (heightScale > widthScale) {
                heightScale
            } else {
                widthScale
            }
            inSampleSize = Math.round(sampleScale)
        }
        options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize
        // Чтение и создание окончательного растрового изображения
        return BitmapFactory.decodeFile(path, options)
    }

    fun getScaledBitmap(path: String, activity: Activity): Bitmap {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        return getScaledBitmap(path, size.x, size.y)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    companion object {
        fun newInstance(uuid: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, uuid)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }
}