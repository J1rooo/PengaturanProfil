package com.example.pengaturanprofil

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged

/** Satu layar pengaturan profil; semua isiannya disimpan di SharedPreferences. */
class MainActivity : AppCompatActivity() {

    companion object {
        // nama file penyimpanan
        private const val NAMA_PREFS = "profil_prefs"

        // kunci data (ditulis sekali agar tidak salah ketik)
        private const val KEY_NAMA = "nama"
        private const val KEY_EMAIL = "email"
        private const val KEY_KOTA = "kota"                 // latihan 2
        private const val KEY_BIO = "bio"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_GELAP = "mode_gelap"
        private const val KEY_JUMLAH_BUKA = "jumlah_buka"   // latihan 3

        // urutan avatar sesuai tombol Ganti Avatar dan baris pilihan avatar
        private val DAFTAR_AVATAR = listOf(
            R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3
        )
    }

    private val prefs by lazy { getSharedPreferences(NAMA_PREFS, MODE_PRIVATE) }
    private var indeksAvatar = 0

    private lateinit var layoutUtama: View
    private lateinit var ivAvatar: ImageView
    private lateinit var tvNamaKartu: TextView
    private lateinit var tvEmailKartu: TextView
    private lateinit var tvKotaKartu: TextView
    private lateinit var tvLabelAvatar: TextView
    private lateinit var tvJumlahBuka: TextView
    private lateinit var rowAvatar: LinearLayout
    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etKota: EditText
    private lateinit var etBio: EditText
    private lateinit var swGelap: SwitchCompat
    private lateinit var btnGantiAvatar: Button
    private lateinit var btnReset: Button

    // kelompok view yang warnanya ikut berubah pada mode gelap
    private val kolomIsian by lazy { listOf(etNama, etEmail, etKota, etBio) }
    private val tombolGhost by lazy { listOf(btnGantiAvatar, btnReset) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) ambil semua view
        layoutUtama = findViewById(R.id.layoutUtama)
        ivAvatar = findViewById(R.id.ivAvatar)
        tvNamaKartu = findViewById(R.id.tvNamaKartu)
        tvEmailKartu = findViewById(R.id.tvEmailKartu)
        tvKotaKartu = findViewById(R.id.tvKotaKartu)
        tvLabelAvatar = findViewById(R.id.tvLabelAvatar)
        tvJumlahBuka = findViewById(R.id.tvJumlahBuka)
        rowAvatar = findViewById(R.id.rowAvatar)
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etKota = findViewById(R.id.etKota)
        etBio = findViewById(R.id.etBio)
        swGelap = findViewById(R.id.swGelap)
        btnGantiAvatar = findViewById(R.id.btnGantiAvatar)
        btnReset = findViewById(R.id.btnReset)

        // 2) aksi tombol
        btnGantiAvatar.setOnClickListener { gantiAvatar() }
        findViewById<Button>(R.id.btnSimpan).setOnClickListener { simpanProfil() }
        btnReset.setOnClickListener { hapusSemuaData() }
        rowAvatar.children.forEachIndexed { indeks, opsi ->
            opsi.setOnClickListener { pilihAvatar(indeks) }
        }

        // 3) kartu profil ikut berubah saat isian diketik
        etNama.doOnTextChanged { _, _, _, _ -> perbaruiKartu() }
        etEmail.doOnTextChanged { _, _, _, _ -> perbaruiKartu() }
        etKota.doOnTextChanged { _, _, _, _ -> perbaruiKartu() }

        // 4) tema berubah langsung saat sakelar digeser (tersimpan setelah Simpan Profil)
        swGelap.setOnCheckedChangeListener { _, aktif -> terapkanModeGelap(aktif) }

