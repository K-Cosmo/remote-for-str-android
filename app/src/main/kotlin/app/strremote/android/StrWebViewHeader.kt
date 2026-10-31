package app.strremote.android

/**
 * STR's browser/PWA header adds safe-area top padding for standalone browsers.
 * Android's native frame already owns the system-bar insets. Keep a small 10px
 * separation below the native toolbar without touching STR's controls.
 * Guard on the known STR DOM structure so a changed upstream page is untouched.
 */
internal object StrWebViewHeader {
    val compactTopPaddingScript = """
        (function() {
          var header = document.querySelector('body > header');
          if (!header || !header.querySelector('#dev') ||
              !header.querySelector('#powerBtn')) return;
          if (document.getElementById('str-remote-android-header-spacing')) return;
          var style = document.createElement('style');
          style.id = 'str-remote-android-header-spacing';
          style.textContent = 'body > header { padding-top: 10px !important; }';
          document.head.appendChild(style);
        })();
    """.trimIndent()
}
