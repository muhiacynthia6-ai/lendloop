package com.example.lendloop.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────
//  SHAPES — Generously rounded, soft & modern
// ─────────────────────────────────────────────
val LendLoopShapes = Shapes(
    // Chips, small badges
    extraSmall = RoundedCornerShape(6.dp),

    // Text fields, buttons, small cards
    small      = RoundedCornerShape(10.dp),

    // Cards, dialogs, bottom sheets
    medium     = RoundedCornerShape(16.dp),

    // Large cards, featured sections
    large      = RoundedCornerShape(20.dp),

    // FABs, avatar containers, full-pill shapes
    extraLarge = RoundedCornerShape(28.dp),
)