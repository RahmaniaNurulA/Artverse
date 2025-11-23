package com.example.artverse

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.artverse.ui.theme.ArtverseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtverseTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Dashboard()
                }
            }
        }
    }
}

@Composable
fun ArtverseTheme(content: @Composable () -> Unit){
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF5F5F5)
    ),
        content = content
    )
}

data class ArtObject(
    val objectID: Int,
    val title: String,
    val artistDisplayName: String,
    val primaryImage: String,
    val objectDate: String,
    val medium: String,
    val department: String,
    val culture: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(){
    var selectedTab by remember { mutableStateOf(0) }
    var artObjects by remember { mutableStateOf<List<ArtObject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedArt by remember { mutableStateOf<ArtObject?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        artObjects = fetchArtObjects()
        isLoading = false
    }

    Scaffold(
        topBar = {
            if (selectedTab != 2) {
                TopAppBar(
                    title = {
                        Text(
                            "Artverse",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, "Search") },
                    label = { Text("Search") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, "AI Assistant") },
                    label = { Text("AI") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    artObjects = artObjects,
                    isLoading = isLoading,
                    onArtClick = { selectedArt = it }
                )
                1 -> SearchScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    artObjects = artObjects,
                    onArtClick = { selectedArt = it }
                )
                2 -> AIAssistantScreen(artObjects = artObjects)
            }

            selectedArt?.let { art ->
                ArtDetailDialog(
                    art = art,
                    onDismiss = { selectedArt = null }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    artObjects: List<ArtObject>,
    isLoading: Boolean,
    onArtClick: (ArtObject) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(artObjects) { art ->
                ArtCard(art = art, onClick = { onArtClick(art) })
            }
        }
    }
}

@Composable
fun ArtCard(art: ArtObject, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (art.primaryImage.isNotEmpty()) {
                AsyncImage(
                    model = art.primaryImage,
                    contentDescription = art.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = art.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (art.artistDisplayName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = art.artistDisplayName,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    artObjects: List<ArtObject>,
    onArtClick: (ArtObject) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search artworks...") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        val filteredArt = artObjects.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artistDisplayName.contains(searchQuery, ignoreCase = true) ||
                    it.culture.contains(searchQuery, ignoreCase = true)
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(filteredArt) { art ->
                ArtCard(art = art, onClick = { onArtClick(art) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(artObjects: List<ArtObject>) {
    var userMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("AI Art Assistant", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Hello! I'm your AI art assistant. Ask me anything about the artworks in the gallery!",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }

            items(chatMessages.size) { index ->
                val (sender, message) = chatMessages[index]
                ChatBubble(sender = sender, message = message)
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about artworks...") },
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (userMessage.isNotBlank() && !isLoading) {
                        val message = userMessage
                        chatMessages = chatMessages + ("user" to message)
                        userMessage = ""
                        isLoading = true

                        scope.launch {
                            val response = sendMessageToGroq(message, artObjects)
                            chatMessages = chatMessages + ("ai" to response)
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun ChatBubble(sender: String, message: String) {
    val isUser = sender == "user"

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ArtDetailDialog(art: ArtObject, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(art.title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (art.primaryImage.isNotEmpty()) {
                    AsyncImage(
                        model = art.primaryImage,
                        contentDescription = art.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                DetailRow("Artist", art.artistDisplayName)
                DetailRow("Date", art.objectDate)
                DetailRow("Medium", art.medium)
                DetailRow("Department", art.department)
                DetailRow("Culture", art.culture)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(text = value, fontSize = 14.sp)
        }
    }
}

suspend fun fetchArtObjects(): List<ArtObject> = withContext(Dispatchers.IO) {
    try {
        val searchUrl = "https://collectionapi.metmuseum.org/public/collection/v1/search?hasImages=true&q=painting"
        val searchConn = URL(searchUrl).openConnection() as HttpURLConnection
        searchConn.requestMethod = "GET"

        val searchResponse = searchConn.inputStream.bufferedReader().readText()
        val searchJson = JSONObject(searchResponse)
        val objectIDs = searchJson.getJSONArray("objectIDs")

        val artList = mutableListOf<ArtObject>()
        val targetCount = 80 // Target jumlah artwork yang ingin ditampilkan
        var index = 0

        // Loop sampai dapat 80 artwork ATAU kehabisan data
        while (artList.size < targetCount && index < objectIDs.length()) {
            try {
                val objectID = objectIDs.getInt(index)
                val objectUrl = "https://collectionapi.metmuseum.org/public/collection/v1/objects/$objectID"
                val objectConn = URL(objectUrl).openConnection() as HttpURLConnection
                objectConn.requestMethod = "GET"

                val objectResponse = objectConn.inputStream.bufferedReader().readText()
                val objectJson = JSONObject(objectResponse)

                if (objectJson.optString("primaryImage").isNotEmpty()) {
                    artList.add(
                        ArtObject(
                            objectID = objectJson.getInt("objectID"),
                            title = objectJson.optString("title", "Untitled"),
                            artistDisplayName = objectJson.optString("artistDisplayName", "Unknown Artist"),
                            primaryImage = objectJson.optString("primaryImage", ""),
                            objectDate = objectJson.optString("objectDate", "Unknown"),
                            medium = objectJson.optString("medium", "Unknown"),
                            department = objectJson.optString("department", "Unknown"),
                            culture = objectJson.optString("culture", "Unknown")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            index++
        }

        artList
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun sendMessageToGroq(message: String, artObjects: List<ArtObject>): String = withContext(Dispatchers.IO) {
    try {
        val artContext = artObjects.take(5).joinToString("\n") {
            "- ${it.title} by ${it.artistDisplayName} (${it.objectDate})"
        }

        val requestBody = """
        {
            "model": "llama3-8b-8192",
            "messages": [
                {
                    "role": "system",
                    "content": "You are an art expert assistant. Help users understand artworks from the Met Museum collection. Here are some artworks in the gallery:\n$artContext"
                },
                {
                    "role": "user",
                    "content": "$message"
                }
            ],
            "temperature": 0.7,
            "max_tokens": 500
        }
        """.trimIndent()

        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer ${constant.GROQ_API_KEY}")
        conn.doOutput = true

        conn.outputStream.write(requestBody.toByteArray())

        val responseCode = conn.responseCode
        if (responseCode == 200) {
            val response = conn.inputStream.bufferedReader().readText()
            val jsonResponse = JSONObject(response)
            val choices = jsonResponse.getJSONArray("choices")
            val message = choices.getJSONObject(0).getJSONObject("message")
            message.getString("content")
        } else {
            "Sorry, I couldn't process your request. Please make sure you've added your Groq API key."
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: ${e.message ?: "Unable to connect to AI service"}"
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    ArtverseTheme() {
        Dashboard()
    }
}