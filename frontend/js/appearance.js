
const root = document.documentElement;

let mode = localStorage.getItem("spliz-mode") || "friends";
let theme = localStorage.getItem("spliz-theme") || "light";

const modeButtons = document.querySelectorAll("[data-spliz-mode]");
const themeButton = document.getElementById("btn-spliz-theme");

function applyAppearance() {
  root.dataset.splizMode = mode;
  root.dataset.splizTheme = theme;

  modeButtons.forEach((button) => {
    const active = button.dataset.splizMode === mode;
    button.classList.toggle("active", active);
    button.setAttribute("aria-pressed", String(active));
  });

  if (themeButton) {
    themeButton.textContent =
      theme === "light" ? "☾ Dark" : "☀ Light";
  }
}

modeButtons.forEach((button) => {
  button.addEventListener("click", () => {
    mode = button.dataset.splizMode;
    localStorage.setItem("spliz-mode", mode);
    applyAppearance();
  });
});

themeButton?.addEventListener("click", () => {
  theme = theme === "light" ? "dark" : "light";
  localStorage.setItem("spliz-theme", theme);
  applyAppearance();
});

function createWaterCanvas() {
  let canvas = document.getElementById("spliz-water-canvas");

  if (canvas) return;

  canvas = document.createElement("canvas");
  canvas.id = "spliz-water-canvas";
  canvas.setAttribute("aria-hidden", "true");

  Object.assign(canvas.style, {
    position: "fixed",
    inset: "0",
    width: "100%",
    height: "100%",
    pointerEvents: "none",
    zIndex: "1"
  });

  document.body.prepend(canvas);

  const ctx = canvas.getContext("2d");
  if (!ctx) return;

  let width = 0;
  let height = 0;
  let frame = 0;

  function resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);

    width = window.innerWidth;
    height = window.innerHeight;

    canvas.width = Math.round(width * dpr);
    canvas.height = Math.round(height * dpr);

    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  function draw() {
    ctx.clearRect(0, 0, width, height);

    const reducedMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches;

    if (mode === "friends") {
      if (!reducedMotion) frame += 0.012;

      const dark = theme === "dark";

      // Gentle turquoise water glow
      const glow = ctx.createRadialGradient(
        width * (0.35 + Math.sin(frame * 0.4) * 0.12),
        height * (0.3 + Math.cos(frame * 0.3) * 0.1),
        0,
        width * 0.5,
        height * 0.45,
        Math.max(width, height) * 0.85
      );

      glow.addColorStop(
        0,
        dark
          ? "rgba(0, 190, 195, 0.16)"
          : "rgba(40, 190, 180, 0.10)"
      );

      glow.addColorStop(
        0.55,
        dark
          ? "rgba(0, 110, 135, 0.08)"
          : "rgba(80, 210, 195, 0.06)"
      );

      glow.addColorStop(1, "rgba(0, 0, 0, 0)");

      ctx.fillStyle = glow;
      ctx.fillRect(0, 0, width, height);

      // Moving, curved sunlight caustic patterns
      ctx.globalCompositeOperation = dark ? "screen" : "multiply";
      ctx.lineCap = "round";
      ctx.lineJoin = "round";

      const spacing = 27;
      const cols = Math.ceil(width / spacing);
      const rows = Math.ceil(height / spacing);

      for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
          const x = col * spacing;
          const y = row * spacing;

          const wave =
            Math.sin(x * 0.009 + frame * 1.2 + Math.sin(y * 0.012)) +
            Math.sin(y * 0.013 - frame + Math.cos(x * 0.008)) +
            Math.sin((x + y) * 0.006 + frame * 0.7);

          if (wave < 1.15) continue;

          const strength = Math.min(
            1,
            (wave - 1.15) / 1.2
          );

          const bend1 =
            Math.sin(y * 0.018 + frame + col * 0.5) * 12;

          const bend2 =
            Math.cos(x * 0.014 - frame + row * 0.4) * 10;

          ctx.beginPath();

          ctx.moveTo(
            x + bend1,
            y + bend2
          );

          ctx.bezierCurveTo(
            x + spacing * 0.3 + bend2,
            y - spacing * 0.2 + bend1,
            x + spacing * 0.65 - bend1,
            y + spacing * 0.8 + bend2,
            x + spacing + bend1 * 0.3,
            y + spacing * 0.4 - bend2
          );

          ctx.strokeStyle = dark
            ? `rgba(100, 255, 245, ${0.10 + strength * 0.38})`
            : `rgba(0, 115, 125, ${0.07 + strength * 0.19})`;

          ctx.lineWidth = dark
            ? 0.9 + strength * 2.2
            : 0.8 + strength * 1.5;

          ctx.shadowBlur = dark ? 12 : 5;

          ctx.shadowColor = dark
            ? "rgba(70, 255, 245, 0.55)"
            : "rgba(0, 150, 155, 0.24)";

          ctx.stroke();
        }
      }

      ctx.shadowBlur = 0;
      ctx.globalCompositeOperation = "source-over";
    }

    requestAnimationFrame(draw);
  }

  window.addEventListener("resize", resize);

  resize();
  draw();
}
applyAppearance();
