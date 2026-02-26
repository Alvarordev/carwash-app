package com.example.carwash.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carwash.domain.model.Order

@Composable
fun OrderCard(order: Order, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        println(order)
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Placa: ${order.vehicle?.plate}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Estado: ${order.status}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}