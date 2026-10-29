package app.strremote.android

/**
 * One bounded STR-discovery generation. mDNS announcements may be duplicated,
 * but neither in-flight nor completed endpoint probes may be restarted by them.
 */
internal class DiscoveryCycleGuard {
    private val seenHosts = mutableSetOf<String>()

    fun newGeneration() = seenHosts.clear()

    fun claimProbe(host: String): Boolean =
        host.isNotBlank() && seenHosts.add(host)

    companion object {
        /** A discovery deadline is always terminal, even with stuck I/O. */
        fun shouldShowChecking(
            discoveryActive: Boolean,
            discoveryTimedOut: Boolean,
            pendingProbes: Boolean
        ): Boolean = discoveryActive && !discoveryTimedOut && pendingProbes
    }
}
