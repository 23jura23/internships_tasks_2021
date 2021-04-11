package com.jetbrains.summer.ssh_proxy;

import java.math.BigInteger;

public class Fibonacci {
    private static BigInteger[][] id;
    private static BigInteger[][] fibMatrix;

    static {
        id = new BigInteger[][]{
                {BigInteger.valueOf(1), BigInteger.valueOf(0)},
                {BigInteger.valueOf(0), BigInteger.valueOf(1)}
        };
        fibMatrix = new BigInteger[][]{
                {BigInteger.valueOf(0), BigInteger.valueOf(1)},
                {BigInteger.valueOf(1), BigInteger.valueOf(1)}
        };
    }

    private static BigInteger[][] multiply2x2(BigInteger[][] m1, BigInteger[][] m2) {
        BigInteger[][] m3 = new BigInteger[2][2];
        m3[0][0] = m1[0][0].multiply(m2[0][0]).add(m1[0][1].multiply(m2[1][0]));
        m3[1][1] = m1[1][0].multiply(m2[1][0]).add(m1[1][1].multiply(m2[1][1]));
        m3[0][1] = m1[0][0].multiply(m2[1][0]).add(m1[0][1].multiply(m2[1][1]));
        m3[1][0] = m1[1][0].multiply(m2[0][0]).add(m1[1][1].multiply(m2[1][0]));

        return m3;
    }

    private static BigInteger[][] byNumberHelper(BigInteger[][] m, long n) {
        if (n == 0) return id;
        if (n == 1) return m;
        if (n % 2 == 1) return multiply2x2(m, byNumberHelper(m, n - 1));
        BigInteger[][] powHalfN = byNumberHelper(m, n / 2);
        return multiply2x2(powHalfN, powHalfN);
    }

    static BigInteger byNumber(long n) {
        BigInteger[][] fibMatrixPowered = byNumberHelper(fibMatrix, n);
        return fibMatrixPowered[0][1];
    }
}
