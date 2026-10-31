package app.strremote.android

/** Release names are strict public GitHub tags, not Android versionCode values. */
internal data class AvailableAppUpdate(val tag: String, val version: String) {
    val releaseUrl: String
        get() = "https://github.com/K-Cosmo/remote-for-str-android/releases/tag/$tag"
}

internal object AppUpdatePolicy {
    private val releaseTag = Regex("^v(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)$")
    private val installedVersion = Regex("^(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)$")

    private fun components(value: String, pattern: Regex): List<Long>? {
        val match = pattern.matchEntire(value) ?: return null
        return (1..3).map { match.groupValues[it].toLongOrNull() ?: return null }
    }

    fun newerThanInstalled(tag: String, current: String): AvailableAppUpdate? {
        val candidate = components(tag, releaseTag) ?: return null
        val installed = components(current, installedVersion) ?: return null
        val newer = candidate.zip(installed)
            .firstOrNull { (a, b) -> a != b }
            ?.let { (a, b) -> a > b } ?: false
        return if (newer) AvailableAppUpdate(tag, tag.removePrefix("v")) else null
    }
}
