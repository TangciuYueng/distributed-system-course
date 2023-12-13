import numpy as np
import matplotlib.pyplot as plt

# Function to calculate Amdahl's Law
def amdahls_law(f, x):
    if isinstance(x, list):
        return [1 / (1 - f + f / xi) for xi in x]
    else:
        return 1 / (1 - f + f / x)

# Numerical derivative function
def numerical_derivative(f, x, h=1e-5):
    return (f(x + h) - f(x)) / h

# Given values
f = 0.4  # Fraction of the algorithm that is sequential
p_values = np.linspace(1, 30, 100)  # Varying parallelization factor P from 1 to 20

# Calculate speedup for different values of P
speedup_values = amdahls_law(f, p_values)

# Numerical derivative values
derivative_values = [numerical_derivative(lambda x: amdahls_law(f, x), p) for p in p_values]

# Data points to highlight
highlight_points = [i for i in range(6, 20)]

# Plot the results
plt.figure(figsize=(12, 6))

# Plot Amdahl's Law
plt.subplot(1, 2, 1)
plt.plot(p_values, speedup_values, label=f'Amdahl\'s Law (f={f})')
plt.scatter(highlight_points, amdahls_law(f, highlight_points), color='red', marker='o', label='Highlighted Points')
plt.title('Amdahl\'s Law')
plt.xlabel('Parallelization Factor (P)')
plt.ylabel('Speedup (S_total)')
plt.legend()
plt.grid(True)

# Plot the derivative
plt.subplot(1, 2, 2)
plt.plot(p_values, derivative_values, label=f'Derivative of Amdahl\'s Law (f={f})')
plt.scatter(highlight_points, [numerical_derivative(lambda x: amdahls_law(f, x), p) for p in highlight_points],
            color='red', marker='o', label='Highlighted Points')
plt.title('Derivative of Amdahl\'s Law')
plt.xlabel('Parallelization Factor (P)')
plt.ylabel('Derivative')
plt.legend()
plt.grid(True)

plt.tight_layout()
plt.show()
