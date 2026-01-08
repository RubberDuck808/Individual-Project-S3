export function renderHazardIcon(hazard) {
  const el = document.createElement("div");

  el.className =
    "hazard-marker flex items-center justify-center cursor-pointer";

  el.style.width = "25px";
  el.style.height = "25px";
  el.style.borderRadius = "50%";

  const iconUrl = hazard?.category?.iconUrl;

  if (iconUrl) {
    const img = document.createElement("img");
    img.src = iconUrl;
    img.alt = hazard?.category?.name ?? "Hazard";
    img.style.width = "25px";
    img.style.height = "25px";
    img.style.objectFit = "contain";

    el.appendChild(img);
  } else {
    el.textContent = "⚠️";
    el.style.fontSize = "10px";
  }

  return el;
}
