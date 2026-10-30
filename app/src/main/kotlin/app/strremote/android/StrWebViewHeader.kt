package app.strremote.android

/**
 * STR's browser/PWA header adds safe-area top padding for standalone browsers.
 * Android's native frame already owns the system-bar insets. Remove ONLY the
 * redundant space; do not remove STR's device, power or accessibility controls.
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
          style.textContent = 'body > header { padding-top: 0 !important; }';
          document.head.appendChild(style);
        })();
    """.trimIndent()
}
