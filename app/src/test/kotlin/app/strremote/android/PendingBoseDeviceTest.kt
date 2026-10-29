package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingBoseDeviceTest {
    @Test
    fun newWifiSubmissionInvalidatesStaleHostButRetainsRoom() {
        val assigned = PendingBoseDevice(
            deviceId = "AABBCC",
            host = "192.168.178.31",
            verifiedOnWifi = true,
            displayName = "SoundTouch",
            room = RoomAssignment(preset = RoomPreset.KITCHEN)
        )

        val submitted = assigned.afterSubmission()
        assertNull(submitted.host)
        assertFalse(submitted.verifiedOnWifi)
        assertEquals(assigned.deviceId, submitted.deviceId)
        assertEquals(assigned.room, submitted.room)
        assertEquals(assigned.displayName, submitted.displayName)
    }

    @Test
    fun verifiedWifiKeepsAssignedRoom() {
        val original = PendingBoseDevice(
            deviceId = "AABBCC",
            room = RoomAssignment(customName = "Arbeitszimmer")
        )
        val verified = original.afterVerification("192.168.178.44")
        assertTrue(verified.verifiedOnWifi)
        assertEquals("192.168.178.44", verified.host)
        assertEquals(original.room, verified.room)
    }
}
