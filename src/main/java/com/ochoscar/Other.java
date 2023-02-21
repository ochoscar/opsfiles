package com.ochoscar;

import java.util.Random;

public class Other {

    public static void main(String[] args) {
        System.out.println("BUSCANDO SOLUCION");
        boolean notSolve = true;
        Random r = new Random();
        int [][] m = new int[3][3];


        while(notSolve) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    m[i][j] = 0;
                }
            }

            // Llenar matrix
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    boolean notValidNum = false;
                    int num = 0;

                    do {
                        num = r.nextInt(10);
                        notValidNum = false;
                        for (int k = 0; k < 3; k++) {
                            for (int s = 0; s < 3; s++) {
                                if(m[k][s] == num) {
                                    notValidNum = true;
                                }
                            }
                        }
                    }while(notValidNum);
                    m[i][j] = num;
                }
            }

            // Verificar la matrix

            System.out.println("VERIFICANDO MATRIX:");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.print(m[i][j] + " ");
                }
                System.out.println("");
            }

            notSolve = false;

            for (int i = 0; i < 3; i++) {
                int sum = 0;
                for (int j = 0; j < 3; j++) {
                    sum += m[i][j];
                }
                if(sum != 15) {
                    notSolve = true;
                    break;
                }
            }

            for (int i = 0; i < 3; i++) {
                int sum = 0;
                for (int j = 0; j < 3; j++) {
                    sum += m[j][i];
                }
                if(sum != 15) {
                    notSolve = true;
                    break;
                }
            }

            if(m[0][0] + m[1][1] + m[2][2] != 15) {
                notSolve = true;
            }

            if(m[0][2] + m[1][1] + m[2][0] != 15) {
                notSolve = true;
            }
        }

        System.out.println("SOLUCION:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(m[i][j] + " ");
            }
            System.out.println("");
        }

    }
}
