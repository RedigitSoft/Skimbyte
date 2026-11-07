package io.redigit.skimbyte.ui

/**
 * The single source of truth for every user-facing string the app renders — screen titles, labels,
 * dialog copy, status-prompt messages, errors, and content descriptions. UI code and ViewModels
 * reference these entries instead of inlining literals, so a copy change is a one-file edit (and a
 * future localization pass has one surface to lift).
 *
 * Grouped by feature; `Common` holds the words that appear across features. Sample/preview data
 * (fake posts, demo names) is deliberately *not* here — that is fixture data, not copy.
 */
object SkimbyteStrings {

    object Common {
        const val BACK = "Back"
        const val CANCEL = "Cancel"
        const val DELETE = "Delete"
        const val SIGN_IN = "Sign in"
        const val GOT_IT = "Got it"
        const val TRY_AGAIN = "Try again"
        const val UPLOAD_FAILED = "Upload failed"
    }

    object Feed {
        const val TITLE = "For You"
        const val LOAD_FAILED_TITLE = "Couldn't load posts"
        const val LOAD_FAILED_NETWORK = "Check your connection and try again."
        const val LOAD_FAILED_UNKNOWN = "Something went wrong on our end. Try again in a moment."
        const val EMPTY_TITLE = "No posts yet"
        const val EMPTY_MESSAGE = "New stories for this topic will show up here as they're published."
    }

    object Story {
        const val SAVE = "Save"
        const val SAVED = "Saved"
        const val BOOKMARK = "Bookmark"
        const val REMOVE_BOOKMARK = "Remove bookmark"
        const val READ_FULL_SOURCE = "Read full source"
        const val VIEW_SOURCE = "View source"
        const val META_SEPARATOR = "·"
        const val NO_STORY_SELECTED_TITLE = "No story selected"
        const val NO_STORY_SELECTED_MESSAGE = "Pick a story from the list to read it here."
        fun minRead(minutes: Int) = "$minutes min read"
    }

    object Bookmarks {
        const val TITLE = "Bookmarks"
        const val SIGNED_OUT_TITLE = "Your bookmarks live here"
        const val SIGNED_OUT_MESSAGE = "Sign in to save stories from the feed and pick them up on any device."
        const val EMPTY_TITLE = "No bookmarks yet"
        const val EMPTY_MESSAGE = "Tap the bookmark icon on any story in the feed to save it here."
        const val DELETE_DIALOG_TITLE = "Delete bookmark?"
        const val DELETE_BOOKMARK = "Delete bookmark"
        fun deleteDialogText(postTitle: String) = "Do you want to delete “$postTitle” from your bookmarks?"
    }

    object Account {
        const val SIGNED_OUT_TITLE = "You're not signed in"
        const val SIGNED_OUT_MESSAGE = "Sign in to sync your profile, bookmarks, and reading stats across devices."
        const val SECTION = "Account"
        const val SETTINGS_LABEL = "Settings"
        const val SETTINGS_SUPPORTING = "Theme, account, privacy & more"
        const val VERIFIED = "Verified"
        const val SELECT_OPTION_PLACEHOLDER = "Select an option to see its details"
        const val VIEW_AVATAR = "View avatar"
        const val CHANGE_AVATAR = "Change avatar"
        const val VIEW_BANNER = "View banner"
        const val CHANGE_BANNER = "Change banner"
        const val CHANGE_PHOTO = "Change photo"
        const val AVATAR_UPDATE_FAILED = "Could not update avatar"
        const val BANNER_UPDATE_FAILED = "Could not update banner"
        const val DELETE_DATA_FAILED = "Could not delete your data"
        fun followedTopics(count: Int) = "Followed topics · $count"
    }

    object ImageEdit {
        const val SHEET_TITLE = "Edit photo"
        const val TAKE_PHOTO = "Take photo"
        const val TAKE_PHOTO_SUBTITLE = "Use your camera"
        const val CHOOSE_FROM_GALLERY = "Choose from gallery"
        const val CHOOSE_FROM_GALLERY_SUBTITLE = "JPEG, PNG or WebP"
        const val REMOVE_IMAGE = "Remove current image"
        const val REMOVE_IMAGE_SUBTITLE = "Reset to the default"
        const val CROP_HINT = "Drag to reposition · pinch to zoom"
        const val USE_PHOTO = "Use photo"
        const val COMPRESSING = "Compressing…"
        const val UPLOADING = "Uploading…"
    }

