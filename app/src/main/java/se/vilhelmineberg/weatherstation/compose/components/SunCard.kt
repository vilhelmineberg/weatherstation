package se.vilhelmineberg.weatherstation.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.vilhelmineberg.weatherstation.R

@Composable
fun SunCard(sunrise: String, sunset: String, daytimeMinutesDiff: Int, daytimeMidWinterDiff: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row {
            Column {
                SunTimeRow(icon = R.drawable.baseline_sunny_24, text = "Soluppgång: $sunrise")
                SunTimeRow(icon = R.drawable.baseline_mode_night_24, text = "Solnedgång: $sunset")
            }

            Column {
                SunTimeRow(icon = R.drawable.baseline_calendar_view_week_24, text = "$daytimeMinutesDiff min")
                SunTimeRow(icon = R.drawable.baseline_timer_24, text = "$daytimeMidWinterDiff min")
            }
        }
    }
}

@Composable
fun SunTimeRow(icon: Int, text: String) {
    Row(
        modifier = Modifier.padding(top = 4.dp, start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(text = text, modifier = Modifier.padding(4.dp))
    }
}


@Preview(showBackground = true)
@Composable
fun SunTimeRowPreview() {
    SunTimeRow(icon = R.drawable.baseline_sunny_24, text = "Soluppgång: 07:00")
}


@Preview(showBackground = true)
@Composable
fun SunCardPreview() {
    SunCard("07:00", "18:00", 10, 20)
}