package io.redigit.skimbyte.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SkimbyteShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * Shape tokens that have no slot in Material 3's [Shapes], so they cannot live in [SkimbyteShapes].
 */
data object SkimbyteShapeTokens {
    /**
     * A true capsule, for chips and other pill-shaped containers. Chips used to borrow
     * `shapes.extraLarge` (a fixed 32.dp radius) as a stand-in, which only reads as a capsule at
     * the one height it happened to be tuned for and squares off as soon as the chip grows.
     * A 50% radius stays a capsule at any height.
     */
    val pill = RoundedCornerShape(percent = 50)
}