    object Login {
        const val TITLE = "Sign in to save stories"
        const val SUBTITLE = "Sync your bookmarks across devices and personalize your daily feed."
        const val CONTINUE_WITH_GITHUB = "Continue with GitHub"
        const val CONTINUE_WITH_GOOGLE = "Continue with Google"
        const val CONTINUE_WITH_APPLE = "Continue with Apple"
        const val NOT_NOW = "Not now"
        const val PRIVACY_NOTE = "Developer-first privacy. No tracking."
        const val GITHUB_COMING_SOON = "GitHub sign-in is coming soon"
        const val APPLE_COMING_SOON = "Apple sign-in is coming soon"
        const val GOOGLE_FAILED = "Google sign-in failed"
    }

    object Settings {
        const val TITLE = "Settings"
        const val SECTION_APPEARANCE = "Appearance"
        const val SECTION_CONTENT = "Content"
        const val SECTION_ABOUT = "About"
        const val SECTION_ACCOUNT = "Account"
        const val THEME = "Theme"
        const val THEME_LIGHT = "Light"
        const val THEME_DARK = "Dark"
        const val THEME_SYSTEM = "System"
        const val EDIT_INTERESTS = "Edit interests"
        const val EDIT_INTERESTS_SUPPORTING = "Choose the topics in your daily brief"
        const val PRIVACY_POLICY = "Privacy policy"
        const val PRIVACY_POLICY_SUPPORTING = "How Skimbyte handles your data"
        const val LICENSES = "Open source licenses"
        const val LICENSES_SUPPORTING = "The libraries this app is built on"
        const val LOG_OUT = "Log out"
        const val LOG_OUT_SUPPORTING = "Sign out of your account"
        const val LOG_OUT_DIALOG_TITLE = "Log out?"
        const val LOG_OUT_DIALOG_TEXT = "You'll need to sign in again to access your account."
        const val DELETE_DATA = "Delete my data"
        const val DELETE_DATA_SUPPORTING = "Permanently remove your Skimbyte data"
        const val DELETE_DATA_DIALOG_TITLE = "Delete my data?"
        const val DELETE_DATA_DIALOG_TEXT = "This permanently deletes your bookmarks and other Skimbyte data from our " +
            "servers and signs you out. All your data related to the Skimbyte app " +
            "will be removed. This can't be undone."
    }

    object Onboarding {
        const val PICK_TOPICS_TITLE = "Pick your topics"
        const val EDIT_TOPICS_TITLE = "Edit your topics"
        const val PICK_TOPICS_SUBTITLE = "Choose at least 3 topics to personalize your feed - follow as many as you like."
        const val SAVE = "Save"
        const val GET_STARTED = "Get started"
        fun pickMore(count: Int) = "Pick $count more"

        const val TUTORIAL_SWIPE_UP_TITLE = "Swipe up for the next story"
        const val TUTORIAL_SWIPE_UP_BODY = "The feed shows one brief at a time - swipe up to move to the next."
        const val TUTORIAL_SWIPE_SIDEWAYS_TITLE = "Swipe sideways for topics"
        const val TUTORIAL_SWIPE_SIDEWAYS_BODY = "Swipe left or right, or tap a chip, to switch between your topics."
        const val TUTORIAL_SAVE_TITLE = "Save what matters"
        const val TUTORIAL_SAVE_BODY = "Tap the bookmark on a story to keep it in your Bookmarks."
        const val TUTORIAL_PROFILE_TITLE = "Make it yours"
        const val TUTORIAL_PROFILE_BODY = "Open Profile to set your digest time, summary length and more."
        const val SKIP = "Skip"
        const val NEXT = "Next"

        const val NEW_TOPIC_ADDED = "New topic added"
        const val NEW_TOPICS_ADDED = "New topics added"
        fun newTopicsBody(labels: String, plural: Boolean) =
            "Skimbyte now covers $labels. Follow ${if (plural) "them" else "it"} " +
                "from Interests in your Account tab to see ${if (plural) "their" else "its"} posts in your feed."
    }
}
