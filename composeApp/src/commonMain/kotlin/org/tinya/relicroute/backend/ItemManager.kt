package org.tinya.relicroute.backend

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import org.tinya.relicroute.Orders
import org.tinya.relicroute.Relic

class ItemManager {
    val client = HttpClient()
    suspend fun GetRelic(name: String): Relic {
        var relic = Relic()
        var nameEdited = name.replace(" ", "%20").lowercase()
        try {
            var response = client.get("https://api.warframestat.us/items/$nameEdited/")
            val body = response.bodyAsText()
            relic = Json.decodeFromString<Relic>(body)
            return relic
        }
        catch (e: Exception) {
            println("Error: ${e.message}")
        }
        return relic
    }

    suspend fun GetItemPrice(name: String): Double {
        var price = Double.POSITIVE_INFINITY
        var response = client.get("https://api.warframe.market/v1/items/$name/orders")
        if(response.bodyAsText() == "") return price
        var orderObj = Json.decodeFromString<Orders>(response.bodyAsText())
        var orders = orderObj.payload.orders
        for(order in orders) {
            if(order.order_type == "buy" || order.visible == false || order.user.status != "ingame") continue
            if(order.platinum < price)
                price = order.platinum
        }
        return price
    }

}