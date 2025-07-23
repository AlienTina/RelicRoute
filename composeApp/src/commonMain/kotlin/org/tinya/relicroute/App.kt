package org.tinya.relicroute

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.tinya.relicroute.backend.ItemManager
import org.tinya.relicroute.ui.AppTheme
import org.tinya.relicroute.ui.darkScheme
import org.tinya.relicroute.ui.lightScheme
import kotlin.math.round

val settings: Settings = Settings()

@Serializable
@JsonIgnoreUnknownKeys
data class Relic(
    val imageName: String = "",
    val marketInfo: MarketInfo = MarketInfo("null"),
    val name: String = "",
    val rewards: List<Reward> = emptyList(),
    var price: Double = 0.0,
)

@Serializable
@JsonIgnoreUnknownKeys
data class MarketInfo(
    val urlName: String,
)

@Serializable
@JsonIgnoreUnknownKeys
data class Reward(
    val rarity: String,
    val item: Item,
)

@Serializable
@JsonIgnoreUnknownKeys
data class Item(
    val name: String,
    val warframeMarket: WarframeMarket = WarframeMarket("null"),
)

@Serializable
@JsonIgnoreUnknownKeys
data class WarframeMarket(
    val urlName: String,
)

@Serializable
@JsonIgnoreUnknownKeys
data class Orders(
    val payload: Payload,
)

@Serializable
@JsonIgnoreUnknownKeys
data class Payload(
    val orders: List<Order>,
)

@Serializable
@JsonIgnoreUnknownKeys
data class Order(
    val order_type: String,
    val platinum: Double,
    val user: User,
    val visible: Boolean,
)

@Serializable
@JsonIgnoreUnknownKeys
data class User(
    val status: String,
)


var itemManager = ItemManager()

fun RarityToChance(name: String, rarity: String): Double{
    val type = name.split(" ").last()
    when (type){
        "Intact" -> {
            when (rarity) {
                "Rare" -> return 0.02
                "Uncommon" -> return 0.11
                "Common" -> return 0.2533
            }
        }
        "Exceptional" -> {
            when (rarity) {
                "Rare" -> return 0.04
                "Uncommon" -> return 0.13
                "Common" -> return 0.2333
            }
        }
        "Flawless" -> {
            when (rarity) {
                "Rare" -> return 0.06
                "Uncommon" -> return 0.17
                "Common" -> return 0.20
            }
        }
        "Radiant" -> {
            when (rarity) {
                "Rare" -> return 0.10
                "Uncommon" -> return 0.20
                "Common" -> return 0.1667
            }
        }
    }
    return 0.0
}

suspend fun CalculatePrice(relic: Relic) : Double {
    var priceCombined = 0.0
    for(item in relic.rewards){
        if(item.item.warframeMarket.urlName == "null") continue
        val chance = RarityToChance(relic.name, item.rarity)
        var price: Double = 0.0
        price = itemManager.GetItemPrice(item.item.warframeMarket.urlName)

        priceCombined += price * chance
    }
    return priceCombined
}

@Composable
@Preview
fun App() {
    val composableScope = rememberCoroutineScope()
    var openAddDialog by remember { mutableStateOf(false) }
    var relicCount = settings.getInt("RelicCount", 0)
    var relicArray = emptyList<Relic>()
    for(i in 0..relicCount-1){
        try {
            relicArray = relicArray + Json.decodeFromString<Relic>(settings.getString("Relic$i", ""))
        }
        catch (e: Exception) {

        }
    }
    var relics by remember { mutableStateOf(relicArray) }
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(relics){
        if(relics.isNotEmpty()) {
            for (i in 0..relics.size - 1) {
                settings.putString("Relic$i", Json.encodeToString(relics[i]))
            }
            settings.putInt("RelicCount", relics.size)
        }
    }
    var darkTheme by remember { mutableStateOf(settings.getBoolean("darkMode", false)) }
    var colors by remember{ mutableStateOf(if (darkTheme) darkScheme else lightScheme) }
    LaunchedEffect(darkTheme){
        colors = if (darkTheme) darkScheme else lightScheme
        settings.putBoolean("darkMode", darkTheme)
    }

    MaterialTheme(
        colorScheme = colors,
    ) {
        Box(
            Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                Modifier.safeContentPadding()
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        relics = relics.sortedBy { it.price }
                        relics = relics.reversed()
                    }){
                        Text("Sort")
                    }
                    Button(onClick = {
                        darkTheme = !darkTheme

                    }) {
                        Text("Dark Mode")
                    }
                    Button(
                        onClick = { openAddDialog = true },
                    ) {
                        Text("+")
                    }
                    Button(
                        onClick = {
                            composableScope.launch {
                                isLoading = true
                                relics = relics.map { relic ->
                                    relic.copy(price = CalculatePrice(relic)) // assuming `Relic` is a data class
                                }
                                isLoading = false
                            }
                        },
                    ) {
                        Text("Refresh")
                    }
                }
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(count = relics.size) {
                        RelicItem(relics[it])
                        Row() {
                            Button(onClick = {
                                relics = relics.filterIndexed { index, _ -> index != it }
                                println(it)
                            }) {
                                Text("Remove")
                            }
                            Button(onClick = {
                                composableScope.launch {
                                    isLoading = true
                                    relics = relics.mapIndexed { i, relic ->
                                        if (i == it) relic.copy(price = CalculatePrice(relics[it])) else relic
                                    }
                                    isLoading = false
                                }
                            }) {
                                Text("Refresh Price")
                            }
                        }
                    }
                }
            }
            if (openAddDialog) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Button(onClick = {openAddDialog = false}){
                        Text("X")
                    }
                    Column(
                        Modifier.background(MaterialTheme.colorScheme.surface)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        var relicName by remember { mutableStateOf("") }

                        TextField(
                            value = relicName,
                            onValueChange = { relicName = it },
                            label = { Text("Name") },
                        )
                        Button(onClick = {
                            composableScope.launch {
                                isLoading = true
                                val relic = itemManager.GetRelic(relicName)
                                if(relic != Relic())
                                    relics = relics + itemManager.GetRelic(relicName)
                                println(relics)
                                openAddDialog = false
                                isLoading = false
                            }
                        }) {
                            Text("Add")
                        }
                    }
                }
            }
            if (isLoading) {
                Box(
                    Modifier.fillMaxSize()
                        .background(color = Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {},
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun RelicItem(relic: Relic) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .clip(
                shape = RoundedCornerShape(20.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.onSurfaceVariant), shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Text(text = relic.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth()
                .height(50.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ){
            items(relic.rewards.size) {
                var color = MaterialTheme.colorScheme.onBackground
                if(it == 0) color = Color(0xFFFFD700)
                else if(it < 3) color = Color(0xFFC0C0C0)
                Text(relic.rewards[it].item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    textAlign = TextAlign.Center,
                )
            }
        }
        var rounded = round(relic.price * 100) / 100.0
        Text(text = "Price: $rounded", color = MaterialTheme.colorScheme.onBackground)
    }
}

/* var stringList: List<String> by remember { mutableStateOf(settings.getString("strings", "").split(",")) }
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                stringList = stringList + "hello"
                settings.putString("strings", stringList.joinToString(","))
            }) {
                Text("Click me!")
            }
            for(string in stringList) {
                Text(string)
            }
        } */