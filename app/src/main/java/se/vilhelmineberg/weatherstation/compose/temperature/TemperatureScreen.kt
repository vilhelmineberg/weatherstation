package se.vilhelmineberg.weatherstation.compose.temperature


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.vilhelmineberg.weatherstation.R
import se.vilhelmineberg.weatherstation.compose.components.SunCard
import se.vilhelmineberg.weatherstation.compose.components.TemperatureCard
import se.vilhelmineberg.weatherstation.ui.theme.WeatherstationTheme

@Composable
fun Temperature(
    state: TemperatureState,
    onAction: (TemperatureAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, top = 50.dp, end = 16.dp)
    ) {
        Row {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_circle_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (state.connection == ConnectionStatus.CONNECTED) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
            )
            Text(text = "${state.connection}")
        }

        Text(
            text = "Vilhelmineberg",
            modifier = Modifier
                .padding(top = 50.dp, bottom = 16.dp)
                .align(alignment = Alignment.CenterHorizontally)
        )

        SunCard(
            state.sunTimes.first,
            state.sunTimes.second,
            state.sunTimeDiffPreviousWeek,
            state.sunTimeDiffFromMidWinter
        )

        TemperatureCard(
            " Växthus",
            state.temperatureGreenHouse.temperature,
            state.temperatureGreenHouse.humidity,
            state.temperatureGreenHouse.timestamp,
            icon = ImageVector.vectorResource(id = R.drawable.baseline_local_florist_24)
        )
        Spacer(modifier = Modifier.height(16.dp))

        TemperatureCard(
            "Bryggeri",
            state.temperatureBrewery.temperature,
            state.temperatureBrewery.humidity,
            state.temperatureBrewery.timestamp,
            icon = ImageVector.vectorResource(id = R.drawable.baseline_local_drink_24)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onAction(if (state.connection == ConnectionStatus.CONNECTED) TemperatureAction.Disconnect else TemperatureAction.Connect) },
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        ) {
            Text(text = if (state.connection == ConnectionStatus.CONNECTED) "Disconnect" else "Connect")
        }
    }
}


@Composable
@Preview(showBackground = true)
fun TemperaturePreview() {
    WeatherstationTheme {
        Surface {
            Temperature(
                state = TemperatureState(
                    temperatureGreenHouse = TemperatureClient(
                        temperature = "20.0",
                        humidity = "50.0",
                        timestamp = "2023-04-01 12:00:00"
                    ),
                    temperatureBrewery = TemperatureClient(
                        temperature = "20.0",
                        humidity = "50.0",
                        timestamp = "2023-04-01 12:00:00"
                    ),
                    connection = ConnectionStatus.CONNECTED
                ),
                onAction = {}
            )
        }
    }
}

