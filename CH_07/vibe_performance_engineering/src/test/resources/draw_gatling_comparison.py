import matplotlib.pyplot as plt
import pandas as pd

# Data preparation
data = {
    "Metric": ["95th percentile", "99th percentile", "Max", "Mean"],
    "Baseline": [66, 82, 148, 20],
    "Gatling caching": [9, 17, 36, 20],
    "Mistakes & Trade-offs caching": [3, 65, 554, 5],
}

df = pd.DataFrame(data)

# Plot
fig, ax = plt.subplots(figsize=(10, 6))
df.set_index("Metric").plot(kind="bar", ax=ax)

ax.set_title("Performance Comparison", fontsize=16)
ax.set_ylabel("Value (ms)", fontsize=14)
ax.set_xlabel("Metric", fontsize=14)
ax.legend(title="Scenario", fontsize=12)
ax.tick_params(axis="x", labelrotation=0)
ax.grid(axis="y", linestyle="--", alpha=0.7)

plt.tight_layout()
plt.show()
