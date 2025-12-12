package com.afsar.titipin.ui.buy

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AddOrderDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    itemName: String, onNameChange: (String) -> Unit,
    quantity: Int, onQtyChange: (Int) -> Unit,
    price: String, onPriceChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Titip Barang", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = itemName,
                    onValueChange = onNameChange,
                    label = { Text("Nama Barang") },
                    placeholder = { Text("Contoh: Seblak Ceker") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = onPriceChange,
                        label = { Text("Estimasi Harga") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(56.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                            .padding(horizontal = 4.dp)
                    ) {
                        IconButton(onClick = { if (quantity > 1) onQtyChange(quantity - 1) }) {
                            Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                        }
                        Text(quantity.toString(), fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onQtyChange(quantity + 1) }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Catatan (Opsional)") },
                    placeholder = { Text("Pedas level 5, tanpa sayur...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF370061)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Pesan")
                    }
                }
            }
        }
    }
}