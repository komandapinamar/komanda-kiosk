package com.komanda.kiosk.core.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.komanda.kiosk.core.network.MobileTenantDto
import com.komanda.kiosk.ui.theme.Amber400
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.Red400
import com.komanda.kiosk.ui.theme.Zinc300
import com.komanda.kiosk.ui.theme.Zinc400
import com.komanda.kiosk.ui.theme.Zinc600
import com.komanda.kiosk.ui.theme.Zinc700
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950

@Composable
fun TenantSelectionScreen(
    tenants: List<MobileTenantDto>,
    onSelectTenant: (tenantId: String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Zinc950),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar Comercio",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Zinc300
                )

                Text(
                    text = "Elegí el comercio express para operar esta terminal:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Zinc400,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                val hasExpressTenants = tenants.any { it.preset == null || it.preset == "express_retail" }
                if (!hasExpressTenants) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x33F87171)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Esta cuenta no posee comercios de formato Express activos. Komanda Kiosk es exclusivo para terminales de autoservicio y drugstores/minimarkets. Para negocios gastronómicos, utilizá Komanda POS.",
                            color = Red400,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tenants, key = { it.id }) { tenant ->
                        val isExpress = tenant.preset == null || tenant.preset == "express_retail"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isExpress) Modifier.clickable { onSelectTenant(tenant.id) }
                                    else Modifier
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpress) Zinc800 else Zinc900.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = if (isExpress) null else BorderStroke(1.dp, Zinc800)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tenant.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isExpress) Zinc300 else Zinc400.copy(alpha = 0.6f)
                                    )
                                    val locationLabel = tenant.primaryLocation?.name
                                        ?: "Sin sucursal asignada"
                                    Text(
                                        text = "Sucursal: $locationLabel",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Zinc400
                                    )
                                    if (!isExpress) {
                                        Text(
                                            text = "Exclusivo gastronomía (utilizar Komanda POS)",
                                            color = Amber400,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isExpress) tenant.role.uppercase() else "GASTRONOMÍA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isExpress) KomandaTokens.AccentTertiary else Zinc600,
                                    modifier = Modifier
                                        .background(if (isExpress) Zinc700 else Zinc800, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.textButtonColors(contentColor = Zinc400)
                ) {
                    Text("Cerrar sesión", fontSize = 14.sp)
                }
            }
        }
    }
}
