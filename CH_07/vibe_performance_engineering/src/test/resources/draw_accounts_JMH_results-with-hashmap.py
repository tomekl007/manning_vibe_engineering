import matplotlib.pyplot as plt
import pandas as pd

# Raw data
data = [
    ("concurrent", 1000, 160.591),
    ("concurrent", 10000, 9917.469),
    ("concurrent", 100000, 1094837.347),
    ("hashMap", 1000, 0.010),
    ("hashMap", 10000, 0.010),
    ("hashMap", 100000, 0.009),
    ("hashMapExists", 1000, 0.008),
    ("hashMapExists", 10000, 0.009),
    ("hashMapExists", 100000, 0.011),
    ("optimized", 1000, 243.579),
    ("optimized", 10000, 284.536),
    ("optimized", 100000, 419.391),
    ("parallelStream", 1000, 19.198),
    ("parallelStream", 10000, 27.036),
    ("parallelStream", 100000, 61.387),
    ("singleThreaded", 1000, 1.305),
    ("singleThreaded", 10000, 14.376),
    ("singleThreaded", 100000, 137.888),
]

df = pd.DataFrame(data, columns=["method", "size", "time_us"])

# Plot
plt.figure(figsize=(10, 6))
for method, group in df.groupby("method"):
    plt.plot(group["size"], group["time_us"], marker="o", label=method)

plt.xscale("log")
plt.yscale("log")
plt.xlabel("Input Size")
plt.ylabel("Time (us/op)")
plt.title("JMH Benchmark Results (log-log scale)")
plt.legend(loc="upper left")
plt.grid(True, which="both", linestyle="--", linewidth=0.5)
plt.tight_layout()

plt.show()