        // 5) hitung pembukaan aplikasi, lalu tampilkan data yang tersimpan
        if (savedInstanceState == null) tambahJumlahBuka() // tidak bertambah saat layar diputar
        muatProfil()
    }

    // ================= menyimpan =================
    private fun simpanProfil() {
        prefs.edit()
            .putString(KEY_NAMA, etNama.text.toString().trim())
            .putString(KEY_EMAIL, etEmail.text.toString().trim())
            .putString(KEY_KOTA, etKota.text.toString().trim())
            .putString(KEY_BIO, etBio.text.toString().trim())
            .putInt(KEY_AVATAR, indeksAvatar)
            .putBoolean(KEY_GELAP, swGelap.isChecked)
            .apply() // WAJIB: tanpa ini data tidak tertulis ke penyimpanan

        Toast.makeText(this, getString(R.string.pesan_tersimpan), Toast.LENGTH_SHORT).show()
    }

    // ================= penghitung pembukaan aplikasi =================
    private fun tambahJumlahBuka() {
        val jumlah = prefs.getInt(KEY_JUMLAH_BUKA, 0) + 1
        prefs.edit().putInt(KEY_JUMLAH_BUKA, jumlah).apply()
    }

    private fun tampilkanJumlahBuka() {
        val jumlah = prefs.getInt(KEY_JUMLAH_BUKA, 0)
        tvJumlahBuka.text = getString(R.string.info_jumlah_buka, jumlah)
    }

    // ================= membaca =================
    private fun muatProfil() {
        // nilai default dipakai bila data belum pernah disimpan
        val nama = prefs.getString(KEY_NAMA, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val kota = prefs.getString(KEY_KOTA, "") ?: ""
        val bio = prefs.getString(KEY_BIO, "") ?: ""
        val gelap = prefs.getBoolean(KEY_GELAP, false)
        indeksAvatar = prefs.getInt(KEY_AVATAR, 0).coerceIn(DAFTAR_AVATAR.indices)

        etNama.setText(nama)
        etEmail.setText(email)
        etKota.setText(kota)
        etBio.setText(bio)
        swGelap.isChecked = gelap

        tampilkanAvatar()
        terapkanModeGelap(gelap)
        perbaruiKartu()
        tampilkanJumlahBuka()
    }

    // ================= menghapus =================
    private fun hapusSemuaData() {
        prefs.edit().clear().apply()

        Toast.makeText(this, getString(R.string.pesan_reset), Toast.LENGTH_SHORT).show()
        muatProfil() // tampilan kembali ke nilai default
    }

    // ================= tampilan =================
    private fun gantiAvatar() = pilihAvatar((indeksAvatar + 1) % DAFTAR_AVATAR.size)

    private fun pilihAvatar(indeks: Int) {
        indeksAvatar = indeks
        tampilkanAvatar()
    }

    private fun tampilkanAvatar() {
        ivAvatar.setImageResource(DAFTAR_AVATAR[indeksAvatar])
        rowAvatar.children.forEachIndexed { indeks, opsi -> opsi.isSelected = indeks == indeksAvatar }
    }

    private fun perbaruiKartu() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val kota = etKota.text.toString().trim()

        tvNamaKartu.text = nama.ifEmpty { getString(R.string.nama_default) }
        tvEmailKartu.text = email.ifEmpty { getString(R.string.email_default) }
        tvKotaKartu.text = kota.ifEmpty { getString(R.string.kota_default) }
    }

    private fun terapkanModeGelap(aktif: Boolean) {
        fun warna(@ColorRes terang: Int, @ColorRes gelap: Int) = getColor(if (aktif) gelap else terang)
        fun gambar(@DrawableRes terang: Int, @DrawableRes gelap: Int) = if (aktif) gelap else terang

        val warnaNama = warna(R.color.nama_terang, R.color.nama_gelap)
        val warnaEmail = warna(R.color.email_terang, R.color.email_gelap)

        layoutUtama.setBackgroundColor(warna(R.color.latar_terang, R.color.latar_gelap))
        tvNamaKartu.setTextColor(warnaNama)
        tvEmailKartu.setTextColor(warnaEmail)
        tvKotaKartu.setTextColor(warnaEmail)
        tvLabelAvatar.setTextColor(warnaEmail)
        tvJumlahBuka.setTextColor(warnaEmail)
        swGelap.setTextColor(warnaNama)

        kolomIsian.forEach {
            it.setBackgroundResource(gambar(R.drawable.bg_input, R.drawable.bg_input_gelap))
            it.setTextColor(warnaNama)
        }
        tombolGhost.forEach {
            it.setBackgroundResource(gambar(R.drawable.bg_tombol_ghost, R.drawable.bg_tombol_ghost_gelap))
            it.setTextColor(warnaEmail)
        }

        supportActionBar?.apply {
            setBackgroundDrawable(ColorDrawable(warna(R.color.appbar_terang, R.color.appbar_gelap)))
            title = SpannableString(getString(R.string.app_name)).apply {
                val warnaJudul = warna(R.color.judul_terang, R.color.judul_gelap)
                setSpan(ForegroundColorSpan(warnaJudul), 0, length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
    }
}