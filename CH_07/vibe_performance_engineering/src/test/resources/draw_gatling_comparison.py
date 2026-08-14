import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from pathlib import Path

metrics = ["95th percentile", "99th percentile", "Max", "Mean"]

# Order: left -> right in each metric group
scenarios = [
    ("LLM caching", [9, 17, 36, 4], "#ff7f0e"),              # orange, left
    ("Baseline", [66, 82, 148, 36], "#1f77b4"),             # blue, middle
    ("Human-based with caching", [3, 65, 554, 5], "#2ca02c"),  # green, right
]

x = range(len(metrics))
width = 0.25

fig, ax = plt.subplots(figsize=(10, 6))

for index, (label, values, color) in enumerate(scenarios):
    offset = (index - 1) * width
    ax.bar([pos + offset for pos in x], values, width=width, label=label, color=color)

ax.set_title("Performance Comparison", fontsize=16)
ax.set_ylabel("Value (ms)", fontsize=14)
ax.set_xlabel("Metric", fontsize=14)
ax.set_xticks(list(x))
ax.set_xticklabels(metrics)
ax.legend(title="Scenario", fontsize=12)
ax.grid(axis="y", linestyle="--", alpha=0.7)

plt.tight_layout()

output = Path(__file__).resolve().parents[3] / "images" / "Gatling comparison.png"
output.parent.mkdir(parents=True, exist_ok=True)
plt.savefig(output, dpi=150, bbox_inches="tight")
print(f"Saved chart to {output}")
