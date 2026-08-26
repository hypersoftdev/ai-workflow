package YOUR.PACKAGE.feature.entrance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import YOUR.PACKAGE.core.design.AppTheme

const val ENTRANCE_ROUTE = "entrance"

@Composable
fun EntranceScreen(
    modifier: Modifier = Modifier,
    navigateToNext: () -> Unit,
) {
    EntranceScreenContent(modifier = modifier)
}

@Composable
private fun EntranceScreenContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun EntranceScreenPreview() {
    AppTheme {
        EntranceScreenContent()
    }
}
