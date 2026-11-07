package io.redigit.skimbyte.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The single source of truth for every icon the app and the designsheet draw. UI code never
 * references `Icons.*` directly — it names the *purpose* ([Back], [BookmarkFilled], [ReadingTime])
 * and this object decides which glyph that purpose gets, so swapping an icon set or a single glyph
 * is a one-file change.
 *
 * Grouped by role; [all] feeds the designsheet's icon catalog and must list every entry.
 */
object SkimbyteIcons {
    // Navigation and chrome.
    val Back = Icons.AutoMirrored.Filled.ArrowBack
    val ChevronRight = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val Close = Icons.Filled.Close
    val Check = Icons.Filled.Check
    val Home = Icons.Filled.Home
    val Profile = Icons.Filled.Person
    val Settings = Icons.Filled.Settings

    // Stories and bookmarks.
    val BookmarkFilled = Icons.Filled.Bookmark
    val BookmarkOutline = Icons.Outlined.BookmarkBorder
    val ReadingTime = Icons.Outlined.Schedule
    val OpenInNew = Icons.Filled.OpenInNew
    val Document = Icons.Filled.Description

    // Account and profile images.
    val AccountCircle = Icons.Filled.AccountCircle
    val Verified = Icons.Filled.Verified
    val Camera = Icons.Filled.PhotoCamera
    val Gallery = Icons.Filled.Image
    val Delete = Icons.Filled.Delete
    val DeleteForever = Icons.Filled.DeleteForever
    val SignOut = Icons.AutoMirrored.Filled.Logout
    val Privacy = Icons.Filled.PrivacyTip
    val Interests = Icons.Filled.Interests

    // Theme mode (settings) and the designsheet's preview toggle.
    val ThemeLight = Icons.Filled.LightMode
    val ThemeDark = Icons.Filled.DarkMode
    val ThemeSystem = Icons.Filled.Contrast
    val ThemeLightOutline = Icons.Outlined.LightMode
    val ThemeDarkOutline = Icons.Outlined.DarkMode

    // Terminal states.
    val Error = Icons.Filled.ErrorOutline
    val Offline = Icons.Filled.CloudOff
    val EmptyInbox = Icons.Filled.Inbox

    // Tutorial gestures.
    val SwipeUp = Icons.Filled.KeyboardArrowUp
    val SwipeSideways = Icons.Filled.SwapHoriz

    // Designsheet category markers.
    val CategoryTokens = Icons.Outlined.Palette
    val CategoryComponents = Icons.Outlined.Widgets
    val CategoryFeed = Icons.AutoMirrored.Outlined.Article
    val CategoryNavigation = Icons.Outlined.Explore
    val CategoryAccount = Icons.Outlined.Person
    val CategoryOnboarding = Icons.Outlined.Flag

    /** Every entry with its name, in declaration order — the designsheet icon catalog renders this. */
    val all: List<Pair<String, ImageVector>> = listOf(
        "Back" to Back,
        "ChevronRight" to ChevronRight,
        "Close" to Close,
        "Check" to Check,
        "Home" to Home,
        "Profile" to Profile,
        "Settings" to Settings,
        "BookmarkFilled" to BookmarkFilled,
        "BookmarkOutline" to BookmarkOutline,
        "ReadingTime" to ReadingTime,
        "OpenInNew" to OpenInNew,
        "Document" to Document,
        "AccountCircle" to AccountCircle,
        "Verified" to Verified,
        "Camera" to Camera,
        "Gallery" to Gallery,
        "Delete" to Delete,
        "DeleteForever" to DeleteForever,
        "SignOut" to SignOut,
        "Privacy" to Privacy,
        "Interests" to Interests,
        "ThemeLight" to ThemeLight,
        "ThemeDark" to ThemeDark,
        "ThemeSystem" to ThemeSystem,
        "ThemeLightOutline" to ThemeLightOutline,
        "ThemeDarkOutline" to ThemeDarkOutline,
        "Error" to Error,
        "Offline" to Offline,
        "EmptyInbox" to EmptyInbox,
        "SwipeUp" to SwipeUp,
        "SwipeSideways" to SwipeSideways,
        "CategoryTokens" to CategoryTokens,
        "CategoryComponents" to CategoryComponents,
        "CategoryFeed" to CategoryFeed,
        "CategoryNavigation" to CategoryNavigation,
        "CategoryAccount" to CategoryAccount,
        "CategoryOnboarding" to CategoryOnboarding,
    )
}
