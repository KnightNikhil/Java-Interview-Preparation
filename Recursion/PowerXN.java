package Recursion;

class PowerXN {
    public static void main(String[] args) {
        System.out.println(myPow(2.0, 10));
    }


    /*
    Function to calculate x raised to the power n (x^n)
    Handles both positive and negative values of n
    Uses recursion and exponentiation by squaring for efficiency
    1. If n is negative, compute the power for -n and take the reciprocal
    2. If n is zero, return 1 (base case)
    3. If n is one, return x (base case)
    4. If n is even, recursively compute (x * x) raised to the power of n/2
    5. If n is odd, recursively compute x multiplied by x raised to the power of (n - 1)

    even and odd logic:
    - Even: x^n = (x^x)^(n/2)
    -  O dd: x^n = x * x^(n-1)
    note:  when x is odd, and we make x * x^(n-1), this will be as at that point, not initial value of x
    This approach reduces the number of multiplications needed, making it more efficient than a simple loop
     */
    public static double myPow(double x, int n) {
        // Convert n to long to avoid overflow
        long num = n;

        // If n is negative
        if (num < 0) {
            // Calculate the power of -n and take reciprocal
            return (1.0 / power(x, -num));
        }
        // If n is non-negative
        return power(x, num);
    }


    // Function to calculate power of 'x' raised to 'n'
    private static double power(double x, long n) {
        // Base case: anything raised to 0 is 1
        if (n == 0) return 1.0;

        // Base case: anything raised to 1 is itself
        if (n == 1) return x;

        // If 'n' is even
        if (n % 2 == 0) {
            // Recursive call: (x * x), n / 2
            return power(x * x, n / 2);
        }
        // If 'n' is odd
        return x * power(x, n - 1);


    }




    //
}



