class Multilingual {
  language;
  defaultValues = {};
  selectedValues = {};
  constructor(supported) {
    const user = (navigator.language || navigator.userLanguage).split("-")[0];
    if (supported.includes(user)) {
      this.language = localStorage.getItem("language") || user || "en";
    } else {
      this.language = localStorage.getItem("language") || "en";
    }
  }
  _decode(string) {
    return string
      .replace(/\\n/g, "\n")
      .replaceAll("\\'", "'")
      .replaceAll('\\"', '"')
      .replaceAll("\\\\", "\\")
      .replaceAll("\\t", "\t");
  }
  async loadStrings() {
    const defaultValuesResponse = await fetch("res/values/strings.xml");
    const defaultValues = await defaultValuesResponse.text();
    if (this.language == "en") {
      var selectedValues = defaultValues;
    } else {
      const selectedValuesResponse = await fetch(
        `res/values-${this.language}/strings.xml`,
      );
      var selectedValues = await selectedValuesResponse.text();
    }

    // Default language
    const parserDefault = new DOMParser();
    const xmlDocDefault = parserDefault.parseFromString(
      defaultValues,
      "text/xml",
    );
    const stringsDefault = xmlDocDefault.getElementsByTagName("string");
    for (const string of stringsDefault) {
      const name = string.getAttribute("name");
      const value = this._decode(string.textContent);
      this.defaultValues[name] = value;
    }

    // Selected language
    const parserSelected = new DOMParser();
    const xmlDocSelected = parserSelected.parseFromString(
      selectedValues,
      "text/xml",
    );
    const stringsSelected = xmlDocSelected.getElementsByTagName("string");

    for (const string of stringsSelected) {
      const name = string.getAttribute("name");
      const value = this._decode(string.textContent);
      this.selectedValues[name] = value;
    }
  }
  getString(name) {
    return this.selectedValues[name] || this.defaultValues[name];
  }
  updateDom() {
    function replaceLast(x, y, z) {
      var a = x.split("");
      a[x.lastIndexOf(y)] = z;
      return a.join("");
    }
    const allElements = Array.prototype.slice.call(
      document.body.getElementsByTagName("*"),
    );
    allElements.forEach((el) => {
      const text = el.innerHTML.trim();
      if (text.startsWith("[") && text.endsWith("]")) {
        const name = replaceLast(text.replace("[", ""), "]", "");
        const value = this.getString(name) || "";
        el.innerHTML = el.innerHTML.replace(
          `[${name}]`,
          value.replaceAll("\n", "<br>"),
        );
      }
    });
  }
  updateLanguage(el) {
    const lang = el.value;

    // Calculate width for the new language that is selected
    this._calculateWidth(el);

    localStorage.setItem("language", lang);
    window.location.reload();
  }
  loadLanguageSelectInput(el) {
    for (const option of el.children) {
      if (option.value == this.language) {
        option.selected = true;
        break;
      }
    }
    this._calculateWidth(el);
  }
  _calculateWidth(el) {
    const option = el.options[el.selectedIndex].textContent;

    // Calculate width of the text
    const tempSpan = document.createElement("span");
    tempSpan.textContent = option;
    tempSpan.style.visibility = "hidden";
    tempSpan.style.fontSize = "14px";
    const spanEl = document.body.appendChild(tempSpan);

    const textWidth = tempSpan.getBoundingClientRect().width;
    el.style.width = `${Math.round(textWidth)}px`;

    spanEl.remove();
  }
  setAttribute(el) {
    el.setAttribute("lang", this.language);
  }
}
