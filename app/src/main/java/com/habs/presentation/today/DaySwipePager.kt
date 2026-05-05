package com.habs.presentation.today

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Three-page horizontal pager (prev | current | next). When the user settles on an edge page,
 * [onEdgeSettled] runs with -1 or +1 day, then the pager snaps back to the center page.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DaySwipePagerShell(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    onEdgeSettled: (deltaDays: Long) -> Unit,
    centerPageContent: @Composable () -> Unit,
) {
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                when (page) {
                    0 -> {
                        onEdgeSettled(-1)
                        pagerState.scrollToPage(1)
                    }
                    2 -> {
                        onEdgeSettled(1)
                        pagerState.scrollToPage(1)
                    }
                    else -> Unit
                }
            }
    }

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondBoundsPageCount = 1,
        verticalAlignment = Alignment.Top
    ) { page ->
        when (page) {
            1 -> centerPageContent()
            else -> Box(Modifier.fillMaxSize())
        }
    }
}
