package com.masar.maintenance.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.masar.maintenance.data.*
import com.masar.maintenance.ui.components.*
import com.masar.maintenance.ui.theme.*
import com.masar.maintenance.ui.tr

@Composable
fun RentalInboxScreen(nav: NavController) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var rentals by remember { mutableStateOf<List<Rental>>(emptyList()) }

    suspend fun load() {
        loading = true
        when (val r = Net.repo.myRentals()) {
            is Outcome.Ok -> { rentals = r.data; error = null }
            is Outcome.Err -> error = r.message
        }
        loading = false
    }
    LaunchedEffect(Unit) { load() }

    MasarScaffold(title = tr("سيارات للفحص", "Cars to inspect"), onBack = { nav.popBackStack() }) { pad ->
        when {
            loading -> LoadingBox()
            error != null -> ErrorBox(error!!)
            rentals.isEmpty() -> EmptyBox(tr("لا توجد سيارات مُسندة إليك حالياً", "No cars assigned to you currently"), "✓")
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rentals) { r ->
                    val needsHandover = r.needsHandover == true
                    val kind = if (needsHandover) "handover" else "return"
                    Surface(
                        color = Panel, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            nav.navigate("rentalForm/${r.id}?kind=$kind")
                        }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.carName, color = Txt, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(r.plateFull ?: "", color = Muted, fontSize = 12.sp)
                                }
                                Badge(
                                    if (needsHandover) tr("بانتظار تقرير التسليم", "Handover report due")
                                    else tr("تقرير الإرجاع", "Return report"),
                                    if (needsHandover) Yellow else Blue
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(tr("المستأجر: ", "Renter: ") + r.renterName +
                                (r.renterPhone?.let { " · $it" } ?: ""), color = Txt, fontSize = 13.sp)
                            Text(tr("المدة: ", "Duration: ") + "${r.totalDays} " + tr("يوم", "days") +
                                (r.dueAt?.let { " · " + tr("تنتهي: ", "ends: ") + it.take(10) } ?: ""),
                                color = Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
