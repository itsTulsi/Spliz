
const root = document.documentElement;

let mode = localStorage.getItem("spliz-mode") || "friends";
let theme = localStorage.getItem("spliz-theme") || "light";

const modeToggle = document.getElementById("btn-spliz-mode-toggle");
const themeButton = document.getElementById("btn-spliz-theme");

const friendCopy = new Map([
  ["Expense Groups", "The Crew"],
  ["Select a group to manage expenses, simulate fair splits, and optimize debts.", "Pick your crew. We'll sort out the money drama."],
  ["Create Group", "Make a Crew"], ["New Group", "New Crew"],
  ["Back to Groups", "â† Back to the Crew"], ["â† Back to Groups", "â† Back to the Crew"],
  ["Total Spend", "Money Out the Door"], ["Total Spent", "Money Out the Door"],
  ["Active Members", "Who's In"], ["Unsettled Balance", "Who Still Owes"],
  ["Expenses", "The Bills"], ["Balances and Settle Up", "Who Owes Who"],
  ["Fairness Simulator", "Try a Different Split"], ["Activity History", "The Receipts"],
  ["Recorded Expenses", "The Bill Pile"], ["Member Net Balances", "Who's Up, Who's Down"],
  ["Settle Up & Cancel Mutual Splits", "Squash the Money Beef"],
  ["Settle Up & Cancel Expenses", "Tidy Up the Money Mess"],
  ["Settle Up Ready", "Ready to Call It Even"], ["ZERO-SUM VERIFIED", "The Numbers Check Out"],
  ["Zero-Sum Verified", "The Numbers Check Out"], ["NO OFFSETTING SPLITS", "No Money Beef Today"],
  ["No Offsetting Splits", "No Money Beef Today"], ["Add Expense", "Add a Bill"],
  ["Scan Receipt", "Scan That Receipt"], ["Scan a Receipt", "Scan That Receipt"],
  ["Create New Group", "Make a Crew"], ["Settle Up", "Settle Up"],
  ["Spent So Far", "Money Out the Door"], ["In This Group", "Who's In"],
  ["Still to Settle", "Who Still Owes"], ["The Bills", "The Bills"],
  ["Who Owes What", "Who Owes Who"], ["What Happened", "The Receipts"],
  ["Group Bills", "The Bill Pile"], ["Everyoneâ€™s Balance", "Who's Up, Who's Down"],
  ["Tidy Up Overlapping Bills", "Squash the Money Beef"], ["Tidy Up the Bills", "Tidy Up the Money Mess"],
  ["Ready to Settle", "Ready to Call It Even"], ["Balances add up", "The Numbers Check Out"],
  ["Nothing to tidy up", "No Money Beef Today"], ["Add a Bill", "Add a Bill"]
]);
const professionalCopy = new Map([
  ["The Crew", "Expense Groups"],
  ["Pick your crew. We'll sort out the money drama.", "Select a group to manage expenses, simulate fair splits, and optimize debts."],
  ["Make a Crew", "Create Group"], ["New Crew", "New Group"],
  ["â† Back to the Crew", "â† Back to Groups"],
  ["Money Out the Door", "Total Spend"], ["Who's In", "Active Members"],
  ["Who Still Owes", "Unsettled Balance"], ["The Bills", "Expenses"],
  ["Who Owes Who", "Balances and Settle Up"], ["Try a Different Split", "Fairness Simulator"],
  ["The Receipts", "Activity History"], ["The Bill Pile", "Recorded Expenses"],
  ["Who's Up, Who's Down", "Member Net Balances"], ["Squash the Money Beef", "Settle Up & Cancel Mutual Splits"],
  ["Tidy Up the Money Mess", "Settle Up & Cancel Expenses"], ["Ready to Call It Even", "Settle Up Ready"],
  ["The Numbers Check Out", "ZERO-SUM VERIFIED"], ["No Money Beef Today", "NO OFFSETTING SPLITS"],
  ["Add a Bill", "Add Expense"], ["Scan That Receipt", "Scan Receipt"]
]);

function replaceTextNodes(map) {
  const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
  const nodes = [];
  while (walker.nextNode()) nodes.push(walker.currentNode);
  for (const node of nodes) {
    const current = node.nodeValue;
    const trimmed = current.trim();
    if (!trimmed) continue;
    const replacement = map.get(trimmed);
    if (replacement && replacement !== trimmed) node.nodeValue = current.replace(trimmed, replacement);
  }
}

function applyAppearance() {
  root.dataset.splizMode = mode;
  root.dataset.splizTheme = theme;
  if (modeToggle) {
modeToggle.textContent = mode === "friends" ? "Professional" : "Friend";    modeToggle.setAttribute("aria-pressed", String(mode === "professional"));
    modeToggle.title = mode === "friends" ? "Switch to clear, professional wording and styling" : "Switch to frank, goofy friends wording and styling";
  }
if (themeButton) themeButton.textContent = theme === "light" ? "Dark" : "Light";  replaceTextNodes(mode === "friends" ? friendCopy : professionalCopy);
}

modeToggle?.addEventListener("click", () => {
  mode = mode === "friends" ? "professional" : "friends";
  localStorage.setItem("spliz-mode", mode);
  applyAppearance();
});

const textObserver = new MutationObserver(() => {
  if (mode === "friends") replaceTextNodes(friendCopy);
  else replaceTextNodes(professionalCopy);
});
textObserver.observe(document.body, { childList: true, subtree: true, characterData: true });

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

