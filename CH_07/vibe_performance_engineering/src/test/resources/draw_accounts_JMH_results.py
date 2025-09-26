# Prepare and plot the JMH benchmark results with matplotlib (single chart, no custom colors).
import matplotlib.pyplot as plt
import pandas as pd

# Data parsed from the user's message
data = [
    ("concurrent",      1000,   156.071,   38.087),
    ("concurrent",     10000,  9896.064, 2050.364),
    ("concurrent",    100000,1059445.097,61515.917),
    ("optimized",       1000,   248.813,  159.170),
    ("optimized",      10000,   278.543,  269.367),
    ("optimized",     100000,   424.559,  478.451),
    ("parallelStream",  1000,    18.746,   15.515),
    ("parallelStream", 10000,    27.132,    9.933),
    ("parallelStream",100000,    69.923,  156.389),
    ("singleThreaded",  1000,     1.212,    0.351),
    ("singleThreaded", 10000,    14.457,    4.584),
    ("singleThreaded",100000,   137.512,   95.629),
]

df = pd.DataFrame(data, columns=["variant", "size", "avg_us_op", "stddev_us_op"])

# Display the table for reference
from caas_jupyter_tools import display_dataframe_to_user
display_dataframe_to_user("JMH Benchmark Results (us/op)", df)

# Pivot for plotting
pivot_avg = df.pivot(index="size", columns="variant", values="avg_us_op")
pivot_std = df.pivot(index="size", columns="variant", values="stddev_us_op")

# Create the plot
plt.figure(figsize=(9, 6))
for col in pivot_avg.columns:
    # Plot with error bars using stddev
    plt.errorbar(pivot_avg.index, pivot_avg[col], yerr=pivot_std[col], marker='o', capsize=3, label=col)

# Use log scale on y due to large range
plt.yscale('log')

# Labels and title with larger fonts (per preference)
plt.title("AccountFinder JMH: Average time per op (us/op) vs input size", fontsize=16)
plt.xlabel("Input size (N)", fontsize=14)
plt.ylabel("Average time (us/op, log scale)", fontsize=14)

# Larger tick labels
plt.xticks(pivot_avg.index, [str(x) for x in pivot_avg.index], fontsize=12)
plt.yticks(fontsize=12)

# Legend in upper left
plt.legend(loc="upper left", fontsize=11)

plt.grid(True, which="both", axis="y", linestyle="--", linewidth=0.5)

# Save the figure and a CSV
png_path = "/mnt/data/accountfinder_jmh_plot.png"
csv_path = "/mnt/data/accountfinder_jmh_results.csv"
plt.tight_layout()
plt.savefig(png_path, dpi=200, bbox_inches="tight")

df.to_csv(csv_path, index=False)

png_path, csv_path
