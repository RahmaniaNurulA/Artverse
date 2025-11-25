package com.example.artverse

import android.os.Bundle
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
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.example.artverse.ui.theme.ArtverseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.OutlinedTextFieldDefaults
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        artObjects = fetchArtObjects(context)
        isLoading = false
    }

    Scaffold(
        topBar = {
            if (selectedTab != 2) {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.app_name),
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
                    icon = { Icon(Icons.Default.Home, stringResource(R.string.dashboard)) },
                    label = { Text(stringResource(R.string.dashboard)) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, stringResource(R.string.search)) },
                    label = { Text(stringResource(R.string.search)) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, stringResource(R.string.ai_assistant)) },
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
    val isLandscape = isLandscape()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (isLandscape) 3 else 2),
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
                    Text(stringResource(R.string.no_image), color = Color.Gray)
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
    val isLandscape = isLandscape()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        val filteredArt = artObjects.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artistDisplayName.contains(searchQuery, ignoreCase = true) ||
                    it.culture.contains(searchQuery, ignoreCase = true)
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (isLandscape) 3 else 2),
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
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.ai_assistant), fontWeight = FontWeight.Bold) },
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
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ai_greeting),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = Color.White
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
                placeholder = { Text(stringResource(R.string.ai_prompt_placeholder)) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
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
                            val response = sendMessageToGroq(message, artObjects, context)
                            chatMessages = chatMessages + ("ai" to response)
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(stringResource(R.string.send))
            }
        }
    }
}

@Composable
fun ChatBubble(sender: String, message: String) {
    val isUser = sender == "user"
    val isLandscape = isLandscape()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (sender == "user")
                    Color(0xFF2196F3)
                else
                    Color(0xFF3F51B5)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = if (isLandscape) 400.dp else 280.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = Color.White
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

                DetailRow(stringResource(R.string.artist), art.artistDisplayName)
                DetailRow(stringResource(R.string.date), art.objectDate)
                DetailRow(stringResource(R.string.medium), art.medium)
                DetailRow(stringResource(R.string.department), art.department)
                DetailRow(stringResource(R.string.culture), art.culture)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
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

suspend fun fetchArtObjects(context: android.content.Context): List<ArtObject> = withContext(Dispatchers.IO) {
    try {
        val searchUrl = "https://collectionapi.metmuseum.org/public/collection/v1/search?hasImages=true&q=painting"
        val searchConn = URL(searchUrl).openConnection() as HttpURLConnection
        searchConn.requestMethod = "GET"

        val searchResponse = searchConn.inputStream.bufferedReader().readText()
        val searchJson = JSONObject(searchResponse)
        val objectIDs = searchJson.getJSONArray("objectIDs")

        val artList = mutableListOf<ArtObject>()
        val targetCount = 80
        var index = 0

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
                            title = objectJson.optString("title", context.getString(R.string.untitled)),
                            artistDisplayName = objectJson.optString("artistDisplayName", context.getString(R.string.unknown_artist)),
                            primaryImage = objectJson.optString("primaryImage", ""),
                            objectDate = objectJson.optString("objectDate", context.getString(R.string.unknown)),
                            medium = objectJson.optString("medium", context.getString(R.string.unknown)),
                            department = objectJson.optString("department", context.getString(R.string.unknown)),
                            culture = objectJson.optString("culture", context.getString(R.string.unknown))
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

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

suspend fun sendMessageToGroq(
    message: String,
    artObjects: List<ArtObject>,
    context: android.content.Context
): String = withContext(Dispatchers.IO) {
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
            context.getString(R.string.ai_error)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "${context.getString(R.string.connection_error)}: ${e.message}"
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    ArtverseTheme() {
        Dashboard()
    }
}