<h2>Artverse</h2>

<h3>Aplikasi Galeri Seni Berbasis AI</h3>

Artverse adalah aplikasi galeri seni yang menampilkan koleksi dari MetMuseum API dan menyediakan analisis karya menggunakan AI melalui Groq API. Aplikasi ini mendukung tema terang dan gelap serta tampilan portrait maupun landscape untuk pengalaman yang lebih fleksibel.

<h3>Fitur Utama</h3>
1. Dashboard

Menampilkan koleksi seni dari MetMuseum API.

<table> <tr> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/dashboard.jpg" width="150" height="300"></td> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/DashboardDark.jpg" width="150" height="300"></td> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/DashboardLandscape.jpg" width="200"></td> </tr> </table>
2. Search

Mencari karya seni berdasarkan kata kunci.

<table> <tr> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/Search.jpg" width="150" height="300"></td> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/SearchDark.jpg" width="150" height="300"></td> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/SearchLandscape.jpg" width="200"></td> </tr> </table>
3. AI Analysis

Fitur untuk memberikan penjelasan atau interpretasi karya seni menggunakan Groq API.

<table> <tr> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/AI.jpg" width="150" height="300"></td> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/AIDark.jpg" width="150" height="300"></td> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/AILandscape.jpg" width="200"></td> </tr> </table>
4. Card View

Halaman detail untuk menampilkan informasi lengkap karya seni.

<table> <tr> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/CardLight.jpg" width="150" height="300"></td> <td><img src="https://github.com/RahmaniaNurulA/Artverse/blob/master/cardDark.jpg" width="150" height="300"></td> </tr> </table>

<h3>Teknologi yang Digunakan</h3>
<ol>
  <li>Kotlin</li>
  <li>Jetpack Compose</li>
  <li>Material 3 (Compose Material)</li>
  <li>Retrofit — untuk request API</li>
  <li>Coroutine + Flow</li>
  <li>Groq API — untuk fitur AI</li>
  <li>MetMuseum API — sumber data karya seni</li>
  <li>Coil — untuk loading gambar</li>
  <li>MVVM Architecture</li>
</ol>

<h3>Cara Mendapatkan API Key Groq:</h3>
<ol>
  <li>Kunjungi <a href="https://console.groq.com">Groq Console</a></li>
  <li>Daftar atau login ke akun Anda</li>
  <li>Buat API Key baru</li>
  <li>Salin dan simpan di file <code>constant</code></li>
</ol>
