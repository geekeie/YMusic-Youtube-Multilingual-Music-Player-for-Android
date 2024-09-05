class LoadingScreen {
  static loaded() {
    const loadingScreen = document.querySelector(".loadingScreen");
    loadingScreen.style.opacity = "0";
    setTimeout(() => {
      loadingScreen.style.display = "none";
    }, 400);
  }
}
// Dark Mode
const preference = localStorage.getItem("dark");
if (preference == "true") {
  document.body.classList.add("dark");
} else if (
  preference !== "false" &&
  window.matchMedia &&
  window.matchMedia("(prefers-color-scheme: dark)").matches
) {
  document.body.classList.add("dark");
}
function toggleDark() {
  const dark = document.body.classList.contains("dark");
  document.body.classList.toggle("dark");
  localStorage.setItem("dark", !dark);
}
function detecttouch() {
  document.getElementById("toggledark").classList.remove("focustoggle");
}
