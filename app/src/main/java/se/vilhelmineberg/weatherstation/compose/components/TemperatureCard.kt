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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.vilhelmineberg.weatherstation.R

@Composable
fun TemperatureCard(
    header: String,
    temperature: String,
    humidity: String,
    timestamp: String,
    icon: ImageVector,
    dayHigh: String,
    dayLow: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = header,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(alignment = androidx.compose.ui.Alignment.CenterHorizontally)
            )
            Column {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(text = "Temperatur: $temperature C", modifier = Modifier.padding(start = 16.dp))
                        Text(text = "Luftfuktighet: $humidity %", modifier = Modifier.padding(start = 16.dp))

                        Row {
                            Text(
                                text = "Min: $dayLow C",
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                            Text(
                                text = "Max: $dayHigh C",
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                        }

                        Text(text = "Uppdaterad: $timestamp", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun TemperatureCardPreview() {
    TemperatureCard(
        header = "Bryggeri",
        temperature = "3.0",
        humidity = "50.0",
        timestamp = "2023-04-01 12:00:00",
        icon = ImageVector.vectorResource(id = R.drawable.baseline_local_drink_24),
        dayHigh = "4.0",
        dayLow = "2.0"
    )
}
