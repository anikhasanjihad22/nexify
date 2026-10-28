package com.ahr.nexify.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AlbumFolderNameTest {
    @Test
    fun `blank and illegal names fall back to Nexify`() {
        assertThat(AlbumFolderName.sanitize(null)).isEqualTo("Nexify")
        assertThat(AlbumFolderName.sanitize("  ")).isEqualTo("Nexify")
        assertThat(AlbumFolderName.sanitize("../")).isEqualTo("Nexify")
        assertThat(AlbumFolderName.sanitize("a/b")).isEqualTo("ab")
    }

    @Test
    fun `a single segment is kept`() {
        assertThat(AlbumFolderName.sanitize(" 小说 ")).isEqualTo("小说")
    }
}
