package ir.sadteam.loancalc.server

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SupportFilesTest {
    private fun png(w: Int, h: Int): ByteArray = ByteArrayOutputStream().also {
        ImageIO.write(BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB), "png", it)
    }.toByteArray()

    @Test fun pngIsReencodedToJpeg() {
        val r = SupportFiles.sanitize(png(50, 40))
        assertTrue(r is SupportFiles.Result.Ok)
        assertEquals("image/jpeg", r.mime)
        val b = r.bytes
        assertTrue(b[0] == 0xFF.toByte() && b[1] == 0xD8.toByte())
    }

    @Test fun executableAndScriptsRejected() {
        assertTrue(SupportFiles.sanitize(byteArrayOf(0x4D, 0x5A, 0, 0)) is SupportFiles.Result.Rejected) // MZ (exe)
        assertTrue(SupportFiles.sanitize("<?php system(\$_GET[1]); ?>".toByteArray()) is SupportFiles.Result.Rejected)
        assertTrue(SupportFiles.sanitize(byteArrayOf(0x50, 0x4B, 3, 4)) is SupportFiles.Result.Rejected) // zip/apk
    }

    @Test fun fakeJpegHeaderWithGarbageRejected() {
        val fake = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()) + "<script>alert(1)</script>".toByteArray()
        assertTrue(SupportFiles.sanitize(fake) is SupportFiles.Result.Rejected)
    }

    @Test fun mp4Accepted() {
        val mp4 = byteArrayOf(0, 0, 0, 0x18) + "ftypmp42".toByteArray() + ByteArray(32)
        val r = SupportFiles.sanitize(mp4)
        assertTrue(r is SupportFiles.Result.Ok && r.kind == "video")
    }

    @Test fun idPathTraversalBlocked() {
        assertEquals(null, SupportFiles.fileFor("../../etc/passwd"))
    }
}
