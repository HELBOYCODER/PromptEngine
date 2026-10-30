/* mobile.js — Android WebView bridge. No domain logic here. */
(function () {
    "use strict";

    var bridge = window.PromptEngineNative;
    var isNative = typeof bridge !== "undefined" && bridge !== null;

    if (document.body && isNative) document.body.classList.add("pe-android");

    // Android Back: usage sheet first, then settings, then the keyboard, else let the app close
    window.peHandleBack = function () {
        var sidebar = document.getElementById("usage-sidebar");
        if (sidebar && sidebar.classList.contains("open")) {
            document.getElementById("close-sidebar").click();
            return true;
        }
        var modal = document.getElementById("settings-modal");
        if (modal && modal.style.display !== "none" && modal.style.display !== "") {
            document.getElementById("close-modal").click();
            return true;
        }
        var focused = document.activeElement;
        if (focused && (focused.tagName === "TEXTAREA" || focused.tagName === "INPUT")) {
            focused.blur();
            return true;
        }
        return false;
    };
})();
